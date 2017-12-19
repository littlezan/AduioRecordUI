package com.example.administrator.aduiorecordui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.administrator.aduiorecordui.record.AudioRecord;
import com.example.administrator.aduiorecordui.record.RecordCallBack;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: RecordActivity
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2017-12-14  19:59
 */
public class RecordActivity extends AppCompatActivity {
    private static final String TAG = "RecordActivity";
    private long playingTimeInMillis;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        final AudioRecord audioRecord = findViewById(R.id.ruler);
        Button btnStart = findViewById(R.id.btn_start);
        Button btnStop = findViewById(R.id.btn_stop);
        Button btnPlay = findViewById(R.id.btn_play);
        Button btnPause = findViewById(R.id.btn_pause);
        Button btnReset = findViewById(R.id.btn_reset);
        final EditText editText = findViewById(R.id.edit_text);
        Button btnPlayTime = findViewById(R.id.btn_play_time);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioRecord.startRecord();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioRecord.stopRecord();
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioRecord.startPlayRecord(0);
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioRecord.stopPlayRecord();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioRecord.reset();
            }
        });

        btnPlayTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(editText.getText().toString())) {
                    int millis = Integer.parseInt(editText.getText().toString());
                    audioRecord.startPlayRecord(millis*1000);
                }
            }
        });

        audioRecord.setRecordCallBack(new RecordCallBack() {

            @Override
            public float getSamplePercent() {
                return new Random().nextFloat();
            }

            @Override
            public void onScroll(long centerStartTimeMillis) {

            }

            @Override
            public void onRecordCurrent(long centerStartTimeMillis, long recordTimeInMillis) {
                Log.d(TAG, "lll onRecordCurrent: centerStartTimeMillis = " + centerStartTimeMillis + ",  inSecond = " + TimeUnit.MILLISECONDS.toSeconds(centerStartTimeMillis)
                        + ", recordTimeInMillis = " + recordTimeInMillis+", inSecond = " + TimeUnit.MILLISECONDS.toSeconds(recordTimeInMillis));
            }

            @Override
            public void onPlayingRecordFinish() {

            }

            @Override
            public void onPlayingRecord(long playingTimeInMillis) {
                Log.d(TAG, "lll onPlayingRecord: playingTimeInMillis = " + playingTimeInMillis );
                RecordActivity.this.playingTimeInMillis = playingTimeInMillis;
            }

            @Override
            public void onRecordFinish() {

            }

        });

    }
}
