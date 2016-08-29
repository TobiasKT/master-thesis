package com.android.lmu.mt.tokt.authenticator.data;

/**
 * Created by tobiaskeinath on 28.08.16.
 */
public class SensorDataPoint {

    private long timestamp;
    private float[] values;
    private int accuracy;

    public SensorDataPoint(long timestamp, int accuracy, float[] values) {
        this.timestamp = timestamp;
        this.accuracy = accuracy;
        this.values = values;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public float[] getValues() {
        return values;
    }

    public int getAccuracy() {
        return accuracy;
    }


}
