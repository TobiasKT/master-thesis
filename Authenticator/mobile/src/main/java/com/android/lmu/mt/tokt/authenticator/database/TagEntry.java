package com.android.lmu.mt.tokt.authenticator.database;

import io.realm.RealmObject;
import io.realm.annotations.RealmClass;


@RealmClass
public class TagEntry extends RealmObject {

    private String event;
    private long timestamp;
    private String username;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
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
}
