package com.littlezan.recordui.recordaudio.recordviews;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;

import com.littlezan.recordui.recordaudio.BaseDrawAudioRecordView;
import com.littlezan.recordui.recordaudio.mode.RecordSampleLineModel;

import java.util.concurrent.TimeUnit;

/**
 * ClassName: BaseVerticalLineMoveAnimatorView
 * Description: 垂直线移动
 *
 * @author 彭赞
 * @version 1.0
 * @since 2018-08-22  11:25
 */
public class VerticalLineMoveAudioRecordView extends BaseDrawAudioRecordView {

    public VerticalLineMoveAudioRecordView(Context context) {
        super(context);
    }

    public VerticalLineMoveAudioRecordView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public VerticalLineMoveAudioRecordView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void setCanScrollX() {
        int widthMiddle = getMeasuredWidth() / 2;
        maxScrollX = lineLocationX < widthMiddle ? rectGap : lineLocationX - widthMiddle;
        minScrollX = lineLocationX < widthMiddle ? -lineLocationX : -widthMiddle - rectGap;
    }


    @Override
    protected void drawCenterVerticalLine(Canvas canvas) {
        float circleX = translateVerticalLineX;
        int canvasMiddle = canvas.getWidth() / 2;
        if (isStartVerticalLineTranslate || circleX < canvasMiddle) {
            circleX = circleX + getScrollX();
        } else if (isStartRecordTranslateCanvas || circleX >= getScrollX() + canvasMiddle - rectGap) {
            circleX = getScrollX() + canvasMiddle + rectGap;
        }
        centerLineX = circleX;
        float topCircleY = ruleHorizontalLineHeight - middleCircleRadius;
        float bottomCircleY = canvas.getHeight() / 2 + (canvas.getHeight() / 2 - ruleHorizontalLineHeight) + middleCircleRadius;

        // 上圆
        canvas.drawCircle(circleX, topCircleY + middleCircleRadius, middleCircleRadius, middleVerticalLinePaint);
        //下圆
        canvas.drawCircle(circleX, bottomCircleY - middleCircleRadius, middleCircleRadius, middleVerticalLinePaint);
        //垂直 直线
        canvas.drawLine(circleX, topCircleY, circleX, bottomCircleY, middleVerticalLinePaint);

        if (recordCallBack != null) {
            if (centerLineX <= 0) {
                centerTimeMillis = 0;
            } else {
                centerTimeMillis = (long) (centerLineX * 1000L / (recordSamplingFrequency * (lineWidth + rectGap)));
            }

            if (lineLocationX > 0) {
                if (isPlayingRecord()) {
                    if (centerLineX >= lineLocationX) {
                        recordCallBack.onFinishPlayingRecord();
                    }
                }
                recordCallBack.onCenterLineTime(centerTimeMillis);
            }
        }
    }

    @Override
    protected float getCenterVerticalLineXWhileTranslateRecord() {
        return getScrollX() + getMeasuredWidth() / 2 + rectGap;
    }

    @Override
    protected float getOnTickTranslateXWhileTranslateRecord() {
        return getScrollX() + getMeasuredWidth() / 2;
    }

    @Override
    public void startRecord() {
        if (!isRecording) {
            isTouching = false;
            setCanScrollX();
            if (currentRecordTime >= TimeUnit.MINUTES.toMillis(recordTimeInMinutes)) {
                return;
            }
            scrollTo(maxScrollX, 0);
            isRecording = true;
            //移动画布
            float lastSampleLineRightX = getLastSampleLineRightX();
            int middleX = getScrollX() + getMeasuredWidth() / 2;
            if (lastSampleLineRightX >= middleX - lineWidth - rectGap) {
                startRecordTranslateCanvas();
                isAutoScroll = true;
            } else {
                startVerticalLineTranslate();
                isAutoScroll = false;
            }
            if (recordCallBack != null) {
                recordCallBack.onStartRecord();
            }
        }
    }

    @Override
    public void stopRecord() {
        if (isRecording) {
            isTouching = false;
            isRecording = false;
            isAutoScroll = false;
            isStartVerticalLineTranslate = false;
            isStartRecordTranslateCanvas = false;
            overScroller.abortAnimation();
            if (animator != null) {
                animator.removeAllListeners();
                animator.cancel();
            }
            setCanScrollX();
            if (sampleLineList.size() > 0 && !deleteIndexList.contains(sampleLineList.size())) {
                deleteIndexList.add(sampleLineList.size());
                RecordSampleLineModel sampleLineModel = sampleLineList.get(sampleLineList.size() - 1);
                if (showStopFlag) {
                    sampleLineModel.stopFlag = true;
                }
            }
            if (recordCallBack != null) {
                recordCallBack.onStopRecord();
            }
        }
    }

    protected volatile boolean isStartVerticalLineTranslate;

    private void startVerticalLineTranslate() {
        if (isRecording && !isStartVerticalLineTranslate) {
            isStartVerticalLineTranslate = true;
            float startX = translateVerticalLineX;
            float endX = getMeasuredWidth() / 2;
            double dx = Math.abs(endX - startX);
            final double duration = 1000 * dx / (recordSamplingFrequency * (lineWidth + rectGap));
            animator = ObjectAnimator.ofFloat(this, "translateVerticalLineX", startX, endX);
            animator.setInterpolator(new LinearInterpolator());
            animator.setDuration((long) Math.floor(duration));
            isAutoScroll = true;
            animator.removeAllListeners();
            animator.addListener(new AnimatorListenerAdapter() {


                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    isStartVerticalLineTranslate = false;
                    //录制暂停
                    invalidate();
                    //录制暂停
                    animator.removeAllListeners();
                    startRecordTranslateCanvas();
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    isStartVerticalLineTranslate = false;
                    //录制暂停
                    invalidate();
                    animator.removeAllListeners();
                    startRecordTranslateCanvas();
                }
            });
            animator.start();
        } else {
            if (animator != null) {
                animator.cancel();
            }
        }
    }


    private void startRecordTranslateCanvas() {
        if (isRecording && !isStartRecordTranslateCanvas) {
            isStartRecordTranslateCanvas = true;
            maxScrollX = Integer.MAX_VALUE;
            float startX = getScrollX();
            //小于半屏的时候，要重新计算偏移量，因为有个左滑的动作
            float endX = maxLength - getMeasuredWidth() / 2;
            float dx = Math.abs(endX - startX);
            final double duration = 1000 * dx / (recordSamplingFrequency * (lineWidth + rectGap));
            animator = ObjectAnimator.ofFloat(this, "translateX", startX, endX);
            animator.setInterpolator(new LinearInterpolator());
            animator.setDuration((long) Math.floor(duration));
            animator.removeAllListeners();
            animator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    isStartRecordTranslateCanvas = false;
                    //录制暂停
                    invalidate();
                    animator.removeAllListeners();
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    isStartRecordTranslateCanvas = false;
                    //录制暂停
                    invalidate();
                    animator.removeAllListeners();
                    if (currentRecordTime >= TimeUnit.MINUTES.toMillis(recordTimeInMinutes) - 100) {
                        //结束录音
                        stopRecord();
                        if (recordCallBack != null) {
                            recordCallBack.onFinishRecord();
                        }
                    }
                }
            });
            animator.start();
        } else {
            if (animator != null) {
                animator.cancel();
            }
        }
    }

    /**
     * 采样
     *
     * @param translateX 移动距离
     */
    @Override
    protected void onTick(float translateX) {
        if (isRecording) {
            long duration = (long) (translateX * recordTimeInMillis / maxLength);
            currentRecordTime = duration;
            if (currentRecordTime < recordTimeInMillis) {
                //录音中
                //采样
                if (recordCallBack != null) {
                    if (getSampleCount() >= recordTimeInMillis * recordSamplingFrequency) {
                        //结束录音
                        stopRecord();
                        if (recordCallBack != null) {
                            recordCallBack.onFinishRecord();
                        }
                    } else if (duration > getSampleCount() * recordDelayMillis) {
                        makeSampleLine(recordCallBack.getSamplePercent());
                    }
                }
            } else {
                //结束录音
                stopRecord();
                if (recordCallBack != null) {
                    recordCallBack.onFinishRecord();
                }
            }
            if (recordCallBack != null) {
                recordCallBack.onRecordCurrent(currentRecordTime, currentRecordTime);
            }
        }

    }

    @Override
    public void deleteLastRecord() {
        super.deleteLastRecord();
        if (deleteIndexList.size() > 1) {
            Integer index = 0;
            for (int i = deleteIndexList.size() - 1; i >= 0; i--) {
                Integer integer = deleteIndexList.get(i);
                if (integer < sampleLineList.size()) {
                    index = integer;
                    break;
                }
            }
            if (index > 0) {
                sampleLineList.subList(index, sampleLineList.size()).clear();
                deleteIndexList.subList(deleteIndexList.indexOf(index) + 1, deleteIndexList.size()).clear();
                if (sampleLineList.size() > 0) {
                    lineLocationX = Math.round(sampleLineList.get(sampleLineList.size() - 1).startX + lineWidth / 2 + rectGap);
                    setCanScrollX();
                    int middle = getMeasuredWidth() / 2;
                    if (lineLocationX < middle) {
                        scrollTo(maxScrollX, 0);
                        translateVerticalLineX = lineLocationX;
                    } else {
                        scrollTo(maxScrollX, 0);
                        translateVerticalLineX = getScrollX() + middle + rectGap;
                    }
                    currentRecordTime = (long) (translateVerticalLineX * recordTimeInMillis / maxLength);
                } else {
                    reset();
                }
            }
        } else {
            reset();
        }

    }


}
