package com.upfunstudio.emotionsocial.User

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.upfunstudio.emotionsocial.R
import kotlinx.android.synthetic.main.activity_doctor_profile.*

class DoctorProfileAppearForUserActivity : AppCompatActivity() {


    private var myEmail: String? = null
    private var mAuth: FirebaseAuth? = null
    private var myPicture: String? = null
    private var mFireStor: FirebaseFirestore? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_profile)
        mAuth = FirebaseAuth.getInstance()
        mFireStor = FirebaseFirestore.getInstance()
        myEmail = mAuth!!.currentUser!!.email
        loadData()


    }

    private fun loadData() {


        // reading data passing from main user activity
        try {
            val bundle = intent.extras
            val uid = bundle.getString("uid")
            val username = bundle.getString("username")
            val phone = bundle.getString("phone")
            val mail = bundle.getString("mail")
            val speciality = bundle.getString("specialityText")
            var state = bundle.getBoolean("state")
            val pic = bundle.getString("picPath")
            val city = bundle.getString("city")
            val language = bundle.getString("language")


            emailDefault.text = mail
            phoneDefault.text = phone
            dr_name.text = username
            specialityText.text = speciality
            cityPlace.text = city
            languageDefault.text = language
            if (pic.isNullOrEmpty()) {
                Picasso.with(this).load(R.drawable.profile_photo)
                        .into(profile_picture)
            } else {
                Picasso.with(this).load(pic)
                        .placeholder(R.drawable.profile_photo)
                        .into(profile_picture)
            }


            //  handle , if dr online show button for pay else show button dr not available now
            if (!state) {

                availableButton.text = "NOT AVAILABLE NOW"
                availableButton.setOnClickListener {
                    Toast.makeText(this, "Doctor no available now!!",
                            Toast.LENGTH_LONG).show()
                }


            } else {

                availableButton.text = "CONSULT NOW"
                availableButton.setOnClickListener {
                    // pay to consult dr now online
                    val fr = WindowAnalyseOrCalling()
                    val bundle = Bundle()
                    val placeActivity = "calling"
                    bundle.putString("placeActivity", placeActivity)
                    bundle.putString("pictureDoctor", pic)
                    bundle.putString("myEmail", myEmail)
                    bundle.putString("doctorID", uid)
                    mFireStor!!.collection("Users")
                            .document(mAuth!!.currentUser!!.uid)
                            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->

                                if (documentSnapshot.exists()) {
                                    myPicture = documentSnapshot.getString("picPath")
                                    bundle.putString("myPicture", myPicture)

                                }

                            }

                    fr.arguments = bundle
                    val frman = fragmentManager
                    fr.show(frman, "Show")
                    doctorProfileLayout.visibility = View.GONE

                }

            }
        } catch (ex: Exception) {
        }


    }


}
