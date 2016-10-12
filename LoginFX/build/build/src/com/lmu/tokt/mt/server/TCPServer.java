package com.lmu.tokt.mt.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.lmu.tokt.mt.util.AppConstants;
import com.lmu.tokt.mt.util.Checksum;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class TCPServer extends Thread {

	/*-------------------------------------------*/
	// ifconfig | grep "inet " | grep -v 127.0.0.1

	private static final String TAG = TCPServer.class.getSimpleName();

	private ServerSocket mServer;
	private Socket mConnection;

	private int SERVER_PORT = 8888;
	private MessageCallback mMessageListener;

	private BufferedReader mInput;
	private BufferedWriter mOutput;
	private String mIncomingMessage;

	private static Checksum mChecksum;

	private boolean mServerIsRunning = false;
	private boolean mDataExchangeIsRunning = false;
	private boolean mIsConnectedToWatch = false;

	private boolean isHeartBeating = false;
	private boolean isWalking = false;
	private boolean isFar = false;

	private long mLastHeartrateTimestamp = 0;
	private long mLastStepCountTimeStamp = 0;
	private long mLastProximityImmediateNearTimestamp = 0;

	private float mLastStepCount = 0;

	private boolean isAuthenticated = false;
	private boolean isLocked = false;

	/* ------ Constructors ------- */

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

		if (mMessageListener != null) {
			mMessageListener.callbackMessageReceiver(AppConstants.STATE_SERVER_RUNNING, "(Server is runnning)");
		}

		try {
			mServer = new ServerSocket(SERVER_PORT);
			try {
				while (mServerIsRunning) {
					waitForConnection();
					setupStreams();
					whileDataExchange();
				}
				System.out.println(TAG + ": server STOPPED");
			} catch (EOFException eof) {
				System.out.println(TAG + ": server ENDED the connection ");
			} finally {
				closeConnection();
			}
		} catch (IOException ioe) {
			System.out.println(TAG + ": IO ERROR startRunning(). Exception: " + ioe.toString());
		}
	}

	public void stopRunning() {
		System.out.println(TAG + ": Server stopped!");
		mDataExchangeIsRunning = false;
		mIsConnectedToWatch = false;
		mServerIsRunning = false;
		/*
		 * if (mConnection != null) {
		 * sendMessage(AppConstants.COMMAND_PHONE_WATCH_DISCONNECT); }
		 * mMessageListener.callbackMessageReceiver(AppConstants.
		 * STATE_SERVER_STOPPED, "(Server stopped)"); try { mOutput.close();
		 * mInput.close(); } catch (IOException e) { System.out.println(TAG +
		 * ": Error stopping server. Exception " + e.toString()); }
		 */
	}

	private void waitForConnection() {
		System.out.println(TAG + ": wait for client to connect...");

		try {
			mConnection = mServer.accept();
			System.out.println(
					TAG + ": CLIENT connected to " + mConnection.getInetAddress().getHostAddress() + ":" + SERVER_PORT);
			mDataExchangeIsRunning = true;

		} catch (IOException ioe) {
			System.err.println(TAG + ": IO ERROR while trying client to connect! Exception: " + ioe.getMessage());
			mIsConnectedToWatch = false;
		}

	}

	private void setupStreams() throws IOException {
		mOutput = new BufferedWriter(new OutputStreamWriter(mConnection.getOutputStream(), "UTF-8"));
		mOutput.flush();

		mInput = new BufferedReader(new InputStreamReader(mConnection.getInputStream(), "UTF-8"));
		System.out.println(TAG + ": streams are nset up successfully");
	}

	private synchronized void whileDataExchange() throws IOException {

		if (!mIsConnectedToWatch) {
			// send ConfirmConnectionRequest
			sendMessage(AppConstants.COMMAND_PHONE_WATCH_CONNECTION_CONFIRM + ":" + mChecksum.getChecksum());
			System.out.println(TAG + ": confirmation Request to client with: " + mChecksum.getChecksum());
		}

		while (mDataExchangeIsRunning) {
			mIncomingMessage = mInput.readLine();
			if (mIncomingMessage != null && mMessageListener != null) {

				// confirm connection
				if (mIncomingMessage.contains(AppConstants.COMMAND_PHONE_WATCH_CONNECTION_CONFIRM)) {

					int receivedChecksum = Integer.parseInt(mIncomingMessage.split(":")[1]);
					if (receivedChecksum == mChecksum.getChecksum()) {
						mIsConnectedToWatch = true;
						mMessageListener.callbackMessageReceiver(AppConstants.STATE_PHONE_WATCH_CONNECTED,
								"Confirmation successful!");
						sendMessage(AppConstants.COMMAND_PHONE_WATCH_CONNECT);
					} else {
						mDataExchangeIsRunning = false;
						mIsConnectedToWatch = false;
						mMessageListener.callbackMessageReceiver(AppConstants.STATE_NETWORK_ERROR,
								"Confirmation failed!");
						sendMessage(AppConstants.COMMAND_PHONE_WATCH_DISCONNECT);
					}

				}

				if (mIncomingMessage.equals(AppConstants.COMMAND_PHONE_WATCH_DISCONNECT)) {
					mDataExchangeIsRunning = false;
					mIsConnectedToWatch = false;
					mMessageListener.callbackMessageReceiver(AppConstants.STATE_NETWORK_ERROR, "Client disconnected!");
				}

				if (mIncomingMessage.contains(AppConstants.SENSORDATA)) {
					String data = mIncomingMessage.split("::")[1];
					unpackSensorData(data);
				}

				if (mIncomingMessage.equals(AppConstants.COMMAND_USER_AUTHENTICATED)) {
					isAuthenticated = true;
					mMessageListener.callbackMessageReceiver(AppConstants.STATE_USER_AUTHENTICATED,
							"User authenticated");
				}

				if (mIncomingMessage.equals(AppConstants.COMMAND_USER_TYPING_SUCCESS)) {
					mMessageListener.callbackMessageReceiver(AppConstants.STATE_USER_WAS_TYPING, "Typing detected");
				}

				if (mIncomingMessage.equals(AppConstants.COMMAND_USER_TYPING_FAILED)) {
					mMessageListener.callbackMessageReceiver(AppConstants.STATE_USER_WAS_NOT_TYPING, "No typing detected");
				}
				
				if(mIncomingMessage.equals(AppConstants.COMMAND_TYPING_SENSORS_STARTED)){
					mMessageListener.callbackMessageReceiver(AppConstants.STATE_TYPING_SENSORS_STARTED, "recognizing typing");
				}
				// validate states by timeStamp

				validateHeartRateByTimeStamp();
				validateUserStateByTimeStamp();
				validateProximityByTimeStamp();

				validateAuthenticatorState();

				// mMessageListener.callbackMessageReceiver(mIncomingMessage);
			}
			mIncomingMessage = null;
		}
		System.out.println(TAG + ": Data exchange stopped!");

	}

	private void unpackSensorData(String data) {
		JSONParser parser = new JSONParser();
		try {
			JSONObject json = (JSONObject) parser.parse(data);
			String name = (String) json.get("name");
			int type = ((Long) json.get("type")).intValue();
			int accuracy = ((Long) json.get("accuracy")).intValue();
			long timestamp = (long) json.get("timestamp");

			JSONArray jsonArray = (JSONArray) json.get("values");
			float[] values = new float[jsonArray.size()];

			for (int i = 0; i < values.length; i++) {
				values[i] = (float) ((Long) jsonArray.get(i)).doubleValue();
			}

			if (name.equals(AppConstants.SENSOR_NAME_HEART_RATE)) {
				validateHeartRate(name, type, accuracy, timestamp, values);
			}
			if (name.equals(AppConstants.SENSOR_NAME_STEP_COUNTER)) {
				validateStepCount(name, type, accuracy, timestamp, values);
			}
			if (name.equals(AppConstants.SENSOR_NAME_BEACON)) {
				validateBeacon(name, type, accuracy, timestamp, values);
			}
		} catch (ParseException e) {
			System.out.println(TAG + ": PARSE ERROR unpacking sensor data. Exception: " + e.toString());
		}
	}

	private void validateHeartRate(String name, int type, int accuracy, long timestamp, float[] values) {

		System.out.println(TAG + ": " + name + " (" + type + "), values [" + Arrays.toString(values) + "]");

		float heartrate = values[0];
		if (heartrate > 0) {
			System.out.println("E/" + TAG + ": New HEARTRATE [" + heartrate + "]");
			mMessageListener.callbackMessageReceiver(AppConstants.STATE_HEART_BEATING, "" + heartrate);
			mLastHeartrateTimestamp = System.currentTimeMillis();
			isHeartBeating = true;
		}
	}

	private void validateStepCount(String name, int type, int accuracy, long timestamp, float[] values) {

		System.out.println(TAG + ": " + name + " (" + type + "), values [" + Arrays.toString(values) + "]");

		float stepCount = values[0];
		if (stepCount > mLastStepCount) {
			System.out.println("E/" + TAG + ": New step count [" + stepCount + "]");
			mLastStepCount = stepCount;
			mLastStepCountTimeStamp = System.currentTimeMillis();
		}
	}

	private void validateBeacon(String name, int type, int accuracy, long timestamp, float[] values) {

		System.out.println(TAG + ": " + name + " (" + type + "), values [" + Arrays.toString(values) + "]");

		String proximityString;

		int rssi = (int) values[0];
		if (rssi >= -71) {
			proximityString = AppConstants.PROXIMITY_IMMEDIATE;
			mLastProximityImmediateNearTimestamp = System.currentTimeMillis();
		} else if (rssi < -71 && rssi >= -81) {
			proximityString = AppConstants.PROXIMITY_NEAR;
			mLastProximityImmediateNearTimestamp = System.currentTimeMillis();
		} else {
			proximityString = AppConstants.PROXIMITY_FAR;
		}

		mMessageListener.callbackMessageReceiver(AppConstants.STATE_PROXIMITY_DETECTED, proximityString);

	}

	private void validateHeartRateByTimeStamp() {

		if (mLastHeartrateTimestamp != 0) {

			long timeAgo = getTimeAgo(mLastHeartrateTimestamp);

			System.out.println("I/" + TAG + ": last HEART BEAT time ago:" + timeAgo);

			if (timeAgo > 18000) {
				mMessageListener.callbackMessageReceiver(AppConstants.STATE_HEART_STOPPED, "NO HEARTBEAT");
				isHeartBeating = false;
			}
		}

	}

	private void validateUserStateByTimeStamp() {

		if (mLastStepCountTimeStamp != 0) {

			long timeAgo = getTimeAgo(mLastStepCountTimeStamp);

			System.out.println("I/ " + TAG + ": last STEP COUNT time ago:" + timeAgo);

			if (timeAgo > 3000) {
				mMessageListener.callbackMessageReceiver(AppConstants.STATE_USER_STILL, "still");
				isWalking = false;
			} else {
				mMessageListener.callbackMessageReceiver(AppConstants.STATE_USER_WALKING, "walking");
				isWalking = true;
			}
		}
	}

	private void validateProximityByTimeStamp() {

		if (mLastProximityImmediateNearTimestamp != 0) {

			long timeAgo = getTimeAgo(mLastProximityImmediateNearTimestamp);
			System.out.println("I/ " + TAG + ": last PROXIMTIY IMMEDIATE/NEAR time ago:" + timeAgo);

			if (timeAgo > 3500) {
				isFar = true;
			} else {
				isFar = false;
			}
		}
	}

	private long getTimeAgo(long timestamp) {
		long t = System.currentTimeMillis();
		long lastTimestamp = timestamp;
		long timeAgo = t - lastTimestamp;
		return timeAgo;
	}

	private static final int MIN = 1;
	private static final int MAX = 10;

	private int getRandomNumber() {
		Random rnd = new Random();
		int number = rnd.nextInt(MAX - MIN) + MIN;
		System.out.println(TAG + ": random number genereated (" + number + ")");
		return number;
	}

	private void validateAuthenticatorState() {

		if (isAuthenticated) {
			// autenticate again, re enter password
			if (!isHeartBeating && mLastHeartrateTimestamp != 0) {
				isAuthenticated = false;
				mLastHeartrateTimestamp = 0;
				mLastProximityImmediateNearTimestamp = 0;
				mLastStepCountTimeStamp = 0;
				mMessageListener.callbackMessageReceiver(AppConstants.STATE_USER_NOT_AUTHENTICATED, "");
				sendMessage(AppConstants.COMMAND_USER_NOT_AUTHENTICATED);

				if (getRandomNumber() > 7) {
					System.out.println(TAG + ": show NOT AUTHENTICATED dialog");
					mMessageListener.callbackMessageReceiver(AppConstants.DIALOG_EVENT_TYPE_NOT_AUTHENTICATED,
							"Was this UNLOGGING event as expected?");
				}
			}

			// lock
			// TODO: ausbessern
			if (isHeartBeating && (isWalking || isFar)) {
				// if (isHeartBeating && isFar) {
				if (!isLocked) {
					mMessageListener.callbackMessageReceiver(AppConstants.STATE_APP_LOCKED, "locked");
					isLocked = true;
					sendMessage(AppConstants.COMMAND_LOCKED);

					if (getRandomNumber() > 7) {
						System.out.println(TAG + ": show LOCK dialog");
						mMessageListener.callbackMessageReceiver(AppConstants.DIALOG_EVENT_TYPE_LOCK,
								"Was this LOCK event as expected?");
					}
				}

			}

			// unlock
			if (isHeartBeating && !isFar) {
				if (isLocked) {
					mMessageListener.callbackMessageReceiver(AppConstants.STATE_APP_UNLOCKED, "unlocked");
					isLocked = false;

					mMessageListener.callbackMessageReceiver(AppConstants.STATE_SOUND_SIGNAL_SENDING, "sending");
					sendMessage(AppConstants.COMMAND_LISTEN_TO_SOUND);
					// playUnlockSound();
					sendMessage(AppConstants.COMMAND_UNLOCKED);
					mMessageListener.callbackMessageReceiver(AppConstants.STATE_SOUND_SENDING_NONE, "none");

					if (getRandomNumber() > 7) {
						System.out.println(TAG + ": show UNLOCK dialog");
						mMessageListener.callbackMessageReceiver(AppConstants.DIALOG_EVENT_TYPE_UNLOCK,
								"Was this UNLOCK event as expected?");
					}
				}
			}
		}

	}

	private void playUnlockSound() {
		System.out.println(TAG + ": play unlock sound");
		final URL resource = getClass().getResource("back.mp3");
		Media hit = new Media(resource.toString());
		MediaPlayer mediaPlayer = new MediaPlayer(hit);
		mediaPlayer.play();
	}

	// Send a mesage to the client
	public void sendMessage(String message) {
		try {
			mOutput.write(message);
			mOutput.newLine();
			mOutput.flush();
		} catch (IOException ioException) {
			System.err.println(TAG + ": IO ERROR: Cannot send message, please retry! " + ioException.toString());
		}
	}

	private void closeConnection() {
		try {
			sendMessage(AppConstants.COMMAND_PHONE_WATCH_DISCONNECT);
			mOutput.close();
			mInput.close();
			mConnection.close();
			System.out.println(TAG + ": Connection closed!");
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
		return mServerIsRunning;
	}

	public boolean isDataExchange() {
		return mDataExchangeIsRunning;
	}

	public boolean isAuthenticated() {
		return isAuthenticated;
	}

	public void setAuthenticated(boolean isAuthenticated) {
		this.isAuthenticated = isAuthenticated;
	}

	// show on Screen, use as callback to show if authenticated or not
	public interface MessageCallback {
		public void callbackMessageReceiver(String message);

		public void callbackMessageReceiver(int state, String message);
	}
}