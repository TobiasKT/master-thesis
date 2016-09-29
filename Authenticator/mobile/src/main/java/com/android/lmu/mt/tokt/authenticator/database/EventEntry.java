package com.android.lmu.mt.tokt.authenticator.database;

import io.realm.RealmObject;
import io.realm.annotations.RealmClass;

@RealmClass
public class EventEntry extends RealmObject {

    private String eventName;
    private String username;
    private long timeStamp;
    private int state;
    private String usernote;

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getUsernote() {
        return usernote;
    }

    public void setUsernote(String usernote) {
        this.usernote = usernote;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
