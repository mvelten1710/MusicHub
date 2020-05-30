package com.dibsey.musichub.logic


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dibsey.musichub.R
import kotlinx.android.synthetic.main.menu_layout.*


class MenuFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.menu_layout, container, false)

    override fun onStart() {
        super.onStart()
        arrowLeft.setImageDrawable(resources.getDrawable(R.drawable.ic_arrow_back_ios_black_48dp))
        arrowRight.setImageDrawable(resources.getDrawable(R.drawable.ic_arrow_forward_ios_black_48dp))
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            MenuFragment()
    }
}
