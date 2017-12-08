package com.example.administrator.aduiorecordui.rule;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * ClassName: HorizontalRuler
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2017-12-07  19:52
 */
public abstract class BaseHorizontalRuler extends View {

    private static final String TAG = BaseHorizontalRuler.class.getSimpleName();
    private static final int AUTO_SCROLL_DISTANCE = 1;

    private float mLastX = 0;
    protected boolean isAutoScroll;

    public BaseHorizontalRuler(Context context) {
        super(context);
    }

    public BaseHorizontalRuler(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseHorizontalRuler(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float currentX = event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = currentX;
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = mLastX - currentX;
                mLastX = currentX;
                scrollBy((int) (moveX), 0);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
    }

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            scrollBy(AUTO_SCROLL_DISTANCE, 0);
            handler.postDelayed(runnable, 10);
        }
    };

    public void startAutoScroll() {
        if (!isAutoScroll) {
            handler.post(runnable);
            isAutoScroll = true;
        }
    }

    public void stopAutoScroll() {
        if (isAutoScroll) {
            handler.removeCallbacks(runnable);
            isAutoScroll = false;
        }
    }



    /**
     * 生成矩形
     * @param height 矩形高度
     */
    public abstract void makeRect(int height);


}
