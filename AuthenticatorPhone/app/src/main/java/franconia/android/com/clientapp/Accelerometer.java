package franconia.android.com.clientapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Created by tobiaskeinath on 08.08.16.
 */
public class Accelerometer implements SensorEventListener {

    private static final String TAG = Accelerometer.class.getSimpleName();

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    private float last_x, last_y, last_z;

    public Accelerometer(Context context) {
        senSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        Sensor mySensor = event.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            last_x = x;
            last_y = y;
            last_z = z;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "onAccuracyChanged - accuracy: " + accuracy);
    }

    public String getXYZValues() {
        String values = "x: " + last_x + " y: " + last_y + " z:" + last_z;
        Log.d(TAG, "Accelerometer " + values);
        return values;
    }

}
