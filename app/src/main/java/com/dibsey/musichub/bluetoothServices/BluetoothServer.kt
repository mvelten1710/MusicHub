package com.dibsey.musichub.bluetoothServices

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.android.volley.RequestQueue
import com.dibsey.musichub.adapter.*
import com.dibsey.musichub.spotify.SpotifyService
import com.dibsey.musichub.spotify.Message
import com.dibsey.musichub.spotify.TrackService
import java.io.*
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread
import kotlin.time.ExperimentalTime


class BluetoothServer(queue: RequestQueue,
    private val spotifyService: SpotifyService): BluetoothCS {

    override val id: Int
        get() = 0

    private val TAG = "BluetoothServer"
    private var mQueue = queue
    private val trackService = TrackService(mQueue, spotifyService)

    private var limit = 5
    private var mId = 0

    private var mUUID: UUID = UUID.fromString("3fb6b3c8-0368-47fb-9814-3e9157a65173")

    private var bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private var serverThread: AcceptThread? = null

    private var mConnThreads = ArrayList<ConnectedThread>()

    //Action List
    private lateinit var aListAdapter: ActionAdapter
    private lateinit var aList: ArrayList<ActionItem>

    //Playlist
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
        NULL, SERVER, CONNECTED
    }

    private var bluetoothState: BluetoothStates =
        BluetoothStates.NULL

    init {
        start()
    }

    private inner class AcceptThread : Thread() {

        private var mmServerSocket: BluetoothServerSocket? = null

        init {
            var tmp: BluetoothServerSocket? = null
            try {
                tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("HUB", mUUID)
                Log.d(TAG, "AcceptThread: Setting up Server using: $mUUID")
            } catch (e: IOException) {
                Log.e(TAG, "AcceptThread: Setting up Server failed!")
            }
            mmServerSocket = tmp
            bluetoothState =
                BluetoothStates.SERVER
        }

        override fun run() {
            Log.d(TAG, "run: AcceptThread running!")

            var socket: BluetoothSocket? = null

            try {
                Log.d(TAG, "run: RFCOM server socket start...")
                socket = mmServerSocket!!.accept()
            } catch (e: IOException) {
                Log.d(TAG, "run: RFCOM server socket accepted connection!")
            }

            if (socket != null) {
                connected(socket)
            }
            Log.i(TAG, "END AcceptThread")
        }

        fun cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread!")
            try {
                mmServerSocket!!.close()
                bluetoothState =
                    BluetoothStates.NULL
            } catch (e: IOException) {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed! " + e.message)
            }
        }
    }

    inner class ConnectedThread(socket: BluetoothSocket) : Thread() {
        private var mmSocket: BluetoothSocket = socket
        private lateinit var inputStream: InputStream
        private lateinit var outputStream: OutputStream
        var id = mId
        var firstTime = true

        init {
            Log.d(TAG, "ConnectedThread: starting!")
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
        }

        @ExperimentalTime
        override fun run() {
            val buffer = ByteArray(1024) //Buffer store for the stream
            var bytes: Int

            while (true) {
                try {
                    bytes = inputStream.read(buffer)
                    val message = String(buffer, 0, bytes)
                    if(firstTime){
                        addUserToList(this, message)
                        firstTime = false
                    }else{
                        handleMessage(whichMethod(message), message, id)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    cancel()
                    break
                }
            }
        }

        fun write(bytes: ByteArray) {
            val text = String(bytes, Charset.defaultCharset())
            try {
                Log.d(TAG, "write: Writing to Output Stream: $text")
                outputStream.write(bytes)
            } catch (e: IOException) {
                Log.e(TAG, "write: Error writing to Output Stream " + e.message)
            }
        }

        fun cancel() {
            try {
                Log.d(TAG, "Closed Connection")
                mmSocket.close()
                bluetoothState =
                    BluetoothStates.NULL
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    @Synchronized
    fun start() {
        Log.d(TAG, "Start Server")
        if (serverThread == null) {
            serverThread = AcceptThread()
            serverThread!!.start()
            mId++
        }
    }

    private fun connected(mmSocket: BluetoothSocket?) {
        Log.d(TAG, "connected: starting!")
        if (mmSocket != null) {
            if (limit < 7) {
                ConnectedThread(mmSocket).start()
                Log.d(TAG, "Started new connectedThread")
                restartAcceptThread()
                limit++
            }
        }
    }

    @Synchronized
    fun closeServerSocket() {
        if (serverThread != null)
            serverThread!!.cancel()
        serverThread = null
    }

    private fun restartAcceptThread() {
        if (limit < 7) {
            closeServerSocket()
            start()
            Log.d(TAG, "Started new AcceptThread for new connection")
        } else {
            Log.e(TAG, "AcceptThread Limit reached!")
        }
    }

    @Synchronized
    fun closeAllConnections() {
        writeToAllClients("Server closed\n".toByteArray(Charset.defaultCharset()))
        if(this::uList.isInitialized) {
            Handler().postDelayed({
                for (elem in uList) {
                    elem.thread()?.cancel()
                }
                limit = 0
            }, 2000)
        }
    }

    private fun handleMessage(spotifyId: Int, oldMessage: String, id: Int){
        val trackName = oldMessage.substring(oldMessage.indexOf(" ")+1, oldMessage.length)
        when(spotifyId){
            1 -> {
                Handler(Looper.getMainLooper()).post {
                    playTrack(trackName, id)
                }
            }
            2 -> {
                skipTrack(trackName)
            }
            3 -> {
                deleteTrack(trackName)
            }
            else -> {
                //TODO Error Message that something went wrong please retry
                playTrack(trackName, id)
            }
        }
    }

    private fun whichMethod(message: String): Int{
        return when(message.substring(0, message.indexOf(" "))){
            "Play" -> 1
            "Skip" -> 2
            "Delete" -> 3
            else -> -1
        }
    }

    private fun playTrack(track: String, id: Int) {
        val newTrack = track.replace(" ", "%20")
        trackService.searchTrack(newTrack)
        thread {
            while(true) {
                if (trackService.track() != null && trackService.track()?.uri()!!.isNotEmpty()) {
                    spotifyService.addTrackToPlaylist(trackService.track()?.uri())
                    Log.d("PlayTrack", trackService.track()?.name().toString())
                    sendMessage(trackService.track()!!, id)
                    trackService.track()!!.clearAll()
                    break
                }
                Thread.sleep(100)
            }
        }
    }

    private fun skipTrack(message: String){

    }

    private fun deleteTrack(message: String){

    }

    //TODO Decide here what the message id is (Track or User)
    private fun sendMessage(message: Message, id: Int){
        if(id == 0){
            message.user("Host")
        }else{
            message.user(uList[id-1].name())
        }
        addActionItemToList(message)
        addToPlaylist(message)
        writeToAllClients(serialize(message))
    }

    override fun addActionItemToList(message: Message){
        Log.d("addActionItemToList", message.name())
        Handler(Looper.getMainLooper()).post {
            aList.add(ActionItem(message.user(), message.actionMessage()))
            aListAdapter.notifyDataSetChanged()
        }
    }

    override fun addToPlaylist(message: Message){
        Log.d("addToPlaylist", message.name())
        if(pListInitialized) {
            Handler(Looper.getMainLooper()).post {
                pList.add(PlaylistItem(message.name(), message.info()))
                pListAdapter.notifyDataSetChanged()
            }
        }else{
            pListBuffer.add(PlaylistItem(message.name(), message.info()))
        }
    }

    override fun addUserToList(thread: ConnectedThread?, name: String) {
        if(uListInitialized) {
            Handler(Looper.getMainLooper()).post {
                uList.add(UserListItem(thread, name))
                uListAdapter.notifyDataSetChanged()
            }
        }else{
            uListBuffer.add(UserListItem(thread, name))
        }
    }

    private fun serialize(message: Message): ByteArray{
        val b = ByteArrayOutputStream()
        val o = ObjectOutputStream(b)
        o.writeObject(message)
        return b.toByteArray()
    }

    @Synchronized
    fun bluetoothState(): BluetoothStates {
        return bluetoothState
    }

    override fun kickDevice(pos: Int): Boolean{
        uList.forEachIndexed{ index, item ->
            if (pos == index){
                item.thread()?.write("<connection_closed>".toByteArray())
                item.thread()?.cancel()
                uList.remove(item)
                return true
            }
        }
        return false
    }

    private fun writeToAllClients(bytes: ByteArray) {
        if (::uList.isInitialized) {
            for (elem in uList) {
                elem.thread()?.write(bytes)
            }
        }else{
            for(elem in uListBuffer){
                elem.thread()?.write(bytes)
            }
        }
    }

    fun writeAndConvert(text: String){
        handleMessage(whichMethod(text), text, 0)
    }

    override fun addActionList(actionListAdapter: ActionAdapter, actionList: ArrayList<ActionItem>){
        aListAdapter = actionListAdapter
        aList = actionList
    }

    override fun addPlaylist(playlistAdapter: PlaylistAdapter, playlist: ArrayList<PlaylistItem>){
        pListAdapter = playlistAdapter
        pList = playlist
        pList.addAll(pListBuffer)
        pListAdapter.notifyDataSetChanged()
        pListInitialized = true
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

    fun limit(): Int {
        return limit
    }
}