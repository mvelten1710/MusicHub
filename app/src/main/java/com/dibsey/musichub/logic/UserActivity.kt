package com.dibsey.musichub.logic

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.dibsey.musichub.R
import com.dibsey.musichub.adapter.ViewPagerAdapter2
import com.dibsey.musichub.bluetoothServices.BluetoothClient
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.user_layout.*
import kotlinx.android.synthetic.main.user_layout.backButton
import kotlinx.android.synthetic.main.user_layout.editText
import kotlinx.android.synthetic.main.user_layout.sendButton
import java.nio.charset.Charset
import java.util.*
import kotlin.concurrent.thread

class UserActivity : AppCompatActivity() {

    private var mUUID: UUID = UUID.fromString("3fb6b3c8-0368-47fb-9814-3e9157a65173")

    private var username: String? = null
    private var runInspector = true
    private var btDevice: BluetoothDevice? = null
    private lateinit var bluetoothClient: BluetoothClient
    private var bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_layout)

        btDevice = intent.extras?.getParcelable("btDevice")
        username = intent.extras?.getString("username")

        bluetoothClient = BluetoothClient(username)

        backButton.setOnClickListener {
            bluetoothClient.closeClientConnectedSocket()
            finish()
        }

        sendButton.setOnClickListener {
            if(editText.text!!.isNotEmpty() && editText.text!!.isNotBlank()){
                bluetoothClient.write("Play ${editText.text}".toByteArray(Charset.defaultCharset()))
                editText!!.text!!.clear()
            }
        }

        thread {
            while(true){
                Thread.sleep(500)
                if(bluetoothClient.bluetoothState() == BluetoothClient.BluetoothStates.NULL){
                    finish()
                }
            }
        }

        initViewPager()
        startBluetoothConnection()
    }

    private fun initViewPager(){
        val adapter = ViewPagerAdapter2(this, bluetoothClient,null, 1)
        userPager.adapter = adapter
        //WHAT IS THIS GOOGLE? PLEASE ADD SUPPORT FOR VIEWPAGER2 TO TAB LAYOUT
        TabLayoutMediator(userTabs, userPager) { tab, position ->
            userPager.setCurrentItem(tab.position, true)
            if (position == 0) tab.text = "Playlist"
            if (position == 1) tab.text = "Actions"
            if (position == 2) tab.text = "Users"
        }.attach()
        userPager.setCurrentItem(1, false)
    }

    /*
    * startBluetoothConnection()
    *
    * Calls createConnection()
    * */
    private fun startBluetoothConnection() {
        createConnection(btDevice!!, mUUID)
        thread {
            while (true) {
                Thread.sleep(500)
                if (bluetoothClient.bluetoothState() == BluetoothClient.BluetoothStates.CONNECTED) {
                    runOnUiThread {
                        loadingPanel.visibility = View.GONE
                    }
                    break
                }
                if (bluetoothClient.bluetoothState() == BluetoothClient.BluetoothStates.NO_CONNECTION) {
                    runOnUiThread {
                        Toast.makeText(this, "Could not connect to device", Toast.LENGTH_LONG)
                            .show()
                        loadingPanel.visibility = View.GONE
                        finish()
                    }
                    break
                }
            }
        }
    }

    /*
    * createConnection()
    *
    * param: device: BluetoothDevice -> Bluetooth Device to that we want to connect
    * param: uuid: UUID -> simple UUID that identifies the program
    * */
    private fun createConnection(device: BluetoothDevice, uuid: UUID) {
        Log.d("UserConnect", "createConnection: Init RFCOM Bluetooth Connection")
        bluetoothClient.startClient(device, uuid)
    }

    private fun runBluetoothInspector() {
        var once = true
        thread {
            while (true) {
                while (runInspector) {
                    Thread.sleep(100)
                    when (bluetoothAdapter.state) {
                        BluetoothAdapter.STATE_TURNING_OFF -> {
                            bluetoothClient.closeClientConnectedSocket()
                        }
                        BluetoothAdapter.STATE_OFF -> {
                            if (once) {
                                finish()
                                runInspector = false
                                once = false
                            }
                        }

                    }
                }
            }
        }
    }
}
