package com.example.administrator.aduiorecordui.record2mp3;

import android.media.MediaRecorder;
import android.os.Handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ClassName: VoiceRecord1
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2018-06-25  10:22
 */
public class VoiceRecord {

    Handler handler = new Handler();


    File audioRecordFile;
    RecordListener recordListener;

    private volatile boolean isRecording = false;
    private ExecutorService executorService;
    MediaRecorder mediaRecorder;
    ArrayList<File> recordFileList = new ArrayList<>();

    public VoiceRecord(File audioRecordFile, RecordListener recordListener) {
        this.audioRecordFile = audioRecordFile;
        this.recordListener = recordListener;
    }


    public void startAudioRecord() {
        if (isRecording) {
            return;
        }
        isRecording = true;
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.execute(new StartRecordThread());
    }

    public void stopAudioRecord() {
        if (isRecording) {
            isRecording = false;
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
            }
            executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.execute(new StopRecordThread());
        }
    }

    public void deleteLastRecordFile() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.execute(new DeleteLastRecordThread());
    }

    public double getDecibel() {
        if (mediaRecorder != null) {
            int maxAmplitude = mediaRecorder.getMaxAmplitude();
            if (maxAmplitude > 1) {
                return 20 * Math.log10(maxAmplitude);
            }
        }
        return 0f;
    }

    public void release() {
        isRecording = false;
    }

    class StartRecordThread extends Thread {

        @Override
        public void run() {
            try {
                String path = makeAudioRecordFilesDir().getAbsolutePath()+File.separator;
                File recordFile = new File(path + UUID.randomUUID() + ".m4a");
                if (recordFile.exists()) {
                    recordFile.delete();
                }
                recordFile.createNewFile();
                mediaRecorder = new MediaRecorder();
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                mediaRecorder.setOutputFile(recordFile.getAbsolutePath());
                mediaRecorder.prepare();
                mediaRecorder.start();
                recordFileList.add(recordFile);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        recordListener.onStartRecord();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private File makeAudioRecordFilesDir() {
        File file = new File(audioRecordFile.getParent() + File.separator + "records");
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    class StopRecordThread extends Thread {

        @Override
        public void run() {
            mediaRecorder.stop();
            mediaRecorder.release();
            appendOutputVoiceFile();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    recordListener.onStopRecord();
                }
            });
        }

    }

    private class DeleteLastRecordThread extends Thread {


        @Override
        public void run() {
            super.run();
            try {
                if (recordFileList.size() > 0) {
                    File file = recordFileList.get(recordFileList.size() - 1);
                    file.delete();
                    recordFileList.remove(recordFileList.size() - 1);
                    appendOutputVoiceFile();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 合并录音
     */
    private void appendOutputVoiceFile() {

        // 创建音频文件,合并的文件放这里
        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream(audioRecordFile);
            // list里面为暂停录音 所产生的 几段录音文件的名字，中间几段文件的减去前面的6个字节头文件
            for (int i = 0; i < recordFileList.size(); i++) {
                File file = recordFileList.get(i);
                try {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    byte[] myByte = new byte[fileInputStream.available()];
                    // 文件长度
                    int length = myByte.length;
                    // 头文件
                    if (i == 0) {
                        while (fileInputStream.read(myByte) != -1) {
                            fileOutputStream.write(myByte, 0, length);
                        }
                    } else {
                        while (fileInputStream.read(myByte) != -1) {
                            fileOutputStream.write(myByte, 6, length - 6);
                        }
                    }// 之后的文件，去掉头文件就可以了
                    fileOutputStream.flush();
                    fileInputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 结束后关闭流
        try {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public interface RecordListener {

        void onStartRecord();

        void onStopRecord();

    }
}
