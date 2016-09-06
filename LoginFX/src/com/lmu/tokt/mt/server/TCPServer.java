package com.lmu.tokt.mt.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.lmu.tokt.mt.util.AppConstants;
import com.lmu.tokt.mt.util.Checksum;

public class TCPServer extends Thread {

	// Terminal Ports Listen: netstat -an | grep -i "listen" , sudo lsof -i -P |
	// grep -i "listen" , netstat -atp tcp | grep -i "listen"

	/*-------------------------------------------*/
	// ifconfig | grep "inet " | grep -v 127.0.0.1

	private ServerSocket mServer;
	private Socket mConnection;

	private int SERVER_PORT = 8888;
	private MessageCallback mMessageListener;

	private BufferedReader mInput;
	private BufferedWriter mOutput;
	private String mIncomingMessage;

	private static Checksum mChecksum;

	private boolean mAllowConnection = true;
	private boolean mServerIsRunning = false;
	private boolean mDataExchangeIsRunning = false;
	private boolean mIsConnectedToWatch = false;

	private boolean mHeartRateDetectd = false;
	private int mHeartRateCounter = 0;
	private long mLastHeartrateTimestamp = 0;

	private boolean mProximityDetected = false;
	private int mProximityCounter = 0;
	private long mLastProximityTimestamp = 0;

	public TCPServer(MessageCallback messageListener) {
		mMessageListener = messageListener;
		mChecksum = Checksum.getInstance();
	}

	public TCPServer(MessageCallback messageListener, int port) {
		this(messageListener);
		SERVER_PORT = port;

	}

	@Override
	public void run() {
		startRunning();
	}

	public void startRunning() {

		mServerIsRunning = true;

		try {
			mServer = new ServerSocket(SERVER_PORT);
			try {
				while (mServerIsRunning) {
					waitForConnection();
					setupStreams();
					whileDataExchange();
				}
				System.out.println("Server stopped!");
			} catch (EOFException eof) {
				System.out.println("SERVER ended the connection! ");
			} finally {
				closeConnection();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void stopRunning() {
		mDataExchangeIsRunning = false;
		mIsConnectedToWatch = false;
		mServerIsRunning = false;
		// closeConnection();
	}

	private void waitForConnection() {
		System.out.println("Wait for client to connect...");

		try {
			mConnection = mServer.accept();
			System.out.println(
					"CLIENT connected to " + mConnection.getInetAddress().getHostAddress() + ":" + SERVER_PORT);
			mDataExchangeIsRunning = true;
			if (mMessageListener != null) {
				mMessageListener.callbackMessageReceiver(AppConstants.STATE_SERVER_RUNNING,
						mConnection.getInetAddress().getHostAddress() + ":" + SERVER_PORT);
			}
			// publish to mainscreen
		} catch (IOException ioe) {
			System.err.println("Error while trying client to connect! Exception: " + ioe.getMessage());
			mIsConnectedToWatch = false;
		}

	}

	private void setupStreams() throws IOException {
		mOutput = new BufferedWriter(new OutputStreamWriter(mConnection.getOutputStream(), "UTF-8"));
		mOutput.flush();

		mInput = new BufferedReader(new InputStreamReader(mConnection.getInputStream(), "UTF-8"));
		System.out.println("Streams are now setup.");
	}

	private synchronized void whileDataExchange() throws IOException {

		// TODO: Request allowance to pair!!!!
		if (!mIsConnectedToWatch) {
			// send ConfirmConnectionRequest
			sendMessage(AppConstants.COMMAND_CONFIRM + ":" + mChecksum.getChecksum());
			System.out.println("Confirmation Request to client with: " + mChecksum.getChecksum());
		}

		while (mDataExchangeIsRunning) {
			mIncomingMessage = mInput.readLine();
			if (mIncomingMessage != null && mMessageListener != null) {

				// confirm connection
				if (mIncomingMessage.contains(AppConstants.COMMAND_CONFIRM)) {

					int receivedChecksum = Integer.parseInt(mIncomingMessage.split(":")[1]);
					if (receivedChecksum == mChecksum.getChecksum()) {
						mIsConnectedToWatch = true;
						mMessageListener.callbackMessageReceiver(AppConstants.STATE_CONNECTED,
								"Confirmation successful!");
						sendMessage(AppConstants.COMMAND_CONNECT);
					} else {
						mDataExchangeIsRunning = false;
						mMessageListener.callbackMessageReceiver(AppConstants.ERROR, "Confirmation failed!");
						sendMessage(AppConstants.COMMAND_DISCONNECT);
					}

				}

				if (mIncomingMessage.equals(AppConstants.COMMAND_DISCONNECT)) {
					mDataExchangeIsRunning = false;
					mIsConnectedToWatch = false;
					mMessageListener.callbackMessageReceiver(AppConstants.ERROR, "Client disconnected!");
				}

				if (mIncomingMessage.contains(AppConstants.SENSORDATA)) {
					String data = mIncomingMessage.split("::")[1];

					JSONParser parser = new JSONParser();
					try {
						JSONObject json = (JSONObject) parser.parse(data);
						String name = (String) json.get("name");

						if (name.equals(AppConstants.STRING_TYPE_HEART_RATE)) {
							validateHeartRate(data);
						}
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if (mIncomingMessage.contains(AppConstants.BEACONDATA)) {
					String data = mIncomingMessage.split("::")[1];

					JSONParser parser = new JSONParser();
					try {
						JSONObject json = (JSONObject) parser.parse(data);
						// String name = (String) json.get("name");
						String proximity = (String) json.get("proximity");
						validateProximity(proximity);

					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				// validate lock state
				validateLockState();
				
				validateHeartRateByTimeStamp();

				mMessageListener.callbackMessageReceiver(mIncomingMessage);
			}
			mIncomingMessage = null;
		}
		System.out.println("Data exchange stopped!");

	}
	
	
	private void validateHeartRateByTimeStamp(){
		
		long t = System.currentTimeMillis();
		long lastTimestamp = mLastHeartrateTimestamp;
		long timeAgo = t - lastTimestamp;
		System.out.println("timeAgo:"+timeAgo);

		if (lastTimestamp != 0 && mHeartRateDetectd) {
			if (timeAgo > 20000) {
				System.out.println("No Heartbeat!");
				mMessageListener.callbackMessageReceiver(AppConstants.STATE_HEART_STOPPED, "No Heartbeat detected");
				sendMessage(AppConstants.COMMAND_NOT_AUTHENTICATED);
				mHeartRateDetectd = false;
				mHeartRateCounter = 0;
				return;
			}
		}

		
		
	}

	private void validateHeartRate(String data) {

		

		JSONParser parser = new JSONParser();
		try {
			JSONObject json = (JSONObject) parser.parse(data);
			String name = (String) json.get("name");
			int type = ((Long) json.get("type")).intValue();
			int accuracy = ((Long) json.get("accuracy")).intValue();
			long timestamp = (long) json.get("timestamp");

			JSONArray values = (JSONArray) json.get("values");
			Iterator<Long> iterator = values.iterator();
			while (iterator.hasNext()) {
				float heartrate = iterator.next();
				if (heartrate > 0) {
					System.out.println("HeartRate is: " + heartrate);
					mHeartRateCounter = 0;
					if (!mHeartRateDetectd) {
						mMessageListener.callbackMessageReceiver(AppConstants.STATE_HEART_BEAT_DETECTED,
								"HeartBeat detected");
						mMessageListener.callbackMessageReceiver(AppConstants.STATE_HEART_BEATING,
								"(" + heartrate + ")");
						mLastHeartrateTimestamp = System.currentTimeMillis();
						mHeartRateDetectd = true;
					} else {
						mMessageListener.callbackMessageReceiver(AppConstants.STATE_HEART_BEATING,
								"(" + heartrate + ")");
						mLastHeartrateTimestamp = System.currentTimeMillis();
						mHeartRateCounter = 0;
					}
					return;
				} else if (mHeartRateDetectd && heartrate == 0 && mHeartRateCounter < 5) {
					mHeartRateCounter++;
				} else if (mHeartRateDetectd && heartrate == 0 && mHeartRateCounter == 5) {

					mMessageListener.callbackMessageReceiver(AppConstants.STATE_HEART_STOPPED, "No Heartbeat detected");
					sendMessage(AppConstants.COMMAND_NOT_AUTHENTICATED);
					mHeartRateDetectd = false;
					mHeartRateCounter = 0;
				}
			}

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void validateProximity(String proximity) {

		/*
		 * if (proximity.equals(AppConstants.PROXIMITY_IMMEDIATE)) {
		 * mProximityCounter = 0;
		 * mMessageListener.callbackMessageReceiver(AppConstants.
		 * STATE_PROXIMITY, AppConstants.PROXIMITY_IMMEDIATE);
		 * mMessageListener.callbackMessageReceiver(AppConstants.STATE_UNLOCKED,
		 * "unlocked"); }
		 */
		if (proximity.equals(AppConstants.PROXIMITY_NEAR)) {
			mProximityDetected = true;
			mProximityCounter = 0;
			if (mHeartRateDetectd) {
				mMessageListener.callbackMessageReceiver(AppConstants.STATE_PROXIMITY, AppConstants.PROXIMITY_NEAR);
				mMessageListener.callbackMessageReceiver(AppConstants.STATE_UNLOCKED, "unlocked");
			}
		}

		if (proximity.equals(AppConstants.PROXIMITY_FAR)) {
			mProximityDetected = true;
			mProximityCounter++;

			System.out.println("Proximity: far (" + mProximityCounter + ")");
			if (mProximityCounter == 3) {
				System.out.println("lock pc!");
				if (mHeartRateDetectd) {
					mMessageListener.callbackMessageReceiver(AppConstants.STATE_PROXIMITY, AppConstants.PROXIMITY_FAR);
					mMessageListener.callbackMessageReceiver(AppConstants.STATE_LOCKED, "locked");
				}
			}

		}

	}

	private void validateLockState() {

		if (mHeartRateDetectd && mProximityDetected) {
			// Authenticated
		}
	}

	// Send a mesage to the client
	public void sendMessage(String message) {
		try {
			mOutput.write(message);
			mOutput.newLine();
			mOutput.flush();
		} catch (IOException ioException) {
			System.err.println("ERROR: Cannot send message, please retry!");
		}
	}

	private void closeConnection() {
		try {
			sendMessage(AppConstants.COMMAND_DISCONNECT);
			mOutput.close();
			mInput.close();
			mConnection.close();
			System.out.println("Connection closed!");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public boolean isConnected() {
		return mIsConnectedToWatch;
	}

	public void seIsConnected(boolean isConnected) {
		mIsConnectedToWatch = isConnected;
	}

	public boolean isRunning() {
		return mDataExchangeIsRunning;
	}

	@SuppressWarnings("unchecked")
	private JSONObject getConnectionJSON() {
		try {
			int checksum = -1;
			String message = "Connection failed!";

			if (mIsConnectedToWatch) {
				checksum = (int) (Math.random() * 9999);
				message = "You are connected!";
			}

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("dataType", "connectionData");
			jsonObject.put("checksum", checksum);
			jsonObject.put("message", message);

			return jsonObject;

		} catch (Exception jsone) {
			System.out.println("ERROR creating  successful connection JSONObject! Exception: " + jsone.getMessage());
			return null;
		}

	}

	// show on Screen, use as callback to show if authenticated or not
	public interface MessageCallback {
		public void callbackMessageReceiver(String message);

		public void callbackMessageReceiver(int state, String message);
	}
}