package com.android.lmu.mt.tokt.authenticator;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.lmu.mt.tokt.authenticator.shared.AppConstants;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

/**
 * Created by tobiaskeinath on 04.09.16.
 */
public class BeaconService extends Service implements BeaconConsumer {

    private static final String TAG = BeaconService.class.getSimpleName();

    private WatchClient mWatchClient;

    private BeaconManager mBeaconManager;

    private LocalBroadcastManager mLocalBroadcastManager;

    private GoogleApiClient mGoogleApiClient;


    @Override
    public void onCreate() {
        super.onCreate();

        mWatchClient = WatchClient.getInstance(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
        mGoogleApiClient.connect();


        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

        mBeaconManager = BeaconManager.getInstanceForApplication(this);

        // Tell Library how to decode the signal
        mBeaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        // Start the Beacon Manager
        mBeaconManager.bind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBeaconManager.unbind(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onBeaconServiceConnect() {
        //TODO: consider hardcoding UID of Beacon
        Identifier identifier = Identifier.parse(AppConstants.BEACON_IDENTIFIER_STRING);
        final Region region = new Region("myBeacons", identifier, null, null);
        mBeaconManager.setMonitorNotifier(new MonitorNotifier() {

            // If the device enters a Beacon region
            @Override
            public void didEnterRegion(Region region) {
                try {
                    Log.d(TAG, "did Enter Region...");
                    mBeaconManager.startRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            // If the device leaves a Beacon region
            @Override
            public void didExitRegion(Region region) {
                try {
                    Log.d(TAG, "did Exit Region...");
                    mBeaconManager.stopRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {

            }
        });

        // If the device finds a Beacon fitting the rules, print it in the console
        mBeaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                for (final Beacon beacon : beacons) {
                    String data = logGenericBeacon(beacon);
                    Log.d(TAG, data);
                    long timestamp = System.currentTimeMillis();
                    String proximity = getProximityStringByRSSI(beacon.getRssi());
                    mWatchClient.sendBeaconData(proximity, timestamp);

                    //TODO: only send if data changed
                    sendResultToMainUI(AppConstants.BEACON_RESULT, AppConstants.BEACON_MESSAGE, proximity);
                    sendResultToMainUI(AppConstants.BEACON_IDENTIFIER_RESULT, AppConstants.BEACON_IDENTIFIER_MESSAGE, beacon.getId1().toString());
                }


            }
        });

        try {
            mBeaconManager.stopMonitoringBeaconsInRegion(region);
            mBeaconManager.startMonitoringBeaconsInRegion(region);
        } catch (RemoteException e) {
            Log.e(TAG, "Error monitoring beacon: " + e.getMessage());
        }

    }

    private void sendResultToMainUI(String intentFilter, String extraName, String message) {
        Intent intent = new Intent(intentFilter);
        if (message != null)
            intent.putExtra(extraName, message);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

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


    private static String getCurrentTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss.SSS", Locale.US);
        Date now = new Date();
        return sdf.format(now);
    }

    private String logGenericBeacon(Beacon beacon) {
        StringBuilder scanString = new StringBuilder();
        scanString.append(" UUID: ").append(beacon.getId1() + "\n");
        scanString.append(" Maj. Mnr.: ");

        if (beacon.getId2() != null) {
            scanString.append(beacon.getId2());
        }

        scanString.append("-");

        if (beacon.getId3() != null) {
            scanString.append(beacon.getId3() + "\n");
        }

        scanString.append(" RSSI: ").append(beacon.getRssi() + "\n");
        scanString.append(" Distance: ").append(beacon.getDistance() + "\n");
        scanString.append(" Proximity: ").append(getProximityStringByRSSI(beacon.getRssi()) + "\n");
        scanString.append(" Power: ").append(beacon.getTxPower() + "\n");
        scanString.append(" Timestamp: ").append(getCurrentTimeStamp());

        return scanString.toString();
    }

}
