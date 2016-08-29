package com.android.lmu.mt.tokt.authenticator.events;

import com.android.lmu.mt.tokt.authenticator.data.TagData;

/**
 * Created by tobiaskeinath on 28.08.16.
 */
public class TagAddedEvent {

    private TagData mTagData;

    public TagAddedEvent(TagData pTagData) {
        mTagData = pTagData;
    }

    public TagData getTag() {
        return mTagData;
    }
}
