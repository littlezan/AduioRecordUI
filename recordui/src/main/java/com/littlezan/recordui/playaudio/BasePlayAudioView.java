package com.littlezan.recordui.playaudio;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;

import com.littlezan.recordui.R;
import com.littlezan.recordui.playaudio.mode.PlaySampleLineMode;

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

    private static final String TAG = "BasePlayAudioView";

    /**
     * 圆点距离上边距
     */
    protected int circleMarginTop = 100;

    /**
     * 矩形距离圆点间距
     */
    protected int rectMarginTop = 150;

    /**
     * 滚动的距离
     */
    public int scrollDx = 2;

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
     * 结束点的位置 包含 间距
     */
    protected float lastSampleXWithRectGap;

    protected Paint linePaint = new Paint();
    protected Paint centerLinePaint = new Paint();
    protected Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    protected PlayAudioCallBack playAudioCallBack;

    protected boolean isTouching;

    protected List<Float> audioSourceList = new ArrayList<>();


    public BasePlayAudioView(Context context) {
        super(context);
        initAttrs(context, null);
        init(context);
        initPaints(context);
    }

    public BasePlayAudioView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        init(context);
        initPaints(context);
    }

    public BasePlayAudioView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        init(context);
        initPaints(context);
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
        circleRadius = typedArray.getDimensionPixelSize(R.styleable.PlayAudio_p_circleRadius, circleRadius);
        swipedColor = typedArray.getColor(R.styleable.PlayAudio_p_swipedColor, Color.RED);
        unSwipeColor = typedArray.getColor(R.styleable.PlayAudio_p_unSwipeColor, Color.DKGRAY);
        lineWidth = typedArray.getDimensionPixelSize(R.styleable.PlayAudio_p_lineWidth, lineWidth);
        rectGap = typedArray.getDimensionPixelSize(R.styleable.PlayAudio_p_rectGap, rectGap);
        canTouchScroll = typedArray.getBoolean(R.styleable.PlayAudio_p_canTouchScroll, false);

        typedArray.recycle();
    }

    private void initPaints(Context context) {
        linePaint.setAntiAlias(true);
        linePaint.setColor(unSwipeColor);
        linePaint.setStrokeWidth(lineWidth);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        centerLinePaint.setAntiAlias(true);
        centerLinePaint.setColor(Color.RED);
        centerLinePaint.setStrokeWidth(centerLineWidth);

        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, context.getResources().getDisplayMetrics()));
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

    void setCanScrollX() {
       float length =  lastSampleXWithRectGap - getWidth();
        maxScrollX = length <0 ?0 : Math.round(length);
        minScrollX = 0;
    }

    private void addSampleLine(final List<Float> audioSourceList) {
        sampleLineList.clear();
        lineLocationX = circleRadius;
        for (Float aFloat : audioSourceList) {
            PlaySampleLineMode sampleLine = new PlaySampleLineMode();
            sampleLine.startX = lineLocationX + lineWidth / 2;
            sampleLine.stopX = sampleLine.startX;
            sampleLine.startY = (getMeasuredHeight() - (getMeasuredHeight() - rectMarginTop) * aFloat) / 2 + circleMarginTop;
            sampleLine.stopY = getMeasuredHeight() + circleMarginTop - sampleLine.startY;
            lineLocationX = lineLocationX + lineWidth + rectGap;
            sampleLineList.add(sampleLine);
        }
        lastSampleXWithRectGap = lineLocationX;
        setCanScrollX();
        invalidate();
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
        float currentX = event.getX();
        //开始速度检测
        startVelocityTracker(event);
        switch (event.getActionMasked()) {
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
                isTouching = false;
                //手指离开屏幕，开始处理惯性滑动Fling
                velocityTracker.computeCurrentVelocity(500, maxVelocity);
                float velocityX = velocityTracker.getXVelocity();
                if (Math.abs(velocityX) > minVelocity) {
                    fling(-velocityX);
                }
                finishVelocityTracker();

                break;
            case MotionEvent.ACTION_CANCEL:
                isTouching = false;
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
        if (!canTouchScroll) {
            return;
        }
        //滑动处理
        if (isTouching && overScroller.computeScrollOffset()) {
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
        invalidate();
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
    public void setPlayingTime(long timeInMillis) {
        if (!isPlaying) {
            centerLineX = timeInMillis * audioSourceFrequency * (lineWidth + rectGap) / 1000;
            if (timeInMillis == 0) {
                centerLineX = circleRadius;
            }
            invalidate();
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



    public void reset() {
        stopPlay();
        centerLineX = circleRadius;
        isAutoScroll = false;
        isTouching = false;
        scrollTo(minScrollX, 0);
        invalidate();
    }


}
