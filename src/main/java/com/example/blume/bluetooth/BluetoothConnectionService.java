package com.example.blume.bluetooth;

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

public class BluetoothConnectionService {

    private static final String TAG = "BluetoothConnectionService";
    private static final String APPNAME = "BLUME";
    private static final String MY_UUID = "e5f7d62d-1941-40d8-80f5-dbc084598d2e";

    private final BluetoothAdapter mBluetoothAdapter;
    private Context mContext;

    private ServerThread mServerThread;
    private ClientThread mClientThread;
    private ConnectionThread mConnectionThread;

    //private BluetoothDevice mBluetoothDevice;
    //private UUID deviceUUID;
    private ProgressDialog mProgressDialog;

    public BluetoothConnectionService(Context context) {
        this.mContext = context;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void connected(BluetoothSocket blSocket) {
        Log.d(TAG,"connected: Starting");
        mConnectionThread = new ConnectionThread(blSocket);
        mConnectionThread.start();
    }

    public void cancel() {
        if(mServerThread != null) {
            mServerThread.cancel();
        }

        if(mClientThread != null) {
            mClientThread.cancel();
        }

        if(mConnectionThread != null) {
            mConnectionThread.cancel();
        }
    }

    private class ServerThread extends Thread {
        private final BluetoothServerSocket mBluetoothServerSocket;

        public ServerThread() {
            BluetoothServerSocket temp = null;

            try {
                temp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(APPNAME, UUID.fromString(MY_UUID));
                Log.d(TAG,"Setting up Server using: "+MY_UUID);
            } catch (IOException e) {
                Log.e(TAG,"Exception in ServerThread(): "+e.getMessage());
            }

            mBluetoothServerSocket = temp;
        }

        @Override
        public void run() {
            BluetoothSocket mSocket = null;
            Log.d(TAG,"ServerThread running");
            Log.d(TAG,"RFCOM Server socket start");

            try {
                mSocket = mBluetoothServerSocket.accept();
                Log.d(TAG,"RFCOM Server socket accepted connection");
            } catch(IOException e) {
                Log.e(TAG,"Socket's accept() method failed",e);
            }

            if(mSocket != null) {
                Log.d(TAG,"Connection accepted");
                connected(mSocket);
            }
            cancel();
        }

        public void cancel() {
            Log.d(TAG,"Closing Server Socket");
            try {
                mBluetoothServerSocket.close();
            } catch(IOException e) {
                Log.e(TAG,"Could not close the connect socket",e);
            }
        }
    }
    private class ClientThread extends Thread {

        private final BluetoothDevice mmDevice;
        private final BluetoothSocket mmSocket;

        public ClientThread(BluetoothDevice device) {
            Log.d(TAG,"ClientThread started");
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
            } catch(IOException e) {
                Log.e(TAG,"Socket's create method failed",e);
            }
            mmSocket = tmp;
        }

        @Override
        public void run() {
            Log.i(TAG,"ClientThread running");
            mBluetoothAdapter.cancelDiscovery();

            try {
                mmSocket.connect();
                Log.d(TAG,"ClientThread connected");
            } catch (IOException e) {
                try {
                    mmSocket.close();
                } catch (IOException e1) {
                    Log.e(TAG,"Could not close the client socket",e1);
                }
                return;
            }

            connected(mmSocket);
        }

        public void cancel() {
            try {
                Log.d(TAG,"Closing client socket");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG,"Could not close the client socket",e);
            }
        }
    }
    private class ConnectionThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInput;
        private final OutputStream mmOutput;

        public ConnectionThread(BluetoothSocket socket) {
            Log.d(TAG,"ConnectionThread starting");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch(IOException e) {
                Log.e(TAG,"Error while creating input/output stream",e);
            }

            mmInput = tmpIn;
            mmOutput = tmpOut;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int numBytes;

            while(true) {
                try {
                    numBytes = mmInput.read(buffer);
                    String incomingMessage = new String(buffer,0,numBytes);
                    Log.d(TAG,"InputStream: "+incomingMessage);
                } catch (IOException e) {
                    Log.e(TAG,"Input stream was disconnected");
                    break;
                }
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG,"Could not close the connect socket",e);
            }
        }

        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG,"Writing to Outputstream: "+text);
            try {
                synchronized(this){mmOutput.write(bytes);}
            } catch (IOException e) {
                Log.e(TAG,"Error writing to OutputStream: "+e.getMessage());
            }
        }
    }

    public synchronized void startServer() {
        Log.d(TAG,"Server started");

        if(mClientThread != null) {
            mClientThread.cancel();
            mClientThread = null;
        }

        if(mServerThread == null) {
            mServerThread = new ServerThread();
            mServerThread.start();
        }
    }

    public void startClient(BluetoothDevice device, String uuid) {
        Log.d(TAG,"Client started");
        mClientThread = new ClientThread(device);
        mClientThread.start();
    }

    public void write(byte[] out) {
        //ConnectionThread r;
        mConnectionThread.write(out);
    }
}
