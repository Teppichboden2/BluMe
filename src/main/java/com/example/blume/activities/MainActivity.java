package com.example.blume;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import com.example.blume.activities.ClientActivity;
import com.example.blume.activities.MeasureActivity;
import com.example.blume.activities.ServerActivity;
import com.example.blume.bluetooth.BluetoothConnectionService;
import com.example.blume.activities.DeviceListAdapter;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String MY_UUID = "e5f7d62d-1941-40d8-80f5-dbc084598d2e";
    private static final int REQUEST_ENABLE_BT = 42;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothConnectionService mBluetoothConnection;

    private BluetoothDevice mBluetoothDevice;
    private ArrayList<BluetoothDevice> mDevices = new ArrayList<>();

    private DeviceListAdapter mDeviceListAdapter;

    private final BroadcastReceiver BondBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if(mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG,"BroadcastReceiver: BOND_BONDED");
                    mBluetoothDevice = mDevice;
                }

                if(mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG,"BroadcastReceiver: BOND_BONDING");
                }

                if(mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG,"BroadcastReceiver: BOND_NONE");
                }

            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Bluetooth not supported
        if(mBluetoothAdapter == null) {
            makeToast("It appears that this device doesn't support Bluetooth");
            this.finishAffinity();
        }

        mBluetoothConnection = new BluetoothConnectionService(this);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG,"onDestroy called");
        super.onDestroy();
        //unregisterReceiver(BondBR);
    }


    private void startConnection() {
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");
        mBluetoothConnection.startClient(mBluetoothDevice,MY_UUID);
    }

    public void btnDiscover(View view) {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery.");

            //check BT permissions in manifest
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();

        }
        if(!mBluetoothAdapter.isDiscovering()){

            //check BT permissions in manifest
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
        }
    }

    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for com.example.blume.bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     *
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    public void onServerButtonClicked(View view) {
        /*tryToEnableBluetooth();

        if(mBluetoothAdapter.isEnabled()) {
            mBluetoothConnection.startServer();
        }*/

        Log.d(TAG,"On ServerButton clicked");
        Intent intent = new Intent(this, ServerActivity.class);
        startActivity(intent);
    }

    public void onClientButtonClicked(View view) {
        Log.d(TAG,"On client button clicked");
        /*tryToEnableBluetooth();

        Set<BluetoothDevice> paired = mBluetoothAdapter.getBondedDevices();

        for(BluetoothDevice temp : paired) {
            mBluetoothDevice = temp;
            break;
        }

        Log.d(TAG,"Name: "+mBluetoothDevice.getName());
        Log.d(TAG,"Address: "+mBluetoothDevice.getAddress());
        Log.d(TAG,"Trying to connect...");

        this.startConnection();*/

        Log.d(TAG,"On ServerButton clicked");
        Intent intent = new Intent(this, ClientActivity.class);
        startActivity(intent);
    }

    public void onMeasureActivityButtonClicked(View view) {
        // String msg = "Dennis Stinke Vinke";
        // mBluetoothConnection.write(msg.getBytes(Charset.defaultCharset()));

        Log.d(TAG,"onMeasureActivityButtonClicked");
        Intent intent = new Intent(this, MeasureActivity.class);
        startActivity(intent);
    }

    public void onExitButtonClicked(View view) {
        Log.d(TAG,"On exit button clicked");
        mBluetoothConnection.cancel();
        this.finishAffinity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_ENABLE_BT) {
            if(resultCode == RESULT_OK) {
                Log.d(TAG,"onActivityResult: RESULT_OK");
            } else if(resultCode == RESULT_CANCELED) {
                Log.d(TAG,"onActivityResult: RESULT_CANCELED");
                this.finishAffinity();
            }
        }
        super.onActivityResult(requestCode,resultCode,data);
    }

    private void makeToast(String msg) {
        Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
    }

    private void tryToEnableBluetooth() {
        if(!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
        }
    }

    public void makeDiscoverable() {
        Log.d(TAG, "makeDiscoverable: Making device discoverable for 300 seconds.");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }
}


























































