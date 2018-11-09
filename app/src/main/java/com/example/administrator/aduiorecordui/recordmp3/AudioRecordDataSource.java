package com.example.administrator.aduiorecordui.recordmp3;

import android.content.Context;

import com.example.administrator.aduiorecordui.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: AudioRecordDataSource
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2018-11-06  18:27
 */
public class AudioRecordDataSource {

    public static final String RECORD_FILE_NAME = "record.mp3";
    public static final String CROP_OUT_RECORD_FILE_NAME = "cropOutRecord.mp3";

    private File recordFile;
    private File cropOutFile;
    private File finalRecordFile;
    public List<Float> decibelList = new ArrayList<>();
    private int cropAudioIndex;
    private Listener listener;



    private AudioRecordDataSource() {
    }

    public static AudioRecordDataSource getInstance() {
        return SingletonHolder.INSTANCE;
    }




    private static class SingletonHolder {
        private static final AudioRecordDataSource INSTANCE = new AudioRecordDataSource();
    }


    public void initRecordFile(Context context) {
        try {
            recordFile = new File(FileUtils.getDiskCacheDir(context) + File.separator + RECORD_FILE_NAME);
            if (recordFile.exists()) {
                recordFile.delete();
            }
            recordFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initCropOutputFile(Context context) {
        try {
            cropOutFile = new File(FileUtils.getDiskCacheDir(context) + File.separator + CROP_OUT_RECORD_FILE_NAME);
            if (cropOutFile.exists()) {
                cropOutFile.delete();
            }
            cropOutFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getRecordFile() {
        return recordFile;
    }

    public File getCropOutFile() {
        return cropOutFile;
    }

    public File getFinalRecordFile() {
        return finalRecordFile;
    }

    public void setFinalRecordFile(File finalRecordFile) {
        this.finalRecordFile = finalRecordFile;
    }

    public void cropDecibelList(int cropIndex) {
        decibelList = decibelList.subList(0, cropIndex);
        if (listener != null) {
            listener.onCrop(cropIndex);
        }
    }


    public int getCropAudioIndex() {
        return cropAudioIndex;
    }

    public void setCropAudioIndex(int cropAudioIndex) {
        this.cropAudioIndex = cropAudioIndex;
    }


    public Listener getListener() {
        return listener;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void  onRelease() {
       decibelList.clear();
    }

    public interface Listener{
        void onCrop(int cropIndex);
    }


}
