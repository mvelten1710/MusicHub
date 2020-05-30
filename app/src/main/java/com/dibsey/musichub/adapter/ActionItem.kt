package com.dibsey.musichub.adapter

import java.io.Serializable

class ActionItem(userName: String? = null, text: String? = null): Serializable {

    private var username = userName
    private var message = text

    fun username(name: String){
        username = name
    }

    fun username(): String?{
        return username
    }

    fun message(msg: String){
        message = msg
    }

    fun message(): String?{
        return message
    }
}