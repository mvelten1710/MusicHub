package com.dibsey.musichub.logic

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dibsey.musichub.R
import com.dibsey.musichub.adapter.UserArrayAdapter
import com.dibsey.musichub.adapter.UserListItem
import com.dibsey.musichub.bluetoothServices.BluetoothCS
import kotlinx.android.synthetic.main.fragment_user.view.*

class UserFragment : Fragment() {

    private lateinit var userListAdapter: UserArrayAdapter
    private var userList = ArrayList<UserListItem>()

    private lateinit var btServer: BluetoothCS

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user, container, false)

        userListAdapter = UserArrayAdapter(
            this.requireContext(),
            userList,
            btServer
        )
        view.userList.adapter = userListAdapter
        btServer.addUserList(userListAdapter, userList)

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(server: BluetoothCS) = UserFragment().apply {
            btServer = server
        }
    }
}