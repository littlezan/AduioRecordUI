package com.example.administrator.aduiorecordui.activity.visualizer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.example.administrator.aduiorecordui.R;
import com.example.administrator.aduiorecordui.activity.BasePlayerActivity;
import com.littlezan.recordui.visualizer.CircleVisualizerView;

/**
 * ClassName: CirclerVisualizerActivity
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2018-12-05  15:30
 */
public class CircleVisualizerActivity extends BasePlayerActivity implements View.OnClickListener {

    private CircleVisualizerView circleVisualizerView;

    public static void start(Context context) {
        context.startActivity(new Intent(context, CircleVisualizerActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_visualizer);
        preparePlay(R.raw.test);
        initView();
    }

    private void initView() {
        circleVisualizerView = findViewById(R.id.circleVisualizerView);
        findViewById(R.id.btnPlay).setOnClickListener(this);
        findViewById(R.id.btnPause).setOnClickListener(this);
        findViewById(R.id.btnResume).setOnClickListener(this);
        findViewById(R.id.btnStop).setOnClickListener(this);
    }

    @Override
    protected void audioSessionId(int audioSessionId) {
        if (circleVisualizerView != null) {
            circleVisualizerView.initVisualizer(audioSessionId);
        }
    }

    @Override
    protected void playerStateChanged(boolean playWhenReady) {
        if (circleVisualizerView != null) {
            circleVisualizerView.setVisualizerEnable(playWhenReady);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPlay:
                seekToPlay(0);
                break;
            case R.id.btnPause:
                pausePlay();
                break;
            case R.id.btnResume:
                resumePlay();
                break;
            case R.id.btnStop:
                stopPlay();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        circleVisualizerView.setVisualizerEnable(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        circleVisualizerView.setVisualizerEnable(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        circleVisualizerView.release();
    }
}
