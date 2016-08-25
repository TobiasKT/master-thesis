import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import org.json.simple.JSONObject;

public class TCPServer extends Thread {

	public static void main(String[] args) {

		TCPServer tcpserver = new TCPServer(new TCPServer.MessageCallback() {

			@Override
			public void callbackMessageReceiver(String message) {
				System.out.println("Message from Client: " + message);
			}

			@Override
			public void callbackMessageReceiver(int state) {
				switch (state) {
				case AppConstants.STATE_CONNECTED:
					System.out.println("Successfully connected to SmartWatch!");
					break;
				case AppConstants.ERROR:
					System.out.println("Failing to connect to SmartWatch!");
					break;
				default:
					break;
				}
			}
		});
		tcpserver.start();
	}

	private ServerSocket mServer;
	private Socket mConnection;

	private int SERVER_PORT = 8080;
	private MessageCallback mMessageListener;

	private BufferedReader mInput;
	private BufferedWriter mOutput;
	private String mIncomingMessage;

	private int mChecksum;
	private boolean mRun = false;
	private boolean mIsConnected = false;

	public TCPServer(MessageCallback messageListener) {
		mMessageListener = messageListener;
	}

	public TCPServer(MessageCallback messageListener, int port) {
		this(messageListener);
		SERVER_PORT = port;
	}

	@Override
	public void run() {
		startRunning();

	}

	private void startRunning() {

		mRun = true;

		try {
			mServer = new ServerSocket(SERVER_PORT);
			try {
				while (true) {
					waitForConnection();
					setupStreams();
					whileDataExchange();
				}
			} catch (EOFException eof) {
				System.out.println("SERVER ended the connection! ");
			} finally {
				closeConnection();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private void waitForConnection() {
		System.out.println("Wait for client to connect...");

		try {
			mConnection = mServer.accept();
			System.out.println(
					"CLIENT connected to " + mConnection.getInetAddress().getHostAddress() + ":" + SERVER_PORT);
			// publish to mainscreen
		} catch (IOException ioe) {
			System.err.println("Error while trying client to connect! Exception: " + ioe.getMessage());
			mIsConnected = false;
		}

	}

	private void setupStreams() throws IOException {
		mOutput = new BufferedWriter(new OutputStreamWriter(mConnection.getOutputStream(), "UTF-8"));
		mOutput.flush();

		mInput = new BufferedReader(new InputStreamReader(mConnection.getInputStream(), "UTF-8"));
		System.out.println("Streams are now setup.");
	}

	private void whileDataExchange() throws IOException {

		// TODO: Request allowance to pair!!!!
		if (!mIsConnected) {
			// send ConfirmConnectionRequest
			int checksum = getChecksum();
			sendMessage(AppConstants.COMMAND_CONFIRM + ":" + checksum);
			System.out.println("Confirmation Request to client with: " + checksum);
		}

		while (mRun) {
			mIncomingMessage = mInput.readLine();
			if (mIncomingMessage != null && mMessageListener != null) {

				// confirm connection
				if (mIncomingMessage.contains(AppConstants.COMMAND_CONFIRM)) {

					int receivedChecksum = Integer.parseInt(mIncomingMessage.split(":")[1]);
					if (receivedChecksum == mChecksum) {
						mIsConnected = true;
						mMessageListener.callbackMessageReceiver(AppConstants.STATE_CONNECTED);
						sendMessage(AppConstants.COMMAND_CONNECT);
					} else {
						mRun = false;
						mMessageListener.callbackMessageReceiver(AppConstants.ERROR);
						sendMessage(AppConstants.COMMAND_DISCONNECT);
					}

				}

				if (mIncomingMessage.equals(AppConstants.COMMAND_DISCONNECT)) {
					mRun = false;
				}
				
				mMessageListener.callbackMessageReceiver(mIncomingMessage);
			}
			mIncomingMessage = null;
		}

	}

	// Send a mesage to the client
	private void sendMessage(String message) {
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
			sendMessage(AppConstants.COMMAND_CONNECT);
			mOutput.close();
			mInput.close();
			mConnection.close();
			System.out.println("Connection closed!");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public boolean isConnected() {
		return mIsConnected;
	}

	public void seIsConnected(boolean isConnected) {
		mIsConnected = isConnected;
	}

	public boolean isRunning() {
		return mRun;
	}

	@SuppressWarnings("unchecked")
	private JSONObject getConnectionJSON() {
		try {
			int checksum = -1;
			String message = "Connection failed!";

			if (mIsConnected) {
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

	private int getChecksum() {

		// max=9999
		// min=1000
		Random rand = new Random();
		int checksum = rand.nextInt(9999 - 1000 + 1) + 1000;
		mChecksum = checksum;
		return checksum;
	}

	// show on Screen, use as callback to show if authenticated or not
	public interface MessageCallback {
		public void callbackMessageReceiver(String message);

		public void callbackMessageReceiver(int state);
	}

}