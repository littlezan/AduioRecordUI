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
     *当前录音时间
     * @param timeInMillis 当前录音时间 单位毫秒
     */
    void onRecordCurrent(long timeInMillis);

    /**
     * 获取采样 样本 百分比
     * @return percent  样本 百分比
     */
    @FloatRange(from = 0,to = 1) float  getSamplePercent();
}
