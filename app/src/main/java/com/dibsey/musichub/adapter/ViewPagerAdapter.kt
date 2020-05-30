package com.dibsey.musichub.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.dibsey.musichub.logic.*


class ViewPagerAdapter(fragmentActivity: FragmentActivity, newPager: ViewPager2) :
    FragmentStateAdapter(fragmentActivity) {

    private var pager: ViewPager2 = newPager

    override fun createFragment(newPosition: Int): Fragment {
       return when (newPosition) {
            0 -> {
                HostFragment.newInstance(pager)
            }
            1 -> {
                MenuFragment.newInstance()
            }
            2 -> {
                JoinFragment.newInstance(pager)
            }
            else -> {
                MenuFragment.newInstance()
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