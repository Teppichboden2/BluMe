package activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.blume.R;

public class MeasureActivity extends AppCompatActivity {

    public static final String TAG = "MeasureActivity";

    private final int listSize = 10;
    private int index = 0;
    private long currentTime = 0;
    private long[] list;
    private boolean isFull = false;

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"MeasureActivity created");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure);
        mTextView = findViewById(R.id.tvMeasure);
        list = new long[listSize];
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

            if(!isFull && index == 9) isFull = true;
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
}