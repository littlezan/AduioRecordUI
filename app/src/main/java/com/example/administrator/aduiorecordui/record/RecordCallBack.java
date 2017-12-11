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
     * @param scale 当前刻度值
     */
    void onScaleChange(int scale);
}
