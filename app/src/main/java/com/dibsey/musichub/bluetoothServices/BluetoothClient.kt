package com.dibsey.musichub.bluetoothServices

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.dibsey.musichub.adapter.*
import com.dibsey.musichub.items.ActionItem
import com.dibsey.musichub.items.PlaylistItem
import com.dibsey.musichub.items.UserListItem
import com.dibsey.musichub.spotify.Message
import java.io.*
import java.util.*
import kotlin.collections.ArrayList

class BluetoothClient(username: String?): BluetoothCS {

    override val id: Int
        get() = 1

    private val TAG = "BluetoothService"

    private var mUUID: UUID = UUID.fromString("3fb6b3c8-0368-47fb-9814-3e9157a65173")

    private var bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var clientThread: ConnectThread? = null
    private var connectedThread: ConnectedThread? = null

    private var mmDevice: BluetoothDevice? = null
    private var deviceUUID: UUID? = mUUID
    private val uname: String? = username

    private lateinit var aListAdapter: ActionAdapter
    private lateinit var aList: ArrayList<ActionItem>

    private lateinit var pListAdapter: PlaylistAdapter
    private lateinit var pList: ArrayList<PlaylistItem>
    private val pListBuffer = ArrayList<PlaylistItem>()
    private var pListInitialized = false

    //User List
    private lateinit var uListAdapter: UserArrayAdapter
    private lateinit var uList: ArrayList<UserListItem>
    private val uListBuffer = ArrayList<UserListItem>()
    private var uListInitialized = false

    enum class BluetoothStates {
        NULL, NO_CONNECTION, CLIENT, CONNECTED
    }
    private var bluetoothState: BluetoothStates =
        BluetoothStates.NULL


    private inner class ConnectThread(device: BluetoothDevice, uuid: UUID) : Thread() {
        private var mmSocket: BluetoothSocket? = null

        init {
            bluetoothState =
                BluetoothStates.CLIENT
            Log.d(TAG, "Connect Thread: started!")
            mmDevice = device
            deviceUUID = uuid
        }

        override fun run() {
            var tmp: BluetoothSocket? = null
            Log.i(TAG, "RUN mmConnectedThread")
            try {
                Log.d(
                    TAG, "ConnectThread: Trying to create InsecureRFCOMMSocket using UUID: " +
                            deviceUUID.toString()
                )
                tmp = mmDevice!!.createInsecureRfcommSocketToServiceRecord(deviceUUID)
            } catch (e: IOException) {
                Log.e(
                    TAG, "ConnectThread: Could not create InsecureRFCOMMSocket! "
                            + e.message
                )
            }
            mmSocket = tmp

            bluetoothAdapter.cancelDiscovery()

            try {
                mmSocket!!.connect()
                Log.d(TAG, "run: ConnectThread connected!")
                connected(mmSocket)
            } catch (e: IOException) {
                try {
                    mmSocket!!.close()
                    Log.d(TAG, "run: Closed Socket!")
                } catch (e2: IOException) {
                    Log.e(
                        TAG, "mConnectThread: run: Unable to close connection in socket "
                                + e2.message
                    )
                }
                Log.d(
                    TAG, "run: ConnectThread: Could not connect to UUID: "
                            + deviceUUID
                )
                bluetoothState =
                    BluetoothStates.NO_CONNECTION
            }
        }
        fun cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket!")
                mmSocket!!.close()
                bluetoothState =
                    BluetoothStates.NULL
            } catch (e: IOException) {
                Log.e(
                    TAG, "cancel: close() of mmSocket in ConnectThread failed "
                            + e.message
                )
            }

        }
    }

    private inner class ConnectedThread(socket: BluetoothSocket) : Thread() {
        private var mmSocket: BluetoothSocket = socket
        private lateinit var inputStream: InputStream
        private lateinit var outputStream: OutputStream

        init {
            Log.d(TAG, "Connected Thread: starting!")
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null
            try {
                tmpIn = mmSocket.inputStream
                tmpOut = mmSocket.outputStream
            } catch (e: IOException) {
                Log.e(TAG, "ConnectedThread: In-/Output exception!")
            }
            if (tmpIn != null) {
                inputStream = tmpIn
            }
            if (tmpOut != null) {
                outputStream = tmpOut
            }
            bluetoothState =
                BluetoothStates.CONNECTED
            write(uname!!.toByteArray())
        }

        override fun run() {
            val buffer = ByteArray(1024) //Buffer store for the stream
            var bytes: Int

            while (true) {
                try {
                    bytes = inputStream.read(buffer)
                    decipher(buffer, bytes)
                } catch (e: IOException) {
                    e.printStackTrace()
                    cancel()
                    break
                }
            }
        }

        fun write(bytes: ByteArray) {
            try {
                outputStream.write(bytes)
            } catch (e: IOException) {
                Log.e(TAG, "write: Error writing to Output Stream " + e.message)
            }
        }

        fun cancel() {
            try {
                mmSocket.close()
                bluetoothState =
                    BluetoothStates.NULL
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun decipher(buffer: ByteArray, bytes: Int){
        val msg = deserialize(buffer, bytes)
        when(msg.id){
            -1 ->{ /*Error*/ }
            0 ->{ //Track & Action
                addActionItemToList(msg)
                addToPlaylist(msg)
            }
            1 ->{ //User
                addUserToList(null, msg.user())
            }
            2 ->{ //Close connection
                connectedThread?.cancel()
            }
        }
    }

    private fun deserialize(buffer: ByteArray, bytes: Int): Message{
        var msg = Message()
        if (bytes > 0) {
            try {
                val b = ByteArrayInputStream(buffer)
                val ois = ObjectInputStream(b)
                msg = ois.readObject() as Message
            }catch (e: IndexOutOfBoundsException){
                closeClientConnectedSocket()
            }
        }
        return msg
    }

    override fun addActionList(actionListAdapter: ActionAdapter, actionList: ArrayList<ActionItem>){
        aListAdapter = actionListAdapter
        aList = actionList
    }

    override fun addPlaylist(playlistAdapter: PlaylistAdapter, playlist: ArrayList<PlaylistItem>){
        pListAdapter = playlistAdapter
        pList = playlist
        pList.addAll(pListBuffer)
        pListInitialized = true
    }

    override fun addActionItemToList(message: Message){
        Handler(Looper.getMainLooper()).post {
            aList.add(
                ActionItem(
                    message.user(),
                    message.actionMessage()
                )
            )
            aListAdapter.notifyDataSetChanged()
        }
    }

    override fun addToPlaylist(message: Message){
        if(pListInitialized) {
            Handler(Looper.getMainLooper()).post {
                pList.add(
                    PlaylistItem(
                        message.name(),
                        message.info()
                    )
                )
                pListAdapter.notifyDataSetChanged()
            }
        }else{
            pListBuffer.add(
                PlaylistItem(
                    message.name(),
                    message.info()
                )
            )
        }
    }

    private fun connected(mmSocket: BluetoothSocket?) {
        Log.d(TAG, "connected: starting!")

        if (mmSocket != null) {
            connectedThread = ConnectedThread(mmSocket)
            connectedThread!!.start()
        }
    }

    fun startClient(device: BluetoothDevice, uuid: UUID) {
        Log.d(TAG, "startClient: started!")
        clientThread = ConnectThread(device, uuid)
        clientThread!!.start()
    }

    fun write(out: ByteArray) {
        Log.d(TAG, "write: Write called!")
        connectedThread!!.write(out)
    }

    @Synchronized
    fun closeClientConnectedSocket() {
        bluetoothState =
            BluetoothStates.NULL
        connectedThread?.cancel()
    }

    @Synchronized
    fun bluetoothState(): BluetoothStates {
        return bluetoothState
    }

    override fun addUserToList(thread: BluetoothServer.ConnectedThread?, name: String) {
        if(uListInitialized) {
            Handler(Looper.getMainLooper()).post {
                uList.add(UserListItem(thread, name))
                uListAdapter.notifyDataSetChanged()
            }
        }else{
            uListBuffer.add(UserListItem(thread, name))
        }
    }

    override fun addUserList(
        userListAdapter: UserArrayAdapter,
        userList: ArrayList<UserListItem>
    ) {
        uListAdapter = userListAdapter
        uList = userList
        uList.addAll(uListBuffer)
        uListAdapter.notifyDataSetChanged()
        uListInitialized = true
    }

    override fun kickDevice(pos: Int): Boolean {
        return false
    }
}