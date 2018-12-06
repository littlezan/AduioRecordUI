package com.littlezan.recordui.playaudio.playviews;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;

import com.littlezan.recordui.playaudio.BaseDrawPlayAudioView;

/**
 * ClassName: VerticalLineMoveByGesturePlayAudioView
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2018-11-03  10:36
 */
public class VerticalLineMoveByGesturePlayAudioView extends BaseDrawPlayAudioView {

    private int verticalLineTouchHotSpot;
    private boolean isTouchViewMode = false;


    public VerticalLineMoveByGesturePlayAudioView(Context context) {
        super(context);
    }

    public VerticalLineMoveByGesturePlayAudioView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public VerticalLineMoveByGesturePlayAudioView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        verticalLineTouchHotSpot = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, Resources.getSystem().getDisplayMetrics());
    }


    float touchActionX;
    int trackingCenterLinePointerId;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        isTouching = true;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                touchEventStopPlay();
                trackingCenterLinePointerId = event.getPointerId(event.getActionIndex());
                float resolveX = event.getX(event.findPointerIndex(trackingCenterLinePointerId)) + getScrollX();
                isTouchViewMode = resolveX < centerLineX - verticalLineTouchHotSpot || resolveX > centerLineX + verticalLineTouchHotSpot;
                if (isTouchViewMode) {
                    super.onTouchEvent(event);
                } else {
                    touchActionX = event.getX(event.getActionIndex());
                }
                break;
            case MotionEvent.ACTION_MOVE:
                touchEventStopPlay();
                if (isTouchViewMode) {
                    super.onTouchEvent(event);
                } else {
                    int index = event.findPointerIndex(trackingCenterLinePointerId);
                    float moveX = event.getX(index) - touchActionX;
                    touchActionX = event.getX(index);
                    centerLineX += moveX;
                    if (centerLineX >= lastSampleXWithRectGap) {
                        centerLineX = lastSampleXWithRectGap;
                    }
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (isTouchViewMode) {
                    super.onTouchEvent(event);
                } else {
                    int newIndex;
                    if (event.getActionIndex() == event.getPointerCount() - 1) {
                        newIndex = event.getPointerCount() - 2;
                    } else {
                        newIndex = event.getPointerCount() - 1;
                    }
                    trackingCenterLinePointerId = event.getPointerId(newIndex);
                    touchActionX = event.getX(newIndex);
                }
                break;
            case MotionEvent.ACTION_UP:
                isTouching = false;
                if (isTouchViewMode) {
                    super.onTouchEvent(event);
                } else {
                    startPlay(getCurrentPlayingTimeInMillis());
                    if (playAudioCallBack != null) {
                        playAudioCallBack.onResumePlay();
                    }
                }
                isTouchViewMode = false;
                break;
            case MotionEvent.ACTION_CANCEL:
                if (isTouchViewMode) {
                    super.onTouchEvent(event);
                }
                isTouchViewMode = false;
                break;
            default:
                break;
        }
        return true;
    }

    private void touchEventStopPlay() {
        if (isTouching && isPlaying) {
            stopPlay();
            if (playAudioCallBack != null) {
                playAudioCallBack.onPausePlay();
            }
        }
    }


    @Override
    public void drawVerticalTargetLine(Canvas canvas) {
        float startY = circleMarginTop;
        canvas.drawCircle(centerLineX, startY, circleRadius, centerLinePaint);
        canvas.drawLine(centerLineX, startY, centerLineX, getMeasuredHeight(), centerLinePaint);
        canvas.drawCircle(centerLineX, getMeasuredHeight()-circleRadius, circleRadius, centerLinePaint);
        long currentPlayingTimeInMillis = getCurrentPlayingTimeInMillis();
        canvas.drawText(formatTime(currentPlayingTimeInMillis), getTimeTextX(centerLineX), startY - timeTextMargin, textTimePaint);

        if (playAudioCallBack != null) {
            if (centerLineX >= lastSampleXWithRectGap) {
                isPlaying = false;
                isAutoScroll = false;
                playAudioCallBack.onPlayingFinish();
            } else {
                playAudioCallBack.onPlaying(currentPlayingTimeInMillis);
            }
        }

    }


    /**
     * 用于在画布移动过程中固定垂直线
     */
    int lastScrollX = 0;

    @Override
    protected void sendTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                lastScrollX = getScrollX();
                break;
            case MotionEvent.ACTION_MOVE:
                int offset = getScrollX() - lastScrollX;
                centerLineX += offset;
                lastScrollX = getScrollX();
                break;
            default:
                break;
        }
    }


    @Override
    protected void fixXAfterScrollXOnFling() {
        int offset = getScrollX() - lastScrollX;
        centerLineX += offset;
        lastScrollX = getScrollX();
    }


    @Override
    protected void fixXAfterScrollXOnAnimatorTranslateX(boolean animatorRunning) {
        if (animatorRunning) {
            int offset = getScrollX() - lastScrollX;
            centerLineX += offset;
            lastScrollX = getScrollX();
        } else {
            lastScrollX = getScrollX();
        }
    }


    public void setInitPlayingTime(final long timeInMillis) {
        stopPlay();
        setCenterLineXByTime(timeInMillis);
        postOnAnimation(new Runnable() {
            @Override
            public void run() {
                scrollTo((int) centerLineX - getWidth(), 0);
                lastScrollX = getScrollX();
                postInvalidateOnAnimation();
            }
        });
    }

    @Override
    public void startPlay(long timeInMillis) {
        if (!isPlaying) {
            isPlaying = true;
            setCenterLineXByTime(timeInMillis);
            lastScrollX = getScrollX();
            if (centerLineX < getWidth() + getScrollX()) {
                startCenterLineToEndAnimation();
            } else {
                startTranslateView();
            }
            if (centerLineX >= lastSampleXWithRectGap - rectGap) {
                stopPlay();
            }
        }
    }

    ObjectAnimator animator;

    private void startCenterLineToEndAnimation() {
        isAutoScroll = false;
        final int animatorFromDX = lastSampleXWithRectGap > getWidth() ? getWidth() + getScrollX() : (int) lastSampleXWithRectGap;
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
                lastScrollX = getScrollX();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                lastScrollX = getScrollX();
                if (isPlaying) {
                    startTranslateView();
                }
            }
        });

    }

    private void startTranslateView() {
        if (lastSampleXWithRectGap <= getWidth()) {
            return;
        }
        isAutoScroll = true;
        int dx = maxScrollX - getScrollX();
        final long duration = (1000 * dx / (audioSourceFrequency * (lineWidth + rectGap)));
        animator = ObjectAnimator.ofFloat(this, "translateX", getScrollX(), maxScrollX);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(duration);
        animator.start();
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                fixXAfterScrollXOnAnimatorTranslateX(false);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animator.removeAllListeners();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animator.removeAllListeners();
            }
        });
    }


    @Override
    public void stopPlay() {
        if (isPlaying) {
            isPlaying = false;
            isAutoScroll = false;
            if (animator != null) {
                animator.removeAllListeners();
                animator.cancel();
            }
        }
    }


}