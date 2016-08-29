package com.android.lmu.mt.tokt.authenticator.events;

import com.android.lmu.mt.tokt.authenticator.data.Sensor;

/**
 * Created by tobiaskeinath on 28.08.16.
 */
public class NewSensorEvent {

    private Sensor mSensor;

    public NewSensorEvent(Sensor sensor) {
        this.mSensor = sensor;
    }

    public Sensor getSensor() {
        return mSensor;
    }

}
