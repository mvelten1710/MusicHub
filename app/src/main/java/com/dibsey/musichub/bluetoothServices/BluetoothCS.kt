package com.dibsey.musichub.bluetoothServices

import com.dibsey.musichub.adapter.*
import com.dibsey.musichub.items.ActionItem
import com.dibsey.musichub.items.PlaylistItem
import com.dibsey.musichub.items.UserListItem
import com.dibsey.musichub.spotify.Message

interface BluetoothCS{
    val id: Int

    fun addActionList(actionListAdapter: ActionAdapter, actionList: ArrayList<ActionItem>)
    fun addPlaylist(playlistAdapter: PlaylistAdapter, playlist: ArrayList<PlaylistItem>)
    fun addUserList(userListAdapter: UserArrayAdapter, userList: ArrayList<UserListItem>)

    fun addToPlaylist(message: Message)
    fun addActionItemToList(message: Message)
    fun addUserToList(thread: BluetoothServer.ConnectedThread? = null, name: String = "")

    fun kickDevice(pos: Int): Boolean
}