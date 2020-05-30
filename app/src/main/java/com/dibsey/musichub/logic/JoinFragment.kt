package com.dibsey.musichub.logic

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager2.widget.ViewPager2
import com.dibsey.musichub.*
import com.dibsey.musichub.adapter.BluetoothArrayAdapter
import kotlinx.android.synthetic.main.join_layout.*
import kotlinx.android.synthetic.main.join_layout.view.*
import kotlin.math.ceil


class JoinFragment : Fragment() {

    private lateinit var pager: ViewPager2

    private var itemHeight = 0

    private var bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var savedDevicesList = mutableSetOf<BluetoothDevice>()
    private lateinit var savedDevicesAdapter: BluetoothArrayAdapter
    private var newDeviceName: String? = null
    private var btDevice: BluetoothDevice? = null

    private var listItemChecked = false
    private var listViewExpanded = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{

        val view = inflater.inflate(R.layout.join_layout, container, false)
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        val defaultUsername = BluetoothAdapter.getDefaultAdapter().name
        val username = sharedPref?.getString(getString(R.string.username_placeholder), defaultUsername)
        view.renameButton.text = resources.getString(R.string.username_placeholder, username)

        activity?.runOnUiThread {
            view?.joinButton?.visibility = View.GONE
            view?.divider5?.visibility = View.GONE
        }

        savedDevicesAdapter = BluetoothArrayAdapter(
            this.requireContext(),
            savedDevicesList
        )
        view.savedDevicesListView.adapter = savedDevicesAdapter
        updateSavedList()

        view.addButton.setOnClickListener {
            val intent = Intent(this.requireContext(), NewDeviceActivity::class.java)
            startActivityForResult(intent, 1338)
        }

        view.renameButton.setOnClickListener {
            showEditDialog()
        }

        view.joinButton.setOnClickListener {
            if(listItemChecked && btDevice != null)
                listItemChecked = false
                val intent = Intent(this.requireContext(), UserActivity::class.java)
                intent.putExtra("btDevice", btDevice)
                intent.putExtra("username", username)
                startActivity(intent)
                Handler().postDelayed({
                    pager.setCurrentItem(1, false)
                }, 500)
        }

        view.otherDevicesButton.setOnClickListener {
            listViewExpanded = true
            setListViewHeight()
        }

        view.savedDevicesListView.setOnItemClickListener { _, _, pos, _ ->
            activity?.runOnUiThread {
                view?.joinButton?.visibility = View.VISIBLE
                view?.divider5?.visibility = View.VISIBLE
            }
            btDevice = savedDevicesList.elementAt(pos)
            listItemChecked = true
        }

        view.backButton.setOnClickListener {
            back()
        }
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(pager: ViewPager2) = JoinFragment().apply {
            this.pager = pager
        }
    }

    private fun showEditDialog() {
        val ft: FragmentTransaction = fragmentManager!!.beginTransaction()
        val prev = fragmentManager!!.findFragmentByTag("RenameDialogFragment")
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)
        val newDialog =
            RenameDialogFragment.newInstance()
        newDialog.setTargetFragment(this, 1337)
        newDialog.show(ft, "RenameDialogFragment")
    }

    private fun setListViewHeight(){
        updateSavedList()
        if(itemHeight == 0 && savedDevicesAdapter.count > 0){
            otherDevicesButton.visibility = View.GONE
            val listItem = savedDevicesAdapter.getView(0, null, savedDevicesListView)
            listItem.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
            itemHeight = listItem.measuredHeight
            view?.savedDevicesListView?.layoutParams?.height = (ceil(itemHeight.toDouble()/10)*10).toInt()*getTotalItemCount()
        }
    }

    private fun getTotalItemCount(): Int{
        return if (savedDevicesAdapter.count > 5){
            5
        }else{
            savedDevicesAdapter.count
        }
    }

    private fun setListView(){
        savedDevicesList.forEachIndexed { index, device ->
            if(device.name == newDeviceName){
                btDevice = device
                savedDevicesListView.smoothScrollToPosition(index)
                savedDevicesAdapter.selectedPosition(index)
                listItemChecked = true
                Log.d("JoinFragment", "FOUND DEVICE")
            }
        }
        savedDevicesAdapter.notifyDataSetChanged()
    }

    private fun updateSavedList(){
        savedDevicesList.clear()
        if(bluetoothAdapter.bondedDevices.size != 0) {
            if (listViewExpanded) {
                savedDevicesList = savedDevicesList.union(bluetoothAdapter.bondedDevices)
            } else {
                when {
                    bluetoothAdapter.bondedDevices.size <= 5 -> {
                        view?.otherDevicesButton?.visibility = View.GONE
                        bluetoothAdapter.bondedDevices.forEach { device ->
                            savedDevicesList.add(device)
                        }
                    }
                    bluetoothAdapter.bondedDevices.size > 0 -> {
                        bluetoothAdapter.bondedDevices.forEachIndexed { index, device ->
                            if (index < 3)
                                savedDevicesList.add(device)
                        }
                    }
                    else -> {
                        view?.otherDevicesButton?.visibility = View.GONE
                        view?.textView6?.visibility = View.GONE
                    }
                }
            }
        }
        savedDevicesAdapter.notifyDataSetChanged()
    }

    private fun back(){
        pager.setCurrentItem(1, true)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == 1337){
            renameButton.text = data?.getStringExtra("username")
        }
        if(resultCode == 1338){
            if (data?.getStringExtra("deviceName") != null)
                newDeviceName = data.getStringExtra("deviceName")
                activity?.runOnUiThread {
                    view?.joinButton?.visibility = View.VISIBLE
                    view?.divider5?.visibility = View.VISIBLE
                }
                listViewExpanded = true
                setListViewHeight()
                setListView()
        }
    }

    override fun onResume() {
        super.onResume()
        updateSavedList()
    }

    private infix fun MutableSet<BluetoothDevice>.union(set: Set<BluetoothDevice>): MutableSet<BluetoothDevice>{
        val mSet = this
        mSet.addAll(set)
        return mSet
    }
}