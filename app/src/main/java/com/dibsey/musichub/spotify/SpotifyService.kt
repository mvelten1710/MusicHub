package com.dibsey.musichub.spotify

import android.content.Context
import android.util.Log
import com.dibsey.musichub.adapter.PlaylistAdapter
import com.dibsey.musichub.adapter.PlaylistItem
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.PlayerState
import java.io.Serializable
import kotlin.concurrent.thread

class SpotifyService(private val context: Context){

    private val TAG = "SpotifyService"
    private lateinit var token: String
    private val REQUEST_CODE = 1337
    private val CLIENT_ID = "dfd2492f39d641889bdebbafc1b65219"
    private val REDIRECT_URI = "com.dibsey.musichub://callback"

    private var mSpotifyAppRemote: SpotifyAppRemote? = null
    private val trackPlaylist = ArrayList<String?>()
    private lateinit var playlist: ArrayList<PlaylistItem>
    private lateinit var playlistAdapter: PlaylistAdapter
    private var keepWatching = false
    private var oneInstance = false
    private var trackHasEnded = true
    private var firstTime = true

    fun start() {
        // Set the connection parameters
        val connectionParams: ConnectionParams =
            ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .showAuthView(true)
                .build()

        SpotifyAppRemote.connect(context, connectionParams,
            object: Connector.ConnectionListener {

                override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                    mSpotifyAppRemote = spotifyAppRemote
                    Log.d(TAG, "Connected! Yay!")
                }

                override fun onFailure(e: Throwable) {
                    Log.e(TAG , e.message, e)
                }
            })
    }

    fun addTrackToPlaylist(track: String?) {
        // Play a playlist
        Log.d(TAG, "Added $track to Playlist")
        trackPlaylist.add(track)
        startPlaylistWatcher()
    }

    private fun startPlaylistWatcher(){
        keepWatching = true
        if(!oneInstance) {
            oneInstance = true
            mSpotifyAppRemote?.playerApi?.play(trackPlaylist[0])
            thread {
                while (keepWatching) {
                    mSpotifyAppRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback {
                        handlePlayerState(it)
                    }
                    Thread.sleep(1000)
                }
            }
        }
    }

    private fun handlePlayerState(playerState: PlayerState){
        setTrackWasStarted(playerState)

        val paused = playerState.isPaused
        val position = playerState.playbackPosition

        if (trackHasEnded && paused && position == 0L) {
            //When Track is finished new Track, if present, starts
            trackHasEnded = false
            Log.d(TAG, "Playing: ${trackPlaylist[0]}")
            if(firstTime)
                removeTrack(0)
                firstTime = false
            mSpotifyAppRemote?.playerApi?.play(trackPlaylist[0])
        }
    }

    private fun setTrackWasStarted(playerState: PlayerState){
        val position = playerState.playbackPosition
        val duration = playerState.track.duration
        val isPlaying = !playerState.isPaused

        if (!trackHasEnded && position > 0 && duration > 0 && isPlaying) {
            trackHasEnded = true
        }
    }

    fun removeTrack(pos: Int){
        Log.d("removeTrack", "${trackPlaylist.size} and ${playlist.size}")
        trackPlaylist.removeAt(pos)
        playlist.removeAt(pos)
        playlistAdapter.notifyDataSetChanged()
    }

    fun addPlaylistAndAdapter(p: ArrayList<PlaylistItem>, pA: PlaylistAdapter){
        playlist = p
        playlistAdapter = pA
        Log.d("addPlaylistAndAdapter", "${trackPlaylist.size} and ${playlist.size}")
    }

    fun stop() {
        mSpotifyAppRemote?.playerApi?.pause()
        SpotifyAppRemote.disconnect(mSpotifyAppRemote)
    }

    fun userID(): String{
        return CLIENT_ID
    }

    fun requestCode(): Int{
        return REQUEST_CODE
    }

    fun redirectUri(): String{
        return REDIRECT_URI
    }

    fun token(newToken: String){
        token = newToken
    }

    fun token(): String{
        return token
    }

    fun stopPlaylistWatcher(){
        keepWatching = false
        oneInstance = false
    }
}