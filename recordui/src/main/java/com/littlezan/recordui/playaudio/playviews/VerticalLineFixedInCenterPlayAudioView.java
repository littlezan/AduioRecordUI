package com.littlezan.recordui.playaudio.playviews;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;

import com.littlezan.recordui.playaudio.BaseDrawPlayAudioView;

/**
 * ClassName: PlayAudioView
 * Description: 播放音频波形图
 *
 * @author 彭赞
 * @version 1.0
 * @since 2017-12-13  15:42
 */
public class VerticalLineFixedInCenterPlayAudioView extends BaseDrawPlayAudioView {

    public VerticalLineFixedInCenterPlayAudioView(Context context) {
        super(context);
    }

    public VerticalLineFixedInCenterPlayAudioView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public VerticalLineFixedInCenterPlayAudioView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void drawVerticalTargetLine(Canvas canvas) {
        centerLineX = isAutoScroll ? getScrollX() + canvas.getWidth() / 2 : centerLineX;
        float startY = circleMarginTop;
        canvas.drawCircle(centerLineX, startY, circleRadius, centerLinePaint);
        canvas.drawLine(centerLineX, startY, centerLineX, getMeasuredHeight(), centerLinePaint);
        if (playAudioCallBack != null) {
            if (centerLineX >= lastSampleXWithRectGap) {
                isPlaying = false;
                isAutoScroll = false;
                playAudioCallBack.onPlayingFinish();
            } else {
                playAudioCallBack.onPlaying(getCurrentPlayingTimeInMillis());
            }
        }
    }

    @Override
    public void startPlay(long timeInMillis) {
        if (!isPlaying) {
            isTouching = false;
            setCenterLineXByTime(timeInMillis);
            isPlaying = true;
            int middle = getMeasuredWidth() / 2;
            if (centerLineX < middle) {
                startCenterLineAnimationFromStart();
            } else if (centerLineX >= lastSampleXWithRectGap - middle) {
                startCenterLineAnimationFromEnd();
            } else {
                startTranslateView();
            }
            if (centerLineX >= lastSampleXWithRectGap - rectGap) {
                stopPlay();
            }
        }
    }

    @Override
    public void stopPlay() {
        if (isPlaying) {
            isTouching = false;
            isPlaying = false;
            isAutoScroll = false;
            if (animator != null) {
                animator.cancel();
            }
        }
    }

    ObjectAnimator animator;

    private void startCenterLineAnimationFromStart() {
        isAutoScroll = false;
        final int animatorFromDX = getMeasuredWidth() / 2;
        float dx = (animatorFromDX - centerLineX);
        final long duration = (long) (1000 * dx / (audioSourceFrequency * (lineWidth + rectGap)));
        animator = ObjectAnimator.ofFloat(this, "centerLineX", centerLineX, animatorFromDX);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(duration);
        animator.start();
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                animator.removeAllListeners();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (isPlaying) {
                    startTranslateView();
                }

            }
        });

    }

    private void startTranslateView() {
        isAutoScroll = true;
        int dx = maxScrollX - getScrollX();
        final long duration = (1000 * dx / (audioSourceFrequency * (lineWidth + rectGap)));
        animator = ObjectAnimator.ofFloat(this, "translateX", getScrollX(), maxScrollX);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(duration);
        animator.start();
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                animator.removeAllListeners();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animator.removeAllListeners();
                if (isPlaying) {
                    startCenterLineAnimationFromEnd();
                }
            }
        });
    }

    private void startCenterLineAnimationFromEnd() {
        if (centerLineX >= lastSampleXWithRectGap - getMeasuredWidth() / 2) {
            return;
        }
        isAutoScroll = false;
        final float animatorEndDX = lastSampleXWithRectGap - rectGap;
        float dx = (animatorEndDX - centerLineX);
        final long duration = (long) (1000 * dx / (audioSourceFrequency * (lineWidth + rectGap)));
        animator = ObjectAnimator.ofFloat(this, "centerLineX", centerLineX, animatorEndDX);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(duration);
        animator.start();
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                stopPlay();
                animator.removeAllListeners();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                stopPlay();
                animator.removeAllListeners();
            }
        });
    }


}
