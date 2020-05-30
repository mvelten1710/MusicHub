package com.dibsey.musichub.adapter

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.dibsey.musichub.R

class ActionAdapter(context: Context, private val item: ArrayList<ActionItem>): BaseAdapter()
{
    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getItem(p0: Int): Any {
        return item[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return item.count()
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {

        val rowView = inflater.inflate(R.layout.listview_action_item, p2, false)

        val message = rowView.findViewById(R.id.deviceName) as TextView
        val userName = rowView.findViewById(R.id.userName) as TextView

        message.text = HtmlCompat.fromHtml(item[p0].message()!!, HtmlCompat.FROM_HTML_MODE_LEGACY)
        userName.text = HtmlCompat.fromHtml(item[p0].username()!!, HtmlCompat.FROM_HTML_MODE_LEGACY)

        return rowView
    }

    fun remove(pos: Int){
        item.removeAt(pos)
    }
}