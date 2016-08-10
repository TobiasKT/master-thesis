package mt.lmu.android.com.authenticationapp;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends WearableActivity {

    private static final String TAG = MainActivity.class.getSimpleName();


    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private BoxInsetLayout mContainerView;
    private TextView mTextView;
    private TextView mClockView;

    private HeartRate mHeartRate;
    private ClientTask mClientTask;
    private Button mButtonAuth;
    private Button mButtonStop;




    Socket socket = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mTextView = (TextView) findViewById(R.id.text);
        mClockView = (TextView) findViewById(R.id.clock);
        mButtonAuth = (Button)findViewById(R.id.btnAuth);
        mButtonStop = (Button)findViewById(R.id.btnStop);

        mHeartRate = new HeartRate(this);


    }

    public void onBtnClicked(View v) {
        Log.i(TAG, "Btn clicked");
        mClientTask = new ClientTask(MainActivity.this, mHeartRate);
        mClientTask.execute();
        mHeartRate.registerHeartRateSensor();
        mButtonAuth.setEnabled(false);
        mButtonStop.setEnabled(true);

    }

    public void onStopAuthenticationClicked(View v) {

        mClientTask.cancel(true);
        mHeartRate.unregisterHeartRateSensor();
        mButtonAuth.setEnabled(true);
        mButtonStop.setEnabled(false);
    }

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
