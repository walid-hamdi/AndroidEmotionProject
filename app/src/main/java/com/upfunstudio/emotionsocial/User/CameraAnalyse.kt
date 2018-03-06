package com.upfunstudio.emotionsocial.User

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.upfunstudio.emotionsocial.R

class CameraAnalyse : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_analyse)


    }

    fun getReportEvent(view: View) {

        /*
          todo : get all results and appear it in report result
          result (graph for emotion , emotion state, tips to avoid the state)
          all the info get when train model to analyse real time video
          with openGl and tensorflow


          */

        startActivity(Intent(this,
                ReportDetailsActivity::class.java))

    }

}
