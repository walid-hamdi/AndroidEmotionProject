package com.upfunstudio.emotionsocial.Companion

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.upfunstudio.emotionsocial.User.MainActivity
import com.upfunstudio.emotionsocial.R


class ConsultRoomActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consult_room)


        // todo : design chat room and handle it with webRtc technology

    }

    fun finishReturnEvent(view: View) {

        startActivity(Intent(this, MainActivity::class.java))

    }


}
