package com.example.blume.activities;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.blume.R;

public class MeasureActivity extends AppCompatActivity {

    public static final String TAG = "MeasureActivity";

    private final int maxListSize = 10;
    private int listSize = 5;
    private int index = 0;
    private long currentTime = 0;
    private long[] list;
    private boolean isFull = false;

    private TextView mTextView;
    private TextView mSeekBarTextView;
    private SeekBar mSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"MeasureActivity created");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mTextView = findViewById(R.id.tvMeasure);
        mSeekBar = findViewById(R.id.sbMeasure);
        mSeekBarTextView = findViewById(R.id.tvMeasureSlider);

        listSize = mSeekBar.getProgress() + 2;
        list = new long[maxListSize];

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                listSize = progress + 2;
                index = 0;
                isFull = false;
                currentTime = 0;
                updateSeekBarTextView();
                Log.d(TAG,"New list size: "+listSize);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        updateSeekBarTextView();
    }

    public void onMeasureButtonClicked(View view) {
        measure();
        updateText();
    }

    private void measure() {
        if(currentTime == 0) {
            currentTime = System.nanoTime();
        } else {
            long time = currentTime;
            currentTime = System.nanoTime();

            // Beats per minute (BPM)
            list[index] = 60000 / ((currentTime - time) / 1000000);

            if(!isFull && index == listSize-1) isFull = true;
            index = (index+1)%listSize;
        }
    }

    private void updateText() {
        long result = 0;
        if(!isFull) {
            for(int i=0; i<index; ++i) {
                result += list[i];
            }
            result /= (index == 0 ? 1 : index);
        } else {
            for(int i=0; i<listSize; ++i) {
                result += list[i];
            }
            result /= listSize;
        }
        mTextView.setText("BPM: "+result);
    }

    private void updateSeekBarTextView() {
        mSeekBarTextView.setText("Taps averaged: "+listSize);
    }
}