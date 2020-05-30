package com.dibsey.musichub.bluetoothServices

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dibsey.musichub.adapter.BluetoothArrayAdapter
import java.io.IOException

class BluetoothReceiver(list1: MutableSet<BluetoothDevice>,
                        adapter1: BluetoothArrayAdapter,
                        setText1: () -> Unit) : BroadcastReceiver() {

    private val list: MutableSet<BluetoothDevice> = list1
    private val adapter: BluetoothArrayAdapter = adapter1
    private val setText: () -> Unit = setText1

    override fun onReceive(c: Context?, i: Intent?) {
        val action = i?.action
        Log.d("onReceive", action.toString())
        when (action) {
            BluetoothDevice.ACTION_FOUND -> {
                val device: BluetoothDevice? =
                    i.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                if (device != null) {
                    try {
                        if(device.name != null) {
                            list.add(device)
                            Log.d("Device", "${device.name} - ${device.address}")
                            adapter.notifyDataSetChanged()
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