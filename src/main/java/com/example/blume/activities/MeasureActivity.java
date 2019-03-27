package com.example.blume.activities;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.blume.R;

public class MeasureActivity extends AppCompatActivity {

    public static final String TAG = "MeasureActivity";

    private final int maxListSize = 10;
    private int listSize;
    private int index;
    private long currentTime;
    private long[] list;
    private boolean isFull;
    private boolean isTicking;

    private TextView mTextView;
    private TextView mSeekBarTextView;
    private SeekBar mSeekBar;
    private Button mBeatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"MeasureActivity created");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        reset();
        mTextView = findViewById(R.id.tvMeasure);
        mSeekBar = findViewById(R.id.sbMeasure);
        mSeekBarTextView = findViewById(R.id.tvMeasureSlider);
        mBeatButton = findViewById(R.id.btnBeat);

        listSize = mSeekBar.getProgress() + 2;
        list = new long[maxListSize];

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                listSize = progress + 2;
                reset();
                updateViewTexts();
                Log.d(TAG,"New list size: "+listSize);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        updateViewTexts();
    }

    public void onMeasureButtonClicked(View view) {
        measure();
        updateText();
    }

    public void onBeatButtonClicked(View view) {
        isTicking = !isTicking;
        updateViewTexts();
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

    private void updateViewTexts() {
        mSeekBarTextView.setText("Taps averaged: "+listSize);
        if(isTicking) {
            mBeatButton.setText(R.string.btnBeatOn);
        } else {
            mBeatButton.setText(R.string.btnBeatOff);
        }
    }

    private void reset() {
        index = 0;
        isFull = false;
        isTicking = false;
        currentTime = 0;
    }
}