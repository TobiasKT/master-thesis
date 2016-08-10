package mt.lmu.android.com.authenticationapp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by tobiaskeinath on 08.08.16.
 */
public class ClientTask extends AsyncTask<Void, Void, Void> {

    //adb forward tcp:4444 localabstract:/adb-hub
    //adb connect 127.0.0.1:4444
    private static final String TAG = ClientTask.class.getSimpleName();

    private static  String mHostname = "192.168.43.108";
    private static int mPort = 8080;

    private Context mContext;

    private HeartRate mHeartRate;

    public ClientTask(Context context) {
        mContext = context;
    }

    public ClientTask(Context context, HeartRate heartRate) {
        mContext = context;
        mHeartRate = heartRate;
    }

    public ClientTask(Context context, String hostname, int port) {
        mHostname = hostname;
        mPort = port;
        mContext = context;
    }


    @Override
    protected Void doInBackground(Void... params) {
        Socket socket = null;
        BufferedWriter bufferedWriter = null;
        try {

            Log.i(TAG, "Attempting to connect server: "+mHostname+":"+mPort);
            socket = new Socket(mHostname, mPort);
            Log.i(TAG, "Connection established");

            while (true) {
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                bufferedWriter.write(mHeartRate.getHeartRateValues());
                bufferedWriter.newLine();
                bufferedWriter.flush();
                Thread.sleep(2000);
            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (socket != null) {
                try {
                    bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    bufferedWriter.write("HEARTRATE:0");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
    }
}
