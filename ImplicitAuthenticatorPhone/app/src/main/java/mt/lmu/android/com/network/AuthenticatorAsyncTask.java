package mt.lmu.android.com.network;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import mt.lmu.android.com.implicitauthenticatorphone.AppConstants;

/**
 * Created by tobiaskeinath on 22.08.16.
 */

public class AuthenticatorAsyncTask extends AsyncTask<String, String, TCPClient> {

    private static final String TAG = AuthenticatorAsyncTask.class.getSimpleName();

    private TCPClient mTcpClient;
    private Handler mHandler;

    public AuthenticatorAsyncTask(Handler handler) {
        mHandler = handler;
    }

    public TCPClient getTCPClient() {
        return mTcpClient;
    }


    @Override
    protected TCPClient doInBackground(String... strings) {
        Log.d(TAG, "starte do in Background....");

        try {

            mTcpClient = new TCPClient(mHandler, new TCPClient.MessageCallBack() {

                @Override
                public void callbackMessageReceiver(String message) {
                    publishProgress(message);
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

/*
        if (values[0].equals(AppConstants.COMMAND_CONNECT)) {
            //mTcpClient.sendMessage(COMMAND_DISCONNECT);
            Message completeMessage =
                    mHandler.obtainMessage(AppConstants.STATE_CONNECTED, AppConstants.COMMAND_CONNECT);
            mHandler.sendMessage(completeMessage);
            //starte Service in MainActivity
        }

        if (values[0].equals(AppConstants.COMMAND_DISCONNECT)) {
            //mTcpClient.sendMessage(COMMAND_DISCONNECT);
            mTcpClient.stopClient();
            Message completeMessage =
                    mHandler.obtainMessage(AppConstants.STATE_DISCONNECTED, AppConstants.COMMAND_CONNECT);
            mHandler.sendMessage(completeMessage);
            //stoppe Service in MainActivity
        }*/
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
