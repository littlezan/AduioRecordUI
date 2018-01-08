package com.example.administrator.aduiorecordui;

import android.Manifest;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.administrator.aduiorecordui.record.AudioRecordView;
import com.example.administrator.aduiorecordui.record.RecordCallBack;
import com.github.piasy.rxandroidaudio.AudioRecorder;
import com.github.piasy.rxandroidaudio.PlayConfig;
import com.github.piasy.rxandroidaudio.RxAmplitude;
import com.github.piasy.rxandroidaudio.RxAudioPlayer;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.functions.Functions;
import io.reactivex.schedulers.Schedulers;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * ClassName: RecordActivity
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2017-12-14  19:59
 */
public class RecordActivity extends AppCompatActivity {

    public static final int MIN_AUDIO_LENGTH_SECONDS = 2;
    static final boolean needVoice = true;

    private static final String TAG = "RecordActivity";
    private RxPermissions mPermissions;
    private long playingTimeInMillis;

    private AudioRecorder mAudioRecorder;
    private RxAudioPlayer mRxAudioPlayer;
    private File mAudioFile;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        initAudioRecorder();
        initView();
    }

    private void initAudioRecorder() {
        mAudioRecorder = AudioRecorder.getInstance();
        mRxAudioPlayer = RxAudioPlayer.getInstance();

        mAudioFile = new File(
                Environment.getExternalStorageDirectory().getAbsolutePath()
                        + File.separator + System.nanoTime() + ".file.m4a");
        if (!mAudioFile.exists()) {
            try {
                mAudioFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mAudioRecorder.setOnErrorListener(new AudioRecorder.OnErrorListener() {
            @Override
            public void onError(int error) {
                Log.d(TAG, "onError: lll error = " + error);
            }
        });
    }

    private void initView() {
        final AudioRecordView audioRecordView = findViewById(R.id.audio_record_ui);
        Button btnStart = findViewById(R.id.btn_start);
        Button btnStop = findViewById(R.id.btn_stop);
        Button btnPlay = findViewById(R.id.btn_play);
        Button btnPause = findViewById(R.id.btn_pause);
        Button btnReset = findViewById(R.id.btn_reset);
        final EditText editText = findViewById(R.id.edit_text);
        Button btnPlayTime = findViewById(R.id.btn_play_time);


        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isPermissionsGranted = getRxPermissions().isGranted(WRITE_EXTERNAL_STORAGE)
                        && getRxPermissions().isGranted(RECORD_AUDIO);
                if (!isPermissionsGranted) {
                    getRxPermissions()
                            .request(WRITE_EXTERNAL_STORAGE, RECORD_AUDIO)
                            .subscribe(new Consumer<Boolean>() {
                                @Override
                                public void accept(Boolean granted) throws Exception {
                                    // not record first time to request permission
                                    if (granted) {
                                        Toast.makeText(getApplicationContext(), "Permission granted",
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(),
                                                "Permission not granted", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    audioRecordView.startRecord();
                }


            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioRecordView.stopRecord();
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioRecordView.startPlayRecord(playingTimeInMillis);
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioRecordView.stopPlayRecord();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioRecordView.reset();
            }
        });

        btnPlayTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(editText.getText().toString())) {
                    int millis = Integer.parseInt(editText.getText().toString());
                    audioRecordView.startPlayRecord(millis * 1000);
                }
            }
        });

        audioRecordView.setRecordCallBack(new RecordCallBack() {

            @Override
            public float getSamplePercent() {
                return new Random().nextFloat();
            }

            @Override
            public void onScroll(long centerStartTimeMillis) {

            }

            @Override
            public void onRecordCurrent(long centerStartTimeMillis, long recordTimeInMillis) {
//                Log.d(TAG, "lll onRecordCurrent: centerStartTimeMillis = " + centerStartTimeMillis + ",  inSecond = " + TimeUnit.MILLISECONDS.toSeconds(centerStartTimeMillis)
//                        + ", recordTimeInMillis = " + recordTimeInMillis + ", inSecond = " + TimeUnit.MILLISECONDS.toSeconds(recordTimeInMillis));
            }

            @Override
            public void onFinishPlayingRecord() {
                playingTimeInMillis = 0;
            }

            @Override
            public void onPlayingRecord(long playingTimeInMillis) {
//                Log.d(TAG, "lll onPlayingRecord: playingTimeInMillis = " + playingTimeInMillis);
                RecordActivity.this.playingTimeInMillis = playingTimeInMillis;
            }

            @Override
            public void onStartRecord() {
                if (needVoice) {
                    startRecord();
                }
            }

            @Override
            public void onStopRecord() {
                if (needVoice) {
                    stopRecord();
                }
            }

            @Override
            public void onFinishRecord() {
            }

            @Override
            public void onStartPlayRecord() {
                if (needVoice) {
                    play();
                }
            }

            @Override
            public void onStopPlayRecode() {
                if (needVoice) {
                    stopPlay();
                }
            }

        });
    }


    private RxPermissions getRxPermissions() {
        if (mPermissions == null) {
            mPermissions = new RxPermissions(this);
        }
        return mPermissions;
    }

    private void startRecord() {
        boolean isPermissionsGranted
                = mPermissions.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                && mPermissions.isGranted(Manifest.permission.RECORD_AUDIO);
        if (!isPermissionsGranted) {
            mPermissions
                    .request(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO)
                    .subscribe(granted -> {
                        // not record first time to request permission
                        if (granted) {
                            Toast.makeText(getApplicationContext(), "Permission granted",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Permission not granted",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }, Throwable::printStackTrace);
        } else {
            recordAfterPermissionGranted();
        }
    }

    private void recordAfterPermissionGranted() {
        boolean startRecord = mAudioRecorder.startRecord();
        Log.d(TAG, "recordAfterPermissionGranted: lll startRecord = " + startRecord);
        Observable
                .fromCallable((() -> {

                    return mAudioRecorder.prepareRecord(MediaRecorder.AudioSource.MIC,
                            MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.AudioEncoder.AAC,
                            192000, 192000, mAudioFile);
                }))
                .flatMap(b -> {
                    Log.d(TAG, "prepareRecord success");
                    Log.d(TAG, "to play audio_record_ready: " + R.raw.audio_record_ready);
                    return mRxAudioPlayer.play(
                            PlayConfig.res(getApplicationContext(), R.raw.audio_record_ready)
                                    .build());
                })
                .doOnComplete(() -> {
                    Log.d(TAG, "audio_record_ready play finished");
                    mAudioRecorder.startRecord();
                })
                .doOnNext(b -> Log.d(TAG, "startRecord success"))
                .flatMap(o -> RxAmplitude.from(mAudioRecorder))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(level -> {
                    int progress = mAudioRecorder.progress();
                    Log.d(TAG, "recordAfterPermissionGranted: lll level = " + level + ", progress = " + progress);
                }, Throwable::printStackTrace);
    }

    private void stopRecord() {
        mAudioRecorder.stopRecord();
    }

    public void play() {
        mRxAudioPlayer.play(
                PlayConfig.file(mAudioFile)
                        .streamType(AudioManager.STREAM_VOICE_CALL)
                        .build())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Functions.emptyConsumer(), Throwable::printStackTrace);
    }

    private void stopPlay() {
//        mRxAudioPlayer.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
