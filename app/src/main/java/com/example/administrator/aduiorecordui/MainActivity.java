package com.example.administrator.aduiorecordui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.administrator.aduiorecordui.activity.play.PlayAudioActivity;
import com.example.administrator.aduiorecordui.activity.record.simple.SimpleRecordActivity;
import com.example.administrator.aduiorecordui.activity.record.verticallinefixed.VerticalLineFixedAudioRecordActivity;
import com.example.administrator.aduiorecordui.activity.record.verticallinemove.first.MediaRecordActivity;
import com.example.administrator.aduiorecordui.activity.record.verticallinemove.second.AudioRecordActivity;
import com.example.administrator.aduiorecordui.activity.record.verticallinemove.third.RecordAudioWithDeleteActivity;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViewById(R.id.btn_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MediaRecordActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.btn_play_audio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PlayAudioActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.btn_audio_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioRecordActivity.start(MainActivity.this);
            }
        });

        findViewById(R.id.btn_audio_record_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecordAudioWithDeleteActivity.start(MainActivity.this);
            }
        });

        findViewById(R.id.btn_audio_record_vertical_line_fixed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VerticalLineFixedAudioRecordActivity.start(MainActivity.this);
            }
        });

        findViewById(R.id.btnSimpleActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleRecordActivity.start(MainActivity.this);
            }
        });
    }
}
