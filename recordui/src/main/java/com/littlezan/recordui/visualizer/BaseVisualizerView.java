package com.littlezan.recordui.visualizer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.audiofx.Visualizer;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.littlezan.recordui.R;

import java.util.Arrays;

/**
 * ClassName: BaseVisualizerView
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2018-12-05  10:05
 */
public abstract class BaseVisualizerView extends View {

    private static final String TAG = "BaseVisualizerView";


    protected byte[] bytes;
    protected Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected Visualizer visualizer;
    protected boolean visualizerEnable = false;


    protected @ColorInt
    int lineColor;


    public BaseVisualizerView(Context context) {
        this(context, null);
    }

    public BaseVisualizerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseVisualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
        initPaint();
    }

    private void init(Context context, AttributeSet attributeSet) {
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.BaseVisualizerView);
        lineColor = typedArray.getColor(R.styleable.BaseVisualizerView_lineColor, Color.RED);
        typedArray.recycle();
    }

    protected void initPaint() {
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(lineColor);
        paint.setStrokeCap(Paint.Cap.ROUND);

    }

    long lastTime = 0;

    public void initVisualizer(int audioSessionId) {
        if (audioSessionId > 0) {
            visualizer = new Visualizer(audioSessionId);
            visualizer.setCaptureSize(getVisualizerCaptureSizeRange());
            Log.e(TAG, "initVisualizer: lll getCaptureSizeRange = " + Arrays.toString(Visualizer.getCaptureSizeRange()));
            Log.e(TAG, "initVisualizer: lll getMaxCaptureRate = " + Visualizer.getMaxCaptureRate());
            visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                @Override
                public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                    Log.e(TAG, "onWaveFormDataCapture: lll samplingRate = " + samplingRate);
                    Log.e(TAG, "onWaveFormDataCapture: lll bytes.length = " + bytes.length + ", bytes = " + Arrays.toString(bytes));
                    long duration = System.currentTimeMillis() - lastTime;
                    Log.e(TAG, "onWaveFormDataCapture: lll duration = " + duration);
                    lastTime = System.currentTimeMillis();
                    BaseVisualizerView.this.bytes = bytes;
                    invalidate();
                }

                @Override
                public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                }
            }, Visualizer.getMaxCaptureRate() / 2, true, false);
            visualizer.setEnabled(visualizerEnable);
        }
    }

    protected int getVisualizerCaptureSizeRange() {
        return Visualizer.getCaptureSizeRange()[1];
    }

    public void release() {
        visualizer.release();
    }

    public Visualizer getVisualizer() {
        return visualizer;
    }


    public void setVisualizerEnable(boolean enable) {
        visualizerEnable = enable;
        if (visualizer != null) {
            visualizer.setEnabled(enable);
        }
    }
}
