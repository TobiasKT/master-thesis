package com.android.lmu.mt.tokt.authenticator.database;

import io.realm.RealmObject;
import io.realm.annotations.RealmClass;


@RealmClass
public class DataEntry extends RealmObject {

    private String androidDevice;


    private String username;
    private long timestamp;
    private float x;
    private float y;
    private float z;
    private int accuracy;

    private String dataSource;
    private long dataType;

    private String text;

    public String getAndroidDevice() {
        return androidDevice;
    }

    public void setAndroidDevice(String androidDevice) {
        this.androidDevice = androidDevice;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public long getDataType() {
        return dataType;
    }

    public void setDataType(long dataType) {
        this.dataType = dataType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
