// tutorial https://www.youtube.com/watch?v=Fz_GT7VGGaQ
package com.example.gaiaboatapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionServ";
    private static final UUID MY_UUID_INSECURE = UUID.fromString("8ce255-200a-11e0-ac64-0800200c9a66");
    private static final UUID MY_UUID_SECURE = UUID.fromString("8ce255-200a-11e0-ac64-0800200c9a66");
    private static final String appName = "MYAPP";
    private final BluetoothAdapter mBluetoothAdapter;
    Context mcontext;
    private AcceptThread mInsecureAcceptThread;

    public BluetoothConnectionService(Context mcontext) {
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mcontext = mcontext;
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
//            create a new listening server socket
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
                Log.d(TAG, "run: Close of AcceptThread ServerSocket failed ~> " + e.getMessage());
            }
        }
    }
}
