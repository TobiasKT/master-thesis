package com.android.lmu.mt.tokt.authenticator.events;

import com.android.lmu.mt.tokt.authenticator.data.Sensor;
import com.android.lmu.mt.tokt.authenticator.data.SensorDataPoint;

/**
 * Created by tobiaskeinath on 28.08.16.
 */
public class SensorUpdatedEvent {

    private Sensor mSensor;
    private SensorDataPoint mSensorDataPoint;

    public SensorUpdatedEvent(Sensor sensor, SensorDataPoint sensorDataPoint) {
        this.mSensor = sensor;
        this.mSensorDataPoint = sensorDataPoint;
    }

    public Sensor getSensor() {
        return mSensor;
    }

    public SensorDataPoint getDataPoint() {
        return mSensorDataPoint;
    }

}
