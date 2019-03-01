package com.example.blume;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter blAdapter;
    private int REQUEST_ENABLE_BT = 42;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        blAdapter = BluetoothAdapter.getDefaultAdapter();
        enableBluetooth();
    }

    public void enableBluetooth() {
        if(blAdapter == null) {
            String log = "Bluetooth not supported on this device";
            Log.e(TAG,log);
            makeToast(log);
            this.finishAffinity();
        }

        if(!blAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
        }
    }

    private void makeToast(String msg) {
        Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
    }

    public void onDiscoverableButtonClicked(View view) {
        Log.d(TAG, "Making the device discoverable for 300 seconds.");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    public void onExitButtonClicked(View view) {
        this.finishAffinity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_ENABLE_BT) {
            if(resultCode == RESULT_OK) {
                String log = "Bluetooth enabled";
                Log.d(TAG,log);
                makeToast(log);
            } else if(resultCode == RESULT_CANCELED) {
                Log.d(TAG,"Bluetooth activation canceled");
                this.finishAffinity();
            }
        }
        super.onActivityResult(requestCode,resultCode,data);
    }

}
