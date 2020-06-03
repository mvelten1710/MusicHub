package com.dibsey.musichub.items

class PlaylistItem(tName: String? = null, i: String? = null) {

    private var trackName = tName
    private var info = i

    fun trackName(): String?{
        return trackName
    }

    fun trackName(newName: String){
        trackName = newName
    }

    fun info(): String?{
        return info
    }

    fun info(newInfo: String){
        info = newInfo
    }
}