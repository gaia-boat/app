package com.example.gaiaboatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final UUID MY_UUID_INSECURE = UUID.fromString("8ce255-200a-11e0-ac64-0800200c9a66");

    BluetoothDevice mBTDevice;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothConnectionService mBluetoothConnection;
    Context context;

    Button btnEnableDisable_Discoverable;
    Button btnStartConnection;
    Button btnCommunicate;
    EditText etSend;
    ListView messageList;

    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    private ArrayList<String> messages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Inits activity");
        super.onCreate(savedInstanceState);
        messages.add("ajsdodjs");
        setContentView(R.layout.activity_main);
        
        Button btnStart  = (Button) findViewById(R.id.start);
        Button btnCommunicate = (Button) findViewById(R.id.btnCommunicate);
        ListView list = (ListView) findViewById(R.id.messageList);

        ArrayAdapter messageList;
        messageList = new ArrayAdapter(this, R.layout.device_adapter_view, messages);

        Log.d(TAG, "onCreate: Inits default adapter");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Log.d(TAG, "onCreate: start btnStart");
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Log.d(TAG, "onClick: Starting the thread for something");
               mBluetoothConnection = new BluetoothConnectionService(context);
            }
        });

        Log.d(TAG, "onCreate: Inits btnCommunicate");
        btnCommunicate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //                byte[] bytes = etSend.getText().toString().getBytes(Charset.defaultCharset());
                byte[] bytes = "S".getBytes(Charset.defaultCharset());
                mBluetoothConnection.write(bytes);
            }
        });
    }
}
