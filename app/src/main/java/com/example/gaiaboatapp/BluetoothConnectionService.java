// tutorial https://www.youtube.com/watch?v=Fz_GT7VGGaQ
package com.example.gaiaboatapp;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
//import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionServ";
    private static final UUID MY_UUID_SECURE = UUID.fromString("8ce255-200a-11e0-ac64-0800200c9a66");
    private static final UUID MY_UUID_INSECURE = UUID.fromString("8ce255-200a-11e0-ac64-0800200c9a66");
    private static final String appName = "MYAPP";
    private BluetoothAdapter mBluetoothAdapter;

    private UUID deviceUUID;
    private BluetoothDevice mDevice;
    private BluetoothSocket socket;
    private List<String> found_devices;
    private Set<BluetoothDevice> paired_devices;
    private static int REQUEST_ENABLE_BT = 1;

    private CommunicationThread communicationThread = null;
    private SearchAndConnectToBTModuleThread searchAndConnectToBTModuleThread = null;

    Context mContext;
    ProgressDialog mProgressDialog;

    public BluetoothConnectionService(Context ctx) {
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mContext = ctx;
        this.found_devices = new ArrayList<String>();
        this.searchAndConnectToBTModuleThread = new SearchAndConnectToBTModuleThread();
        this.searchAndConnectToBTModuleThread.start();
//        this.socket = new BluetoothSocket();
        start();
    }

    public synchronized void start() {
        if (this.communicationThread != null)
            this.communicationThread.interrupt();
        if (this.searchAndConnectToBTModuleThread != null)
            this.searchAndConnectToBTModuleThread.interrupt();

        Log.d(TAG, "start: SOCKET IS -> " + this.socket);

        this.communicationThread = new CommunicationThread(this.socket);
        this.communicationThread.start();
    }

    private class SearchAndConnectToBTModuleThread extends Thread {
        // HC-05
        boolean connectionSuccessful = false;

        @Override
        public void run() {
            Log.d(TAG, "run: Starting search and connect thread");
            super.run();
            Log.d(TAG, "run: getting bonded devices");
            Set<BluetoothDevice> paired_devices = mBluetoothAdapter.getBondedDevices();
            Log.d(TAG, "run: bonded devices = " + paired_devices);

            for (BluetoothDevice bt : paired_devices) {
                Log.d(TAG, "run: " + bt.getName());
                if (bt.getName() == "HC-05") {
                    Log.d(TAG, "run: device HC-05 found with nº: " + bt.getAddress());
                    mDevice = bt;
                    break;
                }
            }

            if (mDevice != null) {
                Log.d(TAG, "run: Device is named: " + mDevice.getName());
                try {
                    socket = mDevice.createRfcommSocketToServiceRecord(MY_UUID_INSECURE);
                } catch (IOException e) {
                    Log.e(TAG, "run: Failed to create a comms socket! " + e.getMessage());
                }

                Log.d(TAG, "run: Trying to set up a connection with the device: " + mDevice.getName());
                Log.d(TAG, "run: Connection timeout is SET to 12s");

                try {
                    socket.connect();
                    Log.d(TAG, "run: Successfully connected to socket!");
                } catch (IOException e) {
                    Log.e(TAG, "run: Failed to make a connection bruh.");
                    Log.e(TAG, "run: ERROR: " + e.getMessage() );
                    Log.e(TAG, "run: trying to close the connection socket...");

                    try {
                        socket.close();
                        Log.e(TAG, "run: closed socket successfully");
                    } catch (IOException closeSocketException) {
                        Log.e(TAG, "run: Error while closing socket: " + closeSocketException.getMessage() );
                    }
                }

                this.connectionSuccessful = true;
            }

            else {
                Log.e(TAG, "searchForBTModule: Could not find the target BT device!");
                communicationThread.interrupt();
            }
        }

        public boolean isConnectionSuccessful() {
            return this.connectionSuccessful;
        }
    }

    private class CommunicationThread extends Thread {
        private BluetoothSocket socket;
        private InputStream recievedMessage;
        private OutputStream sendMessage;
        private byte[] buffer;

        public CommunicationThread(BluetoothSocket bts) {
            this.socket = bts;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            recievedMessage = tmpIn;
            sendMessage = tmpOut;
        }

        public void run() {
            while (true) {
//                SystemClock.sleep(1000);
                Log.d(TAG, "run: waiting for a connection to be stablished...");
                if (searchAndConnectToBTModuleThread.isConnectionSuccessful())
                    Log.d(TAG, "run: starting the connection thread...");
                    break;
            }
            buffer = new byte[1024];
            int num_bytes;

            while(true) {
                try {
                    num_bytes = recievedMessage.read(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
                // SEND TO UI HERE
            }
        }

        public void write(byte[] bytes) {
            try {
                sendMessage.write(bytes);
                // Send to UI activity here
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
            }
        }

        public void cancelConnection () {
            try {
                socket.close();
                Log.d(TAG, "cancelConnection: socket closed successfully!");
            } catch (IOException e) {
                Log.e(TAG, "cancelConnection: could not close the socket connection..." + e);
            }
        }
    }

    public void write(byte[] bytes) {
        // Create temp object
         CommunicationThread r;
        // sync a cp of the ConnectedThread
        Log.d(TAG, "write: Write called.");
        communicationThread.write(bytes);
    }
}
