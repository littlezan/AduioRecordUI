package com.example.administrator.aduiorecordui.record2mp3;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.naman14.androidlame.AndroidLame;
import com.naman14.androidlame.LameBuilder;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

    private final Handler handler = new Handler(Looper.getMainLooper());

    /**
     * 每次暂停时文件的长度
     */
    private ArrayList<Long> fileLengthList = new ArrayList<>();


    private long currentRecordFileLength = 0;

    private ArrayList<File> audioFileList = new ArrayList<>();
    private File currentRecordFile;
    private String initFilePath;

    private static final int SAMPLE_RATE_IN_HZ = 8000;
    private volatile boolean isRecording = false;
    private RecordMp3Listener recordMp3Listener;
    private AudioRecord audioRecord;
    private AndroidLame androidLame;

    private ExecutorService executorService;
    private int minBufferSize;

    public AudioRecordMp3(File audioFile, RecordMp3Listener recordMp3Listener) {
        this.currentRecordFile = audioFile;
        this.recordMp3Listener = recordMp3Listener;
        minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        initFilePath = currentRecordFile.getAbsolutePath();
        initRecordFile();
    }

    private void initRecordFile() {
        try {
            if (currentRecordFile == null) {
                currentRecordFile = new File(initFilePath);
            }
            if (!currentRecordFile.exists()) {
                currentRecordFile.createNewFile();
            }
        } catch (IOException e) {
        }
    }


    public void startAudioRecord() {
        if (isRecording) {
            return;
        }
        isRecording = true;
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        initAudioRecord();
        executorService = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.AbortPolicy());
        executorService.execute(new RecordThread());

    }

    private void initAudioRecord() {
        if (audioRecord == null) {
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize * 2);
            androidLame = new LameBuilder()
                    .setInSampleRate(SAMPLE_RATE_IN_HZ)
                    .setOutChannels(1)
                    .setOutBitrate(32)
                    .setOutSampleRate(SAMPLE_RATE_IN_HZ)
                    .build();
        }
    }


    public void stopRecord() {
        isRecording = false;
    }

    public void deleteLastRecord() {

        executorService = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.AbortPolicy());
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    //上一段录音长度
                    Long lastElement = null;
                    for (int i = fileLengthList.size() - 1; i >= 0; i--) {
                        Long aLong = fileLengthList.get(i);
                        if (aLong < currentRecordFileLength) {
                            lastElement = aLong;
                            break;
                        }
                    }
                    if (lastElement != null && lastElement > 0) {
                        splitFile(lastElement);
                        fileLengthList.subList(fileLengthList.indexOf(lastElement)+1, fileLengthList.size()).clear();
                        currentRecordFileLength = getFinalRecordFile().length();
                    } else {
                        deleteAllRecordFile();
                        fileLengthList.clear();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (recordMp3Listener != null) {
                            recordMp3Listener.onDeletedLastRecord();
                        }
                    }
                });
            }
        });

    }

    private void deleteAllRecordFile() {
        for (File file : audioFileList) {
            if (file.exists()) {
                file.delete();
            }
        }
        currentRecordFile = null;
    }

    private void splitFile(long actualBlockSize) throws IOException {
        //源文件
        File src = currentRecordFile;
        //得到第几块的路径       List容器取出块路径
        //目标文件
        File destFile = new File(currentRecordFile.getParent() + File.separator + UUID.randomUUID() + "_dest.mp3");

        //2、选择流 //输入流
        RandomAccessFile raf;
        //输出流
        BufferedOutputStream bos = null;
        try {
            raf = new RandomAccessFile(src, "r");
            bos = new BufferedOutputStream(new FileOutputStream(destFile));
            //3、读取文件
            raf.seek(0);
            //4、缓冲区
            byte[] flush = new byte[1024];
            int len;
            while (-1 != (len = raf.read(flush))) {
                //写出
                if (actualBlockSize - len >= 0) {
                    //判断是否足够 //写出
                    bos.write(flush, 0, len);
                    //剩余量
                    actualBlockSize -= len;
                } else {
                    //读取每一块实际大小的最后一小部分   最后一次写出
                    bos.write(flush, 0, (int) actualBlockSize);
                    break;//每个block最后一部分读取完之后，一定要break，否则就会继续读取
                }
            }
            currentRecordFile = destFile;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                bos.close();
            }
        }
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
        deleteAllRecordFile();
    }

    class RecordThread extends Thread {

        @Override
        public void run() {
            short[] buffer = new short[SAMPLE_RATE_IN_HZ * 2 * 5];
            byte[] mp3buffer = new byte[(int) (7200 + buffer.length * 2 * 1.25)];
            try {
                audioRecord.startRecording();
            } catch (IllegalStateException e) {
                stopRecord();
                e.printStackTrace();
            }
            //根据开始录音判断是否有录音权限
            if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                stopRecord();
            } else {
                publishStartRecord();
            }
            try {
                File finalRecordFile = getFinalRecordFile();
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(finalRecordFile, true)));
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
                if (!fileLengthList.contains(finalRecordFile.length())) {
                    fileLengthList.add(finalRecordFile.length());
                    currentRecordFileLength = finalRecordFile.length();
                }
                if (!audioFileList.contains(finalRecordFile)) {
                    audioFileList.add(finalRecordFile);
                }
            } catch (Exception e) {
                Log.d(TAG, "AudioRecordMp3 run: lll e = " + e.toString());
            } finally {
                isRecording = false;
                audioRecord.stop();
                publishStopRecord();
            }
        }
    }


    @NonNull
    public File getFinalRecordFile() {
        initRecordFile();
        return currentRecordFile;
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

        /**
         * 开始录音
         */
        void onStartRecord();

        /**
         * 暂停录音
         */
        void onStopRecord();

        /**
         * 删除上次录音
         */
        void onDeletedLastRecord();

        /**
         * 录音分贝
         *
         * @param decibel 录音分贝
         */
        void onRecordDecibel(float decibel);

    }


}
