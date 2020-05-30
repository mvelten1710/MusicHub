package com.dibsey.musichub.logic

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.*
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.dibsey.musichub.*
import com.google.gson.Gson

class HostFragment : Fragment() {

    private lateinit var pager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        return inflater.inflate(R.layout.server_loading_layout, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(pager: ViewPager2) = HostFragment().apply {
            this.pager = pager
        }
    }

    override fun onResume() {
        super.onResume()
        Handler().postDelayed({
            startActivity(Intent(this.requireContext(), HostActivity::class.java))
            pager.setCurrentItem(1, false)
        }, 1000)
    }
}