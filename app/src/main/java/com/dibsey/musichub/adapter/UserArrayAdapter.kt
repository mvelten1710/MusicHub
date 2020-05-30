package com.dibsey.musichub.adapter

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.dibsey.musichub.bluetoothServices.BluetoothServer
import com.dibsey.musichub.R
import com.dibsey.musichub.bluetoothServices.BluetoothCS

class UserArrayAdapter(context: Context, private val dataSource: ArrayList<UserListItem>, private val btServer: BluetoothCS): BaseAdapter()
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

        val rowView = inflater.inflate(R.layout.listview_users_layout, p2, false)

        val userName = rowView.findViewById(R.id.deviceName) as TextView
        val image = rowView.findViewById(R.id.btImage) as ImageView

        val raise = rowView.findViewById(R.id.raise) as ImageButton
        raise.setImageResource(R.drawable.arrow_back_24px)
        raise.setOnClickListener {
            Log.d("UserAdapter", "Raise")
        }

        val revoke = rowView.findViewById(R.id.revoke) as ImageButton
        revoke.setImageResource(R.drawable.arrow_back_24px)
        revoke.setOnClickListener {
            Log.d("UserAdapter", "Revoke")
        }

        val kick = rowView.findViewById(R.id.kick) as ImageButton
        kick.setImageResource(R.drawable.ic_add_24px)
        kick.setOnClickListener {
            if(btServer.kickDevice(p0)){
                notifyDataSetChanged()
                Log.d("UserAdapter", "Kick")
            }
        }

        val user = getItem(p0) as UserListItem

        userName.text = user.name()
        image.setImageResource(R.drawable.ic_person_24px)

        if (selectedPosition == p0){
            rowView.background = selectedBackground
        }
        return rowView
    }
}