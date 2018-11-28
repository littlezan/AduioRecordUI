package com.littlezan.recordui.playaudio;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;

import com.littlezan.recordui.R;
import com.littlezan.recordui.playaudio.mode.PlaySampleLineMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ClassName: BasePlayAudioView
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2017-12-14  15:08
 */
public abstract class BasePlayAudioView extends View {


    /**
     * 圆点距离上边距
     */
    protected int circleMarginTop = 100;

    /**
     * 矩形距离圆点间距
     */
    protected int rectMarginTop = 0;


    /**
     * 声音数据采集的频率 每秒钟采集个数
     */
    protected int audioSourceFrequency = 10;

    /**
     * 中心垂直线的宽
     */
    protected int centerLineWidth = 4;

    /**
     * 圆形半径
     */
    protected int circleRadius = 10;

    /**
     * 扫描过的矩形颜色
     */
    @ColorInt
    protected int swipedColor;

    /**
     * 没有扫描的矩形颜色
     */
    @ColorInt
    protected int unSwipeColor;

    /**
     * 线宽
     */
    protected int lineWidth = 10;
    /**
     * 间距
     */
    protected int rectGap = 4;

    /**
     * 是否可以滑动View
     */
    protected boolean canTouchScroll = false;

    /**
     * 裁剪区域颜色
     */
    protected @ColorInt
    int cropMashColor = Color.RED;

    /**
     * 裁剪线顶部文字大小
     */
    protected int cropTimeTextSize = 0;

    /**
     * 裁剪区域颜色
     */
    protected @ColorInt
    int cropTimeTextColor = Color.BLACK;

    /**
     * 裁剪线顶部文字距离 圆心的距离
     */
    protected int timeTextMargin = 0;


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

    protected float downX = 0;
    private int trackingPointerId;

    protected List<PlaySampleLineMode> sampleLineList = new ArrayList<>();

    /**
     * 采样点位置x
     */
    protected float lineLocationX = circleRadius;

    /**
     * 画布移动距离
     */
    protected float translateX = 0;
    /**
     * 中心指针位置
     */
    protected float centerLineX = circleRadius;
    /**
     * 中心指针颜色
     */
    protected @ColorInt
    int centerLineColor = Color.RED;

    /**
     * 结束点的位置 包含 间距
     */
    protected float lastSampleXWithRectGap;

    protected Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected Paint centerLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected Paint textTimePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected Rect textTimeBounds = new Rect();

    protected PlayAudioCallBack playAudioCallBack;

    protected boolean isTouching;

    protected List<Float> audioSourceList = new ArrayList<>();


    public BasePlayAudioView(Context context) {
        super(context);
        initAttrs(context, null);
        init(context);
        initPaints();
    }

    public BasePlayAudioView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        init(context);
        initPaints();
    }

    public BasePlayAudioView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        init(context);
        initPaints();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        @SuppressLint("CustomViewStyleable") TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PlayAudio);
        audioSourceFrequency = typedArray.getInt(R.styleable.PlayAudio_p_audioSourceFrequency, audioSourceFrequency);
        circleMarginTop = typedArray.getDimensionPixelSize(R.styleable.PlayAudio_p_circleMarginTop, circleMarginTop);
        rectMarginTop = typedArray.getDimensionPixelSize(R.styleable.PlayAudio_p_rectMarginTop, rectMarginTop);
        centerLineWidth = typedArray.getDimensionPixelSize(R.styleable.PlayAudio_p_centerLineWidth, centerLineWidth);
        centerLineColor = typedArray.getColor(R.styleable.PlayAudio_p_centerLineColor, Color.RED);
        circleRadius = typedArray.getDimensionPixelSize(R.styleable.PlayAudio_p_circleRadius, circleRadius);
        swipedColor = typedArray.getColor(R.styleable.PlayAudio_p_swipedColor, Color.RED);
        unSwipeColor = typedArray.getColor(R.styleable.PlayAudio_p_unSwipeColor, Color.DKGRAY);
        lineWidth = typedArray.getDimensionPixelSize(R.styleable.PlayAudio_p_lineWidth, lineWidth);
        rectGap = typedArray.getDimensionPixelSize(R.styleable.PlayAudio_p_rectGap, rectGap);
        canTouchScroll = typedArray.getBoolean(R.styleable.PlayAudio_p_canTouchScroll, false);
        cropMashColor = typedArray.getColor(R.styleable.PlayAudio_p_cropMashColor, Color.RED);
        cropTimeTextSize = typedArray.getDimensionPixelSize(R.styleable.PlayAudio_p_cropTimeTextSize, 0);
        cropTimeTextColor = typedArray.getColor(R.styleable.PlayAudio_p_cropTimeTextColor, Color.RED);
        timeTextMargin = typedArray.getDimensionPixelSize(R.styleable.PlayAudio_p_timeTextMargin, 0);

        typedArray.recycle();
    }

    private void initPaints() {
        linePaint.setAntiAlias(true);
        linePaint.setColor(unSwipeColor);
        linePaint.setStrokeWidth(lineWidth);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        centerLinePaint.setAntiAlias(true);
        centerLinePaint.setColor(centerLineColor);
        centerLinePaint.setStrokeWidth(centerLineWidth);

        textTimePaint.setStyle(Paint.Style.FILL);
        textTimePaint.setTextAlign(Paint.Align.CENTER);
        textTimePaint.setColor(cropTimeTextColor);
        textTimePaint.setTextSize(cropTimeTextSize);
        String textTemp = formatTime(0);
        textTimePaint.getTextBounds(textTemp, 0, textTemp.length(), textTimeBounds);
    }


    protected void init(Context context) {
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
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        addSampleLine(audioSourceList);
    }

    private void addSampleLine(final List<Float> audioSourceList) {
        sampleLineList.clear();
        lineLocationX = circleRadius;
        float maxLength = getMeasuredHeight() - rectMarginTop*2 - circleMarginTop;
        float centerY = getMeasuredHeight() - maxLength / 2-rectMarginTop;
        for (Float aFloat : audioSourceList) {
            PlaySampleLineMode sampleLine = new PlaySampleLineMode();
            sampleLine.startX = lineLocationX + lineWidth / 2;
            sampleLine.stopX = sampleLine.startX;
            float value = maxLength / 2f * aFloat;
            value = Math.max(2, value);
            sampleLine.startY = centerY - value;
            sampleLine.stopY = centerY + value;
            lineLocationX = lineLocationX + lineWidth + rectGap;
            sampleLineList.add(sampleLine);
        }
        lastSampleXWithRectGap = lineLocationX;
        setCanScrollX();
        invalidate();
    }


    void setCanScrollX() {
        float length = lastSampleXWithRectGap - getMeasuredWidth();
        maxScrollX = length < 0 ? 0 : Math.round(length);
        minScrollX = 0;
    }


    /**
     * 设置音频
     *
     * @param audioSourceList 0-1
     */
    public void setAudioSource(List<Float> audioSourceList) {
        this.audioSourceList = audioSourceList;
        requestLayout();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!canTouchScroll) {
            return false;
        }
        isTouching = true;
        //开始速度检测
        startVelocityTracker(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (!overScroller.isFinished()) {
                    overScroller.abortAnimation();
                }
                downX = event.getX();
                trackingPointerId = event.getPointerId(0);
                sendTouchEvent(event);
                break;
            case MotionEvent.ACTION_MOVE:
                int index = event.findPointerIndex(trackingPointerId);
                float moveX = downX - event.getX(index);
                downX = event.getX(index);
                scrollBy((int) (moveX), 0);
                sendTouchEvent(event);
                postInvalidateOnAnimation();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                trackingPointerId = event.getPointerId(event.getActionIndex());
                downX = event.getX(event.getActionIndex());
                break;
            case MotionEvent.ACTION_POINTER_UP:
                int newIndex;
                if (event.getActionIndex() == event.getPointerCount() - 1) {
                    newIndex = event.getPointerCount() - 2;
                } else {
                    newIndex = event.getPointerCount() - 1;
                }
                trackingPointerId = event.getPointerId(newIndex);
                downX = event.getX(newIndex);
                sendTouchEvent(event);
                break;
            case MotionEvent.ACTION_UP:
                isTouching = false;
                sendTouchEvent(event);
                //手指离开屏幕，开始处理惯性滑动Fling
                velocityTracker.computeCurrentVelocity(1000, maxVelocity);
                float velocityX = velocityTracker.getXVelocity();
                if (Math.abs(velocityX) > minVelocity) {
                    fling(-velocityX);
                } else {
                    onResumePlay();
                }
                finishVelocityTracker();
                break;
            case MotionEvent.ACTION_CANCEL:
                isTouching = false;
                if (!overScroller.isFinished()) {
                    overScroller.abortAnimation();
                }
                finishVelocityTracker();
                sendTouchEvent(event);
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 滑动之后修复位置
     *
     * @param event 点击事件
     */
    protected abstract void sendTouchEvent(MotionEvent event);

    /**
     * 滑动之后修复位置
     */
    protected abstract void fixXAfterScrollXOnFling();

    /**
     * 滑动之后修复位置
     *
     * @param animatorRunning starting
     */
    protected abstract void fixXAfterScrollXOnAnimatorTranslateX(boolean animatorRunning);

    private void onResumePlay() {
        startPlay(getCurrentPlayingTimeInMillis());
        if (playAudioCallBack != null) {
            playAudioCallBack.onResumePlay();
        }
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

    boolean startFling;

    private void fling(float velocity) {
        overScroller.fling(getScrollX(), 0, (int) velocity, 0, minScrollX, maxScrollX, 0, 0);
        startFling = true;
        postInvalidateOnAnimation();
    }

    @Override
    public void computeScroll() {
        if (!canTouchScroll) {
            return;
        }
        //滑动处理
        if (overScroller.computeScrollOffset()) {
            startFling = true;
            scrollTo(overScroller.getCurrX(), overScroller.getCurrY());
            fixXAfterScrollXOnFling();
            postInvalidateOnAnimation();
        }
        if (startFling) {
            if (overScroller.isFinished()) {
                onResumePlay();
                startFling = false;
            }
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

    protected boolean isPlaying;
    protected boolean isAutoScroll;


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
        fixXAfterScrollXOnAnimatorTranslateX(true);
        postInvalidateOnAnimation();
    }

    public long getCurrentPlayingTimeInMillis() {
        return getTimeInMillis(centerLineX);
    }

    public long getTimeInMillis(float x) {
        long timeInMillis;
        if (x <= circleRadius) {
            timeInMillis = 0;
        } else {
            timeInMillis = (long) (x * 1000L / (audioSourceFrequency * (lineWidth + rectGap)));
        }
        return timeInMillis;
    }

    protected long getLength(long timeInMillis) {
        return timeInMillis * audioSourceFrequency * (lineWidth + rectGap) / 1000;
    }

    /**
     * 开始播放
     *
     * @param timeInMillis 开始时间
     */
    public abstract void startPlay(long timeInMillis);

    /**
     * 设置播放时间
     *
     * @param timeInMillis 播放时间 毫秒
     */
    protected void setCenterLineXByTime(long timeInMillis) {
        if (!isPlaying) {
            centerLineX = timeInMillis * audioSourceFrequency * (lineWidth + rectGap) / 1000;
            if (timeInMillis == 0) {
                centerLineX = circleRadius;
            }
        }
    }

    /**
     * 暂停播放录音
     */
    public abstract void stopPlay();


    public void setPlayAudioCallBack(PlayAudioCallBack playAudioCallBack) {
        this.playAudioCallBack = playAudioCallBack;
    }

    public void setAudioSourceFrequency(int audioSourceFrequency) {
        this.audioSourceFrequency = audioSourceFrequency;
    }


    protected float getTimeTextX(float centerLineX) {
        float textX = centerLineX;
        int textWidth = textTimeBounds.width() / 2;
        textX = Math.max(getScrollX() + textWidth, textX);
        textX = Math.min(getScrollX() + getWidth() - textWidth, textX);
        return textX;
    }

    protected String formatTime(long timeMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
        Date date = new Date();
        date.setTime(timeMillis);
        return dateFormat.format(date);
    }

    public void reset() {
        stopPlay();
        centerLineX = circleRadius;
        isAutoScroll = false;
        isTouching = false;
        scrollTo(minScrollX, 0);
        postInvalidateOnAnimation();
    }


}
