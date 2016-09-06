package com.android.lmu.mt.tokt.authenticator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.lmu.mt.tokt.authenticator.shared.AppConstants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by tobiaskeinath on 29.08.16.
 */
public class MainActivity extends WearableActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private BoxInsetLayout mContainerView;
    private TextView mClockView;

    private TextView mConnectionStateText;
    private TextView mHeartRateText;
    private TextView mStepCountText;
    private TextView mProximityText;
    private Button mCloseBtn;

    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mClockView = (TextView) findViewById(R.id.clock);


        mConnectionStateText = (TextView) findViewById(R.id.connection_status_text);
        mHeartRateText = (TextView) findViewById(R.id.heart_rate_text);
        mStepCountText = (TextView) findViewById(R.id.step_text);
        mProximityText = (TextView) findViewById(R.id.proximity_text);

        mCloseBtn = (Button) findViewById(R.id.close);

        mCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(new Intent(MainActivity.this, SensorService.class));
                stopService(new Intent(MainActivity.this, BeaconService.class));
                finish();
            }
        });

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {


                if (intent.getAction().equals(AppConstants.MESSAGE_RECEIVER_RESULT)) {
                    Log.d(TAG, "MessageReceiver result received");
                    String message = intent.getStringExtra(AppConstants.MESSAGE_RECEIVER_MESSAGE);
                    mConnectionStateText.setText(message);
                }

                if (intent.getAction().equals(AppConstants.BEACON_RESULT)) {
                    Log.d(TAG, "Beacon result received");
                    String message = intent.getStringExtra(AppConstants.BEACON_MESSAGE);
                    mProximityText.setText(message);
                }

                if (intent.getAction().equals(AppConstants.SENSOR_HEART_RATE_RESULT)) {
                    Log.d(TAG, "HeartRate result received");
                    String message = intent.getStringExtra(AppConstants.SENSOR_HEART_RATE_MESSAGE);
                    mHeartRateText.setText(message);
                }

                if (intent.getAction().equals(AppConstants.SENSOR_STEP_COUNT_RESULT)) {
                    Log.d(TAG, "StepCount result received");
                    String message = intent.getStringExtra(AppConstants.SENSOR_STEP_COUNT_MESSAGE);
                    mStepCountText.setText(message);
                }

            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstants.BEACON_RESULT);
        filter.addAction(AppConstants.SENSOR_HEART_RATE_RESULT);
        filter.addAction(AppConstants.SENSOR_STEP_COUNT_RESULT);
        filter.addAction(AppConstants.MESSAGE_RECEIVER_RESULT);

        LocalBroadcastManager.getInstance(this).registerReceiver((mBroadcastReceiver),
                filter);

        //JUST FOR TESTING
        // startService(new Intent(this, BeaconService.class));
        //startService(new Intent(this, SensorService.class));
    }

    @Override
    protected void onStop() {
        //JUST FOR TESTING
        //stopService(new Intent(this, BeaconService.class));
        //stopService(new Intent(this, SensorService.class));

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        super.onStop();
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
            mClockView.setVisibility(View.VISIBLE);
            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
        } else {
            mContainerView.setBackground(null);
            mClockView.setVisibility(View.GONE);
        }
    }
}
