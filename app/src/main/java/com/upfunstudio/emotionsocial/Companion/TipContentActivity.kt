package com.upfunstudio.emotionsocial.Companion

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.upfunstudio.emotionsocial.R
import kotlinx.android.synthetic.main.activity_tip_content.*

class TipContentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tip_content)

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
