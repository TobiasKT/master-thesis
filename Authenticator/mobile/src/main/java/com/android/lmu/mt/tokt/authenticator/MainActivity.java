package com.android.lmu.mt.tokt.authenticator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.lmu.mt.tokt.authenticator.data.Sensor;
import com.android.lmu.mt.tokt.authenticator.data.SensorDataPoint;
import com.android.lmu.mt.tokt.authenticator.data.SensorNames;
import com.android.lmu.mt.tokt.authenticator.data.TagData;
import com.android.lmu.mt.tokt.authenticator.events.BusProvider;
import com.android.lmu.mt.tokt.authenticator.events.NewSensorEvent;
import com.android.lmu.mt.tokt.authenticator.events.SensorUpdatedEvent;
import com.android.lmu.mt.tokt.authenticator.events.TagAddedEvent;
import com.android.lmu.mt.tokt.authenticator.network.AuthenticatorAsyncTask;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
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

    private SharedPreferences mSharedPreferences;

    private AuthenticatorAsyncTask mAuthenticatorAsyncTask;

    private Handler mHandler;

    //Views
    private TextView mWatchConnectionStatusText;
    private TextView mHeartrateText;
    private TextView mStepsText;
    private TextView mProximityText;
    private EditText mServerIpEditText;
    private EditText mServerPortEditText;
    private ImageView mServerIpImage;
    private ImageView mServerPortImage;
    private TextView mServerConnectionStatusText;
    private TextView mServerStateText;
    private TextView mServerCommandText;
    private Button mConnectToWatchBtn;
    private Button mConnectToServerBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSharedPreferences = getSharedPreferences(
                AppConstants.SHARED_PREF_APP_KEY, Context.MODE_PRIVATE);

        mExecutorService = Executors.newCachedThreadPool();

        mSensorSparseArray = new SparseArray<Sensor>();
        mSensors = new ArrayList<Sensor>();
        mSensorNames = new SensorNames();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        BusProvider.getInstance().register(this);

        initViews();
        initListeners();
        initHandler();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);
    }

    private String getSavedServerIP() {
        return mSharedPreferences.getString(AppConstants.SHARED_PREF_SEVER_IP,
                AppConstants.DEFAULT_SERVER_IP);
    }

    private int getSavedServerPort() {
        return mSharedPreferences.getInt(AppConstants.SHARED_PREF_SEVER_PORT,
                AppConstants.DEFAULT_SERVER_PORT);
    }

    private void initViews() {
        Log.d(TAG, "init Views...");
        mConnectToWatchBtn = (Button) findViewById(R.id.connect_phone_to_watch_btn);
        mConnectToServerBtn = (Button) findViewById(R.id.connect_phone_to_server_btn);
        mWatchConnectionStatusText = (TextView) findViewById(R.id.watch_connection_status_text);
        mHeartrateText = (TextView) findViewById(R.id.heart_rate_text);
        mStepsText = (TextView) findViewById(R.id.steps_text);
        mProximityText = (TextView) findViewById(R.id.proximity_text);
        mServerIpEditText = (EditText) findViewById(R.id.server_ip_edit_text);
        mServerIpImage = (ImageView) findViewById(R.id.server_ip_img);
        mServerPortEditText = (EditText) findViewById(R.id.server_port_edit_text);
        mServerPortImage = (ImageView) findViewById(R.id.server_port_img);
        mServerConnectionStatusText = (TextView) findViewById(R.id.server_connection_status_text);
        mServerStateText = (TextView) findViewById(R.id.server_state_text);
        mServerCommandText = (TextView) findViewById(R.id.server_command_text);


        mServerIpEditText.setText(getSavedServerIP());
        mServerPortEditText.setText("" + getSavedServerPort());

    }

    private void initListeners() {
        Log.d(TAG, "init Listeners...");

        mConnectToWatchBtn.setOnClickListener(this);
        mConnectToServerBtn.setOnClickListener(this);
        mServerIpImage.setOnClickListener(this);
        mServerPortImage.setOnClickListener(this);
    }


    private void initHandler() {
        Log.d(TAG, "init Handler...");

        mHandler = new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                switch (msg.what) {
                    case AppConstants.STATE_CONFIRM:

                        if (msg.obj != null) {
                            int cheksum = (int) msg.obj;
                            showConfirmConnectionDialog("Allow Connection? (" + cheksum + ")", cheksum);
                        }
                        break;
                    case AppConstants.STATE_CONNECTED:
                        //starte service
                        if (msg.obj != null) {
                            Log.d(TAG, "connect... " + msg.obj.toString());
                            Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        }

                        break;
                    case AppConstants.STATE_DISCONNECTED:
                        //beende service
                        if (msg.obj != null) {
                            Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        }
                        if (mAuthenticatorAsyncTask.getTCPClient().isRunning()) {
                            mAuthenticatorAsyncTask.getTCPClient().stopClient();
                        }
                        mAuthenticatorAsyncTask.cancel(true);
                        break;
                    case AppConstants.STATE_AUTHENTICATED:

                        if (msg.obj != null) {
                            Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        }

                        if(mGoogleApiClient.isConnected()){
                            startMeasurement();
                        }else{
                            //Connect to watch and start sensor service
                            connectToWatch();
                        }
                        break;
                    case AppConstants.STATE_NOT_AUTHENTICATED:
                        stopMeasurement();
                        // stopService(new Intent(MainActivity.this, SensorService.class));
                        break;
                    case AppConstants.ERROR:
                        mConnectToServerBtn.setText("CONNECT");
                        mAuthenticatorAsyncTask = null;
                        Toast.makeText(MainActivity.this, "ERROR! Network problems!", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };


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

        //start sensorservice on watch and provide callback fo MainUIThread
        startMeasurement();
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

        if (messageEvent.getPath().startsWith(AppConstants.SERVER_PATH_SENSOR_DATA)) {
            unpackSensorData(messageEvent.getData());
        }

        if (messageEvent.getPath().equals(AppConstants.SERVER_PATH_BEACON_DATA)) {
            unpackBeaconData(messageEvent.getData());
        }

    }

    private void unpackSensorData(byte[] data) {

        String dataString = new String(data, Charset.forName("UTF-8"));
        int sensorType = -1;
        int accuracy = -1;
        long timestamp = -1;
        float[] values = null;

        try {
            JSONObject jsonObject = new JSONObject(dataString);
            sensorType = jsonObject.getInt("type");
            accuracy = jsonObject.getInt("accuracy");
            timestamp = jsonObject.getLong("timestamp");
            JSONArray jsonArray = jsonObject.getJSONArray("values");
            values = new float[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                values[i] = jsonArray.getLong(i);
            }
        } catch (JSONException je) {

        }


        Log.d(TAG, "Received sensor data " + sensorType + " = " + dataString);

        this.addSensorData(sensorType, accuracy, timestamp, values);

        switch (sensorType) {
            case AppConstants.SENSOR_TYPE_HEART_RATE:

                //TODO: auslagern und im hintergrund senden
                //forward dot server
                sendSensorDataToServer(dataString);
                BusProvider.updateTextViewOnMainThread(mHeartrateText, Arrays.toString(values));
                break;
            case AppConstants.SENSOR_TYPE_STEP_COUNTER:
                //TODO: send to server
                BusProvider.updateTextViewOnMainThread(mStepsText, Arrays.toString(values));
                break;
            case -1:
                Log.e(TAG, "Unknown sensorType");
                break;
        }
    }

    private void unpackBeaconData(byte[] data) {
        String dataString = new String(data, Charset.forName("UTF-8"));
        String proximity = "unknown";

        try {
            JSONObject jsonObject = new JSONObject(dataString);
            proximity = jsonObject.getString("proximity");
        } catch (JSONException je) {

        }

        Log.d(TAG, "Received beacon data: " + dataString);

        //TODO: addBeaconData
        sendBeaconDataToServer(dataString);
        BusProvider.updateTextViewOnMainThread(mProximityText, proximity);


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

                if (path.startsWith("/beacon")) {
                    unpackBeaconData(DataMapItem.fromDataItem(dataItem).getDataMap());
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

                //TODO: auslagern und im hintergrund senden
                //forward dot server
                sendSensorDataToServer(sensorType, accuracy, timestamp, values);
                BusProvider.updateTextViewOnMainThread(mHeartrateText, Arrays.toString(values));
                break;
            case AppConstants.SENSOR_TYPE_STEP_COUNTER:
                BusProvider.updateTextViewOnMainThread(mStepsText, Arrays.toString(values));
                break;
        }
    }

    private void unpackBeaconData(DataMap dataMap) {
        String proxminity = dataMap.getString(AppConstants.DATA_MAP_KEY_BEACON_PROXIMITY);

        Log.d(TAG, "Received beacon data: " + proxminity);

        //TODO: addBeaconData
        sendBeaconDataToServer(proxminity);
        BusProvider.updateTextViewOnMainThread(mProximityText, proxminity);


    }

    /* ------------- Sending to server -------------*/

    public void sendSensorDataToServer(final int sensorType, final int accuracy,
                                       final long timestamp, final float[] values) {

        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {

                JSONObject jsonObject = new JSONObject();

                try {
                    jsonObject.put("name", getSensor(sensorType).getName());
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

                //sendMessageToServer(jsonObject);

                if (mAuthenticatorAsyncTask.getTCPClient() != null) {
                    mAuthenticatorAsyncTask.getTCPClient().sendMessage(
                            AppConstants.SENSORDATA_PREFIX + "::" + jsonObject.toString());
                }
            }
        });
    }

    public void sendSensorDataToServer(final String data) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (mAuthenticatorAsyncTask.getTCPClient() != null) {
                    mAuthenticatorAsyncTask.getTCPClient().sendMessage(
                            AppConstants.SENSORDATA_PREFIX + "::" + data);
                }
            }
        });
    }

    public void sendBeaconDataToServer(final String data) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (mAuthenticatorAsyncTask.getTCPClient() != null) {
                    mAuthenticatorAsyncTask.getTCPClient().sendMessage(
                            AppConstants.BEACONDATA_PREFIX + "::" + data);
                }
            }
        });
    }

    public void sendBeaconDataToServer(final String proximity, final long timestamp) {

        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                JSONObject jsonObject = new JSONObject();

                try {
                    jsonObject.put("name", "beacon");
                    jsonObject.put("proximity", proximity);
                    jsonObject.put("timestamp", timestamp);

                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }

                if (mAuthenticatorAsyncTask.getTCPClient() != null) {
                    mAuthenticatorAsyncTask.getTCPClient().sendMessage(
                            AppConstants.BEACONDATA_PREFIX + "::" + jsonObject.toString());
                }
            }
        });
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
                if (mAuthenticatorAsyncTask == null ||
                        !mAuthenticatorAsyncTask.getTCPClient().isRunning()) {
                    mAuthenticatorAsyncTask = new AuthenticatorAsyncTask(MainActivity.this, mHandler);
                    mAuthenticatorAsyncTask.execute();
                    mConnectToServerBtn.setText("DISCONNECT");
                } else {
                    mAuthenticatorAsyncTask.getTCPClient().stopClient();
                    mAuthenticatorAsyncTask.cancel(true);
                    stopMeasurement();
                    mConnectToServerBtn.setText("CONNECT");
                }
                break;
            case R.id.server_ip_img:
                editOrConfirmServerIp();
                break;
            case R.id.server_port_img:
                editOrConfirmServerPort();
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

        stopMeasurement();

        if (!mResolvingError && (mGoogleApiClient != null) && (mGoogleApiClient.isConnected())) {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            Wearable.CapabilityApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

    }

    private void editOrConfirmServerIp() {

        if (!mServerIpEditText.isEnabled()) {
            mServerIpEditText.setEnabled(true);
            mServerIpImage.setImageResource(android.R.drawable.ic_menu_save);
        } else {
            mServerIpEditText.setEnabled(false);
            mServerIpImage.setImageResource(android.R.drawable.ic_menu_edit);

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(AppConstants.SHARED_PREF_SEVER_IP,
                    mServerIpEditText.getText().toString());
            editor.commit();
        }
    }

    private void editOrConfirmServerPort() {

        if (!mServerPortEditText.isEnabled()) {
            mServerPortEditText.setEnabled(true);
            mServerPortImage.setImageResource(android.R.drawable.ic_menu_save);
        } else {
            mServerPortEditText.setEnabled(false);
            mServerPortImage.setImageResource(android.R.drawable.ic_menu_edit);

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putInt(AppConstants.SHARED_PREF_SEVER_PORT,
                    Integer.parseInt(mServerPortEditText.getText().toString()));
            editor.commit();
        }
    }


    /* --------------------- Confirmation Dialog --------------------- */
    public void showConfirmConnectionDialog(String message, final int number) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        mAuthenticatorAsyncTask.getTCPClient().sendMessage(AppConstants.COMMAND_CONFIRM + ":" + number);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        mAuthenticatorAsyncTask.getTCPClient().sendMessage(AppConstants.COMMAND_DISCONNECT);
                        break;
                }
            }
        };


        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(message).setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }


}
