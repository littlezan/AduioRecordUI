package com.example.administrator.aduiorecordui.playaudio;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.LinearInterpolator;
import android.widget.OverScroller;

import com.example.administrator.aduiorecordui.R;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: BasePlayAudioView
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2017-12-14  15:08
 */
public abstract class BasePlayAudioView extends View {


    boolean canTouchScroll = false;

    /**
     * 圆点距离上边距
     */
    int circleMarginTop = 100;

    /**
     * 矩形距离圆点间距
     */
    int rectMarginTop = 150;

    /**
     * 滚动的距离
     */
    public int scrollDx = 2;

    /**
     * 声音数据采集的频率 每秒钟采集个数
     */
    int audioSourceFrequency = 10;

    /**
     * 中心垂直线的宽
     */
    int centerLineWidth = 4;

    /**
     * 圆形半径
     */
    int circleRadius = 10;

    /**
     * 扫描过的矩形颜色
     */
    @ColorInt
    int swipedColor;

    /**
     * 没有扫描的矩形颜色
     */
    @ColorInt
    int unSwipeColor;

    /**
     * 线宽
     */
    int lineWidth = 10;
    /**
     * 间距
     */
    int rectGap = 4;

    /**
     * 最小可滑动值
     */
    protected int minScrollX = 0;
    /**
     * 最大可滑动值
     */
    protected int maxScrollX = 0;
    /**
     * 速度获取
     */
    protected VelocityTracker velocityTracker;
    /**
     * 控制滑动
     */
    protected OverScroller overScroller;
    /**
     * 惯性最大速度
     */
    protected int maxVelocity;
    /**
     * 惯性最小速度
     */
    protected int minVelocity;

    protected float mLastX = 0;

    List<SampleLine> sampleLineList = new ArrayList<>();

    /**
     * 采样点位置x
     */
    float lineLocationX = circleRadius;

    /**
     * 画布移动距离
     */
    float translateX = 0;
    /**
     * 中心指针位置
     */
    float centerLineX = circleRadius;

    /**
     * 结束点的位置 包含 间距
     */
    float lastSampleXWithRectGap;
    PlayAudioCallBack playAudioCallBack;

    public BasePlayAudioView(Context context) {
        super(context);
        initAttrs(context, null);
        init(context);
    }

    public BasePlayAudioView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        init(context);
    }

    public BasePlayAudioView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        init(context);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PlayAudio, 0, 0);
        audioSourceFrequency = typedArray.getInt(R.styleable.PlayAudio_p_audioSourceFrequency, audioSourceFrequency);
        circleMarginTop = typedArray.getDimensionPixelSize(R.styleable.PlayAudio_p_circleMarginTop, circleMarginTop);
        rectMarginTop = typedArray.getDimensionPixelSize(R.styleable.PlayAudio_p_rectMarginTop, rectMarginTop);
        centerLineWidth = typedArray.getDimensionPixelSize(R.styleable.PlayAudio_p_centerLineWidth, centerLineWidth);
        circleRadius = typedArray.getDimensionPixelSize(R.styleable.PlayAudio_p_circleRadius, circleRadius);
        swipedColor = ContextCompat.getColor(context, android.R.color.holo_red_light);
        unSwipeColor = ContextCompat.getColor(context, android.R.color.darker_gray);
        lineWidth = typedArray.getDimensionPixelSize(R.styleable.PlayAudio_p_rectWidth, lineWidth);
        rectGap = typedArray.getDimensionPixelSize(R.styleable.PlayAudio_p_rectGap, rectGap);

        typedArray.recycle();
    }

    long scrollDelayMillis;

    private void init(Context context) {

        scrollDelayMillis = 1000 * scrollDx / (audioSourceFrequency * (lineWidth + rectGap));

        overScroller = new OverScroller(context);
        velocityTracker = VelocityTracker.obtain();
        maxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        minVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
        checkAPILevel();
    }

    private void checkAPILevel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            setLayerType(LAYER_TYPE_NONE, null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!canTouchScroll) {
            return false;
        }
        float currentX = event.getX();
        //开始速度检测
        startVelocityTracker(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!overScroller.isFinished()) {
                    overScroller.abortAnimation();
                }
                mLastX = currentX;
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = mLastX - currentX;
                mLastX = currentX;
                scrollBy((int) (moveX), 0);
                break;
            case MotionEvent.ACTION_UP:
                //手指离开屏幕，开始处理惯性滑动Fling
                velocityTracker.computeCurrentVelocity(500, maxVelocity);
                float velocityX = velocityTracker.getXVelocity();
                if (Math.abs(velocityX) > minVelocity) {
                    fling(-velocityX);
                }
                finishVelocityTracker();
                break;
            case MotionEvent.ACTION_CANCEL:
                if (!overScroller.isFinished()) {
                    overScroller.abortAnimation();
                }
                finishVelocityTracker();
                break;
            default:
                break;
        }
        return true;
    }

    private void startVelocityTracker(MotionEvent event) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(event);
    }

    private void finishVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    private void fling(float velocity) {
        overScroller.fling(getScrollX(), 0, (int) velocity, 0, minScrollX, maxScrollX, 0, 0);
    }

    @Override
    public void computeScroll() {
        //滑动处理
        if (overScroller.computeScrollOffset()) {
            scrollTo(overScroller.getCurrX(), overScroller.getCurrY());
            invalidate();
        }
    }

    @Override
    public void scrollTo(int x, int y) {
        if (x < minScrollX) {
            x = minScrollX;
        } else if (x > maxScrollX) {
            x = maxScrollX;
        }
        super.scrollTo(x, y);
    }

    long tempTime = 0;
    boolean isPlaying;
    boolean isAutoScroll;
    Handler playHandler = new Handler();
    Runnable playRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPlaying) {
                int middle = getMeasuredWidth() / 2;
                if (centerLineX < middle) {
                    startCenterLineAnimationFromStart();
                } else if (centerLineX >= lastSampleXWithRectGap - middle) {
                    startCenterLineAnimationFromEnd();
                } else {
                    startTranslateCanvas();
                }
                if (centerLineX >= lastSampleXWithRectGap - rectGap) {
                    stopPlay();
                }
            }
        }
    };

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
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (isPlaying){
                   startTranslateCanvas();
                }

            }
        });

    }

    private void startTranslateCanvas() {
        isAutoScroll = true;
        int dx = maxScrollX - getScrollX();
        final long duration = (1000 * dx / (audioSourceFrequency * (lineWidth + rectGap)));
        animator = ObjectAnimator.ofFloat(this, "translateX", getScrollX(), maxScrollX);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(duration);
        animator.start();
        animator.addListener( new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (isPlaying){
                    startCenterLineAnimationFromEnd();
                }
            }
        });
    }

    private void startCenterLineAnimationFromEnd() {
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

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                stopPlay();
            }
        });
    }

    public float getCenterLineX() {
        return centerLineX;
    }

    public void setCenterLineX(float centerLineX) {
        this.centerLineX = centerLineX;
        invalidate();
    }

    public float getTranslateX() {
        return translateX;
    }

    public void setTranslateX(float translateX) {
        this.translateX = translateX;
        scrollTo((int) translateX, 0);
        invalidate();
    }

    public void startPlay(long timeInMillis) {
        if (!isPlaying) {
            tempTime = SystemClock.elapsedRealtime();
            setPlayingTime(timeInMillis);
            isPlaying = true;
            playHandler.post(playRunnable);
        }
    }

    /**
     * 设置播放时间
     *
     * @param timeInMillis 播放时间 毫秒
     */
    public void setPlayingTime(long timeInMillis) {
        if (!isPlaying) {

            centerLineX = timeInMillis*audioSourceFrequency * (lineWidth + rectGap) /1000 ;
            if (timeInMillis == 0) {
                centerLineX = circleRadius;
            }
            int middle = getMeasuredWidth() / 2;
            if (centerLineX <= middle) {
                scrollTo(minScrollX, 0);
                invalidate();
            } else {
                int x = (int) (centerLineX - middle);
                scrollTo(x, 0);
                invalidate();
            }
        }
    }

    /**
     * 暂停播放录音
     */
    public void stopPlay() {
        if (isPlaying) {
            isPlaying = false;
            isAutoScroll = false;
            animator.cancel();
            playHandler.removeCallbacks(playRunnable);

        }
    }


    public void setPlayAudioCallBack(PlayAudioCallBack playAudioCallBack) {
        this.playAudioCallBack = playAudioCallBack;
    }

    public void reset() {
        stopPlay();
        centerLineX = circleRadius;
        isAutoScroll = false;
        scrollTo(minScrollX, 0);
        invalidate();
    }


}
