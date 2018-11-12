package com.littlezan.recordui.playaudio.playviews;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;

import com.littlezan.recordui.playaudio.BaseDrawPlayAudioView;
import com.littlezan.recordui.playaudio.mode.PlaySampleLineMode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ClassName: VerticalLineMoveByGesturePlayAudioView
 * Description:  在VerticalLineMoveByGesturePlayAudioView 的基础上支持裁剪
 *
 * @author 彭赞
 * @version 1.0
 * @since 2018-11-03  10:36
 */
public class VerticalLineMoveAndCropPlayAudioView extends BaseDrawPlayAudioView {

    private static final String TAG = "VerticalLineMoveAndCrop";

    private int verticalLineTouchHotSpot;
    private boolean isTouchViewMode = false;


    Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    float cropLineX = circleRadius;

    int jumpCropLine = 0;


    public VerticalLineMoveAndCropPlayAudioView(Context context) {
        super(context);
    }

    public VerticalLineMoveAndCropPlayAudioView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public VerticalLineMoveAndCropPlayAudioView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        verticalLineTouchHotSpot = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, Resources.getSystem().getDisplayMetrics());
        maskPaint.setStyle(Paint.Style.FILL);
        maskPaint.setColor(Color.parseColor("#20FF0000"));
    }


    float touchActionX;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float currentX = event.getX();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                stopPlay();
                if (!cropLineInVisible()) {
                    scrollTo(jumpCropLine, 0);
                    lastScrollX = getScrollX();
                }
                if (playAudioCallBack != null) {
                    playAudioCallBack.onPausePlay();
                }
                float resolveX = currentX + getScrollX();
                isTouchViewMode = resolveX < cropLineX - verticalLineTouchHotSpot || resolveX > cropLineX + verticalLineTouchHotSpot;
                if (isTouchViewMode) {
                    super.onTouchEvent(event);
                } else {
                    touchActionX = currentX;
                }
                centerLineX = cropLineX;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (isTouchViewMode) {
                    super.onTouchEvent(event);
                } else {
                    float moveX = currentX - touchActionX;
                    touchActionX = currentX;
                    centerLineX += moveX;
                    cropLineX += moveX;
                    checkValid();
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                startPlay(getCropTimeInMillis());
                if (playAudioCallBack != null) {
                    playAudioCallBack.onResumePlay();
                }
                if (isTouchViewMode) {
                    super.onTouchEvent(event);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (isTouchViewMode) {
                    super.onTouchEvent(event);
                }
                break;
            default:
                break;
        }
        return true;
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
        canvas.drawText(formatTime(cropTimeInMillis), cropLineX, startY - textPaint.getFontSpacing() / 2, textPaint);
        float right = lastSampleXWithRectGap > getWidth() ? getWidth() + getScrollX() : lastSampleXWithRectGap;
        canvas.drawRect(cropLineX, startY + circleRadius, right, getHeight(), maskPaint);
    }

    public long getCropTimeInMillis() {
        return getTimeInMillis(cropLineX);
    }

    /**
     * 用于在画布移动过程中固定垂直线
     */
    int lastScrollX = 0;


    @Override
    public void drawVerticalTargetLine(Canvas canvas) {
        int currentScrollX = getScrollX();
        if (isTouching && isTouchViewMode) {
            //手指滑动
            int offset = currentScrollX - lastScrollX;
            centerLineX = centerLineX + offset;
            cropLineX = cropLineX + offset;
            checkValid();
            lastScrollX = currentScrollX;
        } else {
            //自动滚动
            lastScrollX = currentScrollX;
            centerLineX = isAutoScroll ? getScrollX() + getWidth() : centerLineX;
        }

        if (playAudioCallBack != null) {
            if (centerLineX >= lastSampleXWithRectGap - rectGap) {
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

    private String formatTime(long timeMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
        Date date = new Date();
        date.setTime(timeMillis);
        return dateFormat.format(date);
    }


    public void setInitCropLineOffset(long timeMillis) {
        long offset = getLength(timeMillis);
        cropLineX = offset;
        centerLineX = offset;
        if (cropLineX > getWidth() / 2) {
            scrollTo((int) cropLineX-getWidth()/2, 0);
        }
        invalidate();
    }


    @Override
    public void startPlay(long timeInMillis) {
        if (!isPlaying) {
            isTouching = false;
            setPlayingTime(timeInMillis);
            isPlaying = true;
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
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                animator.removeAllListeners();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animator.removeAllListeners();
            }
        });
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