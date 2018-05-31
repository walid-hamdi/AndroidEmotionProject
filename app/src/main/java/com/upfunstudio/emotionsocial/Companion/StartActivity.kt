package com.upfunstudio.emotionsocial.Companion

import android.content.Intent
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.upfunstudio.emotionsocial.R
import com.upfunstudio.emotionsocial.User.MainActivity
import kotlinx.android.synthetic.main.activity_start.*

class StartActivity : AppCompatActivity() {

    var current = 0

    private var mAnalyse: FirebaseAnalytics? = null
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        mAnalyse = FirebaseAnalytics.getInstance(this)
        mAuth = FirebaseAuth.getInstance()

        // viewpager handle
        handleViewPager()


    }


    private fun handleViewPager() {

        view_pager.adapter = AdapterIntro(this)
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
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

    override fun onStart() {
        super.onStart()
        check()

    }

    private fun check() {


        // if doctor entering
        val currentUser = mAuth!!.currentUser
        if (currentUser != null) {


            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()


        }
        // check ormal user
        if (SharedClass(this).loadData() != 0) {

            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

        }


    }


}
