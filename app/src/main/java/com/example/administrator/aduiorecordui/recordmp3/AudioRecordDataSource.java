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

    private static final String RECORD_DIR_NAME = "record";
    private static final String RECORD_FILE_NAME = "record.mp3";
    private static final String CROP_OUT_RECORD_FILE_NAME = "_cropOutRecord.mp3";
    private static final String CROP_OUT_RECORD_FILE_PREFIX = "v";

    private File audioFileDir;
    private File recordFile;
    private File cropOutFile;
    private int cropFileVersion;

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


    public void init(Context context) {
        audioFileDir = new File(FileUtils.getDiskCacheDir(context) + File.separator + RECORD_DIR_NAME);
        try {
            FileUtils.createDir(audioFileDir.getAbsolutePath());
            FileUtils.cleanDirectory(audioFileDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        initRecordFile();
        initNewVersionCropOutputFile();
    }

    public void initRecordFile() {
        try {
            recordFile = new File(audioFileDir, RECORD_FILE_NAME);
            if (recordFile.exists()) {
                recordFile.delete();
            }
            recordFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initNewVersionCropOutputFile() {
        try {
            cropFileVersion++;
            cropOutFile = new File(audioFileDir, CROP_OUT_RECORD_FILE_PREFIX + cropFileVersion + CROP_OUT_RECORD_FILE_NAME);
            if (cropOutFile.exists()) {
                cropOutFile.delete();
            }
            cropOutFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public File getCropOutFile() {
        if (cropOutFile == null) {
            initNewVersionCropOutputFile();
        }
        return cropOutFile;
    }

    public File getRecordFile() {
        if (recordFile == null) {
            initRecordFile();
        }
        return recordFile;
    }

    public void setFinalRecordFile(File finalRecordFile) {
        this.recordFile = finalRecordFile;
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

    public void doAfterCropRecordFile() {
        //1. 删除源文件finalRecordFile
        deleteRecordFile();
        //2. 将录音文件设置为裁剪后的文件
        AudioRecordDataSource.getInstance().setFinalRecordFile(AudioRecordDataSource.getInstance().getCropOutFile());
        //3. 创建新的裁剪文件，等待第二次裁剪
        AudioRecordDataSource.getInstance().initNewVersionCropOutputFile();

    }

    public void deleteRecordFile() {
        FileUtils.deleteFile(recordFile);
    }


    public Listener getListener() {
        return listener;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void onRelease() {
        try {
            decibelList.clear();
            cropAudioIndex = 0;
            FileUtils.cleanDirectory(audioFileDir);
            cropFileVersion = 0;
            audioFileDir = null;
            recordFile = null;
            cropOutFile = null;
            listener = null;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public interface Listener {
        void onCrop(int cropIndex);
    }


}
