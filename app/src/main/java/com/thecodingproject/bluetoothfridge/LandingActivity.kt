package com.thecodingproject.bluetoothfridge

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_landing.*


class LandingActivity : AppCompatActivity() {
    private val REQUEST_ENABLE_BT = 1
    private val BLUETOOTH_FRIDGE = "00:21:13:00:79:72"
    private var bluetoothAdapter: BluetoothAdapter? = null


    override fun onBackPressed() {
        Log.d("Back Key", "Disconnect when going back")
        finish()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        //Device doesn't support Bluetooth
        if (bluetoothAdapter == null) {
            Toast.makeText(applicationContext,"This device does not support Bluetooth",Toast.LENGTH_LONG).show()
            val handler = Handler()
            handler.postDelayed({ finish() }, 3000)
        }

        //Enable Bluetooth if it is disabled
        enableBluetooth()

        enableBluetooth_LandingActivity_button.setOnClickListener {
            enableBluetooth()
        }


        //Check if we are already paired to the fridge
        var pairedFridgeFlag = checkPairBluetooth()
        //the fridge doesn't exist in the list of paired devices
        if (!pairedFridgeFlag) {
            Toast.makeText(applicationContext, "The fridge doesn't exist in the list of paired devices. Please follow the instructions", Toast.LENGTH_LONG).show()
        }

        //check pair via button
        pairBluetooth_LandingActivity_button.setOnClickListener {
            pairedFridgeFlag = checkPairBluetooth()
        }

        //connect via button to the fridge
        connect_LandingActivity_button.setOnClickListener {
            if (pairedFridgeFlag) {
                connectToTheFridge()
            }
        }

    }

    private fun connectToTheFridge(){
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            val deviceHardwareAddress = device.address // MAC address

            //if fridge exists in the list of paired devices we will try to connect to it
            if (deviceHardwareAddress == BLUETOOTH_FRIDGE){
                setConnectVisible()
                val intent = Intent(this, FridgeControl::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }
        }
    }

    private fun checkPairBluetooth(): Boolean {
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            val deviceHardwareAddress = device.address // MAC address

            //if fridge exists in the list of paired devices we will try to connect to it
            if (deviceHardwareAddress == BLUETOOTH_FRIDGE) {
                setConnectVisible()
                return true
            }
        }
        return false
    }

    private fun enableBluetooth(){
        //Enable Bluetooth if it is disabled
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
        else{
            setPairVisible()
        }
    }

    private fun setPairVisible(){
        pairBluetoothInstructions_LandingActivity_textView.visibility = View.VISIBLE
        pairBluetooth_LandingActivity_button.visibility = View.VISIBLE
        pairBluetooth_LandingActivity_textView.visibility = View.VISIBLE
        enableBluetooth_LandingActivity_button.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_tick,0)
    }

    private fun setConnectVisible(){
        standInRange_LandingActivity_textView.visibility = View.VISIBLE
        connect_LandingActivity_button.visibility = View.VISIBLE
        pairBluetooth_LandingActivity_button.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_tick,0)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                if (bluetoothAdapter!!.isEnabled) {
                    Toast.makeText(applicationContext, "Bluetooth has been enabled", Toast.LENGTH_LONG).show()
                    setPairVisible()
                } else {
                    Toast.makeText(applicationContext, "Bluetooth has been disabled", Toast.LENGTH_LONG).show()
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(applicationContext, "Bluetooth enabling has been canceled", Toast.LENGTH_LONG).show()
            }
        }
    }

}