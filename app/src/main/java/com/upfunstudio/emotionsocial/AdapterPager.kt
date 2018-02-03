package com.upfunstudio.emotionsocial

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.slider_intro.view.*


class AdapterPager : PagerAdapter {


    var context: Context
    var layout: LayoutInflater? = null

    constructor(context: Context) {

        this.context = context


    }

    private val images = listOf(R.drawable.icon_emotions,
            R.drawable.icon_dr_online)
    private val titles = listOf("Your Emotion?", "Consult Doctor Now!")


    override fun isViewFromObject(view: View?, `object`: Any?): Boolean {

        return view == `object` as RelativeLayout
    }

    override fun getCount(): Int {
        return images.size + 1
    }

    override fun instantiateItem(container: ViewGroup?, position: Int): Any {

        layout = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layout!!.inflate(R.layout.slider_intro, container, false)

        if (position < 2) {
            view.iconIntro.setImageResource(images[position])
            view.textIntro.text = titles[position]
        }



        container!!.addView(view)
        return view

    }

    override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {

        container!!.removeView(`object` as RelativeLayout)
    }


}