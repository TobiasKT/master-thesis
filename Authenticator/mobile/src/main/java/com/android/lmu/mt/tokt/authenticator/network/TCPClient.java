package com.android.lmu.mt.tokt.authenticator.network;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.lmu.mt.tokt.authenticator.database.EventEntry;
import com.android.lmu.mt.tokt.authenticator.shared.AppConstants;
import com.android.lmu.mt.tokt.authenticator.util.Util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import io.realm.Realm;
import io.realm.exceptions.RealmMigrationNeededException;

/**
 * Created by tobiaskeinath on 30.08.16.
 */
public class TCPClient {

    private static final String TAG = TCPClient.class.getSimpleName();

    private final Handler mHandler;

    private String SERVER_IP = AppConstants.DEFAULT_SERVER_IP; //Public IP address of the computer
    private int SERVER_PORT = AppConstants.DEFAULT_SERVER_PORT;

    private MessageCallBack mMessageListener = null;

    private BufferedReader mInput;
    private BufferedWriter mOutput;

    private String mIncomingMessage;

    private Socket mConnection;

    private boolean mRun = false;

    private Realm mRealm;

    private boolean mIsConnected = false;


    public TCPClient(Context context, Handler handler, MessageCallBack listener) {
        mHandler = handler;
        mMessageListener = listener;


        try {
            mRealm = Realm.getInstance(context);
        } catch (RealmMigrationNeededException e) {
            try {
                Realm.deleteRealmFile(context);
                mRealm = Realm.getInstance(context);
            } catch (Exception ex) {
                throw ex;
                //No Realm file to remove.
            }
        }
    }


    public TCPClient(String ipAddress, int port, Context context, Handler handler, MessageCallBack listener) {
        this(context, handler, listener);
        SERVER_IP = ipAddress;
        SERVER_PORT = port;
    }


    //start TCP Client
    public void run() {

        mRun = true;

        try {
            connectToServer();
            setupStreams();
            whileDataExchange();
        } catch (EOFException eofe) {
            Log.e(TAG, "Client terminated the connection! Exception: " + eofe.getMessage());
            mHandler.sendEmptyMessage(AppConstants.ERROR);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            Log.e(TAG, "Error while connection to server/setting uo streams! Exception: " + ioe.getMessage());
            mHandler.sendEmptyMessage(AppConstants.ERROR);
        } finally {
            Log.d(TAG, "finally closing connection");
            closeConnection();
        }

    }

    // stop TCP client
    public void stopClient() {
        Log.d(TAG, "Client stopped!");
        mRun = false;
        sendMessage(AppConstants.COMMAND_PHONE_WATCH_DISCONNECT);
        try {
            mOutput.close();
            mInput.close();
        } catch (IOException ioe) {
            Log.d(TAG, "IO ERROR stopping tcpsclient. Exception: " + ioe.getMessage());
        }
    }

    // try to connect to server
    private void connectToServer() throws IOException {
        Log.i(TAG, "Attempting to connect server: " + SERVER_IP + ":" + SERVER_PORT);

        mConnection = new Socket(SERVER_IP, SERVER_PORT);
        Log.i(TAG, "Connection Established! Connected to: " + mConnection.getInetAddress().getHostAddress());
    }

    // set up streams
    private void setupStreams() throws IOException {
        mOutput = new BufferedWriter(new OutputStreamWriter(mConnection.getOutputStream(), "UTF-8"));
        mOutput.flush();
        mInput = new BufferedReader(new InputStreamReader(mConnection.getInputStream(), "UTF-8"));

        Log.i(TAG, "Streams are now setup.");
    }


    // receive messages from server and process them
    private void whileDataExchange() throws IOException {


        while (mRun) {

            //TODO: Beobachten
            if (mInput.ready()) {
                mIncomingMessage = mInput.readLine();
            }

            if (mIncomingMessage != null && mMessageListener != null) {

                if (mIncomingMessage.contains(AppConstants.COMMAND_PHONE_WATCH_CONNECTION_CONFIRM)) {

                    int receivedChecksum = Integer.parseInt(mIncomingMessage.split(":")[1]);

                    //Send Message to MainActivity
                    Message completeMessage =
                            mHandler.obtainMessage(AppConstants.STATE_CONFIRM, receivedChecksum);
                    mHandler.sendMessage(completeMessage);
                }

                if (mIncomingMessage.equals(AppConstants.COMMAND_PHONE_WATCH_CONNECT)) {
                    Message completeMessage =
                            mHandler.obtainMessage(AppConstants.STATE_CONNECTED,
                                    "You are connected to autenticaton service");
                    mHandler.sendMessage(completeMessage);
                }

                if (mIncomingMessage.equals(AppConstants.COMMAND_PHONE_WATCH_DISCONNECT)) {
                    Message completeMessage =
                            mHandler.obtainMessage(AppConstants.STATE_DISCONNECTED,
                                    "Server closed connection!");
                    mHandler.sendMessage(completeMessage);
                    stopClient();
                    break;
                }

                if (mIncomingMessage.equals(AppConstants.COMMAND_USER_NOT_AUTHENTICATED)) {

                    Message completeMessage =
                            mHandler.obtainMessage(AppConstants.STATE_NOT_AUTHENTICATED,
                                    "You are not authenticated!");
                    mHandler.sendMessage(completeMessage);
                }

                if (mIncomingMessage.equals(AppConstants.COMMAND_START_SENDING_SENSORDATA)) {
                    Message completeMessage =
                            mHandler.obtainMessage(AppConstants.STATE_AUTHENTICATED,
                                    "You are authenticated! Authenticator service is running...");
                    mHandler.sendMessage(completeMessage);
                    sendMessage(AppConstants.COMMAND_USER_AUTHENTICATED);
                }


                if (mIncomingMessage.equals(AppConstants.COMMAND_LISTEN_TO_SOUND)) {
                    Message completeMessage =
                            mHandler.obtainMessage(AppConstants.START_LISTEN_TO_SOUND,
                                    "Start voice recognition");
                    mHandler.sendMessage(completeMessage);
                }

                if (mIncomingMessage.equals(AppConstants.COMMAND_LOCKED)) {
                    Message completeMessage =
                            mHandler.obtainMessage(AppConstants.STATE_LOCKED,
                                    "PC locked!");
                    mHandler.sendMessage(completeMessage);
                }

                if (mIncomingMessage.equals(AppConstants.COMMAND_UNLOCKED)) {
                    Message completeMessage =
                            mHandler.obtainMessage(AppConstants.STATE_UNLOCKED,
                                    "PC unlocked!");
                    mHandler.sendMessage(completeMessage);
                }

                if (mIncomingMessage.equals(AppConstants.COMMAND_START_TYPING_SENSORS)) {
                    Message completeMessage =
                            mHandler.obtainMessage(AppConstants.STATE_SEND_TYPING_VALUES,
                                    "Start Typing Sensors");
                    mHandler.sendMessage(completeMessage);
                }

                if (mIncomingMessage.equals(AppConstants.COMMAND_STOP_TYPING_SENSORS)) {
                    Message completeMessage =
                            mHandler.obtainMessage(AppConstants.STATE_STOP_SENDING_TYPING_VALUES,
                                    "Stop Typing Sensors");
                    mHandler.sendMessage(completeMessage);
                }

                if (mIncomingMessage.contains(AppConstants.COMMAND_USERNAME)) {
                    String username = mIncomingMessage.split("::")[1];
                    Util.setUsername(username);
                    Message completeMessage =
                            mHandler.obtainMessage(AppConstants.SET_USERNAME,
                                    username);
                    mHandler.sendMessage(completeMessage);
                }
                if (mIncomingMessage.contains(AppConstants.COMMAND_SAVE_DIALOG_EVENT_LOCK)) {
                    saveEventToDB(mIncomingMessage, "lock");
                }
                if (mIncomingMessage.contains(AppConstants.COMMAND_SAVE_DIALOG_EVENT_UNLOCK)) {
                    saveEventToDB(mIncomingMessage, "unlock");
                }
                if (mIncomingMessage.contains(AppConstants.COMMAND_SAVE_DIALOG_EVENT_NOT_AUTHENTICATED)) {
                    saveEventToDB(mIncomingMessage, "logged_out");
                }


                mMessageListener.callbackMessageReceiver(mIncomingMessage);
            }
            mIncomingMessage = null;
        }


    }


    public void sendMessage(String message) {

        try {
            mOutput.write(message);
            mOutput.newLine();
            mOutput.flush();
        } catch (IOException ioException) {
            Log.e(TAG, "Something went wrong while sending message! Exception: " + ioException.getMessage());
        }
    }

    private void closeConnection() {
        try {
            mOutput.close();
            mInput.close();
            mConnection.close();
            mHandler.sendEmptyMessageDelayed(AppConstants.STATE_DISCONNECTED, 3000);
            Log.i(TAG, "Connection closed!");
        } catch (IOException ioException) {
            Log.e(TAG, "Error while closing connection! Exception: " + ioException.getMessage());
        }
    }


    public boolean isRunning() {
        return mRun;
    }

    private void saveEventToDB(String message, String eventName) {
        Log.d(TAG, "save event to DB");

        String[] values = message.split("::");
        int state = Integer.parseInt(values[1]);
        String usernote = values[2];

        mRealm.beginTransaction();
        EventEntry entry = mRealm.createObject(EventEntry.class);
        entry.setEventName(eventName);
        entry.setUsername(Util.getUsername());
        entry.setState(state);
        entry.setUsernote(usernote);
        entry.setTimeStamp(System.currentTimeMillis());
        mRealm.commitTransaction();
    }


    //Declare the interface. The method messageReceived(String message) will must be implemented
    // AsynckTask doInBackground
    public interface MessageCallBack {
        void callbackMessageReceiver(String message);
    }
}
