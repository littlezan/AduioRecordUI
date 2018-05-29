package com.example.administrator.aduiorecordui.record2mp3;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

import com.naman14.androidlame.AndroidLame;
import com.naman14.androidlame.LameBuilder;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ClassName: AudioRecordManager
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2018-05-18  17:56
 */
public class AudioRecordMp3 {

    private static final String TAG = "AudioRecordMp3";
    private final Handler handler;

    private File audioFile;
    private static final int sampleRateInHz = 8000;
    private volatile boolean isRecording = false;
    private RecordMp3Listener recordMp3Listener;
    private AudioRecord audioRecord;
    private AndroidLame androidLame;

    private ExecutorService executorService;

    public AudioRecordMp3(Handler handler, File audioFile, RecordMp3Listener recordMp3Listener) {
        this.handler = handler;
        this.audioFile = audioFile;
        this.recordMp3Listener = recordMp3Listener;
    }


    public void startAudioRecord() {
        if (isRecording) {
            return;
        }
        isRecording = true;
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        if (audioRecord != null) {
            audioRecord.release();
        }
        if (androidLame != null) {
            androidLame.close();
        }
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.execute(new StartRecordThread());

    }


    public void stopRecord() {
        isRecording = false;
    }

    public void onRelease() {
        isRecording = false;
        handler.removeCallbacksAndMessages(null);
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        if (audioRecord != null) {
            audioRecord.release();
        }
        if (androidLame != null) {
            androidLame.close();
        }
    }

    class StartRecordThread extends Thread {

        @Override
        public void run() {
            int minBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            int audioSource = MediaRecorder.AudioSource.MIC;
            audioRecord = new AudioRecord(audioSource, sampleRateInHz, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, minBufferSize * 2);
            short[] buffer = new short[sampleRateInHz * 2 * 5];
            byte[] mp3buffer = new byte[(int) (7200 + buffer.length * 2 * 1.25)];
            androidLame = new LameBuilder()
                    .setInSampleRate(sampleRateInHz)
                    .setOutChannels(1)
                    .setOutBitrate(32)
                    .setOutSampleRate(sampleRateInHz)
                    .build();
            try {
                audioRecord.startRecording();
            } catch (IllegalStateException e) {
                stopRecord();
                e.printStackTrace();
            }
            //根据开始录音判断是否有录音权限
            if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                stopRecord();
            }
            publishStartRecord();

            try {
                if (!audioFile.exists()) {
                    audioFile.createNewFile();
                }
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(audioFile, true)));
                int readSize;
                while (isRecording) {
                    readSize = audioRecord.read(buffer, 0, minBufferSize);
                    if (readSize > 0) {
                        int bytesEncoded = androidLame.encode(buffer, buffer, readSize, mp3buffer);
                        if (bytesEncoded > 0) {
                            dos.write(mp3buffer, 0, bytesEncoded);
                        }
                    }
                    publishRecordDecibel((float) getVolume(buffer, readSize));
                }
                int outputMp3buf = androidLame.flush(mp3buffer);
                if (outputMp3buf > 0) {
                    dos.write(mp3buffer, 0, outputMp3buf);
                }
                dos.close();
            } catch (Exception e) {
                Log.d(TAG, "AudioRecordMp3 run: lll e = " + e.toString());
            } finally {
                publishStopRecord();
                isRecording = false;
                androidLame.close();
                audioRecord.stop();
                audioRecord.release();
            }
        }

    }

    private void publishStopRecord() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (recordMp3Listener != null) {
                    recordMp3Listener.onStopRecord();
                }
            }
        });
    }

    private void publishStartRecord() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (recordMp3Listener != null) {
                    recordMp3Listener.onStartRecord();
                }
            }
        });
    }

    private void publishRecordDecibel(final float volume) {
        if (recordMp3Listener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    recordMp3Listener.onRecordDecibel(volume);
                }
            });

        }

    }

    private double getVolume(short[] buffer, int readSize) {
        long v = 0;
        for (short aBuffer : buffer) {
            v += aBuffer * aBuffer;
        }
        float mean = v / readSize;
        return 10 * Math.log10(mean);
    }


    public interface RecordMp3Listener {

        void onStartRecord();

        void onStopRecord();

        /**
         * 录音分贝
         *
         * @param decibel 录音分贝
         */
        void onRecordDecibel(float decibel);

    }


}
