package com.dibsey.musichub.adapter

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.dibsey.musichub.R

class BluetoothArrayAdapter(context: Context, private val dataSource: Set<BluetoothDevice>): BaseAdapter()
{

    private val selectedBackground = context.resources.getDrawable(R.color.listViewSelectedItem)
    private var selectedPosition = -1

    fun selectedPosition(pos: Int){
        selectedPosition = pos
        notifyDataSetChanged()
    }

    private val inflater: LayoutInflater
        = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getItem(p0: Int): Any {
        return dataSource.elementAt(p0)
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {

        val rowView = inflater.inflate(R.layout.listview_devices_layout, p2, false)

        val deviceName = rowView.findViewById(R.id.deviceName) as TextView
        val image = rowView.findViewById(R.id.btImage) as ImageView

        val device = getItem(p0) as BluetoothDevice

        deviceName.text = device.name
        image.setImageResource(R.drawable.ic_bluetooth_24px)

        if (selectedPosition == p0){
            rowView.background = selectedBackground
        }

        return rowView
    }
}