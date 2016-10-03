package com.android.lmu.mt.tokt.authenticator.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.android.lmu.mt.tokt.authenticator.shared.AppConstants;

/**
 * Created by tobiaskeinath on 30.08.16.
 */
public class AuthenticatorAsyncTask extends AsyncTask<String, String, TCPClient> {

    private static final String TAG = AuthenticatorAsyncTask.class.getSimpleName();

    private TCPClient mTcpClient;
    private Handler mHandler;
    private Context mContext;
    private SharedPreferences mSharedPreferences;


    public AuthenticatorAsyncTask(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
        mSharedPreferences = mContext.getSharedPreferences(
                AppConstants.SHARED_PREF_APP_KEY, Context.MODE_PRIVATE);
    }

    public TCPClient getTCPClient() {
        return mTcpClient;
    }

    private String getSavedServerIP() {
        return mSharedPreferences.getString(AppConstants.SHARED_PREF_SEVER_IP,
                AppConstants.DEFAULT_SERVER_IP);
    }

    private int getSavedServerPort() {
        return mSharedPreferences.getInt(AppConstants.SHARED_PREF_SEVER_PORT,
                AppConstants.DEFAULT_SERVER_PORT);
    }


    @Override
    protected TCPClient doInBackground(String... strings) {
        Log.d(TAG, "starte do in Background....");

        try {
            String ip = getSavedServerIP();
            int port = getSavedServerPort();

            mTcpClient = new TCPClient(ip, port, mContext, mHandler, new TCPClient.MessageCallBack() {

                @Override
                public void callbackMessageReceiver(String message) {
                    //publishProgress(message);
                }
            });
            mTcpClient.run();
        } catch (NullPointerException ne) {
            Log.d(TAG, "Caught nullpointer exception");
            ne.printStackTrace();
            mHandler.sendEmptyMessage(AppConstants.ERROR);
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        Log.d(TAG, "In progress update, values: " + values.toString());

    }

    @Override
    protected void onPostExecute(TCPClient result) {
        super.onPostExecute(result);
        Log.d(TAG, "In onPostExecute...");
        if (result != null && result.isRunning()) {
            result.stopClient();
        }
    }
}
