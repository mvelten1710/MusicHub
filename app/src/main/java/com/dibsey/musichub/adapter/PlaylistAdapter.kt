package com.dibsey.musichub.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.TextView
import com.dibsey.musichub.R
import com.dibsey.musichub.items.PlaylistItem
import com.dibsey.musichub.spotify.SpotifyService

class PlaylistAdapter(): BaseAdapter() {

    private lateinit var context: Context
    private lateinit var item: ArrayList<PlaylistItem>
    private var spotifyService: SpotifyService? = null
    private lateinit var inflater: LayoutInflater

    constructor(mContext: Context, mItem: ArrayList<PlaylistItem>, mSpService: SpotifyService?) : this() {
        context = mContext
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        item = mItem
        spotifyService = mSpService
        spotifyService!!.addPlaylistAndAdapter(item, this)
    }

    constructor(mContext: Context, mItem: ArrayList<PlaylistItem>) : this() {
        context = mContext
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        item = mItem
    }


    override fun getItem(p0: Int): Any {
        return item[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return item.count()
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {

        val rowView = inflater.inflate(R.layout.listview_playlist_layout, p2, false)

        val trackName = rowView.findViewById(R.id.trackName) as TextView
        val artist = rowView.findViewById(R.id.artist) as TextView
        val delete = rowView.findViewById(R.id.delete) as ImageButton

        trackName.text = item[p0].trackName()

        artist.text = item[p0].info()

        delete.setOnClickListener {
            item.removeAt(p0)
            spotifyService?.removeTrack(p0)
            notifyDataSetChanged()
        }
        return rowView
    }
}