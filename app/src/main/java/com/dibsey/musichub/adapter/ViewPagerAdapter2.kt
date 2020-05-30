package com.dibsey.musichub.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dibsey.musichub.bluetoothServices.BluetoothCS
import com.dibsey.musichub.logic.*
import com.dibsey.musichub.spotify.SpotifyService


class ViewPagerAdapter2(fragmentActivity: FragmentActivity, bcs: BluetoothCS, service: SpotifyService?, idd: Int) :
    FragmentStateAdapter(fragmentActivity) {

    private val cs = bcs
    private val spService = service
    private val id = idd

    override fun createFragment(newPosition: Int): Fragment {
        return when (newPosition) {
            0 -> {
                if(id == 0) {
                    PlaylistFragment.newInstance(cs, spService!!)
                }else{
                    PlaylistFragment.newInstance(cs)
                }
            }
            1 -> {
                ActionFragment.newInstance(cs)
            }
            2 -> {
                UserFragment.newInstance(cs)
            }
            else -> {
                ActionFragment.newInstance(cs)
            }
        }
    }

    override fun getItemCount(): Int {
        return CARD_ITEM_SIZE
    }

    companion object {
        private const val CARD_ITEM_SIZE = 3
    }
}