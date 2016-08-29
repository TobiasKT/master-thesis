package mt.lmu.android.com.implicitauthenticatorwatch;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by tobiaskeinath on 17.08.16.
 */
public class SensorService extends Service implements SensorEventListener {

    public static final String TAG = SensorService.class.getSimpleName();

    private final static int ACCELEROMETER_SENSOR = Sensor.TYPE_ACCELEROMETER; //1
    private final static int STEP_COUNTER_SENSOR = Sensor.TYPE_STEP_COUNTER; //19
    private final static int WRIST_TILT_SENSOR = 26;
    private final static int GYRO_SENSOR = Sensor.TYPE_GYROSCOPE; //4
    private final static int LIGHT_SENSOR = Sensor.TYPE_LIGHT; //5
    private final static int HEART_RATE_SENSOR = Sensor.TYPE_HEART_RATE; //21
    private final static int GAME_ROTATION_SENSOR = Sensor.TYPE_GAME_ROTATION_VECTOR; //15
    private final static int WELLNESS_PASSIVE_SENSOR = 65538; //
    private final static int PPG_SENSOR = 65545;
    private final static int SIGNIFICANT_MOTION_SENSOR = Sensor.TYPE_SIGNIFICANT_MOTION; //17
    private final static int DETAILED_STEP_COUNTER_SENSOR = 65537;
    private final static int USER_PROFILE_SENSOR = 65539;
    private final static int USER_STRIDE_FACTOR_SENSOR = 65546;
    private final static int GRAVITY_SENSOR = Sensor.TYPE_GRAVITY; //9
    private final static int LINEAR_ACCELERATION_SENSOR = Sensor.TYPE_LINEAR_ACCELERATION; //10

    SensorManager mSensorManager;

    private Sensor mHeartRateSensor;

    private WatchClient mWatchClient;
    private ScheduledExecutorService mScheduler;

    @Override
    public void onCreate() {
        super.onCreate();

        mWatchClient = WatchClient.getInstance();

        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("Implicit Authenticator");
        builder.setContentText("Authenticate...");
        builder.setSmallIcon(R.mipmap.ic_launcher);

        startForeground(1, builder.build());

        registerSensorListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterSensorListener();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        String sensorName = getSensorName(event);

        if (mWatchClient.isConnectedToServer()) {
            mWatchClient.sendSensorData(event.sensor.getType(), sensorName, event.accuracy,
                    event.timestamp, event.values);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void registerSensorListener() {

        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));

        Sensor accelerometerSensor = mSensorManager.getDefaultSensor(ACCELEROMETER_SENSOR);
        Sensor stepCounterSensor = mSensorManager.getDefaultSensor(STEP_COUNTER_SENSOR);
        Sensor detailedStepCounterSensor = mSensorManager.getDefaultSensor(DETAILED_STEP_COUNTER_SENSOR);
        Sensor wristTiltSensor = mSensorManager.getDefaultSensor(WRIST_TILT_SENSOR);
        Sensor gyroSensor = mSensorManager.getDefaultSensor(GYRO_SENSOR);
        Sensor lightSensor = mSensorManager.getDefaultSensor(LIGHT_SENSOR);
        Sensor gameRotationSensor = mSensorManager.getDefaultSensor(GAME_ROTATION_SENSOR);
        Sensor wellnessPassiveSensor = mSensorManager.getDefaultSensor(WELLNESS_PASSIVE_SENSOR);
        Sensor ppgSensor = mSensorManager.getDefaultSensor(PPG_SENSOR);
        Sensor significantMotionSensor = mSensorManager.getDefaultSensor(SIGNIFICANT_MOTION_SENSOR);
        Sensor userProfileSensor = mSensorManager.getDefaultSensor(USER_PROFILE_SENSOR);
        Sensor userStrideFactorSensor = mSensorManager.getDefaultSensor(USER_STRIDE_FACTOR_SENSOR);
        Sensor linearAccelerationSensor = mSensorManager.getDefaultSensor(LINEAR_ACCELERATION_SENSOR);
        Sensor gravitySensor = mSensorManager.getDefaultSensor(GRAVITY_SENSOR);

        mHeartRateSensor = mSensorManager.getDefaultSensor(HEART_RATE_SENSOR);

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
                //  mSensorManager.registerListener(this, detailedStepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "No DetailedStepCounterSensor found");
            }

            //Wrist Tilt
            if (wristTiltSensor != null) {
                //   mSensorManager.registerListener(this, wristTiltSensor, SensorManager.SENSOR_DELAY_NORMAL);
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
                //   mSensorManager.registerListener(this, linearAccelerationSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "No LinearAccelerationSensor found");
            }

            //Gravity
            if (gravitySensor != null) {
                //  mSensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "No GravitySensor found");
            }

            if (mHeartRateSensor != null) {
                final int measurementDuration = 2;   // Seconds 6
                final int measurementBreak = 1;    // Seconds 3

                mScheduler = Executors.newScheduledThreadPool(1);
                mScheduler.scheduleAtFixedRate(
                        new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "register Heartrate Sensor");
                                mSensorManager.registerListener(SensorService.this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);

                                try {
                                    Thread.sleep(measurementDuration * 1000);
                                } catch (InterruptedException e) {
                                    Log.e(TAG, "Interrupted while waitting to unregister Heartrate Sensor");
                                }

                                Log.d(TAG, "unregister Heartrate Sensor");
                                mSensorManager.unregisterListener(SensorService.this, mHeartRateSensor);
                            }
                        }, 3, measurementDuration + measurementBreak, TimeUnit.SECONDS);

            } else {
                Log.d(TAG, "No Heartrate Sensor found");
            }

        }

    }

    public void unregisterSensorListener() {

        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }

        if (mScheduler != null && !mScheduler.isTerminated()) {
            mScheduler.shutdown();
        }
    }

    public String getSensorName(SensorEvent event) {

        String sensorName = "";
        switch (event.sensor.getType()) {
            case WRIST_TILT_SENSOR:
                sensorName = "wrist_tilt";
                break;
            case WELLNESS_PASSIVE_SENSOR:
                sensorName = "wellness_passive";
                break;
            case PPG_SENSOR:
                sensorName = "ppg";
                break;
            case DETAILED_STEP_COUNTER_SENSOR:
                sensorName = "detailed_step";
                break;
            case USER_PROFILE_SENSOR:
                sensorName = "user_profile";
                break;
            case USER_STRIDE_FACTOR_SENSOR:
                sensorName = "user_stride_factor";
                break;
            default:
                // sensorName = event.sensor.getStringType().split(".")[2];
                sensorName = event.sensor.getStringType().split("\\.")[2];
                break;
        }
        return sensorName;
    }
}
