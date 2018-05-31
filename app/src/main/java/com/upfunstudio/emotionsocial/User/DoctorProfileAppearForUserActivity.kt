package com.upfunstudio.emotionsocial.User

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.upfunstudio.emotionsocial.Companion.WindowAnalyseOrCalling
import com.upfunstudio.emotionsocial.R
import kotlinx.android.synthetic.main.activity_doctor_profile.*

class DoctorProfileAppearForUserActivity : AppCompatActivity() {


    private var mAuth: FirebaseAuth? = null
    private var mFireStor: FirebaseFirestore? = null
    private var doctorID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_profile)
        mAuth = FirebaseAuth.getInstance()
        mFireStor = FirebaseFirestore.getInstance()



        loadDrID()
        loadData()


    }

    private fun loadDrID() {
        try {

            doctorID = intent.extras.getString("doctorID")

        } catch (ex: Exception) {

        }


    }


    private fun loadData() {


        try {
            if (intent.extras.getBoolean("fromReport")) {
                supportActionBar!!.hide()
                //ActionBar.DISPLAY_SHOW_HOME

            }


            //  handle , if dr online show button for pay else show button dr not available now
            mFireStor!!
                    .collection("Doctors")
                    .whereEqualTo("uid", doctorID)
                    .addSnapshotListener { querySnapshot, _ ->
                        // to adapte real time chage
                        try {
                            for (change in querySnapshot.documentChanges) {

                                if (change.document.exists() && change != null) {

                                    val username = change.document.getString("username")
                                    val phone = change.document.getString("phone")
                                    val mail = change.document.getString("mail")
                                    val speciality = change.document.getString("specialityText")
                                    val state = change.document.getBoolean("state")
                                    val pic = change.document.getString("picPath")
                                    val city = change.document.getString("city")
                                    val language = change.document.getString("language")
                                    val connectionNow = change.document.getBoolean("connectNow")
                                    val request = change.document.getBoolean("request")


                                    loadDataCheckState(username,
                                            phone, mail, speciality, state, pic,
                                            city, language, connectionNow, request)


                                } else {
                                    Toast.makeText(applicationContext,
                                            getString(R.string.check_ccx_first),
                                            Toast.LENGTH_LONG).show()

                                }

                            }


                        } catch (ex: Exception) {
                            Toast.makeText(applicationContext,
                                    getString(R.string.check_ccx_first),
                                    Toast.LENGTH_LONG).show()
                        }


                    }

        } catch (ex: Exception) {

        }

    }

    private fun loadDataCheckState(username: String, phone: String, mail: String,
                                   speciality: String, state: Boolean, pic: String,
                                   city: String, language: String,
                                   connectionNow: Boolean, request: Boolean) {


        emailDefault.text = mail
        phoneDefault.text = phone
        dr_name.text = username
        specialityText.text = speciality
        cityPlace.text = city
        languageDefault.text = language

        if (pic.isEmpty()) {
            Picasso.with(this).load(R.drawable.profile_photo)
                    .into(profile_picture)
        } else {
            Picasso.with(this).load(pic)
                    .placeholder(R.drawable.profile_photo)
                    .into(profile_picture)
        }




        if (!state) {

            availableButton.text = getString(R.string.fail_avail_msg)
            //availableButton.background=resources.getDrawable(R.drawable.button_refuse_background)
            availableButton.setOnClickListener {
                Toast.makeText(this, getString(R.string.fail_avail_msg),
                        Toast.LENGTH_LONG).show()
            }


            // if the dr connection with someone you can't connect him
        } else {
            if (connectionNow || request) {
                availableButton.text = getString(R.string.dr_cx_msg)
                //availableButton.background=resources.getDrawable(R.drawable.button_refuse_background)
                availableButton.setOnClickListener {
                    Toast.makeText(this, getString(R.string.connect_now_msg),
                            Toast.LENGTH_LONG).show()
                }
                // that mean you can connect dr now
            } else {

                availableButton.text = getString(R.string.cosult_msg)
                availableButton.setOnClickListener {
                    // todo : pay to consult dr now online
                    // check out if the cx worked well

                    val fr = WindowAnalyseOrCalling()
                    val bundle = Bundle()
                    val placeActivity = "calling"
                    bundle.putString("placeActivity", placeActivity)
                    bundle.putString("pictureDoctor", pic)
                    bundle.putString("doctorID", doctorID)
                    bundle.putString("doctorName", username)


                    fr.arguments = bundle
                    fr.show(fragmentManager, "Show")
                    //doctorProfileLayout.visibility = View.GONE


                }
            }
        }
    }


}
