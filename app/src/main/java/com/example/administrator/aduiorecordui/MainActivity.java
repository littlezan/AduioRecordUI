package com.example.administrator.aduiorecordui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.administrator.aduiorecordui.activity.AudioRecordActivity;
import com.example.administrator.aduiorecordui.activity.MediaRecordActivity;
import com.example.administrator.aduiorecordui.activity.PlayAudioActivity;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnRecord = findViewById(R.id.btn_record);
        Button btnPlayAudio = findViewById(R.id.btn_play_audio);
        Button btnAudioRecord = findViewById(R.id.btn_audio_record);

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MediaRecordActivity.class);
                startActivity(intent);
            }
        });

        btnPlayAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PlayAudioActivity.class);
                startActivity(intent);
            }
        });

        btnAudioRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioRecordActivity.start(MainActivity.this);
            }
        });

    }
}
