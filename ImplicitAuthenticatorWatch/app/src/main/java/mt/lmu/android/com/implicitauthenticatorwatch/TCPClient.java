package mt.lmu.android.com.implicitauthenticatorwatch;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by tobiaskeinath on 21.08.16.
 */
public class TCPClient {

    private static final String TAG = TCPClient.class.getSimpleName();

    private static final String SERVER_ENDED_CONNECTION = "server_end";

    private static String SERVER_IP = "192.168.178.96";
    //private static String SERVER_IP = "127.0.0.1";
    private static int SERVER_PORT = 8080;

    private OnMessageReceived mMessageListener = null;

    private BufferedReader mInput;
    private BufferedWriter mOutput;
    private String mMessage = "";
    private Socket mConnection;


    public TCPClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    public void startRunning() {
        try {
            connectToServer();
            setupStreams();
            whileDataExchange();
        } catch (EOFException eofException) {
            // System.out.println
            System.out.println("\n Client terminated the connection");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            closeConnection();
        }

    }

    // connect to server
    private void connectToServer() throws IOException {
        Log.i(TAG, "Attempting to connect server: " + SERVER_IP + ":" + SERVER_PORT);
        mConnection = new Socket(SERVER_IP, SERVER_PORT);
        Log.i(TAG, "Connection Established! Connected to: " + mConnection.getInetAddress().getHostName());
    }

    private void setupStreams() throws IOException {
        mOutput = new BufferedWriter(new OutputStreamWriter(mConnection.getOutputStream(), "UTF-8"));
        mOutput.flush();

        mInput = new BufferedReader(new InputStreamReader(mConnection.getInputStream(), "UTF-8"));
        System.out.println("\n Streams are now setup \n");
    }


    private void whileDataExchange() throws IOException {
        do {
            try {
                mMessage = (String) mInput.readLine();
                if (mMessage != null && mMessageListener != null) {
                    Log.i(TAG, "Message from SERVER: " + mMessage + "\n");
                }

            } catch (IOException ioe) {
                System.out.println("Unknown data received!");
                Log.e(TAG, ioe.getMessage());
            }
        } while (mMessage != null && !mMessage.equals(SERVER_ENDED_CONNECTION));
    }

    public void sendMessage(String message) {

        try {
            mOutput.write(message);
            mOutput.newLine();
            mOutput.flush();
            // Thread.sleep(5000);
            System.out.println("\nCLIENT - " + message);
        } catch (IOException ioException) {
            System.out.println("\n Oops! Something went wrong!");
            //  } catch (InterruptedException ie) {
            //    Log.e(TAG, ie.getMessage());

        }
    }

    private void closeConnection() {
        System.out.println("\n Closing the connection!");

        try {
            mOutput.close();
            mInput.close();
            mConnection.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public interface OnMessageReceived {
        void messageReceived(String message);
    }

}
