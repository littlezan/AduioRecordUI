package com.littlezan.recordui.playaudio.playviews;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;

import com.littlezan.recordui.playaudio.BaseDrawPlayAudioView;
import com.littlezan.recordui.playaudio.mode.PlaySampleLineMode;

/**
 * ClassName: VerticalLineMoveByGesturePlayAudioView
 * Description:  在VerticalLineMoveByGesturePlayAudioView 的基础上支持裁剪
 *
 * @author 彭赞
 * @version 1.0
 * @since 2018-11-03  10:36
 */
public class VerticalLineMoveAndCropPlayAudioView extends BaseDrawPlayAudioView {


    private int verticalLineTouchHotSpot;
    private boolean isTouchViewMode = false;


    Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    float cropLineX = circleRadius;

    int jumpCropLine = 0;


    public VerticalLineMoveAndCropPlayAudioView(Context context) {
        super(context);
        initValues();
    }

    public VerticalLineMoveAndCropPlayAudioView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initValues();
    }

    public VerticalLineMoveAndCropPlayAudioView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initValues();
    }

    void initValues() {
        verticalLineTouchHotSpot = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, Resources.getSystem().getDisplayMetrics());
        maskPaint.setStyle(Paint.Style.FILL);
        maskPaint.setColor(cropMashColor);
    }


    float touchActionX;
    int trackingCropLinePointerId;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        isTouching = true;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                touchEventStopPlay();
                jumpToCropLinePosition();
                trackingCropLinePointerId = event.getPointerId(event.getActionIndex());
                float resolveX = event.getX(event.getActionIndex()) + getScrollX();
                isTouchViewMode = resolveX < cropLineX - verticalLineTouchHotSpot || resolveX > cropLineX + verticalLineTouchHotSpot;
                if (isTouchViewMode) {
                    super.onTouchEvent(event);
                } else {
                    touchActionX = event.getX(event.getActionIndex());
                }
                centerLineX = cropLineX;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchEventStopPlay();
                if (isTouchViewMode) {
                    super.onTouchEvent(event);
                } else {
                    float moveX = event.getX(event.findPointerIndex(trackingCropLinePointerId)) - touchActionX;
                    touchActionX = event.getX(event.findPointerIndex(trackingCropLinePointerId));
                    cropLineX += moveX;
                    centerLineX = cropLineX;
                    checkValid();
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
                    trackingCropLinePointerId = event.getPointerId(newIndex);
                    touchActionX = event.getX(newIndex);
                }
                break;
            case MotionEvent.ACTION_UP:
                isTouching = false;
                if (isTouchViewMode) {
                    super.onTouchEvent(event);
                } else {
                    startPlay(getCropTimeInMillis());
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
                //手指滑动
                int offset = getScrollX() - lastScrollX;
                cropLineX += offset;
                centerLineX = cropLineX;
                checkValid();
                lastScrollX = getScrollX();
                break;
            default:
                break;
        }
    }

    @Override
    protected void fixXAfterScrollXOnFling() {
        int offset = getScrollX() - lastScrollX;
        cropLineX += offset;
        centerLineX = cropLineX;
        lastScrollX = getScrollX();
    }

    @Override
    protected void fixXAfterScrollXOnAnimatorTranslateX(boolean animatorRunning) {
        if (animatorRunning) {
            int offset = getScrollX() - lastScrollX;
            cropLineX += offset;
            centerLineX = cropLineX;
            lastScrollX = getScrollX();
        } else {
            lastScrollX = getScrollX();
        }
    }

    private void jumpToCropLinePosition() {
        if (!cropLineInVisible()) {
            scrollTo(jumpCropLine, 0);
            lastScrollX = getScrollX();
            invalidate();
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawCropLine(canvas);
    }

    private void drawCropLine(Canvas canvas) {
        float startY = circleMarginTop;
        canvas.drawCircle(cropLineX, startY, circleRadius, centerLinePaint);
        canvas.drawLine(cropLineX, startY, cropLineX, getHeight(), centerLinePaint);
        long cropTimeInMillis = getCropTimeInMillis();
        canvas.drawText(formatTime(cropTimeInMillis), getTimeTextX(cropLineX), startY - timeTextMargin, textTimePaint);
        float right = lastSampleXWithRectGap > getWidth() ? getWidth() + getScrollX() : lastSampleXWithRectGap;
        canvas.drawRect(cropLineX, startY + circleRadius, right, getHeight(), maskPaint);
        if (playAudioCallBack != null) {
            playAudioCallBack.onCurrentCropLineTime(cropTimeInMillis);
        }
    }

    public long getCropTimeInMillis() {
        return getTimeInMillis(cropLineX);
    }


    @Override
    public void drawVerticalTargetLine(Canvas canvas) {
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

    private void checkValid() {
        cropLineX = Math.max(circleRadius, cropLineX);
        cropLineX = Math.min(lastSampleXWithRectGap, cropLineX);
        centerLineX = Math.max(circleRadius, centerLineX);
        centerLineX = Math.min(lastSampleXWithRectGap, centerLineX);
    }

    private boolean cropLineInVisible() {
        return cropLineX <= getWidth() + getScrollX() && cropLineX >= getScrollX();
    }


    public void setInitCropLineOffset(long timeMillis) {
        long offset = getLength(timeMillis);
        cropLineX = offset;
        centerLineX = offset;
        if (cropLineX > getWidth() / 2) {
            scrollTo((int) cropLineX - getWidth() / 2, 0);
        }
        invalidate();
    }

    public void setInitPlayingTime(final long timeInMillis) {
        stopPlay();
        setCenterLineXByTime(timeInMillis);
        postOnAnimation(new Runnable() {
            @Override
            public void run() {
                jumpToCropLinePosition();
                invalidate();
            }
        });
    }


    @Override
    public void startPlay(long timeInMillis) {
        if (!isPlaying) {
            isPlaying = true;
            setCenterLineXByTime(timeInMillis);
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
        if (lastSampleXWithRectGap <= getWidth()) {
            return;
        }
        jumpCropLine = getScrollX();
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

    public void crop() {
        stopPlay();
        int cropIndex = getCropIndex();
        audioSourceList = audioSourceList.subList(0, cropIndex);
        centerLineX = cropLineX;
        requestLayout();
        if (playAudioCallBack != null) {
            playAudioCallBack.onCrop(cropIndex, getCropTimeInMillis());
        }
    }

    private int getCropIndex() {
        int cropIndex = sampleLineList.size();
        for (int i = sampleLineList.size() - 1; i >= 0; i--) {
            PlaySampleLineMode playSampleLineMode = sampleLineList.get(i);
            if (playSampleLineMode.startX > cropLineX) {
                cropIndex = i;
            } else {
                break;
            }
        }
        return cropIndex;
    }


}