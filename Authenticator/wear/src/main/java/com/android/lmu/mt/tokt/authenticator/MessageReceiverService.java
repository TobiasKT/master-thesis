package com.android.lmu.mt.tokt.authenticator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.lmu.mt.tokt.authenticator.shared.AppConstants;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.charset.Charset;

/**
 * Created by tobiaskeinath on 28.08.16.
 */
public class MessageReceiverService extends WearableListenerService {

    private static final String TAG = MessageReceiverService.class.getSimpleName();

    private WatchClient mWatchClient;

    private LocalBroadcastManager mLocalBroadcastManager;

    private SharedPreferences mSharedPreferences;


    @Override
    public void onCreate() {
        super.onCreate();

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

        mWatchClient = WatchClient.getInstance(this);

        mSharedPreferences = getSharedPreferences(
                AppConstants.SHARED_PREF_APP_KEY, Context.MODE_PRIVATE);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        super.onDataChanged(dataEventBuffer);

        //TODO: Process Data received from watch

        for (DataEvent dataEvent : dataEventBuffer) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataItem dataItem = dataEvent.getDataItem();
                Uri uri = dataItem.getUri();
                String path = uri.getPath();


            }
        }
    }

    @Override
    public void onPeerDisconnected(Node node) {
        super.onPeerDisconnected(node);
        Log.d(TAG, "Disconnect from Phone");

        stopService(new Intent(this, AuthenticatorWatchService.class));
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        Log.d(TAG, "Received message: " + messageEvent.getPath());

        //TODO: show that service is running in Activity

        if (messageEvent.getPath().equals(AppConstants.CLIENT_PATH_START_MEASUREMENT)) {
            //start Activity
            Intent startIntent = new Intent(this, MainActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);

            //start service
            startService(new Intent(this, AuthenticatorWatchService.class));
        }

        if (messageEvent.getPath().equals(AppConstants.CLIENT_PATH_STOP_MEASUREMENT)) {

            sendResult(AppConstants.MESSAGE_RECEIVER_RESULT, AppConstants.MESSAGE_RECEIVER_MESSAGE, "DISCONNECTED");

            //stopservice
            stopService(new Intent(this, AuthenticatorWatchService.class));
        }

        if (messageEvent.getPath().equals(AppConstants.CLIENT_PATH_LISTEN_TO_SOUND)) {
            sendResult(AppConstants.SOUND_LISTENING_RESULT, AppConstants.SOUND_LISTENING_MESSAGE, "");
        }

        if (messageEvent.getPath().equals(AppConstants.CLIENT_PATH_LOCKED)) {
            sendResult(AppConstants.MESSAGE_RECEIVER_LOCK_RESULT, AppConstants.MESSAGE_RECEIVER_LOCK_MESSAGE, "locked");
        }
        if (messageEvent.getPath().equals(AppConstants.CLIENT_PATH_UNLOCKED)) {
            sendResult(AppConstants.MESSAGE_RECEIVER_LOCK_RESULT, AppConstants.MESSAGE_RECEIVER_LOCK_MESSAGE, "unlocked");
        }

        if (messageEvent.getPath().equals(AppConstants.CLIENT_PATH_USER_AUTHENTICATED)) {
            mWatchClient.setAuthenticated(true);
        }
        if (messageEvent.getPath().equals(AppConstants.CLIENT_PATH_USER_NOT_AUTHENTICATED)) {
            mWatchClient.setAuthenticated(false);
        }
        if (messageEvent.getPath().equals(AppConstants.CLIENT_PATH_BEACON_UUID)) {

            String beaconUUID = new String(messageEvent.getData(), Charset.forName("UTF-8"));
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(AppConstants.SHARED_PREF_BEACON_UUID, beaconUUID);
            editor.commit();

            stopService(new Intent(this, AuthenticatorWatchService.class));
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ie) {
                Log.e(TAG, "Error restarting service after beacon Id changed. Exception: " + ie.toString());
            }

            startService(new Intent(this, AuthenticatorWatchService.class));
        }

        if (messageEvent.getPath().equals(AppConstants.CLIENT_PATH_START_KEYPRESS_DETECTOR)) {


            sendResult(AppConstants.MESSAGE_RECEIVER_RESULT, AppConstants.MESSAGE_RECEIVER_MESSAGE, "CONNECTED");
            startService(new Intent(this, KeypressDetectorService.class));
        }

        if (messageEvent.getPath().equals(AppConstants.CLIENT_PATH_STOP_KEYPRESS_DETECTOR)) {
            stopService(new Intent(this, KeypressDetectorService.class));
        }

    }

    @Override
    public void onPeerConnected(Node node) {
        super.onPeerConnected(node);
        Log.d(TAG, "watch connected to phone");
    }

    private void sendResult(String intentAction, String extraName, String message) {
        Intent intent = new Intent(intentAction);
        if (message != null)
            intent.putExtra(extraName, message);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

}
