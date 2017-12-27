package com.example.administrator.aduiorecordui;

import android.content.Context;
import android.media.AudioManager;
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

import com.example.administrator.aduiorecordui.record.AudioRecord;
import com.example.administrator.aduiorecordui.record.RecordCallBack;
import com.github.piasy.audioprocessor.AudioProcessor;
import com.github.piasy.rxandroidaudio.StreamAudioPlayer;
import com.github.piasy.rxandroidaudio.StreamAudioRecorder;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
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

    static final boolean needVoice = false;
    static final int BUFFER_SIZE = 2048;

    private static final String TAG = "RecordActivity";
    private long playingTimeInMillis;


    private RxPermissions mPermissions;
    private StreamAudioRecorder mStreamAudioRecorder;
    private StreamAudioPlayer mStreamAudioPlayer;
    private AudioProcessor mAudioProcessor;
    private FileOutputStream mFileOutputStream;
    private File mOutputFile;
    private byte[] mBuffer;
    private boolean mIsRecording = false;
    private float mRatio = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        initAudioRecorder();
        initView();

    }

    private void initAudioRecorder() {
        mOutputFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                File.separator + System.nanoTime() + ".stream.m4a");
        try {
            mOutputFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mStreamAudioRecorder = StreamAudioRecorder.getInstance();
        mStreamAudioPlayer = StreamAudioPlayer.getInstance();
        mAudioProcessor = new AudioProcessor(BUFFER_SIZE);
        mBuffer = new byte[BUFFER_SIZE];

    }

    private void initView() {
        final AudioRecord audioRecordUi = findViewById(R.id.audio_record_ui);
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
                    audioRecordUi.startRecord();
                    mIsRecording = true;
                }


            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioRecordUi.stopRecord();
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioRecordUi.startPlayRecord(playingTimeInMillis);
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioRecordUi.stopPlayRecord();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioRecordUi.reset();
            }
        });

        btnPlayTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(editText.getText().toString())) {
                    int millis = Integer.parseInt(editText.getText().toString());
                    audioRecordUi.startPlayRecord(millis * 1000);
                }
            }
        });

        audioRecordUi.setRecordCallBack(new RecordCallBack() {

            @Override
            public float getSamplePercent() {
                return new Random().nextFloat();
            }

            @Override
            public void onScroll(long centerStartTimeMillis) {

            }

            @Override
            public void onRecordCurrent(long centerStartTimeMillis, long recordTimeInMillis) {
                Log.d(TAG, "lll onRecordCurrent: centerStartTimeMillis = " + centerStartTimeMillis + ",  inSecond = " + TimeUnit.MILLISECONDS.toSeconds(centerStartTimeMillis)
                        + ", recordTimeInMillis = " + recordTimeInMillis + ", inSecond = " + TimeUnit.MILLISECONDS.toSeconds(recordTimeInMillis));
            }

            @Override
            public void onFinishPlayingRecord() {
                playingTimeInMillis = 0;
            }

            @Override
            public void onPlayingRecord(long playingTimeInMillis) {
                Log.d(TAG, "lll onPlayingRecord: playingTimeInMillis = " + playingTimeInMillis);
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
        try {
            mFileOutputStream = new FileOutputStream(mOutputFile);
            mStreamAudioRecorder.start(new StreamAudioRecorder.AudioDataCallback() {
                @Override
                public void onAudioData(byte[] data, int size) {
                    if (mFileOutputStream != null) {
                        try {
                            mFileOutputStream.write(data, 0, size);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onError() {
                    mIsRecording = false;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void stopRecord() {
        mStreamAudioRecorder.stop();
        try {
            mFileOutputStream.close();
            mFileOutputStream = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void play() {
        Context context = getApplicationContext();
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setSpeakerphoneOn(true);

        Observable.just(mOutputFile)
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<File>() {
                    @Override
                    public void accept(File file) throws Exception {
                        try {
                            mStreamAudioPlayer.init();
                            FileInputStream inputStream = new FileInputStream(file);
                            int read;
                            while ((read = inputStream.read(mBuffer)) > 0) {
                                mStreamAudioPlayer.play(mBuffer, read);
                            }
                            inputStream.close();
                            mStreamAudioPlayer.release();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void stopPlay() {
        mStreamAudioPlayer.release();
    }
}
