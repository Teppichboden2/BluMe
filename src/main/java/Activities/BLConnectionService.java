package Activities;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class BLConnectionService {

    private static final String TAG = "BLConnectionService";
    private static final String APPNAME = "BLUME";
    private static final String MY_UUID = "e5f7d62d-1941-40d8-80f5-dbc084598d2e";

    private final BluetoothAdapter mBluetoothAdapter;
    private Context mContext;

    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    private BluetoothDevice mBluetoothDevice;
    private UUID deviceUUID;
    private ProgressDialog mProgressDialog;

    public BLConnectionService(Context context) {
        this.mContext = context;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.start();
    }
    public void connected(BluetoothSocket blSocket, BluetoothDevice bld) {
        Log.d(TAG,"connected: Starting");
        mConnectedThread = new ConnectedThread(blSocket);
        mConnectedThread.start();
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mBluetoothServerSocket;

        public AcceptThread() {
            BluetoothServerSocket temp = null;

            try {
                temp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(APPNAME, UUID.fromString(MY_UUID));
                Log.d(TAG,"Setting up Server using: "+MY_UUID);
            } catch (IOException e) {
                Log.e(TAG,"Exception in AcceptThread(): "+e.getMessage());
            }

            mBluetoothServerSocket = temp;
        }

        @Override
        public void run() {
            BluetoothSocket mSocket = null;
            Log.d(TAG,"AcceptThread running");
            Log.d(TAG,"RFCOM Server socket start");

            try {
                mSocket = mBluetoothServerSocket.accept();
                Log.d(TAG,"RFCOM Server socket accepted connection");
            } catch(IOException e) {
                Log.e(TAG,"Exception in AcceptThread.run: "+e.getMessage());
            }

            if(mSocket != null) {
                connected(mSocket, mBluetoothDevice);
            }

        }

        public void cancel() {
            Log.d(TAG,"Cancelling AcceptThread");
            try {
                mBluetoothServerSocket.close();
            } catch(IOException e) {
                Log.e(TAG,"Exception in AcceptThread.cancel: "+e.getMessage());
            }
        }
    }
    private class ConnectThread extends Thread {
        private BluetoothSocket bls;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG,"ConnectThread started");
            mBluetoothDevice = device;
            deviceUUID = uuid;
        }

        @Override
        public void run() {
            BluetoothSocket temp = null;
            Log.i(TAG,"ConnectThread running");

            try {
                temp = mBluetoothDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch(IOException e) {
                Log.e(TAG,"Exception in ConnectThread.run1: "+e.getMessage());
            }

            bls = temp;
            mBluetoothAdapter.cancelDiscovery();

            try {
                bls.connect();
                Log.d(TAG,"ConnectThread connected");
            } catch (IOException e) {
                Log.e(TAG,"Exception in ConnectThread.run2: "+e.getMessage());
                try {
                    bls.close();
                } catch (IOException e1) {
                    Log.e(TAG,"Unable to close connection in socket");
                }
            }

            connected(bls,mBluetoothDevice);
        }

        public void cancel() {
            try {
                Log.d(TAG,"Closing client socket");
                bls.close();
            } catch (IOException e) {
                Log.e(TAG,"Exception in ConnectThread.cancel: "+e.getMessage());
            }
        }
    }
    private class ConnectedThread extends Thread {
        private final BluetoothSocket bls;
        private final InputStream mInputStream;
        private final OutputStream mOutputStream;
        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG,"ConnectedThread starting");

            bls = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            mProgressDialog.dismiss();

            try {
                tmpIn = bls.getInputStream();
                tmpOut = bls.getOutputStream();
            } catch(IOException e) {
                Log.e(TAG,"Exception in ConnectedThread(): "+e.getMessage());
            }

            mInputStream = tmpIn;
            mOutputStream = tmpOut;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while(true) {
                try {
                    bytes = mInputStream.read(buffer);
                    String incomingMessage = new String(buffer,0,bytes);
                    Log.d(TAG,"InputStream: "+incomingMessage);
                } catch (IOException e) {
                    Log.e(TAG,"Error reading from Inputstream: "+e.getMessage());
                    break;
                }
            }
        }

        public void cancel() {
            try {
                bls.close();
            } catch (IOException e) {}
        }

        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG,"Writing to Ouputstream: "+text);
            try {
                mOutputStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG,"Error writing to OutputStream: "+e.getMessage());
            }
        }
    }

    public synchronized  void start() {
        Log.d(TAG,"Start");

        if(mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if(mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
    }
    public void startClient(BluetoothDevice device, String uuid) {
        Log.d(TAG,"Client started");
        mProgressDialog = ProgressDialog.show(mContext,"Connecting Bluetooth","Please wait...",true);
        mConnectThread = new ConnectThread(device, UUID.fromString(uuid));
        mConnectThread.start();
    }

    public void write(byte[] out) {
        ConnectedThread r;
        Log.d(TAG,"Write called.");
        mConnectedThread.write(out);
    }
}
