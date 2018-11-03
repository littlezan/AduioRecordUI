package com.example.administrator.aduiorecordui.activity.record.simple;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.example.administrator.aduiorecordui.R;
import com.example.administrator.aduiorecordui.activity.BaseAudioRecordActivity;
import com.example.administrator.aduiorecordui.model.Decibel;
import com.example.administrator.aduiorecordui.record2mp3.AudioRecordMp3;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.FileDataSourceFactory;
import com.littlezan.recordui.recordaudio.RecordCallBack;
import com.littlezan.recordui.recordaudio.recordviews.SimpleAudioRecordView;

import java.math.BigDecimal;
import java.util.ArrayList;

import io.reactivex.functions.Consumer;

/**
 * ClassName: SimpleRecordActivity
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2018-11-02  14:58
 */
public class SimpleRecordActivity extends BaseAudioRecordActivity {


    public static final int MAX_RECORD_DECIBEL = 80;
    public static final int MIN_RECORD_DECIBEL = 35;

    private SimpleAudioRecordView audioRecordView;


    private AudioRecordMp3 audioRecordMp3;
    private float recordDecibel;
    private ArrayList<Decibel> decibelList = new ArrayList<>();
    private RecordCallBack recordCallBack;

    public static void start(Context context) {
        context.startActivity(new Intent(context, SimpleRecordActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_record);

        initAudioRecorder();
        initView();
        initRecordViewCallback();
    }

    private void initAudioRecorder() {
        audioRecordMp3 = new AudioRecordMp3(recordFile, new AudioRecordMp3.RecordMp3Listener() {
            @Override
            public void onStartRecord() {
                audioRecordView.startRecord();
            }

            @Override
            public void onStopRecord() {
                audioRecordView.stopRecord();
            }

            @Override
            public void onDeletedLastRecord() {

            }

            @Override
            public void onRecordDecibel(float decibel) {
                recordDecibel = decibel;
            }
        });
    }


    private void initView() {
        audioRecordView = findViewById(R.id.audio_record_view);



        findViewById(R.id.btnStartRecord).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecord();
            }
        });
        findViewById(R.id.btnStopRecord).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecord();
            }
        });
        findViewById(R.id.btnReset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFinishRelease();
            }
        });
        findViewById(R.id.btnPlayRecord).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioRecordView.startPlayRecord(0);
            }
        });
        findViewById(R.id.btnStopPlayRecord).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioRecordView.stopPlayRecord();
            }
        });
        findViewById(R.id.btnPreview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleRecordPreviewActivity.start(SimpleRecordActivity.this, audioRecordMp3.getFinalRecordFile().getAbsolutePath(), decibelList);
            }
        });

    }

    private void initRecordViewCallback() {
        recordCallBack = new RecordCallBack() {
            @Override
            public float getSamplePercent() {
                double percent;
                if (recordDecibel >= MAX_RECORD_DECIBEL) {
                    percent = 1f;
                } else if (recordDecibel <= MIN_RECORD_DECIBEL) {
                    percent = 0.01f;
                } else {
                    int max = MAX_RECORD_DECIBEL - MIN_RECORD_DECIBEL;
                    percent = (recordDecibel - MIN_RECORD_DECIBEL) / max;
                }
                BigDecimal bd = new BigDecimal(percent);
                decibelList.add(new Decibel(bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue()));
                return (float) percent;
            }

            @Override
            public void onScroll(long centerStartTimeMillis) {

            }

            @Override
            public void onRecordCurrent(long centerStartTimeMillis, long recordTimeInMillis) {

            }

            @Override
            public void onCenterLineTime(long playingTimeInMillis) {

            }

            @Override
            public void onStartRecord() {

            }

            @Override
            public void onStopRecord() {
                stopRecord();
            }

            @Override
            public void onFinishRecord() {
                stopRecord();
            }

            @Override
            public void onStartPlayRecord(long timeMillis) {
                play(timeMillis);
            }

            @Override
            public void onStopPlayRecode() {
                stopPlay();
            }

            @Override
            public void onFinishPlayingRecord() {

            }
        };

        audioRecordView.setRecordCallBack(recordCallBack);
    }

    @SuppressLint("CheckResult")
    private void startRecord() {
        if (!isPermissionsGranted) {
            permissions
                    .request(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.RECORD_AUDIO)
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean aBoolean) {
                            isPermissionsGranted = aBoolean;
                        }
                    });
        } else {
            audioRecordMp3.startAudioRecord();
        }

    }

    private void stopRecord() {
        audioRecordMp3.stopRecord();
    }

    private void play(long timeMillis) {
        if (timeMillis < 0) {
            timeMillis = 0;
        }
        // MediaSource代表要播放的媒体。
        MediaSource mediaSource = new ExtractorMediaSource.Factory(new FileDataSourceFactory()).createMediaSource(Uri.fromFile(recordFile));
        //Prepare the player with the source.
        simpleExoPlayer.prepare(mediaSource);
        simpleExoPlayer.seekTo(timeMillis);
        simpleExoPlayer.setPlayWhenReady(true);
    }

    private void stopPlay() {
        simpleExoPlayer.stop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        audioRecordMp3.stopRecord();
        audioRecordView.stopPlayRecord();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (simpleExoPlayer != null) {
            stopPlay();
            simpleExoPlayer.release();
        }
    }

    public void onFinishRelease() {
        stopRecord();
        audioRecordView.reset();
        decibelList.clear();
        audioRecordMp3.onRelease();
    }
}
