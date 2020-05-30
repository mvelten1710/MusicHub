package com.dibsey.musichub.adapter

import com.dibsey.musichub.bluetoothServices.BluetoothServer

class UserListItem(btD: BluetoothServer.ConnectedThread? = null, name: String = ""){

    private var uThread = btD
    private var uName = UName(name)

    fun name(newName: String){
        uName.name(newName)
    }

    fun name(): String{
        return uName.name()
    }

    fun uName(): UName{
        return uName
    }

    fun thread(newThread: BluetoothServer.ConnectedThread){
        uThread = newThread
    }

    fun thread(): BluetoothServer.ConnectedThread?{
        return uThread
    }

}