package com.example.administrator.aduiorecordui.activity.record.verticallinemove.second;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.administrator.aduiorecordui.R;
import com.example.administrator.aduiorecordui.model.Decibel;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.FileDataSourceFactory;
import com.littlezan.recordui.playaudio.PlayAudioCallBack;
import com.littlezan.recordui.playaudio.playviews.VerticalLineFixedInCenterPlayAudioView;

import java.io.File;
import java.util.ArrayList;

/**
 * ClassName: AudioRecordPreviewActivity
 * Description: 录音播放
 *
 * @author 彭赞
 * @version 1.0
 * @since 2018-05-29  14:55
 */
public class AudioRecordPreviewActivity extends AppCompatActivity {


    public static final String EXTRA_KEY_ACTIVE_RECORD_FILE_NAME = "extra_key_active_record_file_name";
    public static final String EXTRA_KEY_DECIBEL_LIST = "extra_key_decibel_list";
    public static final String EXTRA_KEY_RECORD_SAMPLING_FREQUENCY = "extra_key_record_sampling_frequency";
    public static final String EXTRA_KEY_RECORD_TIME_IN_MILLIS = "extra_key_record_time_in_millis";
    private RelativeLayout rlContainer;
    private TextView tvPlay;
    private VerticalLineFixedInCenterPlayAudioView playView;
    private TextView tvAuthPass;
    private TextView tvDuration;

    String activeRecordFileName;
    ArrayList<Decibel> decibelList;
    int recordSamplingFrequency;
    long recordTimeInMillis;

    SimpleExoPlayer simpleExoPlayer;


    public static void start(Context context, String activeRecordFileName, ArrayList<Decibel> decibelList, int recordSamplingFrequency, long recordTimeInMillis) {
        Intent intent = new Intent(context, AudioRecordPreviewActivity.class);
        intent.putExtra(EXTRA_KEY_ACTIVE_RECORD_FILE_NAME, activeRecordFileName);
        intent.putParcelableArrayListExtra(EXTRA_KEY_DECIBEL_LIST, decibelList);
        intent.putExtra(EXTRA_KEY_RECORD_SAMPLING_FREQUENCY, recordSamplingFrequency);
        intent.putExtra(EXTRA_KEY_RECORD_TIME_IN_MILLIS, recordTimeInMillis);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_audio_record_preview);
        parseIntent(getIntent());
        initAudioPlayer();
        initView();
    }

    private void initAudioPlayer() {
        //1. 创建一个默认的 TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTackSelectionFactory);
        LoadControl loadControl = new DefaultLoadControl();
        //2.创建ExoPlayer
        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this), trackSelector, loadControl);
    }

    private void parseIntent(Intent intent) {
        if (intent != null) {
            activeRecordFileName = intent.getStringExtra(EXTRA_KEY_ACTIVE_RECORD_FILE_NAME);
            decibelList = intent.getParcelableArrayListExtra(EXTRA_KEY_DECIBEL_LIST);
            recordSamplingFrequency = intent.getIntExtra(EXTRA_KEY_RECORD_SAMPLING_FREQUENCY, 5);
            recordTimeInMillis = intent.getLongExtra(EXTRA_KEY_RECORD_TIME_IN_MILLIS, 0);
        }
    }

    private void initView() {
        rlContainer = findViewById(R.id.rl_container);
        tvPlay = findViewById(R.id.tvPlay);
        playView = findViewById(R.id.playView);
        tvAuthPass = findViewById(R.id.tvAuthPass);
        tvDuration = findViewById(R.id.tvDuration);

        ArrayList<Float> floatArrayList = new ArrayList<>();
        for (Decibel decibel : decibelList) {
            floatArrayList.add(decibel.percent);
        }
        playView.setAudioSourceFrequency(recordSamplingFrequency);
        playView.setAudioSource(floatArrayList);
        playView.setPlayAudioCallBack(new PlayAudioCallBack() {
            @Override
            public void onPlaying(long timeInMillis) {
                tvDuration.setText(DateUtils.formatElapsedTime(timeInMillis));
            }

            @Override
            public void onStartPlay(long timeInMillis) {
                play(timeInMillis);
            }

            @Override
            public void onPausePlay() {
                stopPlay();
            }

            @Override
            public void onResumePlay() {

            }

            @Override
            public void onPlayingFinish() {
                stopPlay();
            }

            @Override
            public void onCrop(int cropIndex, long remainTimeInMillis) {

            }
        });

        tvPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playView.startPlay(0);
            }
        });
    }

    public void play(long timeMillis) {
        if (timeMillis < 0) {
            timeMillis = 0;
        }
        // MediaSource代表要播放的媒体。
        MediaSource mediaSource = new ExtractorMediaSource.Factory(new FileDataSourceFactory()).createMediaSource(Uri.fromFile(new File(activeRecordFileName)));
        //Prepare the player with the source.
        simpleExoPlayer.prepare(mediaSource);
        simpleExoPlayer.seekTo(timeMillis);
        simpleExoPlayer.setPlayWhenReady(true);

    }

    private void stopPlay() {
        simpleExoPlayer.stop();
    }
}
