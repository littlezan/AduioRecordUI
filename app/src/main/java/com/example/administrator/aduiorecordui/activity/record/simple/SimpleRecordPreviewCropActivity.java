package com.example.administrator.aduiorecordui.activity.record.simple;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.administrator.aduiorecordui.R;
import com.example.administrator.aduiorecordui.model.Decibel;
import com.example.administrator.aduiorecordui.record2mp3.AudioRecordDataSource;
import com.littlezan.recordui.playaudio.PlayAudioCallBack;
import com.littlezan.recordui.playaudio.playviews.VerticalLineMoveAndCropPlayAudioView;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: SimpleRecordPreviewActivity
 * Description: 裁剪
 *
 * @author 彭赞
 * @version 1.0
 * @since 2018-11-03  11:21
 */
public class SimpleRecordPreviewCropActivity extends AppCompatActivity {

    private static final String INTENT_KEY_RECORD_FILE_PATH = "intent_key_record_file_path";
    private static final String INTENT_KEY_DECIBEL_LIST = "intent_key_decibel_list";
    private VerticalLineMoveAndCropPlayAudioView verticalLineMoveAndCropPlayAudioView;
    private String recordFilePath;

    public static void start(Context context, String recordFilePath) {
        Intent intent = new Intent(context, SimpleRecordPreviewCropActivity.class);
        intent.putExtra(INTENT_KEY_RECORD_FILE_PATH, recordFilePath);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_record_preview_crop);
        parseIntent();
        initView();
    }

    private void parseIntent() {
        recordFilePath = getIntent().getStringExtra(INTENT_KEY_RECORD_FILE_PATH);
    }

    private void initView() {
        verticalLineMoveAndCropPlayAudioView = findViewById(R.id.verticalLineMoveAndCropPlayAudioView);
        verticalLineMoveAndCropPlayAudioView.setPlayAudioCallBack(new PlayAudioCallBack() {
            @Override
            public void onPlaying(long timeInMillis) {

            }

            @Override
            public void onStartPlay(long timeInMillis) {

            }

            @Override
            public void onPausePlay() {

            }

            @Override
            public void onResumePlay() {

            }

            @Override
            public void onPlayingFinish() {

            }

            @Override
            public void onCrop(int cropIndex, long remainTimeInMillis) {
                AudioRecordDataSource.getInstance().cropDecibelList(cropIndex);
                SimpleRecordActivity.start(SimpleRecordPreviewCropActivity.this);
            }
        });

        findViewById(R.id.btnPlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verticalLineMoveAndCropPlayAudioView.startPlay(0);
            }
        });
        findViewById(R.id.btnPause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verticalLineMoveAndCropPlayAudioView.stopPlay();
            }
        });

        findViewById(R.id.btnCrop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verticalLineMoveAndCropPlayAudioView.crop();
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        List<Float> audioSourceList = new ArrayList<>();
        for (Decibel decibel : AudioRecordDataSource.getInstance().decibelList) {
            audioSourceList.add(decibel.percent);
        }
        verticalLineMoveAndCropPlayAudioView.setAudioSource(audioSourceList);
    }
}
