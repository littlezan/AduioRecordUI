package com.example.administrator.aduiorecordui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.example.administrator.aduiorecordui.R;
import com.example.administrator.aduiorecordui.fragment.RecordAudioWithDeleteFragment;

/**
 * ClassName: RecordAudioWithDeleteActivity
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2018-06-25  15:07
 */
public class RecordAudioWithDeleteActivity extends AppCompatActivity {

    public static void start(Context context) {
        Intent intent = new Intent(context, RecordAudioWithDeleteActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_record);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        RecordAudioWithDeleteFragment  recordAudioFragment = RecordAudioWithDeleteFragment.newInstance();
        fragmentTransaction.replace(R.id.fl_container, recordAudioFragment);
        fragmentTransaction.commitNow();
    }
}
