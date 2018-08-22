package com.example.administrator.aduiorecordui.activity;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.example.administrator.aduiorecordui.record2mp3.AudioRecordMp3;
import com.example.administrator.aduiorecordui.util.FileUtils;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.tbruyelle.rxpermissions2.RxPermissions;

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
public class BaseAudioRecordActivity extends AppCompatActivity {

    public File recordFile;
    public static final String RECORD_FILE_NAME = "record.mp3";
    public SimpleExoPlayer simpleExoPlayer;

    public enum RecordStatus {
        /**
         * 默认状态
         */
        None,
        Recording,
        PauseRecording,
        FinishRecording,
        Playing,
        PausePlaying,;
    }

    public RxPermissions permissions;
    public RecordStatus recordStatus;
    public boolean isPermissionsGranted;

    private AudioRecordMp3 audioRecordMp3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissions = new RxPermissions(this);
        recordStatus = RecordStatus.None;
        isPermissionsGranted = permissions.isGranted(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) && permissions.isGranted(android.Manifest.permission.RECORD_AUDIO);
        //获取音频服务
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            //打开麦克风
            audioManager.setMicrophoneMute(false);
        }
        initRecordFile();
        initPlayer();
    }

    private void initRecordFile() {
        try {
            recordFile = new File(FileUtils.getDiskCacheDir(this) + File.separator + RECORD_FILE_NAME);
            if (recordFile.exists()) {
                recordFile.delete();
            }
            recordFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
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

    }
}
