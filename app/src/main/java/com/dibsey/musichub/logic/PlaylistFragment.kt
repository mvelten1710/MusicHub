package com.dibsey.musichub.logic

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dibsey.musichub.R
import com.dibsey.musichub.adapter.ActionAdapter
import com.dibsey.musichub.adapter.ActionItem
import com.dibsey.musichub.adapter.PlaylistAdapter
import com.dibsey.musichub.adapter.PlaylistItem
import com.dibsey.musichub.bluetoothServices.BluetoothCS
import com.dibsey.musichub.bluetoothServices.BluetoothServer
import com.dibsey.musichub.spotify.SpotifyService
import kotlinx.android.synthetic.main.fragment_playlist.view.*

class PlaylistFragment : Fragment() {

    private lateinit var playlistAdapter: PlaylistAdapter
    private var playlist = ArrayList<PlaylistItem>()
    private lateinit var btServer: BluetoothCS
    private var spService: SpotifyService? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_playlist, container, false)

        playlistAdapter = if(spService != null){
            PlaylistAdapter(this.requireContext(), playlist, spService) //Server Call
        }else{
            PlaylistAdapter(this.requireContext(), playlist) //Client Call
        }
        view.playlist.adapter = playlistAdapter
        btServer.addPlaylist(playlistAdapter, playlist)
        playlistAdapter.notifyDataSetChanged()

        return view
    }

    override fun onResume() {
        super.onResume()
        playlistAdapter.notifyDataSetChanged()
    }

    companion object {
        @JvmStatic
        fun newInstance(server: BluetoothCS) = PlaylistFragment().apply {
            btServer = server
        }

        @JvmStatic
        fun newInstance(server: BluetoothCS, spotifyService: SpotifyService?) = PlaylistFragment().apply {
            btServer = server
            spService = spotifyService
        }
    }
}