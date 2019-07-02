// tutorial https://www.youtube.com/watch?v=Fz_GT7VGGaQ
package com.example.gaiaboatapp;

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
    private static final String TAG = "BluetoothConnectionServ";
    private static final UUID MY_UUID_SECURE = UUID.fromString("8ce255-200a-11e0-ac64-0800200c9a66");
    private static final UUID MY_UUID_INSECURE = UUID.fromString("8ce255-200a-11e0-ac64-0800200c9a66");
    private static final String appName = "MYAPP";
    
    private final BluetoothAdapter mBluetoothAdapter;
    
    private UUID deviceUUID;
    private BluetoothDevice mDevice;

    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    
    Context mContext;
    ProgressDialog mProgressDialog;

    public BluetoothConnectionService(Context ctx) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mContext = ctx;
        start();
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            // create a new listening server socket
            try {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);
                Log.d(TAG, "AcceptThread: setting up server using: " + MY_UUID_INSECURE);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "run: AcceptThread running...");
            BluetoothSocket socket = null;

            // this is a blocking call that will return once there is a successful connection or a
            // exception is caught.
            Log.d(TAG, "run: RFCOM server socket start...");
            try {
                socket = mServerSocket.accept(5000);
                Log.d(TAG, "run: RFCOM server socket accepted connection!");
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (socket != null) {
                connected(socket, mDevice);
            }

            Log.d(TAG, "END AcceptThread.");
        }

        public void cancel() {
            Log.d(TAG, "cancel: Cancelling AcceptThread...");
            try {
                mServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "run: Close of AcceptThread ServerSocket failed ~> " + e.getMessage());
            }
        }
    }

    // this thread tries to stablish a connection with a device. It runs until
    // the connection fails or succeeds
    private class ConnectThread extends Thread {
        private BluetoothSocket mSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread: started...");
            mDevice = device;
            deviceUUID = uuid;    
        }

        public void run() {
            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN mConnectThread");
            // get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                Log.d(TAG, "ConnectThread: trying to create a InsecureRfcommSocket using UUID: " + MY_UUID_INSECURE);
                tmp = mDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: could not create a InsecureRfcommSocket " + e.getMessage());
            }
            mSocket = tmp;
            mBluetoothAdapter.cancelDiscovery();
            try {
                mSocket.connect();
                Log.d(TAG, "run: ConnectThread connected.");
            } catch (IOException e) {
                try {
                    mSocket.close();
                    Log.d(TAG, "run: Closed socket.");
                } catch (IOException e1) {
                    Log.e(TAG, "mConnectThread:run: Unable to close connection in socket. "+ e1.getMessage());
                }
                Log.d(TAG, "ConnectThread:run: could not connect to UUID: " + MY_UUID_INSECURE);
            }
            connected(mSocket, mDevice);
        }

        public void cancel(){
            try {
                Log.d(TAG, "Cancel: closing client socket");
                mSocket.close();
            } catch (IOException e) {
               Log.e(TAG, "Cancel: could not close the client socket: " + e.getMessage());
            }
        }
    }

    
    // lift -> hackathon
    // seleção de programadores, evento na FIT
    // banco central, caixa, siscob, ; usp vs unb; 4 alunos; 2º semestre;
    // dia 10 as 16h no auditorio
    
    // this is where the chat app begins
    // TODO: change logic to the gaia-boat's app
    public synchronized void start() {
        Log.d(TAG, "start");
        
        // cancel all threads that are trying to make an connection.
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        
        // begins an accept thread if there is none
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }
    
    // AcceptThread sits waiting for a connection
    // Then ConnectThread starts and attempts to make a connection with other device's AcceptThread
    // TODO: this must be refactorated
    public void startClient(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startClient: started.");
        
        // init ProgressDialog
        mProgressDialog = ProgressDialog.show(mContext, "Connecting to bluetooth.", "Please wait...", true);
        
        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
    }
    
    // This class assumes that a connection has been made
    public class ConnectedThread  extends Thread {
        private final BluetoothSocket mSocket;
        private final InputStream mInputStream;
        private final OutputStream mOutputStream;
        
        public ConnectedThread(BluetoothSocket sock) {
            Log.d(TAG, "ConnectedThread: Starting.");
            
            mSocket = sock;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            
            // dissmiss ProgressDialog
            mProgressDialog.dismiss();
            
            try {
                tmpIn = mSocket.getInputStream();
                tmpOut = mSocket.getOutputStream();
            } catch (IOException ie) {
                ie.printStackTrace();
            }
            
            mInputStream = tmpIn;
            mOutputStream = tmpOut;
        }
        
        public void run() {
            // bytearray object to get the input
            // TODO: refact
            byte[] buffer = new byte[1024]; // buffer store for the stream
            int bytes;  // bytes returned from read()
            
            // keep listening to input until exception launches
            while (true) {
                try {
                    bytes = mInputStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);
                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading from inputstream. " + e.getMessage());
                    break;    
                }
                
            }
        }
        
        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: writing to OutputStream: " + text);
            try {
                mOutputStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to outputstream. " + e.getMessage());
            }
        }
        
        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) { }
        }
    }

    // write to ConnectedThread in an unsync manner
    public void write(byte[] out) {
        // Create temp object
        ConnectThread r;
        // sync a cp of the ConnectedThread
        Log.d(TAG, "write: Write called.");
        mConnectedThread.write(out);
    }

    // manages the connection and handles communications
    // TODO: refact and fix this
    private void connected(BluetoothSocket mSocket, BluetoothDevice mDevice) {
        Log.d(TAG, "connected: Starting.");

        // start thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mSocket);
        mConnectedThread.start();
    }
}
