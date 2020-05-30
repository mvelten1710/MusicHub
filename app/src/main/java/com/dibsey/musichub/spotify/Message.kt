package com.dibsey.musichub.spotify

import java.io.Serializable

class Message: Serializable {

    private val serialVersionUID = 6522845678267757690L

    var id = -1
    private var trackName = ""
    private var trackURI = ""
    private var trackArtist = ""
    private var trackDuration = ""
    private var user = ""

    fun uri(): String?{
        return trackURI
    }

    fun uri(newUri: String){
        trackURI = newUri
    }

    fun name(): String{
        return trackName
    }

    fun name(newName: String){
        trackName = newName
    }

    fun artist(): String{
        return trackArtist
    }

    fun artist(newArtist: String){
        trackArtist = newArtist
    }

    fun duration(): String{
        return trackDuration
    }

    fun duration(newDuration: String){
        val min = (newDuration.toInt()/1000) / 60
        val sec = (newDuration.toInt()/1000) % 60
        trackDuration = if(sec < 10){
            "${min}:0${sec}"
        }else{
            "${min}:${sec}"
        }
    }

    fun info(): String{
        return "$trackArtist \u2022 $trackDuration"
    }

    fun user(newUser: String){
        user = newUser
    }

    fun user(): String{
        return user
    }

    fun actionMessage(): String{
        return "Added Track: <b>$trackName</b> <i>by</i> <b>$trackArtist</b>"
    }

    fun clearAll(){
        trackName = ""
        trackURI = ""
        trackArtist = ""
        trackDuration = ""
        user = ""
    }
}