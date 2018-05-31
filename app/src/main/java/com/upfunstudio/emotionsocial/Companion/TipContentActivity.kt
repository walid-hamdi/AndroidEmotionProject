package com.upfunstudio.emotionsocial.Companion

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.upfunstudio.emotionsocial.R
import kotlinx.android.synthetic.main.activity_tip_content.*

class TipContentActivity : AppCompatActivity() {

    private lateinit var mAdView: AdView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tip_content)

        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().addTestDevice("test").build()
        mAdView.loadAd(adRequest)

        try {

            val bundle = intent.extras
            val title = bundle.getString("title")
            val description = bundle.getString("description")

            titleTipContent.text = title
            contentTipContent.text = description


        } catch (ex: Exception) {

        }

    }
}
