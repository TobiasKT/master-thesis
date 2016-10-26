package com.android.lmu.mt.tokt.authenticator.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.lmu.mt.tokt.authenticator.R;
import com.android.lmu.mt.tokt.authenticator.shared.AppConstants;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by tobiaskeinath on 17.09.16.
 */
public class AuthenticatorWatchService extends Service
        implements SensorEventListener, BeaconConsumer {

    private static final String TAG = AuthenticatorWatchService.class.getSimpleName();

    private LocalBroadcastManager mLocalBroadcastManager;

    private WatchClient mWatchClient;

    private ScheduledExecutorService mScheduler;

    //Sensor
    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;

    //Beacon
    private BeaconManager mBeaconManager;
    private String mBeaconIdentifier;
    private String mBeaconBLName;

    private SharedPreferences mSharedPreferences;

    private boolean mRunning = false;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "create AutenticatorWatch service");
        mSharedPreferences = getSharedPreferences(
                AppConstants.SHARED_PREF_APP_KEY, Context.MODE_PRIVATE);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mWatchClient = WatchClient.getInstance(this);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("Authenticator");
        builder.setContentText("Sending data...");
        builder.setSmallIcon(R.mipmap.ic_authenticator_gray_circle);
        startForeground(1, builder.build());
        stopForeground(true);

        //register sensor listener
        startSensorMeasurement();


        //setup Beacon Manager
        mBeaconManager = BeaconManager.getInstanceForApplication(this);

        // Tell Library how to decode the signal
        mBeaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        // Start the Beacon Manager
        mBeaconManager.bind(this);

        mBeaconIdentifier = getBeaconIDFromSharedPrefs();
        sendResultToMainUI(AppConstants.BEACON_IDENTIFIER_RESULT, AppConstants.BEACON_IDENTIFIER_MESSAGE, mBeaconIdentifier);

        mBeaconBLName = getBeaconBLNameFromSharedPrefs();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "AuthenticatorWatchService stopped");

        mRunning = false;

        //unregister sensor listener
        stopSensorMeasurement();

        mBeaconManager.unbind(this);
        resetMainUIValue();
        stopForeground(true);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mRunning = true;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void resetMainUIValue() {
        sendResult(AppConstants.SENSOR_HEART_RATE_RESULT, AppConstants.SENSOR_HEART_RATE_MESSAGE,
                "0.0");
        sendResult(AppConstants.SENSOR_STEP_COUNT_RESULT, AppConstants.SENSOR_STEP_COUNT_MESSAGE,
                "0.0");
        sendResultToMainUI(AppConstants.BEACON_RESULT, AppConstants.BEACON_MESSAGE, "-");
        sendResult(AppConstants.MESSAGE_RECEIVER_LOCK_RESULT, AppConstants.MESSAGE_RECEIVER_LOCK_MESSAGE, "-");
        sendResultToMainUI(AppConstants.BEACON_IDENTIFIER_RESULT, AppConstants.BEACON_IDENTIFIER_MESSAGE, "");
    }

    private String getBeaconIDFromSharedPrefs() {
        return mSharedPreferences.getString(AppConstants.SHARED_PREF_BEACON_UUID, AppConstants.DEFAULT_BEACON_UUID);
    }

    private String getBeaconBLNameFromSharedPrefs() {
        return mSharedPreferences.getString(AppConstants.SHARED_PREF_BEACON_BL_NAME, AppConstants.BEACON_1_BL_NAME);
    }


    /*-------------------- SENSOR ----------------*/

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {


        int type = sensorEvent.sensor.getType();

        //update Watch UI
        switch (type) {
            case AppConstants.SENSOR_TYPE_HEART_RATE:
                sendResult(AppConstants.SENSOR_HEART_RATE_RESULT, AppConstants.SENSOR_HEART_RATE_MESSAGE,
                        Arrays.toString(sensorEvent.values));
                break;
            case AppConstants.SENSOR_TYPE_STEP_COUNTER:
                sendResult(AppConstants.SENSOR_STEP_COUNT_RESULT, AppConstants.SENSOR_STEP_COUNT_MESSAGE,
                        Arrays.toString(sensorEvent.values));
                break;
        }

        //TODO: check if connected to Desktop-App and authenticated
        mWatchClient.sendSensorData(sensorEvent.sensor.getType(), sensorEvent.accuracy,
                sensorEvent.timestamp, sensorEvent.values);
        Log.d(TAG, "AUTHENTICATED - type: " + sensorEvent.sensor.getType() + ", values: " + Arrays.toString(sensorEvent.values));
    }


    private void startSensorMeasurement() {


        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));

        //Available Sensors on Moto 360 watch
        Sensor accelerometerSensor = mSensorManager.getDefaultSensor(AppConstants.SENSOR_TYPE_ACCELEROMETER);
        Sensor stepCounterSensor = mSensorManager.getDefaultSensor(AppConstants.SENSOR_TYPE_STEP_COUNTER);
        Sensor detailedStepCounterSensor = mSensorManager.getDefaultSensor(AppConstants.SENSOR_TYPE_DETAILED_STEP_COUNTER);
        Sensor wristTiltSensor = mSensorManager.getDefaultSensor(AppConstants.SENSOR_TYPE_WRIST_TILT);
        Sensor gyroSensor = mSensorManager.getDefaultSensor(AppConstants.SENSOR_TYPE_GYRO);
        Sensor lightSensor = mSensorManager.getDefaultSensor(AppConstants.SENSOR_TYPE_LIGHT);
        Sensor gameRotationSensor = mSensorManager.getDefaultSensor(AppConstants.SENSOR_TYPE_GAME_ROTATION);
        Sensor wellnessPassiveSensor = mSensorManager.getDefaultSensor(AppConstants.SENSOR_TYPE_WELLNESS_PASSIVE);
        Sensor ppgSensor = mSensorManager.getDefaultSensor(AppConstants.SENSOR_TYPE_PPG);
        Sensor significantMotionSensor = mSensorManager.getDefaultSensor(AppConstants.SENSOR_TYPE_SIGNIFICANT_MOTION);
        Sensor userProfileSensor = mSensorManager.getDefaultSensor(AppConstants.SENSOR_TYPE_USER_PROFILE);
        Sensor userStrideFactorSensor = mSensorManager.getDefaultSensor(AppConstants.SENSOR_TYPE_USER_STRIDE_FACTOR);
        Sensor linearAccelerationSensor = mSensorManager.getDefaultSensor(AppConstants.SENSOR_TYPE_LINEAR_ACCELERATION);
        Sensor gravitySensor = mSensorManager.getDefaultSensor(AppConstants.SENSOR_TYPE_GRAVITY);

        mHeartRateSensor = mSensorManager.getDefaultSensor(AppConstants.SENSOR_TYPE_HEART_RATE);

        if (mSensorManager != null) {

            //Accelerometer
            if (accelerometerSensor != null) {
                // mSensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "No Accelerometer found");
            }

            //Step Counter
            if (stepCounterSensor != null) {
                mSensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "No StepCounterSensor found");
            }

            //Detailed Step Counter
            if (detailedStepCounterSensor != null) {
                //   mSensorManager.registerListener(this, detailedStepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "No DetailedStepCounterSensor found");
            }

            //Wrist Tilt
            if (wristTiltSensor != null) {
                //  mSensorManager.registerListener(this, wristTiltSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "No WristTiltSensor found");
            }

            //Gyro
            if (gyroSensor != null) {
                //  mSensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "No GyroSensor found");
            }

            //Light
            if (lightSensor != null) {
                //  mSensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "No LightSensor found");
            }

            //Game Rotation
            if (gameRotationSensor != null) {
                //   mSensorManager.registerListener(this, gameRotationSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "No GameRotationSensor found");
            }

            //Wellness Passive
            if (wellnessPassiveSensor != null) {
                //   mSensorManager.registerListener(this, wellnessPassiveSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "No WellnessPassiveSensor found");
            }

            //PPG
            if (ppgSensor != null) {
                //  mSensorManager.registerListener(this, ppgSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "No PPGSensor found");
            }

            //Significant Motion
            if (significantMotionSensor != null) {
                //  mSensorManager.registerListener(this, significantMotionSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "No SignificantMotionSensor found");
            }

            //User Profile
            if (userProfileSensor != null) {
                // mSensorManager.registerListener(this, userProfileSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "No UserProfileSensor found");
            }

            //User Stride Factor
            if (userStrideFactorSensor != null) {
                //  mSensorManager.registerListener(this, userStrideFactorSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "No UserStrideFactorSensor found");
            }

            //Linear Acceleration
            if (linearAccelerationSensor != null) {
                //    mSensorManager.registerListener(this, linearAccelerationSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "No LinearAccelerationSensor found");
            }

            //Gravity
            if (gravitySensor != null) {
                // mSensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "No GravitySensor found");
            }

            if (mHeartRateSensor != null) {

                //moto360 sport
                //final int measurementDuration = 20;   // Seconds
                //final int measurementBreak = 2;    // Seconds

                //moto 360
                final int measurementDuration = 8;   // Seconds
                final int measurementBreak = 1;    // Seconds

                mScheduler = Executors.newScheduledThreadPool(1);
                mScheduler.scheduleAtFixedRate(
                        new Runnable() {
                            @Override
                            public void run() {
                                // Log.d(TAG, "register Heartrate Sensor");
                                mSensorManager.registerListener(AuthenticatorWatchService.this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);

                                try {
                                    Thread.sleep(measurementDuration * 1000);
                                } catch (InterruptedException e) {
                                    Log.e(TAG, "Interrupted while waitting to unregister Heartrate Sensor");
                                }

                                // Log.d(TAG, "unregister Heartrate Sensor");
                                mSensorManager.unregisterListener(AuthenticatorWatchService.this, mHeartRateSensor);
                            }
                        }, 3, measurementDuration + measurementBreak, TimeUnit.SECONDS);

            } else {
                Log.d(TAG, "No Heartrate Sensor found");
            }

        }
    }

    private void stopSensorMeasurement() {

        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
        if (mScheduler != null && !mScheduler.isTerminated()) {
            mScheduler.shutdown();
        }
    }

    private void sendResult(String intentAction, String extraName, String message) {
        Intent intent = new Intent(intentAction);
        if (message != null)
            intent.putExtra(extraName, message);
        mLocalBroadcastManager.sendBroadcast(intent);
    }


    /*-------------------- Beacon ----------------*/

    @Override
    public void onBeaconServiceConnect() {

        Identifier identifier = Identifier.parse(mBeaconIdentifier);
        final Region region = new Region("myBeacons", identifier, null, null);
        mBeaconManager.setMonitorNotifier(new MonitorNotifier() {

            // If the device enters a Beacon region
            @Override
            public void didEnterRegion(Region region) {
                try {
                    Log.d(TAG, "did Enter Region...");
                    mBeaconManager.startRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            // If the device leaves a Beacon region
            @Override
            public void didExitRegion(Region region) {
                try {
                    Log.d(TAG, "did Exit Region...");
                    mBeaconManager.stopRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {

            }
        });


        // If the device finds a Beacon fitting the rules, print it in the console
        mBeaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                for (final Beacon beacon : beacons) {
                    //String data = logGenericBeacon(beacon);
                    if (mBeaconBLName.equals(beacon.getBluetoothName())) {
                        Log.d(TAG, "Proximity: " + getProximityStringByRSSI(beacon.getRssi()));
                        long timestamp = System.currentTimeMillis();
                        String proximity = getProximityStringByRSSI(beacon.getRssi());

                        //TODO: PROXIMITY values als int
                        //  if (mWatchClient.isAuthenticated()) {

                        mWatchClient.sendSensorData(AppConstants.SENSOR_TYPE_BEACON, beacon.getTxPower(), timestamp, new float[]{beacon.getRssi()});


                        //TODO: only send if data changed
                        if (mRunning) {
                            sendResultToMainUI(AppConstants.BEACON_RESULT, AppConstants.BEACON_MESSAGE, proximity);
                        }
                        //   }
                    }
                }


            }
        });

        try {
            mBeaconManager.stopMonitoringBeaconsInRegion(region);
            mBeaconManager.startMonitoringBeaconsInRegion(region);
        } catch (RemoteException e) {
            Log.e(TAG, "Error monitoring beacon: " + e.getMessage());
        }

    }

    private void sendResultToMainUI(String intentFilter, String extraName, String message) {
        Intent intent = new Intent(intentFilter);
        if (message != null)
            intent.putExtra(extraName, message);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    public static String getProximityStringByRSSI(int rssi) {
        String proximityString = "undefined";
        if (rssi >= -85) {
            proximityString = AppConstants.PROXIMITY_NEAR;
        } else if (rssi < -85) {
            proximityString = AppConstants.PROXIMITY_FAR;
        }
        return proximityString;
    }


    private static String getCurrentTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss.SSS", Locale.US);
        Date now = new Date();
        return sdf.format(now);
    }

    private String logGenericBeacon(Beacon beacon) {
        StringBuilder scanString = new StringBuilder();
        scanString.append(" UUID: ").append(beacon.getId1() + "\n");
        scanString.append(" Maj. Mnr.: ");

        if (beacon.getId2() != null) {
            scanString.append(beacon.getId2());
        }

        scanString.append("-");

        if (beacon.getId3() != null) {
            scanString.append(beacon.getId3() + "\n");
        }

        scanString.append(" RSSI: ").append(beacon.getRssi() + "\n");
        scanString.append(" Distance: ").append(beacon.getDistance() + "\n");
        scanString.append(" Proximity: ").append(getProximityStringByRSSI(beacon.getRssi()) + "\n");
        scanString.append(" Power: ").append(beacon.getTxPower() + "\n");
        scanString.append(" Timestamp: ").append(getCurrentTimeStamp());

        return scanString.toString();
    }


}

