package com.android.lmu.mt.tokt.authenticator.events;

import com.android.lmu.mt.tokt.authenticator.data.Sensor;
import com.android.lmu.mt.tokt.authenticator.data.SensorDataPoint;

/**
 * Created by tobiaskeinath on 28.08.16.
 */
public class SensorUpdatedEvent {

    private Sensor mSensor;
    private SensorDataPoint mSensorDataPoint;
    private String mUsername;

    public SensorUpdatedEvent(Sensor sensor, SensorDataPoint sensorDataPoint, String username) {
        this.mSensor = sensor;
        this.mSensorDataPoint = sensorDataPoint;
        this.mUsername = username;
    }

    public Sensor getSensor() {
        return mSensor;
    }

    public SensorDataPoint getDataPoint() {
        return mSensorDataPoint;
    }

    public String getUsername(){
        return mUsername;
    }

}
