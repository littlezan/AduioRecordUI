package com.example.administrator.aduiorecordui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.administrator.aduiorecordui.playaudio.PlayAudioView;

import java.util.ArrayList;
import java.util.Random;

/**
 * ClassName: PlayAudioActivity
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2017-12-14  19:59
 */
public class PlayAudioActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_audio);

        final PlayAudioView playAudioView = findViewById(R.id.play_audio_view);
        Button play = findViewById(R.id.play);
        Button stop = findViewById(R.id.stop);
        Button reset = findViewById(R.id.reset);
        Button playMiddle = findViewById(R.id.play_middle);

        ArrayList<Float> audioSourceList = new ArrayList<>();
        fakeData(audioSourceList);
        playAudioView.setAudioSource(audioSourceList);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudioView.startPlay();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudioView.stopPlay();
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudioView.reset();
            }
        });

        playMiddle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudioView.setPlayingTime(max/2/10*1000);
            }
        });
    }

    int max = 100;

    private void fakeData(ArrayList<Float> audioSourceList) {
        for (int i = 0; i < max; i++) {
            audioSourceList.add(new Random().nextFloat());
        }
    }
}
