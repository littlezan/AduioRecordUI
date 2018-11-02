package com.example.administrator.aduiorecordui.activity.record.verticallinemove.second;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.example.administrator.aduiorecordui.R;
import com.example.administrator.aduiorecordui.fragment.RecordAudioFragment;

/**
 * ClassName: AudioRecordActivity
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2018-05-29  11:14
 */
public class AudioRecordActivity extends AppCompatActivity {


    private AppBarLayout appBarLayout;
    private Toolbar toolbar;
    private TextView tvNavCenter;
    private TextView tvNavRight;

    Context context;
    private RecordAudioFragment recordAudioFragment;

    public static void start(Context context) {
        Intent intent = new Intent(context, AudioRecordActivity.class);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_audio_record);
        context = this;
        initView();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        recordAudioFragment = RecordAudioFragment.newInstance();
        fragmentTransaction.replace(R.id.fl_container, recordAudioFragment);
        fragmentTransaction.commitNow();

        recordAudioFragment.setFragmentInteraction(new RecordAudioFragment.FragmentInteraction() {
            @Override
            public void canUseAudio(boolean canUseAudio) {
                tvNavRight.setEnabled(canUseAudio);
            }
        });


    }

    private void initView() {
        appBarLayout = findViewById(R.id.app_bar_layout);
        toolbar = findViewById(R.id.toolbar);
        tvNavCenter = findViewById(R.id.tv_nav_center);
        tvNavRight = findViewById(R.id.tv_nav_right);

        tvNavCenter.setText("录音");
        tvNavRight.setText("完成");
        tvNavRight.setTextColor(ContextCompat.getColorStateList(this, R.color.text_enable_blue));
        tvNavRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioRecordPreviewActivity.start(context, recordAudioFragment.getActiveRecordFilePath(), recordAudioFragment.getDecibelList(),
                        recordAudioFragment.getRecordSamplingFrequency(), recordAudioFragment.getRecordTimeInMillis());
            }
        });
    }
}
