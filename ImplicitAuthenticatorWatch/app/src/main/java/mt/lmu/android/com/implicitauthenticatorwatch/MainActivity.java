package mt.lmu.android.com.implicitauthenticatorwatch;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends WearableActivity {

    //adb forward tcp:4444 localabstract:/adb-hub
    //adb connect 127.0.0.1:4444
    //adb -s 127.0.0.1:4444 tcpip 5555
    // adb connect 192.168.178.96:5555

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private BoxInsetLayout mContainerView;
    private TextView mTextView;
    private TextView mClockView;
    private Button mStop;
    private Button mStart;

    private WatchClient mWatchClient;

    private Handler mHandler;

    private AuthenticatorAsyncTask mAuthenticatorAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        //Init Views
        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mTextView = (TextView) findViewById(R.id.text);
        mClockView = (TextView) findViewById(R.id.clock);

        mStart = (Button) findViewById(R.id.start);
        mStop = (Button) findViewById(R.id.stop);


        //Authenticator

        mHandler = new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                switch (msg.what) {
                    case AppConstants.STATE_CONFIRM:

                        if (msg.obj != null) {
                            int cheksum = (int) msg.obj;
                            showConfirmConnectionDialog("Allow Connection? (" + cheksum + ")", cheksum);
                        }
                        break;
                    case AppConstants.STATE_CONNECTED:
                        //starte service
                        if (msg.obj != null) {
                            Log.d(TAG, "connect... " + msg.obj.toString());
                            Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        }

                        break;
                    case AppConstants.STATE_DISCONNECTED:
                        //beende service
                        if (msg.obj != null) {
                            Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        }
                        mAuthenticatorAsyncTask.getTCPClient().stopClient();
                        mAuthenticatorAsyncTask.cancel(true);
                        break;
                    case AppConstants.STATE_AUTHENTICATED:

                        if (msg.obj != null) {
                            Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        }
                        //sehr wichtig!!!
                        if (WatchClient.getInstance() == null) {
                            WatchClient.setInstance(MainActivity.this, mAuthenticatorAsyncTask.getTCPClient());
                        }
                        MainActivity.this.startService(new Intent(MainActivity.this, SensorService.class));
                        break;
                    case AppConstants.STATE_NOT_AUTHENTICATED:
                        stopService(new Intent(MainActivity.this, SensorService.class));
                        break;
                    case AppConstants.STATE_HEART_BEATING:
                        if (msg.obj != null) {
                            Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        }
                    case AppConstants.ERROR:
                        Toast.makeText(MainActivity.this, "ERROR! Network problems!", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };


        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Start service...");
                mAuthenticatorAsyncTask = new AuthenticatorAsyncTask(mHandler);
                mAuthenticatorAsyncTask.execute();
                mStart.setEnabled(false);
                mStop.setEnabled(true);
            }
        });

        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Stop service...");
                stopService(new Intent(MainActivity.this, SensorService.class));
                mAuthenticatorAsyncTask.getTCPClient().stopClient();
                mAuthenticatorAsyncTask.cancel(true);
                mStart.setEnabled(true);
                mStop.setEnabled(false);
            }
        });
    }

    public void showConfirmConnectionDialog(String message, final int number) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        mAuthenticatorAsyncTask.getTCPClient().setIsConnected(true);
                        mAuthenticatorAsyncTask.getTCPClient().sendMessage(AppConstants.COMMAND_CONFIRM + ":" + number);


                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        mAuthenticatorAsyncTask.getTCPClient().sendMessage(AppConstants.COMMAND_DISCONNECT);
                        break;
                }
            }
        };


        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(message).setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }


    /*-------------Watch specific------------*/
    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
            mTextView.setTextColor(getResources().getColor(android.R.color.white));
            mClockView.setVisibility(View.VISIBLE);

            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
        } else {
            mContainerView.setBackground(null);
            mTextView.setTextColor(getResources().getColor(android.R.color.black));
            mClockView.setVisibility(View.GONE);
        }
    }
}
