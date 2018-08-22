package com.example.administrator.aduiorecordui.activity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.administrator.aduiorecordui.R;
import com.github.lassana.recorder.AudioRecorder;
import com.github.lassana.recorder.AudioRecorderBuilder;
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
import com.littlezan.recordui.recordaudio.recordview.VerticalLineMoveAudioRecordView;
import com.littlezan.recordui.recordaudio.RecordCallBack;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import io.reactivex.functions.Consumer;

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
public class MediaRecordActivity extends AppCompatActivity {

    static float DECIBEL_MAX = 90.3f;
    static float DECIBEL_MIX = 0f;

    private static final String TAG = "RecordActivity";
    private RxPermissions mPermissions;
    private long playingTimeInMillis;

    AudioRecorder mAudioRecorder;
    private SimpleExoPlayer mSimpleExoPlayer;
    public String activeRecordFileName;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        initAudioRecorder();
        initPlayer();
        initView();
    }

    private void initAudioRecorder() {
        if (mAudioRecorder == null || mAudioRecorder.getStatus() == AudioRecorder.Status.STATUS_UNKNOWN) {
            mAudioRecorder = AudioRecorderBuilder.with(this)
                    .fileName(getNextFileName())
                    .config(AudioRecorder.MediaRecorderConfig.DEFAULT)
                    .loggable()
                    .build();
        }
    }

    private String getNextFileName() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                .getAbsolutePath()
                + File.separator
                + "MY_"
                + "Record_"
                + System.currentTimeMillis()
                + ".mp4";
    }


    void initPlayer() {
        //1. 创建一个默认的 TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTackSelectionFactory);
        LoadControl loadControl = new DefaultLoadControl();
        //2.创建ExoPlayer
        mSimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this), trackSelector, loadControl);

    }

    private void initView() {
        final VerticalLineMoveAudioRecordView audioRecordView = findViewById(R.id.audio_record_ui);
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
                    if (!mAudioRecorder.isRecording()) {
                        startRecord();
                        audioRecordView.startRecord();
                    }
                }


            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecord();
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
                stopPlay();
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

        initListener(audioRecordView);
    }

    private void initListener(VerticalLineMoveAudioRecordView audioRecordView) {
        audioRecordView.setRecordCallBack(new RecordCallBack() {

            @Override
            public float getSamplePercent() {
                double ratio = mAudioRecorder.getMediaRecorder().getMaxAmplitude();
                if (ratio > 1) {
                    ratio = 20 * Math.log10(ratio);
                }

                float percent = (float) (ratio / DECIBEL_MAX);
                if (percent - 0.4f <= 0) {
                    float minX = 0.2f;
                    float maxX = 0.4f;
                    Random random = new Random();
                    percent = random.nextFloat() * (maxX - minX) + minX;
                } else if (percent - 1 >= 0) {
                    percent = 1f;
                }
                Log.d(TAG, "getSamplePercent: lll ratio = " + ratio + ", percent = " + percent);
                return percent;
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
            public void onCenterLineTime(long playingTimeInMillis) {
//                Log.d(TAG, "lll onPlayingRecord: playingTimeInMillis = " + playingTimeInMillis);
                MediaRecordActivity.this.playingTimeInMillis = playingTimeInMillis;
            }

            @Override
            public void onStartRecord() {

            }

            @Override
            public void onStopRecord() {

        }

            @Override
            public void onFinishRecord() {
            }

            @Override
            public void onStartPlayRecord(long timeMillis) {
                    play(timeMillis);
            }

            @Override
            public void onStopPlayRecode() {
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
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean granted) throws Exception {
                            // not record first time to request permission
                            if (granted) {
                                Toast.makeText(MediaRecordActivity.this.getApplicationContext(), "Permission granted",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MediaRecordActivity.this.getApplicationContext(), "Permission not granted",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            recordAfterPermissionGranted();
        }
    }

    private void recordAfterPermissionGranted() {
        mAudioRecorder.start(new AudioRecorder.OnStartListener() {
            @Override
            public void onStarted() {

            }

            @Override
            public void onException(Exception e) {

            }
        });
    }

    private void stopRecord() {
        mAudioRecorder.pause(new AudioRecorder.OnPauseListener() {
            @Override
            public void onPaused(String activeRecordFileName) {
                MediaRecordActivity.this.activeRecordFileName = activeRecordFileName;
                saveCurrentRecordToMediaDB(activeRecordFileName);
            }

            @Override
            public void onException(Exception e) {

            }
        });
    }

    public void play(long timeMillis) {
        // MediaSource代表要播放的媒体。
        MediaSource mediaSource = new ExtractorMediaSource.Factory(new FileDataSourceFactory()).createMediaSource(Uri.fromFile(new File(activeRecordFileName)));
        //Prepare the player with the source.
        mSimpleExoPlayer.prepare(mediaSource);
        mSimpleExoPlayer.seekTo(timeMillis);
        mSimpleExoPlayer.setPlayWhenReady(true);

    }

    private void stopPlay() {
        mSimpleExoPlayer.stop();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private Uri mAudioRecordUri;

    /**
     * Creates new item in the system's media database.
     *
     * @see <a href="https://github.com/android/platform_packages_apps_soundrecorder/blob/master/src/com/android/soundrecorder/SoundRecorder.java">Android Recorder source</a>
     */
    public Uri saveCurrentRecordToMediaDB(final String fileName) {
        if (mAudioRecordUri != null) {
            return mAudioRecordUri;
        }

        final Resources res = getResources();
        final ContentValues cv = new ContentValues();
        final File file = new File(fileName);
        final long current = System.currentTimeMillis();
        final long modDate = file.lastModified();
        final Date date = new Date(current);
        final String dateTemplate = res.getString(R.string.audio_db_title_format);
        final SimpleDateFormat formatter = new SimpleDateFormat(dateTemplate, Locale.getDefault());
        final String title = formatter.format(date);
        final long sampleLengthMillis = 1;
        // Lets label the recorded audio file as NON-MUSIC so that the file
        // won't be displayed automatically, except for in the playlist.
        cv.put(MediaStore.Audio.Media.IS_MUSIC, "0");

        cv.put(MediaStore.Audio.Media.TITLE, title);
        cv.put(MediaStore.Audio.Media.DATA, file.getAbsolutePath());
        cv.put(MediaStore.Audio.Media.DATE_ADDED, (int) (current / 1000));
        cv.put(MediaStore.Audio.Media.DATE_MODIFIED, (int) (modDate / 1000));
        cv.put(MediaStore.Audio.Media.DURATION, sampleLengthMillis);
        cv.put(MediaStore.Audio.Media.MIME_TYPE, "audio/*");
        cv.put(MediaStore.Audio.Media.ARTIST, res.getString(R.string.audio_db_artist_name));
        cv.put(MediaStore.Audio.Media.ALBUM, res.getString(R.string.audio_db_album_name));

        Log.d(TAG, "Inserting audio record: " + cv.toString());

        final ContentResolver resolver = getContentResolver();
        final Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Log.d(TAG, "ContentURI: " + base);

        mAudioRecordUri = resolver.insert(base, cv);
        if (mAudioRecordUri == null) {
            return null;
        }
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, mAudioRecordUri));
        return mAudioRecordUri;
    }

}
