package com.example.administrator.aduiorecordui.playaudio;

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
     * 开始播放
     *
     * @param timeInMillis 时间  毫秒
     */
    void onStartPlay(long timeInMillis);

    /**
     * 暂停播放
     */
    void onPausePlay();

    /**
     * 动画播放结束
     */
    void onPlayingFinish();
}
