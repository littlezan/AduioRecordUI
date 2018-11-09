package com.example.administrator.aduiorecordui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.administrator.aduiorecordui.R;
import com.example.administrator.aduiorecordui.model.Decibel;
import com.example.administrator.aduiorecordui.recordmp3.AudioRecordMp3;
import com.example.administrator.aduiorecordui.util.FastClickLimitUtil;
import com.example.administrator.aduiorecordui.util.FileUtils;
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
import com.littlezan.recordui.recordaudio.RecordCallBack;
import com.littlezan.recordui.recordaudio.recordviews.VerticalLineMoveAudioRecordView;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;


/**
 * ClassName: RecordAudioFragment
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2018-05-10  15:23
 */
public class RecordAudioFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "RecordAudioFragment";

    public static final String RECORD_FILE_NAME = "record.mp3";
    /**
     * 最长录音时间 分钟
     */
    public static final int RECORD_TIME_IN_MINUTES = 5;
    public static final int MAX_RECORD_DECIBEL = 80;
    public static final int MIN_RECORD_DECIBEL = 35;

    /**
     * 最短录制时长 3秒
     */
    public static final int MIN_RECORD_SECOND = 3;
    private RecordStatus recordStatus;
    private RecordCallBack recordCallBack;
    private long recordTimeInMillis;
    private boolean isPermissionsGranted;
    private AudioRecordMp3 audioRecordMp3;
    private float recordDecibel;
    private File recordFile;


    enum RecordStatus {
        None,
        Recording,
        PauseRecording,
        FinishRecording,
        Playing,
        PausePlaying,;
    }

    VerticalLineMoveAudioRecordView audioRecordView;
    TextView tvHint;
    TextView tvDuration;
    TextView tvDelete;
    TextView tvRecord;
    TextView tvPlay;

    private SimpleExoPlayer simpleExoPlayer;
    private long centerLineTime;

    private RxPermissions permissions;

    private ArrayList<Decibel> decibelList = new ArrayList<>();


    public static RecordAudioFragment newInstance() {
        return new RecordAudioFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissions = new RxPermissions(getActivity());
        recordStatus = RecordStatus.None;
        isPermissionsGranted = permissions.isGranted(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) && permissions.isGranted(android.Manifest.permission.RECORD_AUDIO);
        //获取音频服务
        AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            //打开麦克风
            audioManager.setMicrophoneMute(false);
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutResId(), container, false);
    }

    protected int getLayoutResId() {
        return R.layout.fragment_record_audio;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        audioRecordView = view.findViewById(R.id.audio_record_view);
        tvHint = view.findViewById(R.id.tv_hint);
        tvDuration = view.findViewById(R.id.tv_duration);
        tvDelete = view.findViewById(R.id.tv_delete);
        tvRecord = view.findViewById(R.id.tv_record);
        tvPlay = view.findViewById(R.id.tv_play);

        tvDelete.setOnClickListener(this);
        tvRecord.setOnClickListener(this);
        tvPlay.setOnClickListener(this);
        initView();
    }




    @Override
    public void onResume() {
        super.onResume();
        audioRecordView.setRecordCallBack(recordCallBack);
    }

    @Override
    public void onPause() {
        super.onPause();
        audioRecordMp3.stopRecord();
        audioRecordView.stopPlayRecord();
        audioRecordView.setRecordCallBack(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (audioRecordView != null) {
            audioRecordView.setRecordCallBack(null);
        }
        if (simpleExoPlayer != null) {
            stopPlay();
            simpleExoPlayer.release();
        }
    }

    protected void initView() {
        initRecordFile();
        initAudioRecorder();
        initPlayer();
        initListener();
        renderUIByRecordStatus();
    }

    @Override
    public void onClick(View view) {
        if (FastClickLimitUtil.isFastClick()) {
            return;
        }
        int viewId = view.getId();
        if (viewId == R.id.tv_delete) {
            //删除
            initRecordFile();
            recordStatus = RecordStatus.None;
            audioRecordMp3.onRelease();
            audioRecordView.reset();
            renderUIByRecordStatus();
        } else if (viewId == R.id.tv_record) {
            //录音
            switch (recordStatus) {
                case None:
                case PauseRecording:
                case PausePlaying:
                    startRecord();
                    break;
                case Recording:
                    stopRecord();
                    break;
                case Playing:
                    break;
            }
        } else if (viewId == R.id.tv_play) {
            //播放
            switch (recordStatus) {
                case None:
                case Recording:
                    break;
                case PauseRecording:
                case PausePlaying:
                case FinishRecording:
                    //小于300毫秒从头开始播放
                    if (Math.abs(centerLineTime - recordTimeInMillis) < 300) {
                        centerLineTime = 0;
                    }
                    audioRecordView.startPlayRecord(centerLineTime);
                    break;
                case Playing:
                    audioRecordView.stopPlayRecord();
                    break;
                default:
                    break;
            }
        }
    }


    public void renderUIByRecordStatus() {
        if (tvHint == null || tvDelete == null || tvPlay == null || tvRecord == null) {
            return;
        }

        tvRecord.setEnabled(recordTimeInMillis <= TimeUnit.MINUTES.toMillis(RECORD_TIME_IN_MINUTES) - 300);
        tvDelete.setEnabled(true);
        tvPlay.setEnabled(true);

        switch (recordStatus) {
            case None:
                tvRecord.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.selector_record_continue, 0, 0);
                tvRecord.setText("点击录制");
                tvHint.setVisibility(View.VISIBLE);
                tvDelete.setVisibility(View.INVISIBLE);
                tvPlay.setVisibility(View.INVISIBLE);
                tvDuration.setText(R.string.record_time_init);
                break;
            case Recording:
                tvRecord.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.release_end, 0, 0);
                tvRecord.setText("点击停止");
                tvPlay.setEnabled(false);
                tvDelete.setEnabled(false);
                break;
            case PausePlaying:
            case PauseRecording:
                tvRecord.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.selector_record_continue, 0, 0);
                tvRecord.setText("继续录制");
                tvPlay.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.selector_record_play, 0, 0);
                tvDelete.setVisibility(View.VISIBLE);
                tvPlay.setVisibility(View.VISIBLE);
                break;
            case Playing:
                tvRecord.setEnabled(false);
                tvDelete.setEnabled(false);
                tvPlay.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.release_suspend, 0, 0);
                tvDelete.setVisibility(View.VISIBLE);
                tvPlay.setVisibility(View.VISIBLE);
                break;
            case FinishRecording:
                tvRecord.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.selector_record_continue, 0, 0);
                tvRecord.setEnabled(false);
                break;
        }
        setCanUseAudio();
    }

    private void setCanUseAudio() {
        if (fragmentInteraction != null) {
            boolean status = recordStatus != RecordStatus.Recording;
            boolean length = TimeUnit.SECONDS.toMillis(MIN_RECORD_SECOND) < recordTimeInMillis;
            fragmentInteraction.canUseAudio(status && length);
        }
    }


    private void initAudioRecorder() {

        audioRecordMp3 = new AudioRecordMp3(recordFile, new AudioRecordMp3.RecordMp3Listener() {
            @Override
            public void onStartRecord() {
                audioRecordView.startRecord();
                recordStatus = RecordStatus.Recording;
                renderUIByRecordStatus();
            }

            @Override
            public void onStopRecord() {
                if (audioRecordView == null) {
                    return;
                }
                audioRecordView.stopRecord();
                recordStatus = RecordStatus.PauseRecording;
                renderUIByRecordStatus();
                setCanUseAudio();
            }

            @Override
            public void onDeletedLastRecord() {
                audioRecordView.deleteLastRecord();
            }

            @Override
            public void onRecordDecibel(float decibel) {
                recordDecibel = decibel;
            }

        });
    }

    private void initRecordFile() {
        try {
            recordFile = new File(FileUtils.getDiskCacheDir(getContext())+File.separator+RECORD_FILE_NAME);
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
        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(getContext()), trackSelector, loadControl);
    }

    private void initListener() {
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
                RecordAudioFragment.this.recordTimeInMillis = recordTimeInMillis;
                if (tvDuration != null) {
                    tvDuration.setText(formatDuration(TimeUnit.MILLISECONDS.toSeconds(recordTimeInMillis)));
                }
                if (tvHint != null) {
                    if (recordTimeInMillis > TimeUnit.SECONDS.toMillis(MIN_RECORD_SECOND)) {
                        tvHint.setVisibility(View.GONE);
                    } else {
                        tvHint.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFinishPlayingRecord() {
                recordStatus = RecordStatus.PausePlaying;
                centerLineTime = 0;
                renderUIByRecordStatus();
            }

            @Override
            public void onCenterLineTime(long centerLineTime) {
                RecordAudioFragment.this.centerLineTime = centerLineTime;

            }

            @Override
            public void onStartRecord() {
            }

            @Override
            public void onStopRecord() {
            }

            @Override
            public void onFinishRecord() {
                recordStatus = RecordStatus.FinishRecording;
                renderUIByRecordStatus();
                setCanUseAudio();
                if (tvDuration != null) {
                    tvDuration.setText(formatDuration(TimeUnit.MINUTES.toSeconds(RECORD_TIME_IN_MINUTES)));
                }
            }

            @Override
            public void onStartPlayRecord(long timeMillis) {
                recordStatus = RecordStatus.Playing;
                play(timeMillis);
                renderUIByRecordStatus();
            }

            @Override
            public void onStopPlayRecode() {
                recordStatus = RecordStatus.PausePlaying;
                stopPlay();
                renderUIByRecordStatus();
            }

        };
    }

    public static String formatDuration(long seconds) {
        return String.format(Locale.getDefault(),
                "%02d:%02d:%02d",
                seconds / 3600,
                (seconds % 3600) / 60,
                seconds % 60);
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

    public void play(long timeMillis) {
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


    /**
     * 音频文件
     *
     * @return 音频文件
     */
    public String getActiveRecordFilePath() {
        String filePath = null;
        if (recordFile != null && recordFile.exists()) {
            filePath = recordFile.getAbsolutePath();
        }
        return filePath;
    }

    /**
     * 录音采样频率
     *
     * @return 采样频率
     */
    public int getRecordSamplingFrequency() {
        return audioRecordView.getRecordSamplingFrequency();
    }

    /**
     * 录音时长
     *
     * @return 录音时长
     */
    public long getRecordTimeInMillis() {
        return recordTimeInMillis;
    }

    /**
     * 获取采样分贝点
     *
     * @return 采样分贝点
     */
    public ArrayList<Decibel> getDecibelList() {
        return decibelList;
    }

    /**
     * 点击返回按钮
     */
    public void onFinishRelease() {
        audioRecordView.setRecordCallBack(null);
        stopRecord();
        audioRecordView.reset();
        audioRecordMp3.onRelease();
        //删除
        if (recordFile != null && recordFile.exists()) {
            recordFile.delete();
        }
    }

    public boolean hasRecorded() {
        //删除
        return recordTimeInMillis > 0;
    }


    FragmentInteraction fragmentInteraction;

    public void setFragmentInteraction(FragmentInteraction fragmentInteraction) {
        this.fragmentInteraction = fragmentInteraction;
    }

    public interface FragmentInteraction {

        /**
         * 音频是否可用
         *
         * @param canUseAudio 是否可用
         */
        void canUseAudio(boolean canUseAudio);

    }


}
