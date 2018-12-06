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
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.littlezan.recordui.playaudio.PlayAudioCallBack;
import com.littlezan.recordui.playaudio.playviews.VerticalLineMoveByGesturePlayAudioView;

/**
 * ClassName: SimpleRecordPreviewActivity
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2018-11-03  11:21
 */
public class SimpleRecordPreviewActivity extends BasePlayerActivity {

    private static final String TAG = "SimpleRecordPreviewActi";

    private VerticalLineMoveByGesturePlayAudioView verticalLineMoveByGesturePlayAudioView;
    private long currentPlayingTimeInMillis;

    public static void start(Context context) {
        Intent intent = new Intent(context, SimpleRecordPreviewActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_record_preview);
        initView();

    }


    private void initView() {
        verticalLineMoveByGesturePlayAudioView = findViewById(R.id.verticalLineMoveByGesturePlayAudioView);
        initListener();

        findViewById(R.id.btnPlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        findViewById(R.id.btnCut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleRecordPreviewCropActivity.start(SimpleRecordPreviewActivity.this);
            }
        });

    }

    private void initListener() {
        verticalLineMoveByGesturePlayAudioView.setPlayAudioCallBack(new PlayAudioCallBack() {
            @Override
            public void onPlaying(long timeInMillis) {
                Log.e(TAG, "onPlaying: lll timeInMillis = "+ timeInMillis );
                currentPlayingTimeInMillis = timeInMillis;
            }

            @Override
            public void onCurrentCropLineTime(long cropLineTimeInMillis) {

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
                currentPlayingTimeInMillis = 0;
                verticalLineMoveByGesturePlayAudioView.setInitPlayingTime(0);
            }

            @Override
            public void onCrop(int cropIndex, long remainTimeInMillis) {

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
                Log.e(TAG, "onLoadingChanged: lll isLoading = " + isLoading);

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playWhenReady) {
                    if (playbackState == Player.STATE_READY) {
                        Log.e(TAG, "onPlayerStateChanged: lll duration = "+ simpleExoPlayer.getDuration() );
                        verticalLineMoveByGesturePlayAudioView.startPlay(currentPlayingTimeInMillis);
                    }
                } else {
                    verticalLineMoveByGesturePlayAudioView.stopPlay();
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
                Log.e(TAG, "onPlayerError: lll error = " + error.toString());
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
    }


    @Override
    protected void onResume() {
        super.onResume();
        preparePlay(AudioRecordDataSource.getInstance().getRecordFile());
        verticalLineMoveByGesturePlayAudioView.setAudioSource(AudioRecordDataSource.getInstance().decibelList);
        currentPlayingTimeInMillis = 0;
        verticalLineMoveByGesturePlayAudioView.postDelayed(new Runnable() {
            @Override
            public void run() {
                seekToPlay(0);
            }
        },300);


    }

    @Override
    protected void onPause() {
        super.onPause();
        pausePlay();
    }
}
