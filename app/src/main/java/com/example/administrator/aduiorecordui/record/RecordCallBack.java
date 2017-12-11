package com.example.administrator.aduiorecordui.record;

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
     * 尺子当前刻度值
     *
     * @param scrollX      滑动距离
     * @param timeInMillis 当前录音时间 单位毫秒
     */
    void onScaleChange(int scrollX, long timeInMillis);
}
