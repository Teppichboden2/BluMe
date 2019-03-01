package com.example.blume;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter ba;
    private int REQUEST_ENABLE_BT = 42;
    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ba = BluetoothAdapter.getDefaultAdapter();
    }

    public void onMainButton1Clicked(View view) {
        if(ba == null) {
            Log.e(TAG,"Bluetooth not supported on this device");
            makeToast("Bluetooth not supported on this device!");
            this.finishAffinity();
        }

        if(!ba.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_ENABLE_BT) {
            if(resultCode == RESULT_OK) {
                makeToast("Bluetooth enabled");
            } else if(resultCode == RESULT_CANCELED) {
                Log.d(TAG,"Bluetooth activation canceled");
                this.finishAffinity();
            }
        }
        super.onActivityResult(requestCode,resultCode,data);
    }


    private void makeToast(String msg) {
        Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
    }
}
