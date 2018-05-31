package com.upfunstudio.emotionsocial.Companion

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.upfunstudio.emotionsocial.R
import kotlinx.android.synthetic.main.activity_start.view.*
import kotlinx.android.synthetic.main.slider_intro.view.*


class AdapterIntro(contexts: Context) : PagerAdapter() {


    var context: Context = contexts
    var layout: LayoutInflater? = null

    private val images = listOf(R.drawable.icon_emotions,
            R.drawable.icon_dr_online)
    private val titles = listOf(context.getString(R.string.your_emotion_text_intro),
            context.getString(R.string.cosult_dr_text_intro))


    override fun getCount(): Int {
        return images.size + 1
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object` as RelativeLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {


        layout = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layout!!.inflate(R.layout.slider_intro, container, false)

        if (position < 2) {
            view.iconIntro.setImageResource(images[position])
            view.textIntro.text = titles[position]

        }



        container.addView(view)
        return view


    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {

        container.removeView(`object` as RelativeLayout)

    }
}


