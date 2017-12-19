package com.example.administrator.aduiorecordui.record;

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
     * 录音结束
     */
    void onRecordFinish();

    /**
     * 结束播放录音
     */
    void onPlayingRecordFinish();

    /**
     * 当前播放时间
     *
     * @param playingTimeInMillis 当前播放时间 单位毫秒
     */
    void onPlayingRecord(long playingTimeInMillis);
}
