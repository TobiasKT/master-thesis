package com.android.lmu.mt.tokt.authenticator;

import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.lmu.mt.tokt.authenticator.data.Sensor;
import com.android.lmu.mt.tokt.authenticator.data.SensorDataPoint;
import com.android.lmu.mt.tokt.authenticator.data.SensorNames;
import com.android.lmu.mt.tokt.authenticator.data.TagData;
import com.android.lmu.mt.tokt.authenticator.events.BusProvider;
import com.android.lmu.mt.tokt.authenticator.events.NewSensorEvent;
import com.android.lmu.mt.tokt.authenticator.events.SensorUpdatedEvent;
import com.android.lmu.mt.tokt.authenticator.events.TagAddedEvent;
import com.android.lmu.mt.tokt.authenticator.shared.AppConstants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener,
        CapabilityApi.CapabilityListener,
        MessageApi.MessageListener {

    //adb forward tcp:4444 localabstract:/adb-hub
    //adb connect 127.0.0.1:4444

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_RESOLVE_ERROR = 1000;

    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError = false;

    private ExecutorService mExecutorService;
    private SparseArray<Sensor> mSensorSparseArray;
    private ArrayList<Sensor> mSensors;
    private SensorNames mSensorNames;

    private LinkedList<TagData> mTags = new LinkedList<>();

    //Views
    private TextView mWatchConnectionStatusText;
    private TextView mHeartrateText;
    private TextView mStepsText;
    private TextView mServerConnectionStatusText;
    private TextView mServerStateText;
    private TextView mServerCommandText;
    private Button mConnectToWatchBtn;
    private Button mConnectToServerBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initListeners();

        mExecutorService = Executors.newCachedThreadPool();

        mSensorSparseArray = new SparseArray<Sensor>();
        mSensors = new ArrayList<Sensor>();
        mSensorNames = new SensorNames();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }


    private void initViews() {

        mConnectToWatchBtn = (Button) findViewById(R.id.connect_phone_to_watch_btn);
        mConnectToServerBtn = (Button) findViewById(R.id.connect_phone_to_server_btn);
        mWatchConnectionStatusText = (TextView) findViewById(R.id.watch_connection_status_text);
        mHeartrateText = (TextView) findViewById(R.id.heart_rate_text);
        mStepsText = (TextView) findViewById(R.id.steps_text);
        mServerConnectionStatusText = (TextView) findViewById(R.id.server_connection_status_text);
        mServerStateText = (TextView) findViewById(R.id.server_state_text);
        mServerCommandText = (TextView) findViewById(R.id.server_command_text);

    }

    private void initListeners() {
        mConnectToWatchBtn.setOnClickListener(this);
    }


    /* --------------------- Sensor handling --------------------- */
    public List<Sensor> getSensors() {
        return (List<Sensor>) mSensors.clone();
    }

    public Sensor getSensor(long sensorId) {
        return mSensorSparseArray.get((int) sensorId);
    }

    private Sensor createSensor(int sensorId) {
        Sensor sensor = new Sensor(sensorId, mSensorNames.getName(sensorId));

        mSensors.add(sensor);
        mSensorSparseArray.append(sensorId, sensor);

        //TODO: new Sensor detected --> refector for authenticator purposes
        BusProvider.postOnMainThread(new NewSensorEvent(sensor));

        return sensor;
    }

    private Sensor getOrCreateSensor(int sensorId) {
        Sensor sensor = mSensorSparseArray.get(sensorId);

        if (sensor == null) {
            sensor = createSensor(sensorId);
        }
        return sensor;
    }


    public synchronized void addSensorData(int sensorType, int accuracy, long timestamp, float[] values) {
        Sensor sensor = getOrCreateSensor(sensorType);

        SensorDataPoint dataPoint = new SensorDataPoint(timestamp, accuracy, values);
        sensor.addDataPoint(dataPoint);

        //TODO: new SensorUpdateEvent detected --> refector for authenticator purposes
        BusProvider.postOnMainThread(new SensorUpdatedEvent(sensor, dataPoint));
    }

    //TODO: e.g. unlocking state is removed/ connection lost/  etc.
    public synchronized void addTag(String tagName) {

        TagData tag = new TagData(tagName, System.currentTimeMillis());
        mTags.add(tag);

        BusProvider.postOnMainThread(new TagAddedEvent(tag));
    }

    public LinkedList<TagData> getTags() {
        return (LinkedList<TagData>) mTags.clone();
    }


     /* --------------------- Connection handling --------------------- */

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Google API Client was connected");
        mResolvingError = false;
        mConnectToWatchBtn.setText("Disconnect");
        mWatchConnectionStatusText.setText("CONNECTED");

        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Wearable.CapabilityApi.addListener(
                mGoogleApiClient, this, Uri.parse("wear://"), CapabilityApi.FILTER_REACHABLE);

        startMeasurement();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection to Google API client was suspended");
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        if (!mResolvingError) {
            if (connectionResult.hasResolution()) {
                try {
                    mResolvingError = true;
                    connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
                } catch (IntentSender.SendIntentException e) {
                    mGoogleApiClient.connect();
                }
            }
        } else {
            Log.e(TAG, "Connection to Google API client has failed");
            mResolvingError = false;
            mConnectToWatchBtn.setText("Connect");
            mWatchConnectionStatusText.setText("DISCONNECTED");
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            Wearable.CapabilityApi.removeListener(mGoogleApiClient, this);
            stopMeasurement();
            BusProvider.getInstance().unregister(this);
        }

    }

     /* --------------------- Message handling --------------------- */

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived() A message from watch was received:"
                + messageEvent.getRequestId() + " " + messageEvent.getPath());


    }

    /* --------------------- Capability handling --------------------- */

    @Override
    public void onCapabilityChanged(final CapabilityInfo capabilityInfo) {
        Log.d(TAG, "onCapabilityChanged: " + capabilityInfo);

    }

    /* ------------- Receive data from connected Watch (DataApi.DataListener) -------------*/
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged: " + dataEvents);

        //TODO: Daten von Uhr verarbeiten und anzeigen

        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataItem dataItem = dataEvent.getDataItem();
                Uri uri = dataItem.getUri();
                String path = uri.getPath();

                if (path.startsWith("/sensors/")) {
                    unpackSensorData(
                            Integer.parseInt(uri.getLastPathSegment()),
                            DataMapItem.fromDataItem(dataItem).getDataMap()
                    );
                }

                if (path.startsWith("/close/")) {

                    if (Integer.parseInt(uri.getLastPathSegment()) == 1) {
                        closeConnectionToWatch();
                    }

                }
            }
        }
    }

    private void unpackSensorData(int sensorType, DataMap dataMap) {
        int accuracy = dataMap.getInt(AppConstants.DATA_MAP_KEY_ACCURACY);
        long timestamp = dataMap.getLong(AppConstants.DATA_MAP_KEY_TIMESTAMP);
        float[] values = dataMap.getFloatArray(AppConstants.DATA_MAP_KEY_VALUES);

        Log.d(TAG, "Received sensor data " + sensorType + " = " + Arrays.toString(values));

        this.addSensorData(sensorType, accuracy, timestamp, values);

        switch (sensorType) {
            case AppConstants.SENSOR_TYPE_HEART_RATE:
                BusProvider.updateTextViewOnMainThread(mHeartrateText, Arrays.toString(values));
                break;
            case AppConstants.SENSOR_TYPE_STEP_COUNTER:
                BusProvider.updateTextViewOnMainThread(mStepsText, Arrays.toString(values));
                break;
        }
    }

    /* ------------- Sending commands to watch -------------*/

    public void startMeasurement() {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                controlMeasurementInBackground(AppConstants.CLIENT_PATH_START_MEASUREMENT);
            }
        });
    }

    public void stopMeasurement() {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                controlMeasurementInBackground(AppConstants.CLIENT_PATH_STOP_MEASUREMENT);
            }
        });
    }

    public void filterBySensorId(final int sensorId) {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                filterBySensorIdInBackground(sensorId);
            }
        });
    }


    //TODO only allow ONE node!!!
    private void controlMeasurementInBackground(final String path) {

        List<Node> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await().getNodes();

        Log.d(TAG, "Sending to nodes: " + nodes.size());

        for (Node node : nodes) {
            Log.i(TAG, "add node " + node.getDisplayName());
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, node.getId(), path, null
            ).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                @Override
                public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                    Log.d(TAG, "controlMeasurementInBackground(" + path + "): " + sendMessageResult.getStatus().isSuccess());
                }
            });
        }
    }

    private void filterBySensorIdInBackground(final int sensorId) {
        Log.d(TAG, "filterBySensorId(" + sensorId + ")");

        if (mGoogleApiClient.isConnected()) {
            PutDataMapRequest dataMap = PutDataMapRequest.create("/filter");

            dataMap.getDataMap().putInt(AppConstants.DATA_MAP_KEY_FILTER, sensorId);
            dataMap.getDataMap().putLong(AppConstants.DATA_MAP_KEY_TIMESTAMP, System.currentTimeMillis());

            PutDataRequest putDataRequest = dataMap.asPutDataRequest();
            Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                @Override
                public void onResult(DataApi.DataItemResult dataItemResult) {
                    Log.d(TAG, "Filter by sensor " + sensorId + ": " + dataItemResult.getStatus().isSuccess());
                }
            });
        }
    }

    public void getNodes(ResultCallback<NodeApi.GetConnectedNodesResult> pCallback) {
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(pCallback);
    }

     /* --------------------- OnClick handling --------------------- */

    @Override
    public void onClick(View view) {
        int id = view.getId();


        switch (id) {
            case R.id.connect_phone_to_watch_btn:
                if (!mGoogleApiClient.isConnected()) {
                    connectToWatch();
                } else {
                    disconnectFromWatch();
                    mConnectToWatchBtn.setText("Connect");
                    mWatchConnectionStatusText.setText("DISCONNECTED");
                }
                break;
            case R.id.connect_phone_to_server_btn:
                break;
            default:
                Log.d(TAG, "Unkown view clicked");
                break;
        }
    }

    private void connectToWatch() {
        if (!mResolvingError) {
            mGoogleApiClient.connect();
        }
    }

    private void disconnectFromWatch() {

        //TODO never called in watch
        stopMeasurement();
        BusProvider.getInstance().unregister(this);

        if (!mResolvingError && (mGoogleApiClient != null) && (mGoogleApiClient.isConnected())) {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            Wearable.CapabilityApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

    }

    private void closeConnectionToWatch() {

    }
}
