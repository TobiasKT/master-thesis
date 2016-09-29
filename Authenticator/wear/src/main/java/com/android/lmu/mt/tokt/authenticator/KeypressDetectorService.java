package com.android.lmu.mt.tokt.authenticator;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.lmu.mt.tokt.authenticator.shared.AppConstants;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by tobiaskeinath on 28.09.16.
 */
public class KeypressDetectorService extends Service implements SensorEventListener {


    private static final String TAG = KeypressDetectorService.class.getSimpleName();

    private LocalBroadcastManager mLocalBroadcastManager;

    private WatchClient mWatchClient;

    private ArrayList<Float> mAccelerometerX;
    private ArrayList<Float> mAccelerometerY;
    private ArrayList<Float> mAccelerometerZ;

    private ArrayList<Float> mGyroX;
    private ArrayList<Float> mGyroY;
    private ArrayList<Float> mGyroZ;

    private int counter = 0;
    //Sensor
    private SensorManager mSensorManager;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate KeypressDetectorService");

        mAccelerometerX = new ArrayList<>();
        mAccelerometerY = new ArrayList<>();
        mAccelerometerZ = new ArrayList<>();

        mGyroX = new ArrayList<>();
        mGyroY = new ArrayList<>();
        mGyroZ = new ArrayList<>();

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mWatchClient = WatchClient.getInstance(this);

        registerSensors();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterSensors();
        userWasTyping();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (sensorEvent.sensor.getType() == AppConstants.SENSOR_TYPE_ACCELEROMETER) {
            mAccelerometerX.add(sensorEvent.values[0]);
            mAccelerometerY.add(sensorEvent.values[1]);
            mAccelerometerZ.add(sensorEvent.values[2]);
        }

        if (sensorEvent.sensor.getType() == AppConstants.SENSOR_TYPE_GYRO) {
            mGyroX.add(sensorEvent.values[0]);
            mGyroY.add(sensorEvent.values[1]);
            mGyroZ.add(sensorEvent.values[2]);
        }

        //mWatchClient.sendSensorData(sensorEvent.sensor.getType(), sensorEvent.accuracy,
        //        sensorEvent.timestamp, sensorEvent.values);

        Log.d(TAG, "type: " + sensorEvent.sensor.getType() + ", values: " + Arrays.toString(sensorEvent.values));

        try {
            Thread.sleep(500);
        } catch (InterruptedException ie) {

        }

    }

    private void registerSensors() {

        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));

        Sensor accelerometerSensor = mSensorManager.getDefaultSensor(AppConstants.SENSOR_TYPE_ACCELEROMETER);
        Sensor gyroSensor = mSensorManager.getDefaultSensor(AppConstants.SENSOR_TYPE_GYRO);

        if (mSensorManager != null) {

            //Accelerometer
            if (accelerometerSensor != null) {
                mSensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "No Accelerometer found");
            }

            //Gyro
            if (gyroSensor != null) {
                mSensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "No GyroSensor found");
            }
        }
    }

    private void unregisterSensors() {
        mSensorManager.unregisterListener(this);
    }

    private double calculateStandardDeviation(ArrayList<Float> values, String tag) {
        Statistics stats = new Statistics(values);
        double standardDev = stats.getStdDev();
        Log.d(TAG, "Standard Dev - " + tag + ": " + standardDev);
        return standardDev;
    }

    private int inRangeCounter = 0;

    private boolean userWasTyping() {

        double sdAccX = calculateStandardDeviation(mAccelerometerX, "AccX");
        double sdAccY = calculateStandardDeviation(mAccelerometerY, "AccY");
        double sdAccZ = calculateStandardDeviation(mAccelerometerZ, "AccZ");

        double sdGyroX = calculateStandardDeviation(mAccelerometerX, "GyroX");
        double sdGyroY = calculateStandardDeviation(mAccelerometerY, "GyroY");
        double sdGyroZ = calculateStandardDeviation(mAccelerometerZ, "GyroZ");

        if (sdAccX != 0 && sdAccX > 0.1 && sdAccX < 0.25) {
            inRangeCounter++;
        }

        if (sdAccY != 0 && sdAccY > 0.1 && sdAccY < 0.25) {
            inRangeCounter++;
        }

        if (sdAccZ != 0 && sdAccZ > 0.1 && sdAccZ < 0.25) {
            inRangeCounter++;
        }

        if (sdGyroX != 0 && sdGyroX > 0.1 && sdGyroX < 0.25) {
            inRangeCounter++;
        }

        if (sdGyroY != 0 && sdGyroY > 0.1 && sdGyroY < 0.25) {
            inRangeCounter++;
        }

        if (sdGyroZ != 0 && sdGyroZ > 0.1 && sdGyroZ < 0.25) {
            inRangeCounter++;
        }

        if (inRangeCounter >= 4) {
            return true;
        } else {
            return false;
        }
    }
}
