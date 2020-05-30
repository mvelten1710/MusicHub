package com.dibsey.musichub.adapter

class UName(name: String){

    private var uName: String = name
    private val track = false

    fun _track(): Boolean{
        return track
    }

    fun name(newName: String){
        uName = newName
    }

    fun name(): String{
        return uName
    }
}