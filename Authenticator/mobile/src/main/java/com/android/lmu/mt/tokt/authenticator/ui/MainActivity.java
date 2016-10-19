package com.android.lmu.mt.tokt.authenticator.ui;

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
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.lmu.mt.tokt.authenticator.R;
import com.android.lmu.mt.tokt.authenticator.data.Sensor;
import com.android.lmu.mt.tokt.authenticator.data.SensorDataPoint;
import com.android.lmu.mt.tokt.authenticator.data.SensorNames;
import com.android.lmu.mt.tokt.authenticator.data.TagData;
import com.android.lmu.mt.tokt.authenticator.database.DataEntry;
import com.android.lmu.mt.tokt.authenticator.database.TagEntry;
import com.android.lmu.mt.tokt.authenticator.events.BusProvider;
import com.android.lmu.mt.tokt.authenticator.events.NewSensorEvent;
import com.android.lmu.mt.tokt.authenticator.events.SensorUpdatedEvent;
import com.android.lmu.mt.tokt.authenticator.events.TagAddedEvent;
import com.android.lmu.mt.tokt.authenticator.network.AuthenticatorAsyncTask;
import com.android.lmu.mt.tokt.authenticator.shared.AppConstants;
import com.android.lmu.mt.tokt.authenticator.util.Util;
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
import com.squareup.otto.Subscribe;

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

import io.realm.Realm;
import io.realm.exceptions.RealmMigrationNeededException;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        AdapterView.OnItemSelectedListener,
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

    private static LinkedList<TagData> mTags = new LinkedList<>();

    private SharedPreferences mSharedPreferences;

    private AuthenticatorAsyncTask mAuthenticatorAsyncTask;

    private Handler mHandler;

    private Realm mRealm;
    private String mAndroidId;


    //Views
    private TextView mWatchConnectionStatusText;
    private TextView mHeartrateText;
    private TextView mStepsText;
    private TextView mProximityText;
    private EditText mServerIpEditText;
    private EditText mServerPortEditText;
    private Button mServerIpBtn;
    private Button mServerPortBtn;
    private TextView mServerConnectionStatusText;
    private TextView mServerStateText;
    private Button mConnectToWatchBtn;
    private Button mConnectToServerBtn;
    private TextView mLockstateText;
    private TextView mUsernameText;
    private ImageView mBeaconEditImg;
    private EditText mBeaconEditText;

    private Spinner mBeaconSpinner;

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
    protected void onResume() {
        super.onResume();


        try {
            mRealm = Realm.getInstance(this);
        } catch (RealmMigrationNeededException e) {
            try {
                Realm.deleteRealmFile(this);
                mRealm = Realm.getInstance(this);
            } catch (Exception ex) {
                throw ex;
                //No Realm file to remove.
            }
        }


        mAndroidId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @Subscribe
    public void onSensorUpdatedEvent(SensorUpdatedEvent event) {
        Log.d(TAG, "Subscribed onSensorUpdatedEvent");
        mRealm.beginTransaction();
        DataEntry entry = mRealm.createObject(DataEntry.class);
        entry.setAndroidDevice(mAndroidId);
        entry.setUsername(Util.getUsername());
        entry.setTimestamp(event.getDataPoint().getTimestamp());
        if (event.getDataPoint().getValues().length > 0) {
            entry.setX(event.getDataPoint().getValues()[0]);
        } else {
            entry.setX(0.0f);
        }

        if (event.getDataPoint().getValues().length > 1) {
            entry.setY(event.getDataPoint().getValues()[1]);
        } else {
            entry.setY(0.0f);
        }

        if (event.getDataPoint().getValues().length > 2) {
            entry.setZ(event.getDataPoint().getValues()[2]);
        } else {
            entry.setZ(0.0f);
        }

        entry.setAccuracy(event.getDataPoint().getAccuracy());
        entry.setDataSource("Acc");
        entry.setDataType(event.getSensor().getSensorId());
        entry.setSensorName(mSensorNames.getName((int) event.getSensor().getSensorId()));
        mRealm.commitTransaction();
    }

    @Subscribe
    public void onTagAddedEvent(TagAddedEvent event) {
        Log.d(TAG, "Subscribed onTagAddedEvent");
        mRealm.beginTransaction();
        TagEntry entry = mRealm.createObject(TagEntry.class);
        entry.setEvent(event.getTag().getTagName());
        entry.setUsername(Util.getUsername());
        entry.setTimestamp(event.getTag().getTimestamp());
        mRealm.commitTransaction();
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

    private int getSavedSelectedBeacon() {
        return mSharedPreferences.getInt(AppConstants.SHARED_PREF_BEACON_SELECTED_POS,
                AppConstants.DEFAULT_SELECTED_BEACON_POS);
    }

    private String getSavedBeaconBLName() {
        return mSharedPreferences.getString(AppConstants.SHARED_PREF_BEACON_BL_NAME,
                AppConstants.BEACON_1_BL_NAME);
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
        mServerIpBtn = (Button) findViewById(R.id.server_ip_btn);
        mServerPortEditText = (EditText) findViewById(R.id.server_port_edit_text);
        mServerPortBtn = (Button) findViewById(R.id.server_port_btn);
        mServerConnectionStatusText = (TextView) findViewById(R.id.server_connection_status_text);
        mServerStateText = (TextView) findViewById(R.id.server_state_text);
        mLockstateText = (TextView) findViewById(R.id.lock_state_txt);
        mUsernameText = (TextView) findViewById(R.id.username_txt);
        mBeaconEditImg = (ImageView) findViewById(R.id.edit_beacon_uuid_img);
        mBeaconEditText = (EditText) findViewById(R.id.beacon_uuid_edit);
        mBeaconSpinner = (Spinner) findViewById(R.id.beacon_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.beacons_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBeaconSpinner.setAdapter(adapter);
        mBeaconSpinner.setSelection(getSavedSelectedBeacon());


        mServerIpEditText.setText(getSavedServerIP());
        mServerPortEditText.setText("" + getSavedServerPort());

    }

    private void initListeners() {
        Log.d(TAG, "init Listeners...");

        mConnectToWatchBtn.setOnClickListener(this);
        mConnectToServerBtn.setOnClickListener(this);
        mServerIpBtn.setOnClickListener(this);
        mServerPortBtn.setOnClickListener(this);
        mBeaconEditImg.setOnClickListener(this);

        mBeaconSpinner.setOnItemSelectedListener(this);
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

                            StringBuilder sb = new StringBuilder();
                            sb.append(getResources().getString(R.string.allow_connection));
                            sb.append(" (");
                            sb.append(cheksum);
                            sb.append(")");

                            showConfirmConnectionDialog(sb.toString(), cheksum);
                        }
                        break;
                    case AppConstants.STATE_CONNECTED:
                        addTag("connected");
                        //starte service
                        if (msg.obj != null) {
                            Log.d(TAG, "connect... " + msg.obj.toString());
                            Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        }

                        break;
                    case AppConstants.STATE_DISCONNECTED:
                        addTag("disconnected");
                        //beende service
                        if (msg.obj != null) {
                            Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        }
                        if (mAuthenticatorAsyncTask.getTCPClient().isRunning()) {
                            mAuthenticatorAsyncTask.getTCPClient().stopClient();
                        }
                        mAuthenticatorAsyncTask.cancel(true);
                        disconnectFromWatch();
                        break;
                    case AppConstants.STATE_AUTHENTICATED:
                        addTag("authenticated");

                        if (msg.obj != null) {
                            Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        }

                        if (mGoogleApiClient.isConnected()) {
                            startMeasurement(getSavedBeaconBLName());
                            setUserIsAuthenticatedState(true);
                        } else {
                            //Connect to watch and start sensor service
                            connectToWatch();
                            startMeasurement(getSavedBeaconBLName());
                        }
                        break;
                    case AppConstants.STATE_NOT_AUTHENTICATED:
                        addTag("not_authenticated");
                        setUserIsAuthenticatedState(false);
                        stopMeasurement();

                        break;
                    case AppConstants.ERROR:
                        addTag("error");
                        mConnectToServerBtn.setText(getResources().getString(R.string.connect));
                        mAuthenticatorAsyncTask = null;
                        Toast.makeText(MainActivity.this, "ERROR! Network problems!", Toast.LENGTH_SHORT).show();
                        break;
                    case AppConstants.START_LISTEN_TO_SOUND:
                        addTag("listen_to_sound");
                        if (msg.obj != null) {
                            Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        }
                        //startListeningToSound();
                        break;
                    case AppConstants.STATE_LOCKED:
                        addTag("state_locked");
                        sendLockStateToWatch(true);
                        BusProvider.updateTextViewOnMainThread(mLockstateText,
                                getResources().getString(R.string.locked));
                        //update MainUI
                        break;
                    case AppConstants.STATE_UNLOCKED:
                        addTag("state_unlocked");
                        sendLockStateToWatch(false);
                        BusProvider.updateTextViewOnMainThread(mLockstateText,
                                getResources().getString(R.string.unlocked));
                        break;
                    case AppConstants.SET_USERNAME:
                        String username = "-";
                        if (msg.obj != null) {
                            username = msg.obj.toString();
                        }
                        BusProvider.updateTextViewOnMainThread(mUsernameText, username);
                        break;
                    case AppConstants.STATE_SEND_TYPING_VALUES:
                        startKeyDetectorServiceWatch(true);
                        break;
                    case AppConstants.STATE_STOP_SENDING_TYPING_VALUES:
                        startKeyDetectorServiceWatch(false);
                        break;

                }
            }
        };


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        // return true so that the menu pop up is opened
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_export:
                FragmentManager fm = getSupportFragmentManager();
                ExportFragmentDialog exportFragment = new ExportFragmentDialog();
                exportFragment.show(fm, "export");
                //startActivity(new Intent(MainActivity.this, ExportActivity.class));
                return true;
            default:
                return true;
        }
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
        String username = Util.getUsername();
        //TODO: new SensorUpdateEvent detected --> refector for authenticator purposes
        BusProvider.postOnMainThread(new SensorUpdatedEvent(sensor, dataPoint, username));
    }


    //TODO: e.g. unlocking state is removed/ connection lost/  etc.
    public synchronized void addTag(String tagName) {

        TagData tag = new TagData(tagName, System.currentTimeMillis());
        mTags.add(tag);

        BusProvider.postOnMainThread(new TagAddedEvent(tag));
    }

    public static LinkedList<TagData> getTags() {
        return (LinkedList<TagData>) mTags.clone();
    }


     /* --------------------- Connection handling --------------------- */

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Google API Client was connected");
        mResolvingError = false;
        mConnectToWatchBtn.setText(getResources().getString(R.string.disconnect));
        mWatchConnectionStatusText.setText(getResources().getString(R.string.connected));

        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Wearable.CapabilityApi.addListener(
                mGoogleApiClient, this, Uri.parse("wear://"), CapabilityApi.FILTER_REACHABLE);

        //start sensorservice on watch and provide callback fo MainUIThread
        //startMeasurement();
        startAuthenticatorActivityOnWatch();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection to Google API client was suspended");
        addTag("connection_suspended");
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
            mConnectToWatchBtn.setText(getResources().getString(R.string.connect));
            mWatchConnectionStatusText.setText(getResources().getString(R.string.disconnected));
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

        if (messageEvent.getPath().equals(AppConstants.SERVER_PATH_TYPING_SUCCESS)) {
            //TODO an Server senden
            addTag("typing_success");
            mAuthenticatorAsyncTask.getTCPClient().sendMessage(AppConstants.COMMAND_USER_TYPING_SUCCESS);
        }

        if (messageEvent.getPath().equals(AppConstants.SERVER_PATH_TYPING_FAILED)) {
            //TODO an Server senden
            addTag("typing_failed");
            mAuthenticatorAsyncTask.getTCPClient().sendMessage(AppConstants.COMMAND_USER_TYPING_FAILED);
        }

        if (messageEvent.getPath().equals(AppConstants.SERVER_PATH_TYPING_SENSOR_STARTED)) {
            //TODO an Server senden
            addTag("typing_started");
            mAuthenticatorAsyncTask.getTCPClient().sendMessage(AppConstants.COMMAND_TYPING_SENSORS_STARTED);
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
                sendSensorDataToServer(dataString);
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
        Log.d(TAG, "onDataChanged");

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
            }
        }
    }


    private void unpackSensorData(int sensorType, DataMap dataMap) {
        int accuracy = dataMap.getInt(AppConstants.DATA_MAP_KEY_ACCURACY);
        long timestamp = dataMap.getLong(AppConstants.DATA_MAP_KEY_TIMESTAMP);
        float[] values = dataMap.getFloatArray(AppConstants.DATA_MAP_KEY_VALUES);

        Log.d(TAG, "Received sensor data " + sensorType + " = " + Arrays.toString(values));

        this.addSensorData(sensorType, accuracy, timestamp, values);

        //forward data to server
        sendSensorDataToServer(sensorType, accuracy, timestamp, values);

        //update MainUi watch
        switch (sensorType) {
            case AppConstants.SENSOR_TYPE_HEART_RATE:
                BusProvider.updateTextViewOnMainThread(mHeartrateText, Arrays.toString(values));
                break;
            case AppConstants.SENSOR_TYPE_STEP_COUNTER:
                BusProvider.updateTextViewOnMainThread(mStepsText, Arrays.toString(values));
                break;
            case AppConstants.SENSOR_TYPE_BEACON:
                BusProvider.updateTextViewOnMainThread(mProximityText, Util.getProximityStringByRSSI((int) values[0]));
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


    public void startAuthenticatorActivityOnWatch() {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                sendRemoteCommandToWatch(AppConstants.CLIENT_PATH_START_ACTIVITY);
            }
        });
    }

    public void startMeasurement(final String beaconBLName) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                byte[] data = beaconBLName.getBytes(Charset.forName("UTF-8"));
                sendRemoteCommandToWatch(AppConstants.CLIENT_PATH_START_MEASUREMENT, data);
            }
        });
    }

    public void stopMeasurement() {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                sendRemoteCommandToWatch(AppConstants.CLIENT_PATH_STOP_MEASUREMENT);
            }
        });
    }

    public void setUserIsAuthenticatedState(final boolean isAuthenticated) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (isAuthenticated) {
                    sendRemoteCommandToWatch(AppConstants.CLIENT_PATH_USER_AUTHENTICATED);
                } else {
                    sendRemoteCommandToWatch(AppConstants.CLIENT_PATH_USER_NOT_AUTHENTICATED);
                }

            }
        });
    }


    public void sendLockStateToWatch(final boolean isLocked) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (isLocked) {
                    sendRemoteCommandToWatch(AppConstants.CLIENT_PATH_LOCKED);
                } else {
                    sendRemoteCommandToWatch(AppConstants.CLIENT_PATH_UNLOCKED);
                }
            }
        });
    }

    public void filterBySensorId(final int sensorId) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                filterBySensorIdInBackground(sensorId);
            }
        });
    }

    public void sendBeaconUUIDToWatch(final String beaconUUID) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                byte[] data = beaconUUID.getBytes(Charset.forName("UTF-8"));
                sendRemoteCommandToWatch(AppConstants.CLIENT_PATH_BEACON_UUID, data);
            }
        });
    }

    public void sendBeaconBLNameToWatch(final String beaconBLName) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                byte[] data = beaconBLName.getBytes(Charset.forName("UTF-8"));
                sendRemoteCommandToWatch(AppConstants.CLIENT_PATH_BEACON_BL_NAME, data);
            }
        });
    }

    public void startKeyDetectorServiceWatch(final boolean start) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (start) {
                    sendRemoteCommandToWatch(AppConstants.CLIENT_PATH_START_KEYPRESS_DETECTOR);
                } else {
                    sendRemoteCommandToWatch(AppConstants.CLIENT_PATH_STOP_KEYPRESS_DETECTOR);
                }
            }
        });
    }

    //TODO only allow ONE node!!!
    private void sendRemoteCommandToWatch(final String path) {

        List<Node> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await().getNodes();

        Log.d(TAG, "Sending to nodes: " + nodes.size());

        for (Node node : nodes) {
            Log.i(TAG, "add node " + node.getDisplayName());
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, node.getId(), path, null
            ).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                @Override
                public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                    Log.d(TAG, "sendRemoteCommandToWatch(" + path + "): " + sendMessageResult.getStatus().isSuccess());
                }
            });
        }
    }

    //TODO only allow ONE node!!!
    private void sendRemoteCommandToWatch(final String path, byte[] data) {

        List<Node> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await().getNodes();

        Log.d(TAG, "Sending to nodes: " + nodes.size());

        for (Node node : nodes) {
            Log.i(TAG, "add node " + node.getDisplayName());
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, node.getId(), path, data
            ).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                @Override
                public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                    Log.d(TAG, "sendRemoteCommandToWatch(" + path + "): " + sendMessageResult.getStatus().isSuccess());
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
                    mConnectToWatchBtn.setText(getResources().getString(R.string.connect));
                    mWatchConnectionStatusText.setText(getResources().getString(R.string.disconnected));
                }
                break;
            case R.id.connect_phone_to_server_btn:
                if (mAuthenticatorAsyncTask == null ||
                        !mAuthenticatorAsyncTask.getTCPClient().isRunning()) {
                    mAuthenticatorAsyncTask = new AuthenticatorAsyncTask(MainActivity.this, mHandler);
                    mAuthenticatorAsyncTask.execute();
                    mConnectToServerBtn.setText(getResources().getString(R.string.disconnect));
                } else {
                    if (mAuthenticatorAsyncTask != null) {
                        mAuthenticatorAsyncTask.getTCPClient().stopClient();
                        mAuthenticatorAsyncTask.cancel(true);
                    }
                    stopMeasurement();
                    mConnectToServerBtn.setText(getResources().getString(R.string.connect));
                }
                break;
            case R.id.server_ip_btn:
                editOrConfirmServerIp();
                break;
            case R.id.server_port_btn:
                editOrConfirmServerPort();
                break;
            case R.id.edit_beacon_uuid_img:
                editOrConfirmBeaconUUID();
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

    @Override
    public void finish() {
        super.finish();
        Log.d(TAG, "App finished");
        disconnectFromWatch();
    }

    private void disconnectFromWatch() {

        stopMeasurement();
        resetMainUIValues();

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
            mServerIpBtn.setText(R.string.save);
        } else {
            mServerIpEditText.setEnabled(false);
            mServerIpBtn.setText(R.string.edit);

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(AppConstants.SHARED_PREF_SEVER_IP,
                    mServerIpEditText.getText().toString());
            editor.commit();
        }
    }

    private void editOrConfirmServerPort() {

        if (!mServerPortEditText.isEnabled()) {
            mServerPortEditText.setEnabled(true);
            mServerPortBtn.setText(R.string.save);
        } else {
            mServerPortEditText.setEnabled(false);
            mServerPortBtn.setText(R.string.edit);

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putInt(AppConstants.SHARED_PREF_SEVER_PORT,
                    Integer.parseInt(mServerPortEditText.getText().toString()));
            editor.commit();
        }
    }

    private void editOrConfirmBeaconUUID() {
        if (!mBeaconEditText.isEnabled()) {
            mBeaconEditText.setEnabled(true);
            mBeaconEditImg.setImageResource(android.R.drawable.ic_menu_save);
        } else {
            mBeaconEditText.setEnabled(false);
            mBeaconEditImg.setImageResource(android.R.drawable.ic_menu_edit);

            String beaconUUID = mBeaconEditText.getText().toString();
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(AppConstants.SHARED_PREF_BEACON_UUID, beaconUUID);
            editor.commit();

            sendBeaconUUIDToWatch(beaconUUID);
            mProximityText.setText(getResources().getString(R.string.placeholder));

        }
    }

    private void resetMainUIValues() {
        mHeartrateText.setText(getResources().getString(R.string.zero_value_double));
        mStepsText.setText(getResources().getString(R.string.zero_value_double));
        mProximityText.setText(getResources().getString(R.string.placeholder));
        mLockstateText.setText(getResources().getString(R.string.placeholder));
    }


     /* --------------------- ONItemSelectedListener --------------------- */

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {

        String blName = "";
        switch (pos) {
            case 0:
                blName = AppConstants.BEACON_1_BL_NAME;
                break;
            case 1:
                blName = AppConstants.BEACON_2_BL_NAME;
                break;
            case 2:
                blName = AppConstants.BEACON_3_BL_NAME;
                break;
            case 3:
                blName = AppConstants.BEACON_4_BL_NAME;
                break;
            case 4:
                blName = AppConstants.BEACON_5_BL_NAME;
                break;
            default:
                blName = AppConstants.BEACON_1_BL_NAME;
                break;
        }

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(AppConstants.SHARED_PREF_BEACON_BL_NAME, blName);
        editor.putInt(AppConstants.SHARED_PREF_BEACON_SELECTED_POS, pos);
        editor.commit();

        if (mGoogleApiClient.isConnected()) {
            sendBeaconBLNameToWatch(blName);
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }


    /* --------------------- Confirmation Dialog --------------------- */
    public void showConfirmConnectionDialog(String message, final int number) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        mAuthenticatorAsyncTask.getTCPClient().sendMessage(AppConstants.COMMAND_PHONE_WATCH_CONNECTION_CONFIRM + ":" + number);
                        connectToWatch();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        mAuthenticatorAsyncTask.getTCPClient().sendMessage(AppConstants.COMMAND_PHONE_WATCH_CONNECTION_DENY);
                        break;
                }
            }
        };


        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(message).setPositiveButton(getResources().getString(R.string.confirm), dialogClickListener)
                .setNegativeButton(getResources().getString(R.string.cancel), dialogClickListener).show();
    }


}
