package com.dibsey.musichub.spotify

import android.content.Context
import android.content.pm.PackageManager

class CheckingServices(context: Context) {

    private var activityContext: Context = context
    private var bluetoothActivated: Boolean = false

    /*
     * checkSpotifyApp()
     *
     * Simple function that checks if the spotify app is present on the user's device to
     * use all functions
     *
     * isInstalled: true -> User can host and join others
     * isInstalled: false -> User can only join others
     * */
    fun checkSpotifyApp(): Boolean {
        return try {
            val pm: PackageManager = activityContext.packageManager
            val spotifyPackage = "com.spotify.music"
            pm.getPackageInfo(spotifyPackage, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun bluetoothActivated(activated: Boolean) {
        bluetoothActivated = activated
    }

    fun bluetoothActivated(): Boolean {
        return bluetoothActivated
    }
}