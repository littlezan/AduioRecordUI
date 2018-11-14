package com.example.administrator.aduiorecordui.activity.play;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.administrator.aduiorecordui.R;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.littlezan.recordui.playaudio.PlayAudioCallBack;
import com.littlezan.recordui.playaudio.playviews.VerticalLineFixedInCenterPlayAudioView;

import java.util.ArrayList;
import java.util.Random;

/**
 * ClassName: PlayAudioActivity
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2017-12-14  19:59
 */
public class PlayAudioActivity extends AppCompatActivity {

    private static final String TAG = "PlayAudioActivity";

    private long millis;
    private SimpleExoPlayer mSimpleExoPlayer;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_audio);
//        initPlayer();
        initView();
    }

//    void initPlayer() {
//        //1. 创建一个默认的 TrackSelector
//        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
//        TrackSelection.Factory videoTackSelectionFactory =
//                new AdaptiveTrackSelection.Factory(bandwidthMeter);
//        TrackSelector trackSelector =
//                new DefaultTrackSelector(videoTackSelectionFactory);
//        LoadControl loadControl = new DefaultLoadControl();
//        //2.创建ExoPlayer
//        mSimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this), trackSelector, loadControl);
//
//        Visualizer mVisualizer =  new Visualizer(mSimpleExoPlayer.getAudioSessionId());
//        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
//
//        mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
//            @Override
//            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
//
//            }
//
//            @Override
//            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
//
//            }
//        }, Visualizer.getMaxCaptureRate() / 2, true, true);
//        mVisualizer.setEnabled(true);
//
//    }

    private void initView() {
        final VerticalLineFixedInCenterPlayAudioView playAudioView = findViewById(R.id.play_audio_view);
        Button play = findViewById(R.id.play);
        Button stop = findViewById(R.id.stop);
        Button reset = findViewById(R.id.reset);
        final EditText editText = findViewById(R.id.edit_text);
        Button playMiddle = findViewById(R.id.play_middle);

        ArrayList<Float> audioSourceList = new ArrayList<>();
        fakeData(audioSourceList);
        playAudioView.setAudioSource(audioSourceList);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudioView.startPlay(millis);
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudioView.stopPlay();
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudioView.reset();
            }
        });

        playMiddle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        playAudioView.setPlayAudioCallBack(new PlayAudioCallBack() {
            @Override
            public void onPlaying(long timeInMillis) {
                Log.d(TAG, "lll onPlaying: timeInMillis = " + timeInMillis);
                millis = timeInMillis;
//                Log.d(TAG, "onPlaying: lll = " + mSimpleExoPlayer.getVolume());
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

            }
        });
    }

    int max = 100;

    private void fakeData(ArrayList<Float> audioSourceList) {
        for (int i = 0; i < max; i++) {
            audioSourceList.add(new Random().nextFloat());
        }
    }
}
