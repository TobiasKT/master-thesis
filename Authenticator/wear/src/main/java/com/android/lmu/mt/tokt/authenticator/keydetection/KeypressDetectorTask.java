package com.android.lmu.mt.tokt.authenticator.keydetection;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.android.lmu.mt.tokt.authenticator.shared.AppConstants;

/**
 * Created by tobiaskeinath on 03.10.16.
 */
public class KeypressDetectorTask extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = KeypressDetector.class.getSimpleName();

    private Context mContext;
    private Handler mHandler;
    private KeypressDetector mKeypressDetector;

    public KeypressDetectorTask(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
        mKeypressDetector = new KeypressDetector(context);
    }

    private volatile boolean running = true;

    @Override
    protected Boolean doInBackground(Void... voids) {

        mKeypressDetector.startTypingSensors();

        mHandler.sendEmptyMessage(AppConstants.STATE_TYPING_SENSORS_STARTED);

        while (running) {

        }
        //todo return boolean typing false/true
        if (mKeypressDetector.stopTypingSensors()) {
            Log.d(TAG, "User was typing - true");
            mHandler.sendEmptyMessage(AppConstants.STATE_USER_WAS_TYPING);
            return true;
        } else {
            Log.d(TAG, "User was typing - false");
            mHandler.sendEmptyMessage(AppConstants.STATE_USER_WAS_NOT_TYPING);
            return false;
        }
    }


    public void stopKeyDetector() {
        running = false;
        cancel(true);
    }


    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        Log.d(TAG, "Result typing: " + aBoolean);
    }
}
