package com.android.lmu.mt.tokt.authenticator.keydetection;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.lmu.mt.tokt.authenticator.util.Statistics;
import com.android.lmu.mt.tokt.authenticator.service.WatchClient;
import com.android.lmu.mt.tokt.authenticator.shared.AppConstants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by tobiaskeinath on 28.09.16.
 */
public class KeypressDetector implements SensorEventListener {


    private static final String TAG = KeypressDetector.class.getSimpleName();

    private LocalBroadcastManager mLocalBroadcastManager;

    private WatchClient mWatchClient;

    private ArrayList<Float> mAccelerometerX;
    private ArrayList<Float> mAccelerometerY;
    private ArrayList<Float> mAccelerometerZ;

    private ArrayList<Float> mGyroX;
    private ArrayList<Float> mGyroY;
    private ArrayList<Float> mGyroZ;

    private int counter = 0;

    private Context mContext;
    //Sensor
    private SensorManager mSensorManager;

    private long mLastTimeStamp = 0;

    public KeypressDetector(Context context) {
        Log.d(TAG, "constructor KeypressDetector called");

        mContext = context;

        mAccelerometerX = new ArrayList<>();
        mAccelerometerY = new ArrayList<>();
        mAccelerometerZ = new ArrayList<>();

        mGyroX = new ArrayList<>();
        mGyroY = new ArrayList<>();
        mGyroZ = new ArrayList<>();

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
        mWatchClient = WatchClient.getInstance(context);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        /*if (mLastTimeStamp != 0) {
            if (getTimeAgo(mLastTimeStamp) <= 500) {
                return;
            }
        }*/

        if (sensorEvent.sensor.getType() == AppConstants.SENSOR_TYPE_ACCELEROMETER) {
            mAccelerometerX.add(sensorEvent.values[0]);
            mAccelerometerY.add(sensorEvent.values[1]);
            mAccelerometerZ.add(sensorEvent.values[2]);
            mLastTimeStamp = System.currentTimeMillis();
        }

        if (sensorEvent.sensor.getType() == AppConstants.SENSOR_TYPE_GYRO) {
            mGyroX.add(sensorEvent.values[0]);
            mGyroY.add(sensorEvent.values[1]);
            mGyroZ.add(sensorEvent.values[2]);
            mLastTimeStamp = System.currentTimeMillis();
        }

        //mWatchClient.sendSensorData(sensorEvent.sensor.getType(), sensorEvent.accuracy,
        //        sensorEvent.timestamp, sensorEvent.values);

        Log.d(TAG, "type: " + sensorEvent.sensor.getType() + ", values: " + Arrays.toString(sensorEvent.values));


    }

    private long getTimeAgo(long timestamp) {
        long t = System.currentTimeMillis();
        long lastTimestamp = timestamp;
        long timeAgo = t - lastTimestamp;
        return timeAgo;
    }

    public void startTypingSensors() {

        mSensorManager = ((SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE));

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

    public boolean stopTypingSensors() {
        mSensorManager.unregisterListener(this);
        boolean wasTyping = userWasTyping();
        return wasTyping;
    }

    private double calculateStandardDeviation(ArrayList<Float> values, String tag) {
        Statistics stats = new Statistics(values);
        double standardDev = round(stats.getStdDev(), 2);
        Log.d(TAG, "Standard Dev - " + tag + ": " + standardDev);
        return standardDev;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private int inRangeCounter = 0;

    private boolean userWasTyping() {

        double sdAccX = calculateStandardDeviation(mAccelerometerX, "AccX");
        double sdAccY = calculateStandardDeviation(mAccelerometerY, "AccY");
        double sdAccZ = calculateStandardDeviation(mAccelerometerZ, "AccZ");

        double sdGyroX = calculateStandardDeviation(mGyroX, "GyroX");
        double sdGyroY = calculateStandardDeviation(mGyroY, "GyroY");
        double sdGyroZ = calculateStandardDeviation(mGyroZ, "GyroZ");

        if (sdAccX != 0 && sdAccX > 0.1 && sdAccX < 1.1) {
            inRangeCounter++;
        }

        if (sdAccY != 0 && sdAccY > 0.1 && sdAccY < 1.3) {
            inRangeCounter++;
        }

        if (sdAccZ != 0 && sdAccZ > 0.08 && sdAccZ < 1.4) {
            inRangeCounter++;
        }

        if (sdGyroX != 0 && sdGyroX > 0.1 && sdGyroX < 0.6) {
            inRangeCounter++;
        }

        if (sdGyroY != 0 && sdGyroY > 0.1 && sdGyroY < 0.4) {
            inRangeCounter++;
        }

        if (sdGyroZ != 0 && sdGyroZ > 0.01 && sdGyroZ < 0.3) {
            inRangeCounter++;
        }

        if (inRangeCounter >= 4) {
            return true;
        } else {
            return false;
        }
    }
}
