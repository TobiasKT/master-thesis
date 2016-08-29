package com.android.lmu.mt.tokt.authenticator;

import android.content.Context;
import android.util.Log;
import android.util.SparseLongArray;

import com.android.lmu.mt.tokt.authenticator.shared.AppConstants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Arrays;
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
    private ExecutorService mExecutorService;
    private int mFilterId;

    private SparseLongArray mLastSensorData;

    private WatchClient(Context context) {
        mContext = context;

        mGoogleApiClient = new GoogleApiClient.Builder(context).addApi(Wearable.API).build();
        mGoogleApiClient.connect();

        mExecutorService = Executors.newCachedThreadPool();
        mLastSensorData = new SparseLongArray();
    }

    public void setSensorFilter(int filterId) {
        Log.d(TAG, "Filter set: " + filterId);
        mFilterId = filterId;
    }

    public void sendSensorData(final int sensorType, final int accuracy, final long timestamp, final float[] values) {
        long t = System.currentTimeMillis();

        long lastTimestamp = mLastSensorData.get(sensorType);
        long timeAgo = t - lastTimestamp;

        if (lastTimestamp != 0) {
            if (mFilterId == sensorType && timeAgo < 100) {
                return;
            }

            if (mFilterId != sensorType && timeAgo < 3000) {
                return;
            }
        }

        mLastSensorData.put(sensorType, t);

        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                sendSensorDataInBackground(sensorType, accuracy, timestamp, values);
            }
        });
    }

    private void sendSensorDataInBackground(int sensorType, int accuracy, long timestamp, float[] values) {

        if (sensorType == mFilterId) {
            Log.i(TAG, "Sensor (filtered) " + sensorType + " = " + Arrays.toString(values));
        } else {
            Log.d(TAG, "Sensor " + sensorType + " = " + Arrays.toString(values));
        }

        PutDataMapRequest dataMap = PutDataMapRequest.create("/sensors/" + sensorType);

        dataMap.getDataMap().putInt(AppConstants.DATA_MAP_KEY_ACCURACY, accuracy);
        dataMap.getDataMap().putLong(AppConstants.DATA_MAP_KEY_TIMESTAMP, timestamp);
        dataMap.getDataMap().putFloatArray(AppConstants.DATA_MAP_KEY_VALUES, values);

        PutDataRequest putDataRequest = dataMap.asPutDataRequest();
        send(putDataRequest);
    }

    private boolean validateConnection() {
        if (mGoogleApiClient.isConnected()) {
            return true;
        }

        ConnectionResult result = mGoogleApiClient.blockingConnect(CLIENT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);

        return result.isSuccess();
    }

    private void send(PutDataRequest putDataRequest) {
        if (validateConnection()) {
            Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                @Override
                public void onResult(DataApi.DataItemResult dataItemResult) {
                    Log.v(TAG, "Sending sensor data: " + dataItemResult.getStatus().isSuccess());
                }
            });
        }
    }



}
