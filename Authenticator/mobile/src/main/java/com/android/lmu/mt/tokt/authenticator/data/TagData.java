package com.android.lmu.mt.tokt.authenticator.data;

/**
 * Created by tobiaskeinath on 28.08.16.
 */
public class TagData {
    private String tagName;
    private long timestamp;

    public TagData(String tagName, long timestamp) {
        this.tagName = tagName;
        this.timestamp = timestamp;
    }

    public String getTagName() {
        return tagName;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
