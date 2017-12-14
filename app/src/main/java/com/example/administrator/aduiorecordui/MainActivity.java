package com.example.administrator.aduiorecordui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.administrator.aduiorecordui.record.AudioRecord;
import com.example.administrator.aduiorecordui.record.RecordCallBack;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private AudioRecord ruler;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ruler = findViewById(R.id.ruler);
        Button btnStart = findViewById(R.id.btn_start);
        Button btnStop = findViewById(R.id.btn_stop);
        Button btnPlay = findViewById(R.id.btn_play);
        Button btnPause = findViewById(R.id.btn_pause);
        Button btnReset = findViewById(R.id.btn_reset);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ruler.startRecord();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ruler.stopRecord();
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ruler.startPlayRecord();
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ruler.stopPlayRecord();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ruler.reset();
            }
        });

        ruler.setRecordCallBack(new RecordCallBack() {

            @Override
            public float getSamplePercent() {
                return new Random().nextFloat();
            }

            @Override
            public void onRecordCurrent(long centerStartTimeMillis, long recordTimeInMillis) {
                Log.d(TAG, "lll onRecordCurrent: centerStartTimeMillis = " + centerStartTimeMillis + ",  inSecond = " + TimeUnit.MILLISECONDS.toSeconds(centerStartTimeMillis)
                        + ", recordTimeInMillis = " + recordTimeInMillis+", inSecond = " + TimeUnit.MILLISECONDS.toSeconds(recordTimeInMillis));
            }

            @Override
            public void onPlayingRecordFinish() {

            }

        });

    }
}
