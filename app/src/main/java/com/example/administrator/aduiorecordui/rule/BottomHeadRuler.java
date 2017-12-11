package com.example.administrator.aduiorecordui.rule;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: BottomHeadRuler
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2017-12-07  20:00
 */
public class BottomHeadRuler extends BaseHorizontalRuler {

    private static final String TAG = BottomHeadRuler.class.getSimpleName();

    Paint outLinePaint = new Paint();
    Paint middleLinePaint = new Paint();
    Paint mSmallScalePaint = new Paint();
    Paint mBigScalePaint = new Paint();
    Paint mTextPaint = new Paint();
    Paint pointPaint = new Paint();
    Paint rectPaint = new Paint();
    Paint rectInvertedPaint = new Paint();
    Paint centerLine = new Paint();

    /**
     * 刻度间隔
     */
    private int scaleInterval = 18;

    //一格大刻度多少格小刻度
    protected int mCount = 10;
    //提前刻画量
    protected int mDrawOffset;
    //大小刻度的长度
    private int mSmallScaleLength = 30, mBigScaleLength = 60;
    //大小刻度的粗细
    private int mSmallScaleWidth = 3, mBigScaleWidth = 5;
    //数字Text距离顶部高度
    private int mTextMarginHead = 120;
    //数字字体大小
    private int mTextSize = 28;

    /**
     * 刻度尺底部 轮廓线的高度
     */
    float scaleHeight = mTextMarginHead + 50;

    int rectLocationX;
    int rectWidth = 6;
    protected List<Rect> radioRectList = new ArrayList<>();


    private int circleRadius = 10;

    public BottomHeadRuler(Context context) {
        super(context);
        init();
    }

    public BottomHeadRuler(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BottomHeadRuler(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {

        pointPaint.setAntiAlias(true);
        pointPaint.setStrokeWidth(10);
        pointPaint.setColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_light));

        centerLine.setAntiAlias(true);
        centerLine.setStrokeWidth(5);
        centerLine.setColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_light));

        middleLinePaint.setAntiAlias(true);
        middleLinePaint.setStrokeWidth(2);
        middleLinePaint.setColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));


        outLinePaint.setAntiAlias(true);
        outLinePaint.setStrokeWidth(10);
        outLinePaint.setColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_light));

        rectPaint.setAntiAlias(true);
        rectPaint.setStrokeWidth(2);
        rectPaint.setColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_light));

        rectInvertedPaint.setAntiAlias(true);
        rectInvertedPaint.setStrokeWidth(2);
        rectInvertedPaint.setColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));

        mSmallScalePaint = new Paint();
        mSmallScalePaint.setStrokeWidth(mSmallScaleWidth);
        mSmallScalePaint.setColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
        mSmallScalePaint.setStrokeCap(Paint.Cap.ROUND);

        mBigScalePaint = new Paint();
        mBigScalePaint.setColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
        mBigScalePaint.setStrokeWidth(mBigScaleWidth);
        mBigScalePaint.setStrokeCap(Paint.Cap.ROUND);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);


        mDrawOffset = scaleInterval;
    }

    @Override
    public void makeRect(int height) {
        int rectBottom = getMeasuredHeight() / 2;
        int rectTop = rectBottom - height;
        Rect rect = new Rect(rectLocationX, rectTop, rectLocationX + rectWidth, rectBottom);
        rectLocationX = rectLocationX + rect.width();
        radioRectList.add(rect);
        if (!isAutoScroll) {
            invalidate();
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawScale(canvas);
        drawRect(canvas);
        drawCenterVerticalLine(canvas);
    }

    private void drawCenterVerticalLine(Canvas canvas) {
        float circleX = getScrollX() + canvas.getWidth() / 2;
        float topCircleY = scaleHeight - circleRadius;
        canvas.drawCircle(circleX, topCircleY, circleRadius, centerLine);
        float bottomCircleY = canvas.getHeight() / 2 + (canvas.getHeight() / 2 - scaleHeight) + circleRadius;
        canvas.drawCircle(circleX, bottomCircleY, circleRadius, centerLine);
        canvas.drawLine(circleX, topCircleY, circleX, bottomCircleY, centerLine);
    }

    private void drawScale(Canvas canvas) {

        int firstPoint = (getScrollX() - mDrawOffset) / scaleInterval;
        int lastPoint = (getScrollX() + canvas.getWidth() + mDrawOffset) / (scaleInterval);
        for (int i = firstPoint; i < lastPoint; i++) {
            float locationX = i * scaleInterval;
            if (i % mCount == 0) {
                canvas.drawLine(locationX, scaleHeight - mBigScaleLength, locationX, scaleHeight, mBigScalePaint);
                canvas.drawText(String.valueOf(i / mCount), locationX, scaleHeight - mTextMarginHead, mTextPaint);
            } else {
                canvas.drawLine(locationX, scaleHeight - mSmallScaleLength, locationX, scaleHeight, mSmallScalePaint);
            }
        }


        //画轮廓线
        canvas.drawLine(getScrollX() + 100, scaleHeight, getScrollX() + canvas.getWidth() - 100, scaleHeight, outLinePaint);

        //测试用的圆
        canvas.drawCircle(540, canvas.getHeight() / 2, 25, pointPaint);


    }

    private void drawRect(Canvas canvas) {

        List<Rect> drawRectList = getDrawRectList(canvas);
        for (Rect rect : drawRectList) {
            canvas.drawRect(rect, rectPaint);
            Rect rectInverted = new Rect(rect);
            rectInverted.top = canvas.getHeight() / 2;
            rectInverted.bottom = rectInverted.top + rect.height();
            canvas.drawRect(rect, rectPaint);
            canvas.drawRect(rectInverted, rectInvertedPaint);
        }
        //测试用的基准线
        int middleLineY = canvas.getHeight() / 2;
        canvas.drawLine(getScrollX() + 50, middleLineY, getScrollX() + canvas.getWidth() - 50, middleLineY, middleLinePaint);

    }

    private List<Rect> getDrawRectList(Canvas canvas) {
        int middleWidth = canvas.getWidth() / 2;
        List<Rect> rectList = new ArrayList<>();
        int mixWidth = getScrollX() - rectWidth;
        int maxWidth = getScrollX() + middleWidth + rectWidth;
        int recentlyRectIndex = getScrollX() / rectWidth;

        for (int i = recentlyRectIndex; i < radioRectList.size(); i++) {
            Rect next = radioRectList.get(i);
            if (next.left >= mixWidth && next.right <= maxWidth) {
                rectList.add(next);
            }
            if (next.left > maxWidth) {
                break;
            }
        }

        return rectList;
    }
}
