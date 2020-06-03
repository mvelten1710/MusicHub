package com.dibsey.musichub.adapter

import com.dibsey.musichub.items.ActionItem
import java.util.*

class ActionList(list: Vector<ActionItem>) {

    private var itemCount = 0
    private var itemList: MutableList<ActionItem> = list

    fun addItem(item: ActionItem){
        itemList.add(item)
        itemCount++
    }

    fun getItem(pos: Int): ActionItem {
        return itemList[pos]
    }

    fun getAllItems(): List<ActionItem>{
        return itemList
    }

    fun getItemCount(): Int{
        return itemCount
    }

}