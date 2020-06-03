package com.dibsey.musichub.logic

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dibsey.musichub.R
import com.dibsey.musichub.adapter.ActionAdapter
import com.dibsey.musichub.items.ActionItem
import com.dibsey.musichub.bluetoothServices.BluetoothCS
import kotlinx.android.synthetic.main.fragment_action.view.*

class ActionFragment : Fragment() {

    private lateinit var actionHistoryAdapter: ActionAdapter
    private var actionList = ArrayList<ActionItem>()
    private lateinit var btServer: BluetoothCS

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_action, container, false)

        actionHistoryAdapter = ActionAdapter(this.requireContext(), actionList)
        view.actionFragmentHistory.adapter = actionHistoryAdapter
        btServer.addActionList(actionHistoryAdapter, actionList)

        return view
    }

    override fun onResume() {
        super.onResume()
        actionHistoryAdapter.notifyDataSetChanged()
    }

    companion object {
        @JvmStatic
        fun newInstance(server: BluetoothCS) = ActionFragment().apply {
            btServer = server
        }
    }
}
