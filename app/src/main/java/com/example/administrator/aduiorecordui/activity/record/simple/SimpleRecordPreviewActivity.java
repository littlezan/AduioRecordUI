package com.example.administrator.aduiorecordui.activity.record.simple;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.administrator.aduiorecordui.R;
import com.example.administrator.aduiorecordui.model.Decibel;
import com.littlezan.recordui.playaudio.PlayAudioCallBack;
import com.littlezan.recordui.playaudio.playviews.VerticalLineMoveByGesturePlayAudioView;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: SimpleRecordPreviewActivity
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2018-11-03  11:21
 */
public class SimpleRecordPreviewActivity extends AppCompatActivity {

    private static final String INTENT_KEY_RECORD_FILE_PATH = "intent_key_record_file_path";
    private static final String INTENT_KEY_DECIBEL_LIST = "intent_key_decibel_list";
    private VerticalLineMoveByGesturePlayAudioView verticalLineMoveByGesturePlayAudioView;
    private ArrayList<Decibel> decibelList;
    private String recordFilePath;

    public static void start(Context context, String recordFilePath, ArrayList<Decibel> decibelList) {
        Intent intent = new Intent(context, SimpleRecordPreviewActivity.class);
        intent.putExtra(INTENT_KEY_RECORD_FILE_PATH, recordFilePath);
        intent.putExtra(INTENT_KEY_DECIBEL_LIST, decibelList);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_record_preview);
        parseIntent();
        initView();
    }

    private void parseIntent() {
        decibelList = getIntent().getParcelableArrayListExtra(INTENT_KEY_DECIBEL_LIST);
        recordFilePath = getIntent().getStringExtra(INTENT_KEY_RECORD_FILE_PATH);
    }

    private void initView() {
        verticalLineMoveByGesturePlayAudioView = findViewById(R.id.verticalLineMoveByGesturePlayAudioView);
        verticalLineMoveByGesturePlayAudioView.setPlayAudioCallBack(new PlayAudioCallBack() {
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
        });

        findViewById(R.id.btnPlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verticalLineMoveByGesturePlayAudioView.startPlay(0);
            }
        });
        findViewById(R.id.btnPause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verticalLineMoveByGesturePlayAudioView.stopPlay();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        List<Float> audioSourceList = new ArrayList<>();
        for (Decibel decibel : decibelList) {
            audioSourceList.add(decibel.percent);
        }
        verticalLineMoveByGesturePlayAudioView.setAudioSource(audioSourceList);
    }
}
