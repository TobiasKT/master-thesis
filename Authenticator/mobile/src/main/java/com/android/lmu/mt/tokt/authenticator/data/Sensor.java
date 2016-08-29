package com.android.lmu.mt.tokt.authenticator.data;

import android.util.Log;

import com.android.lmu.mt.tokt.authenticator.events.BusProvider;
import com.android.lmu.mt.tokt.authenticator.events.SensorRangeEvent;

import java.util.LinkedList;

/**
 * Created by tobiaskeinath on 28.08.16.
 */
public class Sensor {

    private static final String TAG = Sensor.class.getSimpleName();

    private static final int MAX_DATA_POINTS = 1000;

    private long mSensorId;
    private String mName;
    private float mMinValue = Integer.MAX_VALUE;
    private float mMaxValue = Integer.MIN_VALUE;

    private LinkedList<SensorDataPoint> mDataPoints = new LinkedList<SensorDataPoint>();

    public Sensor(int sensorId, String name) {
        mSensorId = sensorId;
        mName = name;
    }

    public synchronized void addDataPoint(SensorDataPoint dataPoint) {
        mDataPoints.addLast(dataPoint);

        if (mDataPoints.size() > MAX_DATA_POINTS) {
            mDataPoints.removeFirst();
        }

        boolean newLimits = false;

        for (float value : dataPoint.getValues()) {
            if (value > mMaxValue) {
                mMaxValue = value;
                newLimits = true;
            }
            if (value < mMinValue) {
                mMinValue = value;
                newLimits = true;
            }
        }

        if (newLimits) {
            Log.d(TAG, "New range for sensor " + mSensorId + ": " + mMinValue + " - " + mMaxValue);
            BusProvider.postOnMainThread(new SensorRangeEvent(this));
        }
    }

    public long getSensorId() {
        return mSensorId;
    }

    public String getName() {
        return mName;
    }

    public float getMinValue() {
        return mMinValue;
    }

    public float getMaxValue() {
        return mMaxValue;
    }

    public synchronized LinkedList<SensorDataPoint> getDataPoints() {
        return (LinkedList<SensorDataPoint>) mDataPoints.clone();
    }


}
