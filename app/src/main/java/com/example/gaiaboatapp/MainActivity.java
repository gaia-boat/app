 package com.example.gaiaboatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

 public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "MainActivity";
    private static final UUID MY_UUID_INSECURE = UUID.fromString("8ce255-200a-11e0-ac64-0800200c9a66");

    BluetoothDevice mBTDevice;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothConnectionService mBluetoothConnetion;

    Button btnEnableDisable_Discoverable;
    Button btnStartConnection;
    Button btnSend;
    EditText etSend;

    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    ListView lvNewDevices;

     // Create a BroadcastReciever for ACTION_FOUND
     private final BroadcastReciever mBroadcastReciever1 = (context, intent) -> {
         String action = intent.getAction();
         // when discovery finds a device
         if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
             final int state;
             state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

             switch (state) {
                 case BluetoothAdapter.STATE_OFF:
                     Log.d(TAG, "onRecieve: STATE_OFF");
                     break;
                 case BluetoothAdapter.STATE_TURNING_OFF:
                     Log.d(TAG, "onRecieve: STATE_TURNING_OFF");
                     break;
                 case BluetoothAdapter.STATE_TURNING_ON:
                     Log.d(TAG, "onRecieve: STATE_TURNING_ON");
                     break;
                 case BluetoothAdapter.STATE_ON:
                     Log.d(TAG, "onRecieve: STATE_ON");
                     break;
             }
         }
     };

     private final BroadcastReceiver mBroadcastReciever2 = (context, intent) -> {
         final String action = intent.getAction();

         if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
             int mode;
             mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

             switch (mode) {
                 case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                     Log.d(TAG, "onRecieve: discoverability enable");
                     break;
                 case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                     Log.d(TAG, "onRecieve: discoverability disabled. Able to recieve connections.");
                     break;
                 case BluetoothAdapter.SCAN_MODE_NONE:
                     Log.d(TAG, "onRecieve: discoverability disabed. Not able to recieve connections.");
                     break;
                 case BluetoothAdapter.STATE_CONNECTED:
                     Log.d(TAG, "onRecieve: Connected");
                     break;
                 case BluetoothAdapter.STATE_CONNECTING:
                     Log.d(TAG, "onRecieve: Connecting...");
                     break;
             }
         }
     };

     private final BroadcastReceiver mBroadcastReciever3 = (context, intent) -> {
         final String action = intent.getAction();
         Log.d(TAG, "onRecieve: ACTION_FOUND.");

         if (action.equals(BluetoothDevice.ACTION_FOUND)) {
             BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
             mBTDevices.add(device);
             Log.d(TAG, "onRecieve: " + device.getName() + ": " + device.getAddress());
             mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
             lvNewDevices.setAdapter(mDeviceListAdapter);
         }
     };

     private final BroadcastReceiver mBroadcastReciever4 = (context, intent) -> {
         final String action = intent.getAction();

         if (action.equals(BluetoothDevice.EXTRA_BOND_STATE_CHANGED)) {
             BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
             // case 1
             if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                 Log.d(TAG, "BroadcastReciever: BOND_BONDED");
                 mBTDevice = mDevice;
             }
             // case 2
             if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                 Log.d(TAG, "BroadcastReciever: BOND_BONDING");
             }
             // case 3
             if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                 Log.d(TAG, "BroadcastReciever: BOND_NONE");
             }
         }
     };


     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnOnOff = (Button) findViewById(R.id.btnOnOff);
        btnEnableDisable_Discoverable = (Button) findViewById(R.id.btnDiscoverable_on_off);
        lvNewDevices = (ListView) findViewById(R.id.lvNewDevices);
        mBTDevices = new ArrayList<>();

        btnStartConnection = (Button) findViewById(R.id.btnStartConnection);
        btnSend = (Button) findViewById(R.id.btnSend);
        etSend = (Button) findViewById(R.id.etSend);

        // Broadcasts when bond state changes
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        lvNewDevices = setOnItemClickListener(MainActivity.this);

        btnOnOff.setOnClickListener((view) -> {
            Log.d(TAG, "onClick: enabling/disabling bluetooth");
            enableDisbleBT();
        });

        btnStartConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startConnection();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] bytes = etSend.getText().toString().getBytes(Charset.defaultCharset());
                mBluetoothConnetion.write(bytes);
            }
        });
    }

    // create method for starting connection
     public void startConnection() {
         startBTConnection(mBTDevice, MY_UUID_INSECURE);
     }

    // starting chat service method
     public void startBTConnection(BluetoothDevice device, UUID uuid) {
         Log.d(TAG, "startBTConnection: starting bt connection");
         mBluetoothConnetion.startClient(device, uuid);
     }


    public void enableDisbleBT() {
        if (mBluetoothAdapter == null) {
            Log.d(TAG, "enableDisbleBT: does not have BT capabilities");
        }
        if (mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "enableDisbleBT: enabling BT");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReciever1, BTIntent);
        }
        if (mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "enableDisbleBT: disabling BT");
            mBluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReciever1, BTIntent);
        }
    }

     @Override
     protected void onDestroy() {
         Log.d(TAG, "onDestroy: called");
         super.onDestroy();
         unregisterReceiver(mBroadcastReciever1);
         unregisterReceiver(mBroadcastReciever2);
         unregisterReceiver(mBroadcastReciever3);
         unregisterReceiver(mBroadcastReciever4);
     }
}
