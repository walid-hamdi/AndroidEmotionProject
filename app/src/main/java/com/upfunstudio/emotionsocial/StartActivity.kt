package com.upfunstudio.emotionsocial

import android.content.Intent
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.widget.TextView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_start.*

class StartActivity : AppCompatActivity() {

    var dots: Array<TextView>? = null
    var current = 0

    // firebase analyse to analyse the user target
    var firebaseAnalayse: FirebaseAnalytics? = null
    var firebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        firebaseAnalayse = FirebaseAnalytics.getInstance(this)
        firebaseAuth = FirebaseAuth.getInstance()

        // todo : viewpager handle


        handleViewPager()
        dotsIndicator(0)


    }


    fun handleViewPager() {

        view_pager.adapter = AdapterPager(this)
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                dotsIndicator(position)
                if (position == 0) {
                    current = 1
                } else if (position == 1) {
                    current = 2
                } else if (position == 2) {
                    current = 3
                }


                if (current == 3) {
                    val intent = Intent(applicationContext, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)

                }

            }


        })


    }

    fun dotsIndicator(position: Int) {

        dots = arrayOf()
        for (item in 0 until dots!!.size - 1) {
            dots!![item] = TextView(this)
            dots!![item].text = Html.fromHtml("&#8002")
            dots!![item].textSize = 30f
            dots!![item].setTextColor(resources.getColor(R.color.blue))

            layoutDots.addView(dots!![item])
        }
        if (dots!!.isNotEmpty()) {

            dots!![position].setTextColor(resources.getColor(R.color.blueTrans))

        }

    }


    override fun onStart() {
        super.onStart()
        check()


    }

    fun check() {

        val currentUser = firebaseAuth!!.currentUser

        // check if the first time enter the user or not to pass into main activity
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

        }
    }
}
