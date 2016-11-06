package com.android.lmu.mt.tokt.authenticator.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.lmu.mt.tokt.authenticator.keydetection.KeyPressDetectionService;
import com.android.lmu.mt.tokt.authenticator.shared.AppConstants;
import com.android.lmu.mt.tokt.authenticator.ui.MainActivity;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by tobiaskeinath on 28.08.16.
 */
public class MessageReceiverService extends WearableListenerService {

    private static final String TAG = MessageReceiverService.class.getSimpleName();

    private static final long[] logoutVibratePattern = {0, 250, 50, 250};

    private static final long lockUnLockDuration = 150;

    //TODO: first unlock
    private static final long autehnticated = 500;

    private WatchClient mWatchClient;

    private LocalBroadcastManager mLocalBroadcastManager;
    private SharedPreferences mSharedPreferences;

    private ExecutorService mExecutorService;

    private Intent mAuthenticatorIntent;
    private Intent mKeydetectorIntent;

    private Vibrator mVibrator;


    @Override
    public void onCreate() {
        super.onCreate();

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

        mWatchClient = WatchClient.getInstance(this);
        mExecutorService = Executors.newCachedThreadPool();

        mSharedPreferences = getSharedPreferences(
                AppConstants.SHARED_PREF_APP_KEY, Context.MODE_PRIVATE);

        mAuthenticatorIntent = new Intent(this, AuthenticatorWatchService.class);
        mKeydetectorIntent = new Intent(this, KeyPressDetectionService.class);

        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

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
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        Log.d(TAG, "Received message: " + messageEvent.getPath());

        //TODO: show that service is running in Activity

        if (messageEvent.getPath().equals(AppConstants.CLIENT_PATH_START_ACTIVITY)) {
            Intent startIntent = new Intent(this, MainActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);

            sendResult(AppConstants.MESSAGE_RECEIVER_RESULT, AppConstants.MESSAGE_RECEIVER_MESSAGE, "CONNECTED");
        }

        if (messageEvent.getPath().equals(AppConstants.CLIENT_PATH_START_KEYPRESS_DETECTOR)) {
            Log.d(TAG, "message from phone received: START KeyDetectorService");


            sendResult(AppConstants.MESSAGE_RECEIVER_START_TYPING_DETECTION_RESULT,
                    AppConstants.MESSAGE_RECEIVER_START_TYPING_DETECTION_MESSAGE, "START TYPING DETECTION");
            mExecutorService.submit(new Runnable() {
                @Override
                public void run() {
                    startService(mKeydetectorIntent);
                }
            });
        }

        if (messageEvent.getPath().equals(AppConstants.CLIENT_PATH_STOP_KEYPRESS_DETECTOR)) {
            Log.d(TAG, "message from phone received: STOP KeyDetectorService");
            sendResult(AppConstants.MESSAGE_RECEIVER_STOP_TYPING_DETECTION_RESULT,
                    AppConstants.MESSAGE_RECEIVER_STOP_TYPING_DETECTION_MESSAGE, "STOP TYPING DETECTION");

            mExecutorService.submit(new Runnable() {
                @Override
                public void run() {
                    stopService(mKeydetectorIntent);
                }
            });
        }

        if (messageEvent.getPath().equals(AppConstants.CLIENT_PATH_START_MEASUREMENT)) {

            String beaconBLNAme = new String(messageEvent.getData(), Charset.forName("UTF-8"));
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(AppConstants.SHARED_PREF_BEACON_BL_NAME, beaconBLNAme);
            editor.commit();

            //start service

            mExecutorService.submit(new Runnable() {
                @Override
                public void run() {
                    startService(mAuthenticatorIntent);
                }
            });

            //UpdatedUI
            sendResult(AppConstants.MESSAGE_RECEIVER_RESULT, AppConstants.MESSAGE_RECEIVER_MESSAGE, "Collecting Data");

        }

        if (messageEvent.getPath().equals(AppConstants.CLIENT_PATH_STOP_MEASUREMENT)) {

            mVibrator.vibrate(logoutVibratePattern, -1);

            sendResult(AppConstants.MESSAGE_RECEIVER_STOP_TYPING_DETECTION_RESULT,
                    AppConstants.MESSAGE_RECEIVER_STOP_TYPING_DETECTION_MESSAGE, "STOP TYPING DETECTION");
            sendResult(AppConstants.MESSAGE_RECEIVER_RESULT, AppConstants.MESSAGE_RECEIVER_MESSAGE, "DISCONNECTED");

            //stopservice
            mExecutorService.submit(new Runnable() {
                @Override
                public void run() {
                    stopService(mAuthenticatorIntent);
                }
            });
        }

        if (messageEvent.getPath().equals(AppConstants.CLIENT_PATH_LISTEN_TO_SOUND)) {
            sendResult(AppConstants.SOUND_LISTENING_RESULT, AppConstants.SOUND_LISTENING_MESSAGE, "");
        }

        if (messageEvent.getPath().equals(AppConstants.CLIENT_PATH_LOCKED)) {
            mVibrator.vibrate(lockUnLockDuration);
            sendResult(AppConstants.MESSAGE_RECEIVER_LOCK_RESULT, AppConstants.MESSAGE_RECEIVER_LOCK_MESSAGE, "locked");
        }
        if (messageEvent.getPath().equals(AppConstants.CLIENT_PATH_UNLOCKED)) {
            mVibrator.vibrate(lockUnLockDuration);
            sendResult(AppConstants.MESSAGE_RECEIVER_LOCK_RESULT, AppConstants.MESSAGE_RECEIVER_LOCK_MESSAGE, "unlocked");
        }

        if (messageEvent.getPath().equals(AppConstants.CLIENT_PATH_BEACON_UUID)) {

            String beaconUUID = new String(messageEvent.getData(), Charset.forName("UTF-8"));
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(AppConstants.SHARED_PREF_BEACON_UUID, beaconUUID);
            editor.commit();

            stopService(mAuthenticatorIntent);
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ie) {
                Log.e(TAG, "Error restarting service after beacon Id changed. Exception: " + ie.toString());
            }

            startService(mAuthenticatorIntent);
        }

        if (messageEvent.getPath().equals(AppConstants.CLIENT_PATH_BEACON_BL_NAME)) {

            String beaconBLNAme = new String(messageEvent.getData(), Charset.forName("UTF-8"));
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(AppConstants.SHARED_PREF_BEACON_BL_NAME, beaconBLNAme);
            editor.commit();

            stopService(mAuthenticatorIntent);
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ie) {
                Log.e(TAG, "Error restarting service after beacon Id changed. Exception: " + ie.toString());
            }

            startService(mAuthenticatorIntent);
        }


    }

    private void sendResult(String intentAction, String extraName, String message) {
        Intent intent = new Intent(intentAction);
        if (message != null)
            intent.putExtra(extraName, message);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

}
