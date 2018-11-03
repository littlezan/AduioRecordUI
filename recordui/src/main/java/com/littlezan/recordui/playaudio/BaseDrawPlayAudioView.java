package com.littlezan.recordui.playaudio;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.littlezan.recordui.playaudio.mode.PlaySampleLineMode;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: BaseDrawPlayAudioView
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2018-11-03  10:41
 */
public abstract class BaseDrawPlayAudioView extends BasePlayAudioView {



    public BaseDrawPlayAudioView(Context context) {
        super(context);
    }

    public BaseDrawPlayAudioView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseDrawPlayAudioView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
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
        List<PlaySampleLineMode> resultList = getDrawAudioSample(canvas);
        if (resultList != null && resultList.size() > 0) {
            for (PlaySampleLineMode sampleLine : resultList) {
                if (sampleLine.startX + lineWidth / 2 <= centerLineX) {
                    linePaint.setColor(swipedColor);
                } else {
                    linePaint.setColor(unSwipeColor);
                }
                canvas.drawLine(sampleLine.startX, sampleLine.startY, sampleLine.stopX, sampleLine.stopY, linePaint);
            }
        }
    }

    private List<PlaySampleLineMode> getDrawAudioSample(Canvas canvas) {
        if (sampleLineList.size() == 0) {
            return null;
        }
        List<PlaySampleLineMode> resultList = new ArrayList<>();
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
            PlaySampleLineMode next = sampleLineList.get(i);
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
    public abstract void drawVerticalTargetLine(Canvas canvas);

}
