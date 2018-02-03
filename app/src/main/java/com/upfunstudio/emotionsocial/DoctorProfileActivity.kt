package com.upfunstudio.emotionsocial

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_doctor_profile.*

class DoctorProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_profile)

        returnHomeButton.setOnClickListener {

            finish()
            // return home main activity

        }

    }


    fun payNowEvent(view: View) {
        Toast.makeText(this, "Pay now !!!!", Toast.LENGTH_SHORT).show()

        // todo : handle pay credit card

    }
}
