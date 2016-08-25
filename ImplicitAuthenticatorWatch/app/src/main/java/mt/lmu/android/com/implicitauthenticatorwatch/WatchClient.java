package mt.lmu.android.com.implicitauthenticatorwatch;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseLongArray;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by tobiaskeinath on 17.08.16.
 */
public class WatchClient {

    private static final String TAG = WatchClient.class.getSimpleName();
    private static final int CLIENT_CONNECTION_TIMEOUT = 150000;

    public static WatchClient instance;

    private Context mContext;
    private ExecutorService mExecutorService;
    private int mFilterId;

    private SparseLongArray mLastSensorData;

    private TCPClient mTcpClient;
    private ConnectionAsynchTask mConnectionAsynchTask;

    //TODO
    //boolean isConnected
    //sendData


    public static WatchClient getInstance(Context context) {
        if (instance == null) {
            instance = new WatchClient(context.getApplicationContext());
        }
        return instance;
    }

    public WatchClient(Context context) {
        mContext = context;
        mExecutorService = Executors.newCachedThreadPool();
        mLastSensorData = new SparseLongArray();
        mConnectionAsynchTask = new ConnectionAsynchTask();
    }


    public void stopAsyncTCPClient() {
        mConnectionAsynchTask.cancel(true);
    }

    public void startAsyncTCPClient() {
        mConnectionAsynchTask.execute("");
    }

    public void setSensorFilter(int filterId) {
        Log.d(TAG, "Now filtering by sensor: " + filterId);
        mFilterId = filterId;
    }

    public void sendSensorData(final int sensorType, final String sensorName, final int accuracy,
                               final long timestamp, final float[] values) {
        long time = System.currentTimeMillis();
        long lastTimeStamp = mLastSensorData.get(sensorType);
        long timeAgo = time - lastTimeStamp;

        if (lastTimeStamp != 0) {
            if (mFilterId == sensorType && timeAgo < 100) {
                return;
            }
            if (mFilterId != sensorType && timeAgo < 3000) {
                return;
            }
        }

        mLastSensorData.put(sensorType, time);

        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                sendSensorDataInBackground(sensorType, sensorName, accuracy, timestamp, values);
            }
        });
    }

    private void sendSensorDataInBackground(int sensorType, String sensorName, int accuracy, long timestamp, float[] values) {

        if (sensorType == mFilterId) {
            Log.i(TAG, "Sensor" + sensorType + " = " + Arrays.toString(values));
        } else {
            Log.d(TAG, "Sensor" + sensorType + " = " + Arrays.toString(values));
        }


        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("name", sensorName);
            jsonObject.put("typ", sensorType);
            jsonObject.put("accuracy", accuracy);
            jsonObject.put("timestamp", timestamp);

            //test
            JSONArray sensorValues = new JSONArray();
            if (values.length != 0) {
                for (int i = 0; i < values.length; i++) {
                    sensorValues.put(values[i]);
                }
                jsonObject.put("values", sensorValues);
            }

        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        sendMessageToServer(jsonObject);
    }

    private boolean validateConnection() {
        //TODO: validate server connection
        return false;
    }

    private void sendMessageToServer(JSONObject data) {
        if (validateConnection()) {
            //TODO: Send to server
        }
        // TCPClient.sendMessage()!
        //Log.d(TAG, "Send data: " + data.toString());

        //sends the message to the server
        Toast.makeText(mContext, "Send data: " + data.toString(),Toast.LENGTH_SHORT).show();
        if (mTcpClient != null) {
            mTcpClient.sendMessage(data.toString());
        }

    }


    public class ConnectionAsynchTask extends AsyncTask<String, String, TCPClient> {

        @Override
        protected TCPClient doInBackground(String... params) {

            mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {

                @Override
                public void messageReceived(String message) {
                    publishProgress(message);
                    Log.d(TAG, "Server - " + message);
                }
            });
            mTcpClient.startRunning();

            return null;
        }


        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            Toast.makeText(mContext, "Message from Server " + values[0],Toast.LENGTH_SHORT).show();
            //TODO in MainActivity verarbeiten
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Toast.makeText(mContext, "AsyncTask cancelled",Toast.LENGTH_SHORT).show();
        }
    }

}
