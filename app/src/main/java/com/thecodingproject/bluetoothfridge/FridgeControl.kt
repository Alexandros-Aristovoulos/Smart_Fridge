package com.thecodingproject.bluetoothfridge

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_fridge_control.*
import java.io.IOException
import java.util.*


class FridgeControl : AppCompatActivity() {

    companion object {
        var MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var bluetoothSocket: BluetoothSocket? = null
        lateinit var progress: ProgressDialog
        lateinit var bluetoothAdapter: BluetoothAdapter
        var isConnected: Boolean = false
        const val BLUETOOTH_FRIDGE: String = "00:21:13:00:79:72"
    }

    override fun onBackPressed() {
        Log.d("Back Key", "Disconnect when going back")
        disconnect()
    }

    override fun onDestroy() {
        disconnect()
        super.onDestroy()
    }

    override fun onPause() {
        disconnect()
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fridge_control)

        targetTemp_fridgeControl_seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                var progress = progress
                val dispNum :Float = (progress.toFloat()/2)
                targetTempNumber_fridgeControl_textView.text = (dispNum).toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        ConnectToDevice(this).execute()

        update_fridgeControl_button.setOnClickListener {
            //get the number from the seekBar
            val targetTempValInt = targetTemp_fridgeControl_seekBar.progress
            val targetTempValFloat = targetTempValInt.toFloat()/2
            sendCommand(targetTempValFloat.toString())
        }

        cancel_fridgeControl_button.setOnClickListener {
            disconnect()
        }
    }


    private fun sendCommand(input: String) {
        if (bluetoothSocket != null) {
            try{
                bluetoothSocket!!.outputStream.write(input.toByteArray())
                Toast.makeText(applicationContext, "Updated the target temperature", Toast.LENGTH_LONG).show()
            } catch(e: IOException) {
                e.printStackTrace()
                Toast.makeText(applicationContext, "Could not send the target temperature. Please try again", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun disconnect() {
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket!!.close()
                bluetoothSocket = null
                isConnected = false
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        finish()
    }

    private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>() {
        private var connectSuccess: Boolean = true
        private val context: Context

        init {
            this.context = c
        }

        override fun onPreExecute() {
            super.onPreExecute()
            progress = ProgressDialog.show(context, "Connecting...", "please wait")
        }

        override fun doInBackground(vararg p0: Void?): String? {
            try {
                if (bluetoothSocket == null || !isConnected) {
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(BLUETOOTH_FRIDGE)
                    bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    bluetoothSocket!!.connect()
                }
            } catch (e: IOException) {
                connectSuccess = false
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (!connectSuccess) {
                Log.i("data", "couldn't connect")
            } else {
                isConnected = true
            }
            progress.dismiss()
        }


    }
}