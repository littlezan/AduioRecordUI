package com.example.administrator.aduiorecordui.record;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.EdgeEffect;
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
public abstract class BaseAudioRecord extends View {

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


    private boolean needEdgeEffect = false;

    /**
     * 开始边界效果
     */
    protected EdgeEffect startEdgeEffect;
    /**
     * 结束边界效果
     */
    protected EdgeEffect endEdgeEffect;

    protected @ColorInt
    int edgeColor;


    protected long maxLength;
    /**
     * 边缘效应长度
     */
    protected int mEdgeLength;
    private RecordCallBack recordCallBack;

    /**
     * 矩形间距
     */
    protected int rectGap = 2;
    protected List<SampleLineModel> sampleLineList = new ArrayList<>();


    private float mLastX = 0;


    public BaseAudioRecord(Context context) {
        super(context);
        initAttrs(context, null);
        init(context);
    }

    public BaseAudioRecord(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        init(context);
    }

    public BaseAudioRecord(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        init(context);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AudioRecord, 0, 0);
        recordTimeInMinutes = typedArray.getInteger(R.styleable.AudioRecord_recordTimeInMinutes, recordTimeInMinutes);
        recordSamplingFrequency = typedArray.getInteger(R.styleable.AudioRecord_recordSamplingFrequency, recordSamplingFrequency);
        showRuleText = typedArray.getBoolean(R.styleable.AudioRecord_showRuleText, showRuleText);
        intervalCount = typedArray.getInteger(R.styleable.AudioRecord_intervalCount, intervalCount);
        scaleIntervalLength = typedArray.getDimensionPixelSize(R.styleable.AudioRecord_scaleIntervalLength, scaleIntervalLength);

        smallScaleStrokeLength = typedArray.getDimensionPixelSize(R.styleable.AudioRecord_smallScaleStrokeLength, smallScaleStrokeLength);
        smallScaleStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.AudioRecord_smallScaleStrokeWidth, smallScaleStrokeWidth);
        bigScaleStrokeLength = typedArray.getDimensionPixelSize(R.styleable.AudioRecord_bigScaleStrokeLength, bigScaleStrokeLength);
        bigScaleStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.AudioRecord_bigScaleStrokeWidth, bigScaleStrokeWidth);


        ruleVerticalLineColor = typedArray.getColor(R.styleable.AudioRecord_ruleVerticalLineColor, ContextCompat.getColor(getContext(), android.R.color.darker_gray));

        ruleHorizontalLineColor = typedArray.getColor(R.styleable.AudioRecord_ruleHorizontalLineColor, ContextCompat.getColor(getContext(), android.R.color.holo_red_light));
        ruleHorizontalLineStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.AudioRecord_ruleHorizontalLineStrokeWidth, ruleHorizontalLineStrokeWidth);
        ruleHorizontalLineHeight = typedArray.getDimensionPixelSize(R.styleable.AudioRecord_ruleHorizontalLineHeight, ruleHorizontalLineHeight);

        ruleTextColor = typedArray.getColor(R.styleable.AudioRecord_ruleTextColor, ContextCompat.getColor(getContext(), android.R.color.darker_gray));
        ruleTextSize = typedArray.getDimensionPixelSize(R.styleable.AudioRecord_ruleTextSize, ruleTextSize);

        middleHorizontalLineColor = typedArray.getColor(R.styleable.AudioRecord_middleHorizontalLineColor, ContextCompat.getColor(getContext(), android.R.color.holo_red_light));
        middleHorizontalLineStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.AudioRecord_middleHorizontalLineStrokeWidth, middleHorizontalLineStrokeWidth);
        middleVerticalLineColor = typedArray.getColor(R.styleable.AudioRecord_middleVerticalLineColor, ContextCompat.getColor(getContext(), android.R.color.holo_red_light));
        middleVerticalLineStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.AudioRecord_middleVerticalLineStrokeWidth, middleVerticalLineStrokeWidth);
        middleCircleRadius = typedArray.getDimensionPixelSize(R.styleable.AudioRecord_middleCircleRadius, middleCircleRadius);


        rectColor = typedArray.getColor(R.styleable.AudioRecord_rectColor, ContextCompat.getColor(getContext(), android.R.color.holo_red_light));
        rectInvertColor = typedArray.getColor(R.styleable.AudioRecord_rectInvertColor, ContextCompat.getColor(getContext(), android.R.color.darker_gray));
        rectGap = typedArray.getDimensionPixelSize(R.styleable.AudioRecord_rectGap, rectGap);

        bottomTextColor = typedArray.getColor(R.styleable.AudioRecord_bottomTextColor, ContextCompat.getColor(getContext(), android.R.color.white));
        bottomTextSize = typedArray.getDimensionPixelSize(R.styleable.AudioRecord_bottomTextSize, bottomTextSize);

        bottomRectColor = typedArray.getColor(R.styleable.AudioRecord_bottomRectColor, ContextCompat.getColor(getContext(), android.R.color.holo_orange_light));

    }

    private void init(Context context) {
        maxLength = TimeUnit.MINUTES.toSeconds(recordTimeInMinutes) * intervalCount * scaleIntervalLength;
        recordDelayMillis = 1000 / recordSamplingFrequency;
        lineWidth = (intervalCount * scaleIntervalLength) / recordSamplingFrequency - rectGap;
        overScroller = new OverScroller(context);
        velocityTracker = VelocityTracker.obtain();
        maxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        minVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
        initEdgeEffects(context);
        checkAPILevel();
    }

    private void checkAPILevel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            setLayerType(LAYER_TYPE_NONE, null);
        }
    }


    /**
     * 初始化边缘效果
     *
     * @param context context
     */
    public void initEdgeEffects(Context context) {
        needEdgeEffect = true;
        edgeColor = ContextCompat.getColor(context, android.R.color.holo_red_light);
        if (startEdgeEffect == null || endEdgeEffect == null) {
            startEdgeEffect = new EdgeEffect(context);
            endEdgeEffect = new EdgeEffect(context);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                startEdgeEffect.setColor(edgeColor);
                endEdgeEffect.setColor(edgeColor);
            }
            mEdgeLength = scaleIntervalLength * intervalCount;
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isRecording) {
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
            goStartEdgeEffect(x);
            x = minScrollX;
        } else if (x > maxScrollX) {
            x = maxScrollX;
        }
        super.scrollTo(x, y);
        if (recordCallBack != null) {
            recordCallBack.onScaleChange(x, x * 1000L / (intervalCount * scaleIntervalLength));
        }
    }


    /**
     * 头部边缘效果处理
     *
     * @param x 移动距离
     */
    private void goStartEdgeEffect(int x) {
        if (needEdgeEffect) {
            if (!overScroller.isFinished()) {
                startEdgeEffect.onAbsorb((int) overScroller.getCurrVelocity());
                overScroller.abortAnimation();
            } else {
                startEdgeEffect.onPull(x);
                startEdgeEffect.setSize(scaleIntervalLength * intervalCount, getMeasuredHeight());
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                postInvalidateOnAnimation();
            }
        }
    }


    Handler recordHandler = new Handler();
    Runnable recordRunnable = new Runnable() {
        @Override
        public void run() {
            if (recordCallBack != null) {
                makeSampleLine(recordCallBack.getSamplePercent());
            }
            float lastSampleLineRightX = getLastSampleLineRightX();
            int middleX = getScrollX() + getMeasuredWidth() / 2;
            Log.d(TAG, "lll lastSampleLineRightX = " + lastSampleLineRightX+", middleX = " + middleX);
            if (lastSampleLineRightX > middleX) {
                overScroller.startScroll(getScrollX(), 0, (int) lastSampleLineRightX, 0, recordDelayMillis);
            }
            recordHandler.postDelayed(recordRunnable, recordDelayMillis);
        }
    };

    public void startRecord() {
        if (!isRecording) {
            scrollToEnd();
            recordHandler.post(recordRunnable);
            isRecording = true;
        }
    }


    public void stopRecord() {
        if (isRecording) {
            Log.d(TAG, "stopRecord: ");
            recordHandler.removeCallbacks(recordRunnable);
            isRecording = false;
        }
    }

    private void scrollToEnd() {
        if (sampleLineList.size() > 0) {
            scrollTo((int) (getLastSampleLineRightX()), 0);
        }
    }

    private float getLastSampleLineRightX() {
        if (sampleLineList.size() > 0) {
            return sampleLineList.get(sampleLineList.size() - 1).startX + lineWidth / 2;
        } else {
            return 0;
        }
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

}
