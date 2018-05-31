package com.upfunstudio.emotionsocial.User

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.upfunstudio.emotionsocial.Companion.LoginActivity
import com.upfunstudio.emotionsocial.Companion.SharedClass
import com.upfunstudio.emotionsocial.Dr.MainDoctors
import com.upfunstudio.emotionsocial.R
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAuth = FirebaseAuth.getInstance()

        // default fragment appear

        if (mAuth!!.currentUser != null)
            setFragment(DoctorListFragment())



        // if the user have a phone that mean this user is a doctor

        try {

            // dr logi
            if (!mAuth!!.currentUser!!.phoneNumber!!.isEmpty()) {

                // that mean this is a dr
                val intentDoctorAhead = Intent(this,
                        MainDoctors::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intentDoctorAhead)
                finish()

                // user logi
            } else {
                if (!mAuth!!.currentUser!!.isEmailVerified) {
                    if (SharedClass(this).loadData() == 0) {

                        val intentDoctorAhead = Intent(this,
                                LoginActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intentDoctorAhead)
                        finish()

                    }
                }

            }


        } catch (ex: Exception) {
        }

        // adapter all fragment
        adapterAllFragment()


    }


    private fun adapterAllFragment() {

        navigation.setOnNavigationItemSelectedListener { item ->

            when (item.itemId) {
                R.id.doctorList -> {

                    // dr list
                    setFragment(DoctorListFragment())

                }
                R.id.DoctorsTips -> {
                    setFragment(DoctorTipsFragment())


                }

                R.id.RecentConsult -> {
                    // to appear dialog the kind of user use the app without account
                    if (mAuth!!.currentUser != null) {
                        setFragment(RecentConsultsWithDrFragment())

                    } else {
                        AlertDialog.Builder(this)
                                .setMessage(getString(R.string.should_logi))
                                .setPositiveButton(getString(R.string.skip), { dialog, which ->
                                    setFragment(DoctorTipsFragment())
                                    dialog.dismiss()


                                }).show()


                    }


                }

            }
            false
        }


    }

    private fun setFragment(fragment: android.support.v4.app.Fragment) {

        supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .addToBackStack(null)
                .commit()


    }


}
