package com.dibsey.musichub.logic

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.dibsey.musichub.R
import com.dibsey.musichub.adapter.*
import com.dibsey.musichub.bluetoothServices.BluetoothServer
import com.dibsey.musichub.spotify.SpotifyService
import com.google.android.material.tabs.TabLayoutMediator
import com.spotify.sdk.android.authentication.AuthenticationClient
import com.spotify.sdk.android.authentication.AuthenticationRequest
import com.spotify.sdk.android.authentication.AuthenticationResponse
import kotlinx.android.synthetic.main.host_layout.*
import kotlinx.android.synthetic.main.host_layout.backButton
import kotlinx.android.synthetic.main.host_layout.editText
import kotlin.concurrent.thread

class HostActivity : AppCompatActivity(){

    private val mSpotifyService =
        SpotifyService(this)
    private lateinit var sharedPref: SharedPreferences
    private lateinit var mQueue: RequestQueue
    private lateinit var bluetoothServer: BluetoothServer
    private var bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var runInspector = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.host_layout)
        sharedPref = this.getPreferences(Context.MODE_PRIVATE)

        mQueue = Volley.newRequestQueue(this)
        bluetoothServer = BluetoothServer(mQueue, mSpotifyService)

        backButton.setOnClickListener {
            back()
        }

        btVisibility.setOnClickListener {
            val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120)
            }
            startActivity(discoverableIntent)
        }

        hostSend.setOnClickListener {
            if(editText.text!!.isNotEmpty() && editText.text!!.isNotBlank()){
                val text = "${editText.text}"
                bluetoothServer.writeAndConvert("Play $text")
                editText!!.text!!.clear()
            }
        }
        bluetoothServer.start()

        initViewPager()
    }

    private fun initSpotifyLogin() {
        val builder = AuthenticationRequest.Builder(
            mSpotifyService.userID(),
            AuthenticationResponse.Type.TOKEN,
            mSpotifyService.redirectUri()
        )

        builder.setScopes(arrayOf("streaming"))
        val request = builder.build()

        AuthenticationClient.openLoginActivity(this, mSpotifyService.requestCode(), request)

        mSpotifyService.token(sharedPref.getString("token", "no_token").toString())
    }

    private fun back(){
        bluetoothServer.closeAllConnections()
        bluetoothServer.closeServerSocket()
        finish()
    }

    private fun initViewPager(){
        val adapter = ViewPagerAdapter2(this, bluetoothServer, mSpotifyService, 0)
        serverPager.adapter = adapter
        //WHAT IS THIS GOOGLE? PLEASE ADD SUPPORT FOR VIEWPAGER2 TO TAB LAYOUT
        TabLayoutMediator(tabs, serverPager) { tab, position ->
            serverPager.setCurrentItem(tab.position, true)
            if (position == 0) tab.text = "Playlist"
            if (position == 1) tab.text = "Actions"
            if (position == 2) tab.text = "Users"
        }.attach()
        serverPager.setCurrentItem(1, false)
    }

    override fun onStart() {
        super.onStart()
        runInspector = true
        runBluetoothInspector()
        initSpotifyLogin()
    }

    override fun onDestroy() {
        super.onDestroy()
        runInspector = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        // Check if result comes from the correct activity
        if (requestCode == mSpotifyService.requestCode()) {
            val response = AuthenticationClient.getResponse(resultCode, intent)

            when (response.type) {
                // Response was successful and contains auth token.
                // User can use the app with all its features
                AuthenticationResponse.Type.TOKEN -> {
                    Log.d("Host Activity", "Auth TOKEN")
                    mSpotifyService.token(response.accessToken)
                    Log.d("Host Activity", "SpotifyToken is now set: ${mSpotifyService.token()}")
                    val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
                    with(sharedPref.edit()) {
                        putString("token", response.accessToken)
                        commit()
                    }
                    mSpotifyService.start()
                }

                // Auth flow returned an error and the web view gets called
                AuthenticationResponse.Type.ERROR -> {
                    Log.d("Host Activity", "Auth ERROR")
                    val builder = AuthenticationRequest.Builder(
                        mSpotifyService.userID(),
                        AuthenticationResponse.Type.TOKEN,
                        mSpotifyService.redirectUri()
                    )

                    builder.setScopes(arrayOf("streaming"))
                    val request = builder.build()

                    AuthenticationClient.openLoginInBrowser(this, request)
                }
                else -> {
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        val uri = intent?.data
        if (uri != null) {
            val response = AuthenticationResponse.fromUri(uri)

            when (response.type) {
                AuthenticationResponse.Type.TOKEN -> {
                    mSpotifyService.token(response.accessToken)
                    Log.d("Host Activity", "SpotifyToken is now set: ${mSpotifyService.token()}")
                    val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
                    with(sharedPref.edit()) {
                        putString("token", response.accessToken)
                        commit()
                    }
                    mSpotifyService.start()
                }

                AuthenticationResponse.Type.ERROR -> {
                }
                else -> {
                }
            }
        }
    }

    private fun runBluetoothInspector() {
        var once = true
        thread {
            while (true) {
                while (runInspector) {
                    Thread.sleep(100)
                    when (bluetoothAdapter.state) {
                        BluetoothAdapter.STATE_TURNING_OFF -> {
                            bluetoothServer.closeServerSocket()
                            bluetoothServer.closeAllConnections()
                            mSpotifyService.stopPlaylistWatcher()
                            mSpotifyService.stop()
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
