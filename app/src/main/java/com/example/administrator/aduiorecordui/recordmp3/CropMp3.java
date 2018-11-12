package com.example.administrator.aduiorecordui.recordmp3;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * ClassName: CropMp3
 * Description: 裁剪mp3
 *
 * @author 彭赞
 * @version 1.0
 * @since 2018-11-09  09:56
 */
public class CropMp3 {
    private static final String TAG = "CropMp3";

    private static final int SAMPLE_SIZE = 3000;

    private CropCallback cropCallback;

    private Handler handler = new Handler(Looper.getMainLooper());


    public void setCropCallback(CropCallback cropCallback) {
        this.cropCallback = cropCallback;
    }

    public void startCrop(long endTimeInMillis) {
        ExecutorManager.getInstance().getExecutorService().execute(new CropThread(endTimeInMillis));
    }

    class CropThread extends Thread {


        private final long endTimeInMillis;

        CropThread(long endTimeInMillis) {
            this.endTimeInMillis = endTimeInMillis;
        }

        @Override
        public void run() {
            //1. 将源文件裁剪成 新文件
            clip(AudioRecordDataSource.getInstance().getRecordFile().getAbsolutePath(), AudioRecordDataSource.getInstance().getCropOutFile().getAbsolutePath(), endTimeInMillis * 1000);
            AudioRecordDataSource.getInstance().doAfterCropRecordFile();
            if (cropCallback != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        cropCallback.onCropFinish();
                    }
                });
            }
        }

        void clip(String inputPath, String outputPath, long endTimeInUs) {
            MediaExtractor extractor = null;
            BufferedOutputStream outputStream = null;
            try {
                extractor = new MediaExtractor();
                extractor.setDataSource(inputPath);
                int track = getAudioTrack(extractor);
                if (track < 0) {
                    return;
                }
                //选择音频轨道
                extractor.selectTrack(track);
                //音频文件的长度
                outputStream = new BufferedOutputStream(new FileOutputStream(outputPath), SAMPLE_SIZE);
                //跳至开始裁剪位置
                extractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
                while (true) {
                    ByteBuffer buffer = ByteBuffer.allocate(SAMPLE_SIZE);
                    int sampleSize = extractor.readSampleData(buffer, 0);
                    long timeStamp = extractor.getSampleTime();
                    // >= 1000000是要裁剪停止和指定的裁剪结尾不小于1秒，否则可能产生需要9秒音频
                    //裁剪到只有8.6秒，大多数音乐播放器是向下取整，这样对于播放器变成了8秒，
                    // 所以要裁剪比9秒多一秒的边界
                    if (timeStamp >=endTimeInUs ) {
                        break;
                    }
                    if (sampleSize <= 0) {
                        break;
                    }
                    byte[] buf = new byte[sampleSize];
                    buffer.get(buf, 0, sampleSize);
                    //写入文件
                    outputStream.write(buf);
                    //音轨数据往前读
                    extractor.advance();
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (extractor != null) {
                    extractor.release();
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        /**
         * 获取音频数据轨道
         *
         * @param extractor MediaExtractor
         * @return MediaExtractor
         */
        private int getAudioTrack(MediaExtractor extractor) {
            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("audio")) {
                    return i;
                }
            }
            return -1;
        }
    }


    public interface CropCallback {

        /**
         * 开始裁剪
         */
        void onCropFinish();
    }


}
