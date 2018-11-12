package com.example.administrator.aduiorecordui.fragment;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.example.administrator.aduiorecordui.recordmp3.AudioRecordDataSource;
import com.tbruyelle.rxpermissions2.RxPermissions;

/**
 * ClassName: BaseAudioRecordFragment
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2018-11-12  15:11
 */
public abstract class BaseAudioRecordFragment extends Fragment {



    enum RecordStatus {
        /**
         *
         */
        None,
        Recording,
        PauseRecording,
        FinishRecording,
        Playing,
        PausePlaying,;
    }

     RxPermissions permissions;
     boolean isPermissionsGranted;

      RecordStatus recordStatus;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissions = new RxPermissions(getActivity());
        recordStatus = RecordStatus.None;
        isPermissionsGranted = permissions.isGranted(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) && permissions.isGranted(android.Manifest.permission.RECORD_AUDIO);
        //获取音频服务
        AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            //打开麦克风
            audioManager.setMicrophoneMute(false);
        }

        iniFile();
    }

    private void iniFile() {
        AudioRecordDataSource.getInstance().init(getContext());
        AudioRecordDataSource.getInstance().initRecordFile();
        AudioRecordDataSource.getInstance().initNewVersionCropOutputFile();
    }
}
