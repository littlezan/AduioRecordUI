package com.example.administrator.aduiorecordui.record;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: AudioRecord
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2017-12-07  20:00
 */
public class AudioRecord extends BaseAudioRecord {


    Paint ruleHorizontalLinePaint = new Paint();
    Paint smallScalePaint = new Paint();
    Paint bigScalePaint = new Paint();
    TextPaint ruleTextPaint = new TextPaint();
    Paint rectInvertedPaint = new Paint();
    Paint middleHorizontalLinePaint = new Paint();
    Paint middleVerticalLinePaint = new Paint();
    Paint rectPaint = new Paint();
    TextPaint bottomTextPaint = new TextPaint();
    Paint bottomRectPaint = new Paint();


    /**
     * 提前刻画量
     */
    protected int mDrawOffset;

    private int rectLocationX;
    protected List<Rect> radioRectList = new ArrayList<>();


    public AudioRecord(Context context) {
        super(context);
        init();
    }

    public AudioRecord(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AudioRecord(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {

        ruleHorizontalLinePaint.setAntiAlias(true);
        ruleHorizontalLinePaint.setStrokeWidth(ruleHorizontalLineStrokeWidth);
        ruleHorizontalLinePaint.setColor(ruleHorizontalLineColor);


        smallScalePaint.setStrokeWidth(smallScaleStrokeWidth);
        smallScalePaint.setColor(ruleVerticalLineColor);
        smallScalePaint.setStrokeCap(Paint.Cap.ROUND);

        bigScalePaint.setColor(ruleVerticalLineColor);
        bigScalePaint.setStrokeWidth(bigScaleStrokeWidth);
        bigScalePaint.setStrokeCap(Paint.Cap.ROUND);

        ruleTextPaint.setAntiAlias(true);
        ruleTextPaint.setColor(ruleTextColor);
        ruleTextPaint.setTextSize(ruleTextSize);
        ruleTextPaint.setTextAlign(Paint.Align.LEFT);


        middleHorizontalLinePaint.setAntiAlias(true);
        middleHorizontalLinePaint.setStrokeWidth(middleHorizontalLineStrokeWidth);
        middleHorizontalLinePaint.setColor(middleHorizontalLineColor);

        middleVerticalLinePaint.setAntiAlias(true);
        middleVerticalLinePaint.setStrokeWidth(middleVerticalLineStrokeWidth);
        middleVerticalLinePaint.setColor(middleVerticalLineColor);

        rectPaint.setAntiAlias(true);
        rectPaint.setStrokeWidth(1);
        rectPaint.setColor(rectColor);

        rectInvertedPaint.setAntiAlias(true);
        rectInvertedPaint.setStrokeWidth(1);
        rectInvertedPaint.setColor(rectInvertColor);

        bottomTextPaint.setAntiAlias(true);
        bottomTextPaint.setColor(bottomTextColor);
        bottomTextPaint.setTextSize(bottomTextSize);
        bottomTextPaint.setTextAlign(Paint.Align.CENTER);

        bottomRectPaint.setAntiAlias(true);
        bottomRectPaint.setColor(bottomRectColor);


        mDrawOffset = scaleIntervalLength;
    }

    @Override
    public void makeRect(int height) {
        int rectBottom = getMeasuredHeight() / 2;
        int rectTop = rectBottom - height;
        Rect rect = new Rect(rectLocationX, rectTop, rectLocationX + rectWidth, rectBottom);
        rectLocationX = rectLocationX + rect.width();
        radioRectList.add(rect);
        maxScrollX = rectLocationX - getMeasuredWidth()/2;
        if (!isAutoScroll) {
            invalidate();
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawScale(canvas);
        drawRect(canvas);
        drawBottomText(canvas);
        drawCenterVerticalLine(canvas);
    }


    private void drawScale(Canvas canvas) {

        int firstPoint = (getScrollX() - mDrawOffset) / scaleIntervalLength;
        int lastPoint = (getScrollX() + canvas.getWidth() + mDrawOffset) / (scaleIntervalLength);
        for (int i = firstPoint; i < lastPoint; i++) {
            float locationX = i * scaleIntervalLength;
            if (i % intervalCount == 0) {
                canvas.drawLine(locationX, ruleHorizontalLineHeight - bigScaleStrokeLength, locationX, ruleHorizontalLineHeight, bigScalePaint);
                int index = i / intervalCount;
                canvas.drawText(formatTime(index), locationX + bigScaleStrokeWidth + 5, ruleHorizontalLineHeight - bigScaleStrokeLength + ruleTextSize / 1.5f, ruleTextPaint);
            } else {
                canvas.drawLine(locationX, ruleHorizontalLineHeight - smallScaleStrokeLength, locationX, ruleHorizontalLineHeight, smallScalePaint);
            }
        }
        //画轮廓线
        canvas.drawLine(getScrollX(), ruleHorizontalLineHeight, getScrollX() + canvas.getWidth(), ruleHorizontalLineHeight, ruleHorizontalLinePaint);
    }

    private String formatTime(int index) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
        Date date = new Date();
        date.setTime(TimeUnit.SECONDS.toMillis(index));
        return dateFormat.format(date);
    }

    private void drawRect(Canvas canvas) {
        //
        int middleLineY = canvas.getHeight() / 2;
        canvas.drawLine(getScrollX(), middleLineY, getScrollX() + canvas.getWidth(), middleLineY, middleHorizontalLinePaint);

        //从数据源中找出需要绘制的矩形
        List<Rect> drawRectList = getDrawRectList(canvas);
        if (drawRectList == null || drawRectList.size() == 0) {
            return;
        }
        //绘制矩形
        for (Rect rect : drawRectList) {
            canvas.drawRect(rect, rectPaint);
            Rect rectInverted = new Rect(rect);
            rectInverted.top = canvas.getHeight() / 2;
            rectInverted.bottom = rectInverted.top + rect.height();
            canvas.drawRect(rectInverted, rectInvertedPaint);
        }

    }

    private List<Rect> getDrawRectList(Canvas canvas) {
        if (radioRectList.size() == 0) {
            return null;
        }
        List<Rect> rectList = new ArrayList<>();

        int recentlyRectIndex = getScrollX() / rectWidth;
        if (recentlyRectIndex < 0) {
            recentlyRectIndex = 0;
        } else if (recentlyRectIndex >= radioRectList.size()) {
            recentlyRectIndex = radioRectList.size() - 1;
        }

        int mixWidth = getScrollX() - rectWidth;
        int maxWidth = isAutoScroll ? getScrollX() + canvas.getWidth() / 2 + rectWidth : getScrollX() + canvas.getWidth() + rectWidth;
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

    private void drawBottomText(Canvas canvas) {
        float bottomCircleY = canvas.getHeight() / 2 + (canvas.getHeight() / 2 - ruleHorizontalLineHeight) + middleCircleRadius;
        canvas.drawRect(getScaleX(), bottomCircleY - middleCircleRadius, getScrollX() + canvas.getWidth(), canvas.getHeight(), bottomRectPaint);
        //底部文字
        SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
        Date date = new Date();
        int length = intervalCount * scaleIntervalLength;
        date.setTime(getScrollX() * 1000L / length);
        int decimal = getScrollX() * 10 / length % 10;
        String text;
        if (getScrollX() == 0) {
            text = dateFormat.format(date) + "/" + recordTimeInMinutes;
        } else {
            text = dateFormat.format(date) + "." + decimal + "/" + recordTimeInMinutes;
        }
        canvas.drawText(text, getScrollX() + canvas.getWidth() / 2, bottomCircleY + bottomTextSize + 20, bottomTextPaint);
    }

    private void drawCenterVerticalLine(Canvas canvas) {

        Rect lastRect;
        if (radioRectList.size() == 0) {
            lastRect = new Rect();
        } else {
            lastRect = radioRectList.get(radioRectList.size() - 1);
        }

        float circleX = lastRect.centerX();
        if (circleX > getScrollX() + canvas.getWidth() / 2) {
            circleX = getScrollX() + canvas.getWidth() / 2;
        }
        float topCircleY = ruleHorizontalLineHeight - middleCircleRadius;
        //上圆
        canvas.drawCircle(circleX, topCircleY, middleCircleRadius, middleVerticalLinePaint);
        float bottomCircleY = canvas.getHeight() / 2 + (canvas.getHeight() / 2 - ruleHorizontalLineHeight) + middleCircleRadius;
        //下圆
        canvas.drawCircle(circleX, bottomCircleY, middleCircleRadius, middleVerticalLinePaint);
        //直线
        canvas.drawLine(circleX, topCircleY, circleX, bottomCircleY, middleVerticalLinePaint);
    }
}
