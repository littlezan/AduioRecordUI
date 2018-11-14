package com.littlezan.recordui.playaudio;

/**
 * ClassName: PlayAudioCallBack
 * Description: 播放音频回调
 *
 * @author 彭赞
 * @version 1.0
 * @since 2017-12-15  11:32
 */
public interface PlayAudioCallBack {

    /**
     * 当前播放时间
     *
     * @param timeInMillis 播放时间 毫秒
     */
    void onPlaying(long timeInMillis);

    /**
     * 裁剪线的位置所表示的时间
     * @param cropLineTimeInMillis 时间
     */
    void onCurrentCropLineTime(long cropLineTimeInMillis);

    /**
     * 暂停播放
     */
    void onPausePlay();

    /**
     * 继续播放
     */
    void onResumePlay();

    /**
     * 动画播放结束
     */
    void onPlayingFinish();

    /**
     * 裁剪
     *
     * @param cropIndex 裁剪点
     * @param remainTimeInMillis 剩余时间
     */
    void onCrop(int cropIndex, long remainTimeInMillis);
}
