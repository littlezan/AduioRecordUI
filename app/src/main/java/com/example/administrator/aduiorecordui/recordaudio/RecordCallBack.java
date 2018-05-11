package com.example.administrator.aduiorecordui.recordaudio;

import android.support.annotation.FloatRange;

/**
 * ClassName: RecordCallBack
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2017-12-11  11:44
 */
public interface RecordCallBack {

    /**
     * 获取采样 样本 百分比
     *
     * @return percent  样本 百分比
     */
    @FloatRange(from = 0, to = 1)
    float getSamplePercent();


    /**
     * 滑动过程中 指针中心处时间
     *
     * @param centerStartTimeMillis 指针中心处时间
     */
    void onScroll(long centerStartTimeMillis);

    /**
     * 当前录音时间
     *
     * @param centerStartTimeMillis 中心点录音时间
     * @param recordTimeInMillis    当前录音时间 单位毫秒
     */
    void onRecordCurrent(long centerStartTimeMillis, long recordTimeInMillis);


    /**
     * 当前中心线播放时间
     *
     * @param playingTimeInMillis 当前播放时间 单位毫秒
     */
    void onCenterLineTime(long playingTimeInMillis);

    /**
     * 开始录音
     */
    void onStartRecord();

    /**
     * 暂停录音
     */
    void onStopRecord();


    /**
     * 录音结束
     */
    void onFinishRecord();

    /**
     * 开始播放录音
     *
     * @param timeMillis 时间 毫秒
     */
    void onStartPlayRecord(long timeMillis);

    /**
     * 暂停播放录音
     */
    void onStopPlayRecode();

    /**
     * 结束播放录音
     */
    void onFinishPlayingRecord();

}
