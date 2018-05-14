package com.example.administrator.aduiorecordui.recordaudio;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Handler;
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
import java.util.concurrent.TimeUnit;

/**
 * ClassName: BaseAudioRecord
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2017-12-07  19:52
 */
public abstract class BaseAudioRecordView extends View {

    private static final String TAG = "BaseAudioRecord";


    /**
     * 采样时间
     */
    private int recordDelayMillis;

    /**
     * 录音时长 单位分钟
     */
    protected int recordTimeInMinutes = 1;

    /**
     * 录音采样频率 每秒钟采样个数
     */
    protected int recordSamplingFrequency = 10;
    /**
     * 是否显示刻度尺上的文字
     */
    protected boolean showRuleText = false;
    /**
     * 一格大刻度多少格小刻度
     */
    protected int intervalCount = 10;

    /**
     * 刻度间隔
     */
    protected int scaleIntervalLength = 18;

    /**
     * 尺子 小刻度 高
     */
    protected int smallScaleStrokeLength = 30;

    /**
     * 尺子 小刻度 宽
     */
    protected int smallScaleStrokeWidth = 3;
    /**
     * 尺子 大刻度 高
     */
    protected int bigScaleStrokeLength = (int) (smallScaleStrokeLength * 2.5);
    /**
     * 尺子 大刻度 宽
     */
    protected int bigScaleStrokeWidth = 5;

    /**
     * 刻度尺 底部直线颜色
     */
    protected @ColorInt
    int ruleHorizontalLineColor;

    /**
     * 刻度尺 底部直线 宽度
     */
    protected int ruleHorizontalLineStrokeWidth = 10;
    /**
     * 刻度尺 底部直线 高度
     */
    protected int ruleHorizontalLineHeight = bigScaleStrokeLength + 50;

    /**
     * 刻度尺 垂直刻度线的颜色
     */
    protected @ColorInt
    int ruleVerticalLineColor;


    /**
     * 刻度尺 上文字颜色
     */
    protected @ColorInt
    int ruleTextColor;

    /**
     * 刻度尺 上文字 大小
     */
    protected int ruleTextSize = 28;

    /**
     * 水平线 颜色
     */
    protected @ColorInt
    int middleHorizontalLineColor;

    /**
     * 水平线 stroke width
     */
    protected int middleHorizontalLineStrokeWidth = 5;

    /**
     * 垂直线 颜色
     */
    protected @ColorInt
    int middleVerticalLineColor;

    /**
     * 垂直线 stroke width
     */
    protected int middleVerticalLineStrokeWidth = 5;
    /**
     * 垂直线 两个圆的圆心 半径
     */
    protected int middleCircleRadius = 12;
    /**
     * 矩形 颜色
     */
    protected @ColorInt
    int rectColor;
    /**
     * 矩形倒影 颜色
     */
    protected @ColorInt
    int rectInvertColor;


    /**
     * 声波 采样样本 宽
     */
    protected int lineWidth = 8;

    /**
     * 底部文字颜色
     */
    protected @ColorInt
    int bottomTextColor;
    /**
     * 底部文字 大小
     */
    protected int bottomTextSize = 60;
    /**
     * 底部 文字区域 背景颜色
     */
    protected @ColorInt
    int bottomRectColor;

    /**
     * 最小可滑动值
     */
    protected int minScrollX = 0;
    /**
     * 最大可滑动值
     */
    protected int maxScrollX = 0;

    /**
     * 正在录制
     */
    protected boolean isRecording;


    /**
     * 控制滑动
     */
    protected OverScroller overScroller;
    /**
     * 速度获取
     */
    protected VelocityTracker velocityTracker;
    /**
     * 惯性最大速度
     */
    protected int maxVelocity;
    /**
     * 惯性最小速度
     */
    protected int minVelocity;

    protected long maxLength;

    RecordCallBack recordCallBack;

    /**
     * 矩形间距
     */
    protected int rectGap = 2;
    /**
     * 声波矩形 距离顶部垂直间距
     */
    protected int rectMarginTop = 50;
    protected List<SampleLineModel> sampleLineList = new ArrayList<>();

    protected boolean isAutoScroll;

    protected float mLastX = 0;
    /**
     * 当前录音总时长
     */
    protected long currentRecordTime;

    /**
     * 是否在播放录音
     */
    private boolean isPlayingRecord = false;

    /**
     * 中心点的位置
     */
    protected float centerLineX = 0;

    long centerTimeMillis;

    /**
     * 采样最后的位置
     */
    int lineLocationX;

    public BaseAudioRecordView(Context context) {
        super(context);
        initAttrs(context, null);
        init(context);
    }

    public BaseAudioRecordView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        init(context);
    }

    public BaseAudioRecordView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        init(context);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AudioRecordView, 0, 0);
        recordTimeInMinutes = typedArray.getInteger(R.styleable.AudioRecordView_recordTimeInMinutes, recordTimeInMinutes);
        recordSamplingFrequency = typedArray.getInteger(R.styleable.AudioRecordView_recordSamplingFrequency, recordSamplingFrequency);
        showRuleText = typedArray.getBoolean(R.styleable.AudioRecordView_showRuleText, showRuleText);
        intervalCount = typedArray.getInteger(R.styleable.AudioRecordView_intervalCount, intervalCount);
        scaleIntervalLength = typedArray.getDimensionPixelSize(R.styleable.AudioRecordView_scaleIntervalLength, scaleIntervalLength);

        smallScaleStrokeLength = typedArray.getDimensionPixelSize(R.styleable.AudioRecordView_smallScaleStrokeLength, smallScaleStrokeLength);
        smallScaleStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.AudioRecordView_smallScaleStrokeWidth, smallScaleStrokeWidth);
        bigScaleStrokeLength = typedArray.getDimensionPixelSize(R.styleable.AudioRecordView_bigScaleStrokeLength, bigScaleStrokeLength);
        bigScaleStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.AudioRecordView_bigScaleStrokeWidth, bigScaleStrokeWidth);


        ruleVerticalLineColor = typedArray.getColor(R.styleable.AudioRecordView_ruleVerticalLineColor, ContextCompat.getColor(getContext(), android.R.color.darker_gray));

        ruleHorizontalLineColor = typedArray.getColor(R.styleable.AudioRecordView_ruleHorizontalLineColor, ContextCompat.getColor(getContext(), android.R.color.holo_red_light));
        ruleHorizontalLineStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.AudioRecordView_ruleHorizontalLineStrokeWidth, ruleHorizontalLineStrokeWidth);
        ruleHorizontalLineHeight = typedArray.getDimensionPixelSize(R.styleable.AudioRecordView_ruleHorizontalLineHeight, ruleHorizontalLineHeight);

        ruleTextColor = typedArray.getColor(R.styleable.AudioRecordView_ruleTextColor, ContextCompat.getColor(getContext(), android.R.color.darker_gray));
        ruleTextSize = typedArray.getDimensionPixelSize(R.styleable.AudioRecordView_ruleTextSize, ruleTextSize);

        middleHorizontalLineColor = typedArray.getColor(R.styleable.AudioRecordView_middleHorizontalLineColor, ContextCompat.getColor(getContext(), android.R.color.holo_red_light));
        middleHorizontalLineStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.AudioRecordView_middleHorizontalLineStrokeWidth, middleHorizontalLineStrokeWidth);
        middleVerticalLineColor = typedArray.getColor(R.styleable.AudioRecordView_middleVerticalLineColor, ContextCompat.getColor(getContext(), android.R.color.holo_red_light));
        middleVerticalLineStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.AudioRecordView_middleVerticalLineStrokeWidth, middleVerticalLineStrokeWidth);
        middleCircleRadius = typedArray.getDimensionPixelSize(R.styleable.AudioRecordView_middleCircleRadius, middleCircleRadius);


        rectColor = typedArray.getColor(R.styleable.AudioRecordView_rectColor, ContextCompat.getColor(getContext(), android.R.color.holo_red_light));
        rectInvertColor = typedArray.getColor(R.styleable.AudioRecordView_rectInvertColor, ContextCompat.getColor(getContext(), android.R.color.darker_gray));
        rectGap = typedArray.getDimensionPixelSize(R.styleable.AudioRecordView_rectGap, rectGap);
        rectMarginTop = typedArray.getDimensionPixelSize(R.styleable.AudioRecordView_rectMarginTop, rectMarginTop);

        bottomTextColor = typedArray.getColor(R.styleable.AudioRecordView_bottomTextColor, ContextCompat.getColor(getContext(), android.R.color.white));
        bottomTextSize = typedArray.getDimensionPixelSize(R.styleable.AudioRecordView_bottomTextSize, bottomTextSize);

        bottomRectColor = typedArray.getColor(R.styleable.AudioRecordView_bottomRectColor, ContextCompat.getColor(getContext(), android.R.color.holo_orange_light));

        typedArray.recycle();
    }

    private void init(Context context) {
        maxLength = TimeUnit.MINUTES.toSeconds(recordTimeInMinutes) * intervalCount * scaleIntervalLength;
        recordDelayMillis = 1000 / recordSamplingFrequency;
        lineWidth = (intervalCount * scaleIntervalLength) / recordSamplingFrequency - rectGap;
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
        if (isPlayingRecord || isRecording) {
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
        if (recordCallBack != null) {
            long centerStartTimeMillis;
            if (x == minScrollX) {
                centerStartTimeMillis = 0;
            } else if (x == maxScrollX) {
                centerStartTimeMillis = currentRecordTime;
            } else {
                centerStartTimeMillis = (long) (centerLineX * 1000L / (intervalCount * scaleIntervalLength));
            }
            recordCallBack.onScroll(centerStartTimeMillis);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        recordHandler.removeCallbacksAndMessages(null);
    }


    Handler recordHandler = new Handler();
    Runnable recordRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRecording) {
                if (recordCallBack != null) {
                    makeSampleLine(recordCallBack.getSamplePercent());
                }
                float lastSampleLineRightX = getLastSampleLineRightX();
                int middleX = getScrollX() + getMeasuredWidth() / 2;
                long maxX = maxLength;
                if (lastSampleLineRightX > middleX && lastSampleLineRightX <= maxX) {
                    isAutoScroll = true;
                    int dx = Math.round(lastSampleLineRightX + rectGap - getScrollX());
                    overScroller.startScroll(getScrollX(), 0, dx, 0, recordDelayMillis);
                } else {
                    isAutoScroll = false;
                }

                //结束录音
                if (currentRecordTime >= TimeUnit.MINUTES.toMillis(recordTimeInMinutes)) {
                    stopRecord();
                    if (recordCallBack != null) {
                        recordCallBack.onFinishRecord();
                    }
                } else {
                    //录音中
                    recordHandler.postDelayed(recordRunnable, recordDelayMillis);
                    currentRecordTime = currentRecordTime + recordDelayMillis;
                }

                if (recordCallBack != null) {
                    recordCallBack.onRecordCurrent(currentRecordTime, currentRecordTime);
                }
            }
        }
    };



    public void startRecord() {
        if (!isRecording) {
            if (currentRecordTime >= TimeUnit.MINUTES.toMillis(recordTimeInMinutes)) {
                return;
            }
            scrollToEnd();
            isRecording = true;
            recordHandler.post(recordRunnable);
            if (recordCallBack != null) {
                recordCallBack.onStartRecord();
            }
        }
    }


    public void stopRecord() {
        if (isRecording) {
            isRecording = false;
            isAutoScroll = false;
            overScroller.abortAnimation();
            recordHandler.removeCallbacks(recordRunnable);
            if (recordCallBack != null) {
                recordCallBack.onStopRecord();
            }
        }
    }

    private void scrollToEnd() {
        if (sampleLineList.size() > 0) {
            float lastSampleLineRightX = getLastSampleLineRightX();
            int middle = getMeasuredWidth() / 2;
            if (lastSampleLineRightX >= middle) {
                scrollTo((int) lastSampleLineRightX, 0);
            } else {
                scrollTo(0, 0);
            }
        } else {
            scrollTo(0, 0);
        }
    }

    protected float getLastSampleLineRightX() {
        return lineLocationX;
    }


    /**
     * 生成矩形
     *
     * @param percent 矩形高度 百分比
     */
    protected abstract void makeSampleLine(float percent);


    public void setRecordCallBack(RecordCallBack recordCallBack) {
        this.recordCallBack = recordCallBack;
    }


    /**
     * 是否正在录音
     *
     * @return isRecording
     */
    public boolean isRecording() {
        return isRecording;
    }


    public void startPlayRecord(long timeInMillis) {
        if (!isPlayingRecord) {
            stopRecord();
            if (timeInMillis == 0) {
                //从0开始播放
                scrollTo(minScrollX, 0);
            } else {
                //从指定的时间开始播放
                long scrollLength = timeInMillis * scaleIntervalLength * intervalCount / 1000 + minScrollX;
                scrollTo((int) scrollLength, 0);
            }
            invalidate();
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    startTranslateCanvas();
                }
            }, 100);

        }
    }

    /**
     * 暂停播放录音
     */
    public void stopPlayRecord() {
        if (isPlayingRecord) {
            isPlayingRecord = false;
            isAutoScroll = false;
            animator.cancel();
        }
    }


    /**
     * 画布移动距离
     */
    float translateX = 0;
    ObjectAnimator animator;

    private void startTranslateCanvas() {
        int middle = getMeasuredWidth() / 2;
        float startX = getScrollX();
        //小于半屏的时候，要重新计算偏移量，因为有个左滑的动作
        float endX = lineLocationX < middle ? getScrollX() + Math.abs(lineLocationX - centerLineX) : maxScrollX;
        float dx = Math.abs(lineLocationX - centerLineX);
        final long duration = (long) (1000 * dx / (recordSamplingFrequency * (lineWidth + rectGap)));
        animator = ObjectAnimator.ofFloat(this, "translateX", startX, endX);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(duration);
        isPlayingRecord = true;
        isAutoScroll = true;
        animator.removeAllListeners();
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                //播放结束
                stopPlayRecord();
                animator.removeAllListeners();
                if (recordCallBack != null) {
                    recordCallBack.onStopPlayRecode();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                //播放结束
                stopPlayRecord();
                animator.removeAllListeners();
                if (recordCallBack != null) {
                    recordCallBack.onStopPlayRecode();
                }
            }
        });


        animator.start();
        if (recordCallBack != null) {
            recordCallBack.onStartPlayRecord(centerTimeMillis);
        }
    }

    public float getTranslateX() {
        return translateX;
    }

    public void setTranslateX(float translateX) {
        this.translateX = translateX;
        scrollTo((int) translateX, 0);
    }

    /**
     * 是否正在播放录音
     *
     * @return 是否正在播放录音
     */
    public boolean isPlayingRecord() {
        return isPlayingRecord;
    }


}
