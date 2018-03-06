package com.upfunstudio.emotionsocial.Dr

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.upfunstudio.emotionsocial.Companion.LoginActivity
import com.upfunstudio.emotionsocial.Companion.RecentConsultsFragment
import com.upfunstudio.emotionsocial.R
import com.upfunstudio.emotionsocial.User.WindowAnalyseOrCalling
import kotlinx.android.synthetic.main.activity_main_doctors.*


class MainDoctors : AppCompatActivity() {


    private var mAuth: FirebaseAuth? = null
    private var mFireStore: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_doctors)
        mAuth = FirebaseAuth.getInstance()
        mFireStore = FirebaseFirestore.getInstance()

        setFragment(MyDrTipsFragment())

        prepareAllFragment()

    }

    private fun prepareAllFragment() {

        navigationDoctor.setOnNavigationItemSelectedListener { item ->
            when (item!!.itemId) {

                R.id.MydoctorTips -> {
                    setFragment(MyDrTipsFragment())
                }

                R.id.requests -> {
                    setFragment(CallingRequestFragment())
                }
                R.id.RecentConsultDoctor -> {
                    setFragment(RecentConsultsFragment())
                }


            }
            true
        }


    }

    private fun setFragment(fragment: Fragment) {

        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutDoctor, fragment)
                .addToBackStack(null)
                .commit()


    }

    override fun onStart() {
        super.onStart()


        // todo : appear calling dialogue when someone want to consult doctor
        mFireStore!!.collection("Doctors")
                .document(mAuth!!.currentUser!!.uid)
                .collection("Request")
                .document()
                .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                    try {


                        if (documentSnapshot.exists()) {
                            val request = documentSnapshot.get("timeRequest")
                            if (request != null) {
                                if (request == FieldValue.serverTimestamp()) {

                                    val fr = WindowAnalyseOrCalling()
                                    val bundle = Bundle()
                                    val placeActivity = "doctorRequest"
                                    bundle.putString("placeActivity", placeActivity)
                                    fr.arguments = bundle
                                    fr.show(fragmentManager, "Show")

                                }

                            }

                        }
                    } catch (ex: Exception) {
                    }

                    setFragment(MyDrTipsFragment())


                }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_doctor_menu, menu)



        return true
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {

            R.id.profileMenuDoctor -> {

                // go profile settings
                val intent = Intent(this, SetttingsDoctorActivity::class.java)

                startActivity(intent)


            }
            R.id.AddTipsDoctor -> {

                var addTip = AddTipsFragment()
                addTip.show(supportFragmentManager, "show")

            }


            R.id.logoutMenuDoctor -> {

                // logout doctor from system
                checkDoctorAndLogout()


            }
            else -> {
                return super.onOptionsItemSelected(item)

            }


        }

        return true
    }

    private fun checkDoctorAndLogout() {

        // change state doctor to offline before logout
        mFireStore!!.collection("Doctors")
                .document(mAuth!!.currentUser!!.uid)
                .update("state", false)
        // check for doctor and logout
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener {
                    if (mAuth!!.currentUser == null) {
                        val intent = Intent(applicationContext, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                }


    }


    override fun onBackPressed() {
        super.onBackPressed()
    }


}
