package com.android.lmu.mt.tokt.authenticator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.lmu.mt.tokt.authenticator.shared.AppConstants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by tobiaskeinath on 29.08.16.
 */
public class MainActivity extends WearableActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int SPEECH_REQUEST_CODE = 0;

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private BoxInsetLayout mContainerView;
    private RelativeLayout mContainerWatchStateRL;

    private TextView mClockView;
    private TextView mConnectionStateText;

    private TextView mHeartRateText;
    private TextView mStepCountText;
    private TextView mProximityText;
    private TextView mBeaconIdentifierText;

    private TextView mHeartRateTitleTxt;
    private TextView mStepCountTitleTxt;
    private TextView mProximityTitleTxt;

    private TextView mLockStateText;
    private RelativeLayout mLockstateRelative;


    private ImageView mHeartRateImg;
    private ImageView mStepsImg;
    private ImageView mProximityImg;


    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mContainerWatchStateRL = (RelativeLayout) findViewById(R.id.container_clock_state_relative);
        mClockView = (TextView) findViewById(R.id.clock);
        mConnectionStateText = (TextView) findViewById(R.id.connection_status_text);

        mHeartRateText = (TextView) findViewById(R.id.heart_rate_text);
        mStepCountText = (TextView) findViewById(R.id.step_text);
        mProximityText = (TextView) findViewById(R.id.proximity_text);
        mBeaconIdentifierText = (TextView) findViewById(R.id.beacon_identifier_text);

        mHeartRateTitleTxt = (TextView) findViewById(R.id.heart_rate_title_text);
        mStepCountTitleTxt = (TextView) findViewById(R.id.step_title_text);
        mProximityTitleTxt = (TextView) findViewById(R.id.proximity_title_text);

        mHeartRateImg = (ImageView) findViewById(R.id.heartbeat_watch_img);
        mStepsImg = (ImageView) findViewById(R.id.walking_watch_img);
        mProximityImg = (ImageView) findViewById(R.id.proximity_watch_img);

        mLockStateText = (TextView) findViewById(R.id.lock_state_text);
        mLockstateRelative = (RelativeLayout) findViewById(R.id.lock_state_relative);

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

                if (intent.getAction().equals(AppConstants.BEACON_IDENTIFIER_RESULT)) {
                    Log.d(TAG, "Beacon ID result received");
                    String message = intent.getStringExtra(AppConstants.BEACON_IDENTIFIER_MESSAGE);
                    mBeaconIdentifierText.setText(message);
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

                if (intent.getAction().equals(AppConstants.SOUND_LISTENING_RESULT)) {
                    displaySpeechRecognizer();
                }

                if (intent.getAction().equals(AppConstants.MESSAGE_RECEIVER_LOCK_RESULT)) {
                    Log.d(TAG, "lock state result received");
                    String message = intent.getStringExtra(AppConstants.MESSAGE_RECEIVER_LOCK_MESSAGE);
                    mLockStateText.setText(message);
                }


            }
        };

    }

    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    // This callback is invoked when the Speech Recognizer returns.
    // This is where you process the intent and extract the speech text from the intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);

            if (spokenText.toLowerCase().equals(AppConstants.SOUND_PASSWORD)) {
                Toast.makeText(this, "PC wil be unlocked", Toast.LENGTH_SHORT).show();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstants.BEACON_RESULT);
        filter.addAction(AppConstants.SENSOR_HEART_RATE_RESULT);
        filter.addAction(AppConstants.SENSOR_STEP_COUNT_RESULT);
        filter.addAction(AppConstants.MESSAGE_RECEIVER_RESULT);
        filter.addAction(AppConstants.BEACON_IDENTIFIER_RESULT);
        filter.addAction(AppConstants.SOUND_LISTENING_RESULT);
        filter.addAction(AppConstants.MESSAGE_RECEIVER_LOCK_RESULT);

        LocalBroadcastManager.getInstance(this).registerReceiver((mBroadcastReceiver),
                filter);

    }

    @Override
    protected void onStop() {
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
            mContainerWatchStateRL.setBackgroundColor(getResources().getColor(android.R.color.black));
            mClockView.setVisibility(View.VISIBLE);
            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));

            mHeartRateText.setTextColor(getResources().getColor(R.color.white));
            mStepCountText.setTextColor(getResources().getColor(R.color.white));
            mProximityText.setTextColor(getResources().getColor(R.color.white));

            mHeartRateTitleTxt.setTextColor(getResources().getColor(R.color.white));
            mStepCountTitleTxt.setTextColor(getResources().getColor(R.color.white));
            mProximityTitleTxt.setTextColor(getResources().getColor(R.color.white));

            mHeartRateImg.setImageResource(R.drawable.heart_white_1);
            mStepsImg.setImageResource(R.drawable.walking_white);
            mProximityImg.setImageResource(R.drawable.signal_white_1);

            mLockStateText.setTextColor(getResources().getColor(android.R.color.black));
            mLockstateRelative.setBackgroundColor(getResources().getColor(R.color.white));

        } else {
            mContainerView.setBackground(null);
            mContainerWatchStateRL.setBackgroundColor(getResources().getColor(R.color.indigo_500));
            mClockView.setVisibility(View.GONE);

            mHeartRateText.setTextColor(getResources().getColor(R.color.gray_900));
            mStepCountText.setTextColor(getResources().getColor(R.color.gray_900));
            mProximityText.setTextColor(getResources().getColor(R.color.gray_900));

            mHeartRateTitleTxt.setTextColor(getResources().getColor(R.color.gray_900));
            mStepCountTitleTxt.setTextColor(getResources().getColor(R.color.gray_900));
            mProximityTitleTxt.setTextColor(getResources().getColor(R.color.gray_900));

            mHeartRateImg.setImageResource(R.drawable.heartbeat);
            mStepsImg.setImageResource(R.drawable.walking);
            mProximityImg.setImageResource(R.drawable.signal);

            mLockStateText.setTextColor(getResources().getColor(R.color.gray_900));
            mLockstateRelative.setBackgroundColor(getResources().getColor(R.color.gray_400));
        }
    }
}
