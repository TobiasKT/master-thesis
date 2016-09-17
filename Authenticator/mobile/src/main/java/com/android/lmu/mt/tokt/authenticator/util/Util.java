package com.android.lmu.mt.tokt.authenticator.util;

import com.android.lmu.mt.tokt.authenticator.shared.AppConstants;

/**
 * Created by tobiaskeinath on 17.09.16.
 */
public final class Util {

    private static final String TAG = Util.class.getSimpleName();

    public static String getProximityStringByRSSI(int rssi) {
        String proximityString;
        if (rssi >= -72) {
            proximityString = AppConstants.PROXIMITY_IMMEDIATE;
        } else if (rssi < -79 && rssi >= -80) {
            proximityString = AppConstants.PROXIMITY_NEAR;
        } else {
            proximityString = AppConstants.PROXIMITY_FAR;
        }
        return proximityString;
    }


}
