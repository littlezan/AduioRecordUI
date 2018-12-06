package com.example.administrator.aduiorecordui.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.analytics.DefaultAnalyticsListener;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.FileDataSourceFactory;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;

import java.io.File;
import java.io.IOException;

/**
 * ClassName: BaseAudioRecordActivity
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2018-08-22  14:31
 */
public abstract class BasePlayerActivity extends AppCompatActivity {

    private static final String TAG = "BaseAudioRecordActivity";
    public static final int AUDIO_PERMISSION_REQUEST_CODE = 102;
    public static final String[] RECORD_AUDIO_PERMS = {
            Manifest.permission.RECORD_AUDIO
    };

    public SimpleExoPlayer simpleExoPlayer;

    protected boolean permissionGranted = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPermissions();
        initPlayer();
    }

    protected void initPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(RECORD_AUDIO_PERMS, AUDIO_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case AUDIO_PERMISSION_REQUEST_CODE:
                permissionGranted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            default:
                break;
        }
    }

    private void initPlayer() {
        //1. 创建一个默认的 TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTackSelectionFactory);
        LoadControl loadControl = new DefaultLoadControl();
        //2. 创建ExoPlayer
        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this), trackSelector, loadControl);
        simpleExoPlayer.getAudioSessionId();
        Log.e(TAG, "initPlayer: lll simpleExoPlayer.getAudioSessionId()  = " + simpleExoPlayer.getAudioSessionId() );
        simpleExoPlayer.addAnalyticsListener(new DefaultAnalyticsListener() {
            @Override
            public void onAudioSessionId(EventTime eventTime, int audioSessionId) {
                Log.e(TAG, "onAudioSessionId: lll audioSessionId = "+ audioSessionId );
                audioSessionId(audioSessionId);
            }

            @Override
            public void onLoadError(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData, IOException error, boolean wasCanceled) {
                Log.e(TAG, "onLoadError: lll");
            }

            @Override
            public void onPlayerError(EventTime eventTime, ExoPlaybackException error) {
                Log.e(TAG, "onPlayerError: lll = "+ error.getMessage());
            }

            @Override
            public void onPlayerStateChanged(EventTime eventTime, boolean playWhenReady, int playbackState) {
                playerStateChanged(playWhenReady);
            }
        });
    }

    protected void audioSessionId(int audioSessionId) {

    }

    protected void playerStateChanged(boolean playWhenReady) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (simpleExoPlayer != null) {
            simpleExoPlayer.release();
            simpleExoPlayer = null;
        }
    }

    protected void preparePlay(File file) {
        if (file == null) {
            return;
        }
        // MediaSource代表要播放的媒体。
        Log.e(TAG, "startPlay: lll file path = " + file.getAbsolutePath());
        MediaSource mediaSource = new ExtractorMediaSource.Factory(new FileDataSourceFactory()).createMediaSource(Uri.fromFile(file));
        //Prepare the player with the source.
        simpleExoPlayer.prepare(mediaSource);
    }

    protected void preparePlay(int rawResourceId) {

        final RawResourceDataSource rawResourceDataSource = new RawResourceDataSource(this);
        DataSpec dataSpec = new DataSpec(RawResourceDataSource.buildRawResourceUri(rawResourceId));
        try {
            rawResourceDataSource.open(dataSpec);
            DataSource.Factory factory = new DataSource.Factory() {
                @Override
                public DataSource createDataSource() {
                    return rawResourceDataSource;
                }
            };
            MediaSource videoSource = new ExtractorMediaSource.Factory(factory).createMediaSource(rawResourceDataSource.getUri());
            simpleExoPlayer.prepare(videoSource);
        } catch (RawResourceDataSource.RawResourceDataSourceException e) {
            e.printStackTrace();
        }
    }


    protected void seekToPlay(long timeInMillis) {
        simpleExoPlayer.seekTo(timeInMillis);
        simpleExoPlayer.setPlayWhenReady(true);
    }

    protected void pausePlay() {
        simpleExoPlayer.setPlayWhenReady(false);
    }

    protected void resumePlay() {
        simpleExoPlayer.seekTo(simpleExoPlayer.getContentPosition());
        simpleExoPlayer.setPlayWhenReady(false);
    }

    protected void stopPlay() {
        simpleExoPlayer.stop(true);
    }


}
