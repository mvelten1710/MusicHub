package com.dibsey.musichub.logic

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.INTERNET
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import com.dibsey.musichub.R
import com.dibsey.musichub.adapter.DepthPageTransformer
import com.dibsey.musichub.adapter.ViewPagerAdapter
import com.dibsey.musichub.bluetoothServices.BluetoothClient
import com.dibsey.musichub.bluetoothServices.BluetoothServer
import com.dibsey.musichub.spotify.CheckingServices
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.concurrent.thread


@Suppress("DEPRECATION")
class MainActivity : FragmentActivity() {

    //Bluetooth Segment
    private var bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    //Miscellaneous Segment
    private var runInspector = true
    private lateinit var imm: InputMethodManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_main)
        imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        init()
    }

    private fun init(){
        initPermissions()
        initViewPager()
    }

    private fun initPermissions(){
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(ACCESS_FINE_LOCATION),
                1
            )
        }else{
            Log.d("MainActivity", "Access 1")
        }

        if (ContextCompat.checkSelfPermission(this, INTERNET)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(INTERNET),
                2
            )
        }else{
            Log.d("MainActivity", "Access 2")
        }
    }

    private fun initViewPager(){
        pager.adapter = ViewPagerAdapter(this, pager)
        pager.setCurrentItem(1, false)
        pager.setPageTransformer(DepthPageTransformer())
    }

    /*
    * checkSpotifyApp()
    *
    * Simple function that checks if the spotify app is present on the user's device to
    * use all functions
    *
    * isInstalled: true -> User can host and join others
    * isInstalled: false -> User can only join others
    * */
    private fun checkSpotifyApp(): Boolean {
        return try {
            val pm: PackageManager = this.packageManager
            val spotifyPackage = "com.spotify.music"
            pm.getPackageInfo(spotifyPackage, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /*
    * runBluetoothInspector()
    *
    * This function checks the bluetooth status in a specific interval
    * The User needs to activate the devices bluetooth to use the app properly
    *
    * */
    private val dialogFragment = BluetoothDialog()
    private fun runBluetoothInspector() {
        var once = true
        thread {
            while (true) {
                while (runInspector) {
                    Thread.sleep(100)
                    when (bluetoothAdapter.state) {
                        BluetoothAdapter.STATE_OFF -> {
                            if(once){
                                runOnUiThread {
                                    showCustomDialog()
                                    pager.setCurrentItem(1, false)
                                }
                                once = false
                            }
                        }
                        BluetoothAdapter.STATE_ON -> {
                            if(dialogFragment.isAdded && !once) {
                                dialogFragment.dismiss()
                                once = true
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showCustomDialog() {
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        val prev: Fragment? = supportFragmentManager.findFragmentByTag("dialog")
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)

        dialogFragment.isCancelable = false
        dialogFragment.show(ft, "dialog")
    }

    override fun onStart() {
        super.onStart()
        //Check if Spotify is installed
        //If true -> Start spotify auth process
        //If false -> user can only join over bt
        runInspector = true
        runBluetoothInspector()
        checkSpotifyApp()
    }

    override fun onDestroy() {
        super.onDestroy()
        runInspector = false
    }

    private val delay = 1337L
    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click/swipe BACK again to exit", Toast.LENGTH_SHORT).show()
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, delay)
    }
}