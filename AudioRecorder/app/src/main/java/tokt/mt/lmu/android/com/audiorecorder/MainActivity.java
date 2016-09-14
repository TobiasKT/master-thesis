package tokt.mt.lmu.android.com.audiorecorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends WearableActivity {

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private BoxInsetLayout mContainerView;
    private Button mStart;
    private Button mStop;
    private TextView mClockView;

    private boolean recording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mStart = (Button) findViewById(R.id.start);
        mStop = (Button) findViewById(R.id.stop);
        mClockView = (TextView) findViewById(R.id.clock);


        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {


                        recording = true;

                        int minBufferSize = 2048;
                        short[] audioData = new short[minBufferSize];

                        AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                                11025,
                                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                                AudioFormat.ENCODING_PCM_16BIT,
                                minBufferSize);

                        audioRecord.startRecording();

                        String imagePath = "/sdcard/sam/";
                        System.out.println("Inside Folder");


                        File file = new File(imagePath, "test");
                        System.out.println("File Created");

                        FileOutputStream fileOutputStream = null;
                        try

                        {

                            DataOutputStream dataOutputStream = new DataOutputStream(
                                    fileOutputStream);
                            while (recording) {
                                int numberOfShort = audioRecord.read(audioData, 0, minBufferSize);
                                for (int i = 0; i < numberOfShort; i++) {
                                    dataOutputStream.writeShort(audioData[i]);
                                    Log.d("MainActivity", "recording");
                                }
                            }


                        } catch (
                                IOException io
                                )

                        {

                        }


                        audioRecord.stop();

                    }
                }, 5000);
            }
        });

        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recording = false;
            }
        });
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
