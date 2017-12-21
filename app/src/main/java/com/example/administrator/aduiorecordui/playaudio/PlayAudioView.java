package com.example.administrator.aduiorecordui.playaudio;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: PlayAudioView
 * Description: 播放音频波形图
 *
 * @author 彭赞
 * @version 1.0
 * @since 2017-12-13  15:42
 */
public class PlayAudioView extends BasePlayAudioView {

    private static final String TAG = "PlayAudioView";


    Paint linePaint = new Paint();
    Paint centerTargetPaint = new Paint();
    private List<Float> audioSourceList;

    HandlerThread handlerThread = new HandlerThread("PlayAudioView");
    Handler handler;

    public PlayAudioView(Context context) {
        super(context);
        init(context);
    }

    public PlayAudioView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PlayAudioView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        linePaint.setAntiAlias(true);
        linePaint.setColor(unSwipeColor);
        linePaint.setStrokeWidth(lineWidth);


        centerTargetPaint.setAntiAlias(true);
        centerTargetPaint.setColor(ContextCompat.getColor(context, android.R.color.holo_red_light));
        centerTargetPaint.setStrokeWidth(centerLineWidth);
    }


    /**
     * 设置音频
     *
     * @param audioSourceList 0-1
     */
    public void setAudioSource(List<Float> audioSourceList) {
        createAudioSample(audioSourceList);
    }

    private void createAudioSample(final List<Float> audioSourceList) {
        this.audioSourceList = audioSourceList;
        requestLayout();
    }


    private void addSampleLine(final List<Float> audioSourceList) {

        handler.post(new Runnable() {
            @Override
            public void run() {
                sampleLineList.clear();
                lineLocationX = circleRadius;
                for (Float aFloat : audioSourceList) {
                    SampleLine sampleLine = new SampleLine();
                    sampleLine.startX = lineLocationX + lineWidth / 2;
                    sampleLine.stopX = sampleLine.startX;
                    sampleLine.startY = (getMeasuredHeight() - (getMeasuredHeight() - rectMarginTop) * aFloat) / 2 + circleMarginTop;
                    sampleLine.stopY = getMeasuredHeight() + circleMarginTop - sampleLine.startY;
                    lineLocationX = lineLocationX + lineWidth + rectGap;
                    sampleLineList.add(sampleLine);
                }
                initValues();
                if (lineLocationX > getMeasuredWidth()) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            invalidate();
                        }
                    });
                }
            }
        });
    }

    /**
     * 初始化数据
     */
    private void initValues() {
        lastSampleXWithRectGap = lineLocationX;
        int middle = getMeasuredWidth() / 2;
        maxScrollX = Math.round(lastSampleXWithRectGap - getMeasuredWidth())+1 ;
        maxScrollX = maxScrollX > 0 ? maxScrollX : 0;
        minScrollX = 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        addSampleLine(audioSourceList);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //绘制采样波形
        drawSampleLine(canvas);
        //绘制垂直的线
        drawVerticalTargetLine(canvas);

    }

    /**
     * 绘制采样波形
     *
     * @param canvas canvas
     */
    private void drawSampleLine(Canvas canvas) {
        List<SampleLine> resultList = getDrawAudioSample(canvas);
        if (resultList != null && resultList.size() > 0) {
            for (SampleLine sampleLine : resultList) {
                if (sampleLine.startX + lineWidth / 2 <= centerLineX) {
                    linePaint.setColor(swipedColor);
                } else {
                    linePaint.setColor(unSwipeColor);
                }
                canvas.drawLine(sampleLine.startX, sampleLine.startY, sampleLine.stopX, sampleLine.stopY, linePaint);
            }
        }
    }

    private List<SampleLine> getDrawAudioSample(Canvas canvas) {
        if (sampleLineList.size() == 0) {
            return null;
        }
        List<SampleLine> resultList = new ArrayList<>();
        int rectWidthWithGap = lineWidth + rectGap;
        int recentlyRectIndex = getScrollX() / rectWidthWithGap;
        if (recentlyRectIndex < 0) {
            recentlyRectIndex = 0;
        } else if (recentlyRectIndex >= sampleLineList.size()) {
            recentlyRectIndex = sampleLineList.size() - 1;
        }

        float mixWidth = getScrollX() - rectWidthWithGap;
        float maxWidth = getScrollX() + canvas.getWidth() + rectWidthWithGap;
        for (int i = recentlyRectIndex; i < sampleLineList.size(); i++) {
            SampleLine next = sampleLineList.get(i);
            if (next.startX >= mixWidth && next.startX + lineWidth / 2 <= maxWidth) {
                resultList.add(next);
            }
            if (next.startX > maxWidth) {
                break;
            }
        }
        return resultList;
    }

    /**
     * 绘制垂直的线
     *
     * @param canvas canvas
     */
    private void drawVerticalTargetLine(Canvas canvas) {
        centerLineX = isAutoScroll ? getScrollX() + canvas.getWidth() / 2 : centerLineX;
        float startY = circleMarginTop;
        canvas.drawCircle(centerLineX, startY, circleRadius, centerTargetPaint);
        canvas.drawLine(centerLineX, startY, centerLineX, getMeasuredHeight(), centerTargetPaint);
    }


}
