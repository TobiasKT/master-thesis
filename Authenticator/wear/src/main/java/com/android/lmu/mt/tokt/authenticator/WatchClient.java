package com.android.lmu.mt.tokt.authenticator;

import android.content.Context;
import android.util.Log;
import android.util.SparseLongArray;

import com.android.lmu.mt.tokt.authenticator.shared.AppConstants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by tobiaskeinath on 28.08.16.
 */
public class WatchClient {


    //TODO: filter entfernen!

    private static final String TAG = WatchClient.class.getSimpleName();

    private static final int CLIENT_CONNECTION_TIMEOUT = 15000;

    public static WatchClient instance;

    public static WatchClient getInstance(Context context) {
        if (instance == null) {
            return new WatchClient(context.getApplicationContext());
        }
        return instance;
    }


    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private ExecutorService mExecutorSensorService;
    private ExecutorService mExecutorBeaconService;
    private int mFilterId;

    private SparseLongArray mLastSensorData;
    private SparseLongArray mLastBeaconData;

    private WatchClient(Context context) {
        mContext = context;

        mGoogleApiClient = new GoogleApiClient.Builder(context).addApi(Wearable.API).build();
        mGoogleApiClient.connect();

        mExecutorSensorService = Executors.newCachedThreadPool();
        mExecutorBeaconService = Executors.newCachedThreadPool();
        mLastSensorData = new SparseLongArray();
        mLastBeaconData = new SparseLongArray();
    }

    public void setSensorFilter(int filterId) {
        Log.d(TAG, "Filter set: " + filterId);
        mFilterId = filterId;
    }

    public void sendSensorData(final int sensorType, final int accuracy, final long timestamp, final float[] values) {
        long t = System.currentTimeMillis();

        long lastTimestamp = mLastSensorData.get(sensorType);
        long timeAgo = t - lastTimestamp;

        if (sensorType != AppConstants.SENSOR_TYPE_HEART_RATE && lastTimestamp != 0) {
            if (timeAgo < 1500) {
                return;
            }
        }

        mLastSensorData.put(sensorType, t);
        mExecutorSensorService.submit(new Runnable() {
            @Override
            public void run() {
                sendSensorDataInBackground(sensorType, accuracy, timestamp, values);
            }
        });
    }

    private static final int BEACON = 123456;

    public void sendBeaconData(final String proximity, final long timestamp) {
        long t = System.currentTimeMillis();

        long lastTimestamp = mLastBeaconData.get(BEACON);
        long timeAgo = t - lastTimestamp;

/*
        if (lastTimestamp != 0) {
            if (timeAgo < 1500) {
                return;
            }
        }
*/
        mLastBeaconData.put(BEACON, t);

        mExecutorBeaconService.submit(new Runnable() {
            @Override
            public void run() {
                sendBeaconDataInBackground(proximity, timestamp);
            }
        });
    }


    private void sendBeaconDataInBackground(String proximity, long timestamp) {

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("name", "beacon");
            jsonObject.put("proximity", proximity);
            jsonObject.put("timestamp", timestamp);

        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        send(AppConstants.SERVER_PATH_BEACON_DATA, jsonObject.toString());

    }


    private void sendSensorDataInBackground(int sensorType, int accuracy, long timestamp, float[] values) {


        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("name", getSensorNameByType(sensorType));
            jsonObject.put("type", sensorType);
            jsonObject.put("accuracy", accuracy);
            jsonObject.put("timestamp", timestamp);

            //test
            JSONArray sensorValues = new JSONArray();
            if (values.length != 0) {
                for (int i = 0; i < values.length; i++) {
                    sensorValues.put(values[i]);
                }
                jsonObject.put("values", sensorValues);
            }

        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        send(AppConstants.SERVER_PATH_SENSOR_DATA, jsonObject.toString());
    }

    private boolean validateConnection() {

        if (mGoogleApiClient.isConnected()) {
            return true;
        }
        ConnectionResult result = mGoogleApiClient.blockingConnect(CLIENT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
        return result.isSuccess();
    }

    private void send(final String path, String data) {
        if (validateConnection()) {
            List<Node> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await().getNodes();

            Log.d(TAG, "Sending to nodes: " + nodes.size());

            for (Node node : nodes) {
                Log.i(TAG, "add node " + node.getDisplayName());

                byte[] byteData = data.getBytes(Charset.forName("UTF-8"));

                Wearable.MessageApi.sendMessage(
                        mGoogleApiClient, node.getId(), path, byteData
                ).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                        Log.d(TAG, "controlMeasurementInBackground(" + path + "): " + sendMessageResult.getStatus().isSuccess());
                    }
                });
            }
        }
    }

    private String getSensorNameByType(int type) {

        switch (type) {
            case AppConstants.SENSOR_TYPE_HEART_RATE:
                return AppConstants.SENSOR_NAME_HEART_RATE;
            case AppConstants.SENSOR_TYPE_STEP_COUNTER:
                return AppConstants.SENSOR_NAME_STEP_COUNTER;
            default:
                return "Unknown";
        }
    }
}
