package com.android.lmu.mt.tokt.authenticator;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import com.android.lmu.mt.tokt.authenticator.shared.AppConstants;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by tobiaskeinath on 28.08.16.
 */
public class SensorService extends Service implements SensorEventListener {


    private static final String TAG = SensorService.class.getSimpleName();

    private SensorManager mSensorManager;

    private Sensor mHeartRateSensor;

    private WatchClient mWatchClient;
    private ScheduledExecutorService mScheduler;


    @Override
    public void onCreate() {
        super.onCreate();

        mWatchClient = WatchClient.getInstance(this);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("Authenticator");
        builder.setContentText("Authenticate... (Sending data)");
        builder.setSmallIcon(R.mipmap.ic_launcher);

        startForeground(1, builder.build());

        //register sensor listener
        startMeasurement();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        //unregister sensor listener
        stopMeasurement();
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

        //TODO: check if connected to Desktop-App and authenticated
        mWatchClient.sendSensorData(sensorEvent.sensor.getType(), sensorEvent.accuracy,
                sensorEvent.timestamp, sensorEvent.values);
    }

    private void startMeasurement() {


        //TODO: show avaibleble Sensors on Activity with suitable icons

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
                //  mSensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
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
                //    mSensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
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
                //   mSensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "No GravitySensor found");
            }

            if (mHeartRateSensor != null) {
                final int measurementDuration = 10;   // Seconds
                final int measurementBreak = 5;    // Seconds

                mScheduler = Executors.newScheduledThreadPool(1);
                mScheduler.scheduleAtFixedRate(
                        new Runnable() {
                            @Override
                            public void run() {
                               // Log.d(TAG, "register Heartrate Sensor");
                                mSensorManager.registerListener(SensorService.this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);

                                try {
                                    Thread.sleep(measurementDuration * 1000);
                                } catch (InterruptedException e) {
                                    Log.e(TAG, "Interrupted while waitting to unregister Heartrate Sensor");
                                }

                               // Log.d(TAG, "unregister Heartrate Sensor");
                                mSensorManager.unregisterListener(SensorService.this, mHeartRateSensor);
                            }
                        }, 3, measurementDuration + measurementBreak, TimeUnit.SECONDS);

            } else {
                Log.d(TAG, "No Heartrate Sensor found");
            }

        }
    }

    private void stopMeasurement() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
        if (mScheduler != null && !mScheduler.isTerminated()) {
            mScheduler.shutdown();
        }
    }
}
