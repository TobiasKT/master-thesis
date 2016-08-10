package mt.lmu.android.com.authenticationapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Created by tobiaskeinath on 08.08.16.
 */
public class HeartRate implements SensorEventListener {


    private static final String TAG = HeartRate.class.getSimpleName();

    private Context mContext;

    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;


    private int mHeartRateValue;

    public HeartRate(Context context) {

        mContext = context.getApplicationContext();
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            mHeartRateValue = (int) event.values[0];
            Log.d(TAG, "" + mHeartRateValue);
        } else
            Log.d(TAG, "Unknown sensor type");

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "onAccuracyChanged - accuracy: " + accuracy);
    }


    public String getHeartRateValues() {
        String value = "HEARTRATE:" + mHeartRateValue;
        Log.d(TAG, "Heart Rate " + value);
        return value;
    }


    public void registerHeartRateSensor(){
        mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregisterHeartRateSensor(){
        mSensorManager.unregisterListener(this, mHeartRateSensor);
    }

}
