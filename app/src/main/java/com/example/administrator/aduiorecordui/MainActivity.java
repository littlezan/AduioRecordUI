package com.example.administrator.aduiorecordui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.administrator.aduiorecordui.record.AudioRecord;
import com.example.administrator.aduiorecordui.record.RecordCallBack;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int RECORD_TIME = 10;

    private AudioRecord ruler;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ruler = findViewById(R.id.ruler);
        Button btnStart = findViewById(R.id.btn_start);
        Button btnStop = findViewById(R.id.btn_stop);

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


        ruler.setRecordCallBack(new RecordCallBack() {
            @Override
            public void onScaleChange(int scrollX, long timeInMillis) {
            }

            @Override
            public float getSamplePercent() {
                return new Random().nextFloat();
            }

        });

    }
}
