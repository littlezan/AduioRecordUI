package com.example.administrator.aduiorecordui.activity.record.simple;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.example.administrator.aduiorecordui.R;
import com.example.administrator.aduiorecordui.activity.BasePlayerActivity;
import com.example.administrator.aduiorecordui.recordmp3.AudioRecordDataSource;
import com.example.administrator.aduiorecordui.recordmp3.CropMp3;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.littlezan.recordui.playaudio.PlayAudioCallBack;
import com.littlezan.recordui.playaudio.playviews.VerticalLineMoveAndCropPlayAudioView;

/**
 * ClassName: SimpleRecordPreviewActivity
 * Description: 裁剪
 *
 * @author 彭赞
 * @version 1.0
 * @since 2018-11-03  11:21
 */
public class SimpleRecordPreviewCropActivity extends BasePlayerActivity {

    private static final String TAG = "SimpleRecordPreviewCrop";

    private VerticalLineMoveAndCropPlayAudioView verticalLineMoveAndCropPlayAudioView;
    private long currentPlayingTimeInMillis;
    private CropMp3 cropMp3;

    public static void start(Context context) {
        Intent intent = new Intent(context, SimpleRecordPreviewCropActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_record_preview_crop);
        initView();
    }


    private void initView() {
        verticalLineMoveAndCropPlayAudioView = findViewById(R.id.verticalLineMoveAndCropPlayAudioView);
        verticalLineMoveAndCropPlayAudioView.setPlayAudioCallBack(new PlayAudioCallBack() {
            @Override
            public void onPlaying(long timeInMillis) {

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
                preparePlay(AudioRecordDataSource.getInstance().getRecordFile());
                seekToPlay(0);
            }
        });
        findViewById(R.id.btnPause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pausePlay();
            }
        });
        findViewById(R.id.btnResume).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekToPlay(currentPlayingTimeInMillis);
            }
        });

        findViewById(R.id.btnCrop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpleExoPlayer.stop();
                verticalLineMoveAndCropPlayAudioView.crop();
            }
        });

        initListener();
    }

    private void initListener() {
        verticalLineMoveAndCropPlayAudioView.setPlayAudioCallBack(new PlayAudioCallBack() {
            @Override
            public void onPlaying(long timeInMillis) {
                currentPlayingTimeInMillis = timeInMillis;
            }


            @Override
            public void onPausePlay() {
                pausePlay();
            }

            @Override
            public void onResumePlay() {
                seekToPlay(currentPlayingTimeInMillis);
            }

            @Override
            public void onPlayingFinish() {

            }

            @Override
            public void onCrop(int cropIndex, long remainTimeInMillis) {
                Log.e(TAG, "onCrop: lll crop --- remainTimeInMillis = "+remainTimeInMillis);
                AudioRecordDataSource.getInstance().cropDecibelList(cropIndex);
                cropMp3.startCrop(remainTimeInMillis);
            }
        });


        simpleExoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
                Log.e(TAG, "onTimelineChanged: lll ");
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playWhenReady) {
                    if (playbackState == Player.STATE_READY) {
                        Log.e(TAG, "onPlayerStateChanged: lll crop --- duration = "+ simpleExoPlayer.getDuration() );
                        verticalLineMoveAndCropPlayAudioView.startPlay(currentPlayingTimeInMillis);
                    }
                } else {
                    verticalLineMoveAndCropPlayAudioView.stopPlay();
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }
        });

        cropMp3 = new CropMp3();
        cropMp3.setCropCallback(new CropMp3.CropCallback() {
            @Override
            public void onCropFinish() {
                pausePlay();
                preparePlay(AudioRecordDataSource.getInstance().getRecordFile());
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        preparePlay(AudioRecordDataSource.getInstance().getRecordFile());
        verticalLineMoveAndCropPlayAudioView.setAudioSource(AudioRecordDataSource.getInstance().decibelList);
        verticalLineMoveAndCropPlayAudioView.setInitCropLineOffset(1000);
    }
}
