package com.android.lmu.mt.tokt.authenticator;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.lmu.mt.tokt.authenticator.shared.AppConstants;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by tobiaskeinath on 28.08.16.
 */
public class MessageReceiverService extends WearableListenerService {

    private static final String TAG = MessageReceiverService.class.getSimpleName();

    private WatchClient mWatchClient;

    private LocalBroadcastManager mLocalBroadcastManager;

    @Override
    public void onCreate() {
        super.onCreate();

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

        mWatchClient = WatchClient.getInstance(this);
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

                //Example for setting filter
                if (path.startsWith("/filter")) {
                    DataMap dataMap = DataMapItem.fromDataItem(dataItem).getDataMap();
                    int filterById = dataMap.getInt(AppConstants.DATA_MAP_KEY_FILTER);
                    mWatchClient.setSensorFilter(filterById);
                }
            }
        }
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

            sendResult(AppConstants.MESSAGE_RECEIVER_RESULT, AppConstants.MESSAGE_RECEIVER_MESSAGE, "CONNECTED");

            //start service
            startService(new Intent(this, SensorService.class));
            startService(new Intent(this, BeaconService.class));
        }

        if (messageEvent.getPath().equals(AppConstants.CLIENT_PATH_STOP_MEASUREMENT)) {

            sendResult(AppConstants.MESSAGE_RECEIVER_RESULT, AppConstants.MESSAGE_RECEIVER_MESSAGE, "DISCONNECTED");

            //stopservice
            stopService(new Intent(this, SensorService.class));
            stopService(new Intent(this, BeaconService.class));
        }

        if (messageEvent.getPath().equals(AppConstants.CLIENT_PATH_LISTEN_TO_SOUND)) {
            sendResult(AppConstants.SOUND_LISTENING_RESULT, AppConstants.SOUND_LISTENING_MESSAGE, "");
        }
    }

    private void sendResult(String intentAction, String extraName, String message) {
        Intent intent = new Intent(intentAction);
        if (message != null)
            intent.putExtra(extraName, message);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

}
