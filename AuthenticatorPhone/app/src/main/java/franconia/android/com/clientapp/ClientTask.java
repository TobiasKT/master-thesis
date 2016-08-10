package franconia.android.com.clientapp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by tobiaskeinath on 08.08.16.
 */
public class ClientTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = ClientTask.class.getSimpleName();

    private String mHostname = "192.168.43.108";
    private int mPort = 8080;

    private Context mContext;


    public ClientTask(Context context) {
        mContext = context;
    }

    public ClientTask(Context context, String hostname, int port) {
        mHostname = hostname;
        mPort = port;
        mContext = context;
    }


    @Override
    protected Void doInBackground(Void... params) {
        Socket socket = null;

        try {

            Log.i(TAG, "Attempting to connect server: "+mHostname+":"+mPort);
            socket = new Socket(mHostname, mPort);
            Log.i(TAG, "Connection established");

            Accelerometer acc = new Accelerometer(mContext.getApplicationContext());


            while (true) {
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                bufferedWriter.write(acc.getXYZValues());
                bufferedWriter.newLine();
                bufferedWriter.flush();
                Thread.sleep(5000);
            }


        } catch (InterruptedException ie) {
            Log.e(TAG, ie.getMessage());

        } catch (UnknownHostException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (socket != null) {
                try {
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
