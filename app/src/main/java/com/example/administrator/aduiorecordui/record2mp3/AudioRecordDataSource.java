package com.example.administrator.aduiorecordui.record2mp3;

import com.example.administrator.aduiorecordui.model.Decibel;

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

    public List<Decibel> decibelList = new ArrayList<>();
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
