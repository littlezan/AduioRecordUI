package com.littlezan.recordui.visualizer;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

/**
 * ClassName: PlayAudioEffectView
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2018-12-03  11:05
 */
public class CircleVisualizerView extends BaseVisualizerView {

    private static final String TAG = "CircleVisualizerView";

    private static final int MAX_OFFSET_LENGTH = 120;
    private static final int MAX_WAVE_VALUE = 128;

    private static final int POINT_COUNT = 132;
    private static final double INTERVAL_ANGLE = 360 / (float) POINT_COUNT;
    private static final int LINE_MULTIPLE_SIZE = 4;
    private static final int CIRCLE_MULTIPLE_SIZE = 2;

    private float[] linePoints = new float[POINT_COUNT * LINE_MULTIPLE_SIZE];
    private float[] circlePoints = new float[POINT_COUNT * CIRCLE_MULTIPLE_SIZE];

    private int radius = -1;


    public CircleVisualizerView(Context context) {
        super(context);
    }

    public CircleVisualizerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleVisualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (radius == -1) {
            radius = getHeight() < getWidth() ? getHeight() : getWidth();
            radius = (int) (radius * 0.65 / 2);
        }
        double circumference = 2 * Math.PI * radius;
        float strokeWidth = (float) ((circumference / 120) / 2);
        paint.setStrokeWidth(strokeWidth);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        double angle = 0;
        if (bytes != null) {
            for (int i = 0; i < POINT_COUNT; i++, angle += INTERVAL_ANGLE) {
                int offsetLength = (int) (MAX_OFFSET_LENGTH * ((-Math.abs(bytes[i]) + MAX_WAVE_VALUE) / (float) MAX_WAVE_VALUE));
                Log.e(TAG, "onDraw: lll offsetLength = " + offsetLength + ", bytes[x] = " + bytes[i] + ", SIZE_RANGE = " + MAX_WAVE_VALUE);
                linePoints[i * LINE_MULTIPLE_SIZE] = (float) (getWidth() / 2 + radius * Math.cos(Math.toRadians(angle)));
                linePoints[i * LINE_MULTIPLE_SIZE + 1] = (float) (getHeight() / 2 + radius * Math.sin(Math.toRadians(angle)));
                linePoints[i * LINE_MULTIPLE_SIZE + 2] = (float) (getWidth() / 2 + (radius + offsetLength) * Math.cos(Math.toRadians(angle)));
                linePoints[i * LINE_MULTIPLE_SIZE + 3] = (float) (getHeight() / 2 + (radius + offsetLength) * Math.sin(Math.toRadians(angle)));
                if (offsetLength <= 0) {
                    canvas.drawPoint(linePoints[i * LINE_MULTIPLE_SIZE], linePoints[i * LINE_MULTIPLE_SIZE + 1], paint);
                }
            }
            canvas.drawLines(linePoints, paint);
        } else {
            for (int i = 0; i < POINT_COUNT; i++, angle += INTERVAL_ANGLE) {
                circlePoints[i * CIRCLE_MULTIPLE_SIZE] = (float) (getWidth() / 2 + radius * Math.cos(Math.toRadians(angle)));
                circlePoints[i * CIRCLE_MULTIPLE_SIZE + 1] = (float) (getHeight() / 2 + radius * Math.sin(Math.toRadians(angle)));
                Log.e(TAG, "onDraw: lll circlePoints x" + (i * CIRCLE_MULTIPLE_SIZE) + " = " + circlePoints[i * CIRCLE_MULTIPLE_SIZE]);
                Log.e(TAG, "onDraw: lll circlePoints y" + (i * CIRCLE_MULTIPLE_SIZE) + " = " + circlePoints[i * CIRCLE_MULTIPLE_SIZE + 1]);
            }
            canvas.drawPoints(circlePoints, paint);
        }
    }

    @Override
    protected int getVisualizerCaptureSizeRange() {
        return POINT_COUNT;
    }
}
