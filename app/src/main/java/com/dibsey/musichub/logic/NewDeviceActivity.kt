package com.dibsey.musichub.logic


import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import com.dibsey.musichub.adapter.BluetoothArrayAdapter
import com.dibsey.musichub.bluetoothServices.BluetoothReceiver
import com.dibsey.musichub.R
import kotlinx.android.synthetic.main.new_device_layout.*
import java.io.IOException
import kotlin.concurrent.thread

class NewDeviceActivity : AppCompatActivity() {

    private var animationRunning = false

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var discoveredDevicesList = mutableSetOf<BluetoothDevice>()
    private lateinit var discoveredDevicesAdapter: BluetoothArrayAdapter
    private lateinit var bluetoothReceiver: BluetoothReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_device_layout)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        discoveredDevicesAdapter =
            BluetoothArrayAdapter(
                this,
                discoveredDevicesList
            )
        newDeviceList.adapter = discoveredDevicesAdapter

        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
        bluetoothAdapter.startDiscovery()

        bluetoothReceiver =
            BluetoothReceiver(
                discoveredDevicesList,
                discoveredDevicesAdapter,
                ::setText
            )
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.priority = 100
        registerReceiver(bluetoothReceiver, filter)
        Log.d("NewDeviceActivity", "Registered Receiver")
        animateTextView()

        backButton.setOnClickListener {
            finish()
        }

        newDeviceList.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                bluetoothAdapter.cancelDiscovery()
                discoveredDevicesList.elementAt(position).createBond()
                var timeout = 0
                thread {
                    while (timeout != 15000){
                        val bonded = (discoveredDevicesList.elementAt(position).bondState == BluetoothDevice.BOND_BONDED)
                        Log.d("NewDeviceActivity", "Bonded? $bonded")
                        if(bonded){
                            val data = Intent()
                            data.putExtra("deviceName", discoveredDevicesList.elementAt(position).name)
                            setResult(1338, data)
                            finish()
                            break
                        }
                        timeout += 1000
                        Thread.sleep(1000)
                    }
                }
            }
    }

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            Log.d("onReceive", action.toString())
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null) {
                        try {
                            if(device.name != null) {
                                discoveredDevicesList.add(device)
                                discoveredDevicesAdapter.notifyDataSetChanged()
                                setText()
                            }
                        } catch (e: IOException) {
                            Log.e("NewDeviceActivity", "Error: ${e.message}")
                        }
                    }
                }
            }
        }
    }

    private fun animateTextView(){
        animationRunning = true
        val string = resources.getString(R.string.search_devices)
        var offset = 5
        thread {
            while (animationRunning){
                if (offset == 0)
                    offset = 5
                runOnUiThread { searchText.text = string.substring(0..(string.length-1)-offset) }
                offset--
                Thread.sleep(1000)
            }
        }
    }

    private fun setText(){
        animationRunning = false
        runOnUiThread { searchText.text = resources.getString(R.string.select_device) }
    }

    override fun onDestroy() {
        animationRunning = false
        bluetoothAdapter.cancelDiscovery()
        unregisterReceiver(bluetoothReceiver)
        super.onDestroy()
        Log.d("NewDeviceActivity", "Unregistered Receiver")
    }
}