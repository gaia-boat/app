package com.example.gaiaboatapp

import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

//    {-15,839201 -47,902330} {-15,838898 -47,903629} {-15,839497 -47,903575} {-15,38837 -47,902700}

    fun stabilishBluetoothComms() {
        val bluetooth : BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

        if (bluetooth?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }



    }

}
