package com.android.lmu.mt.tokt.authenticator.util;

import com.android.lmu.mt.tokt.authenticator.shared.AppConstants;

/**
 * Created by tobiaskeinath on 17.09.16.
 */
public final class Util {

    private static final String TAG = Util.class.getSimpleName();

    private static String mUsername = "";

    public static String getProximityStringByRSSI(int rssi) {
        String proximityString = "undefined";
        if (rssi >= -85) {
            proximityString = AppConstants.PROXIMITY_NEAR;
        } else if(rssi < -85){
            proximityString = AppConstants.PROXIMITY_FAR;
        }
        return proximityString;
    }

    public static String getUsername() {
        return mUsername;
    }

    public static void setUsername(String username) {
        mUsername = username;
    }


}
