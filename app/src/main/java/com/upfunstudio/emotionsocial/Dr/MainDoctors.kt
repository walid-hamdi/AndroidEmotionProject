package com.upfunstudio.emotionsocial.Dr

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.upfunstudio.emotionsocial.Companion.ConsultRoomActivity
import com.upfunstudio.emotionsocial.Companion.LoginActivity
import com.upfunstudio.emotionsocial.Companion.WindowAnalyseOrCalling
import com.upfunstudio.emotionsocial.R
import kotlinx.android.synthetic.main.activity_main_doctors.*


class MainDoctors : AppCompatActivity() {


    private var mAuth: FirebaseAuth? = null
    private var mFireStore: FirebaseFirestore? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_doctors)

        mAuth = FirebaseAuth.getInstance()
        mFireStore = FirebaseFirestore.getInstance()
        prepareAllFragment()


    }

    override fun onResume() {
        // call to check if there is any request or not
        realTimeRequest()

        super.onResume()

    }


    // real time request
    private fun realTimeRequest() {
        // check it out if there is any request
        mFireStore!!.collection("Doctors")
                .document(mAuth!!.currentUser!!.uid)
                .addSnapshotListener { documentSnapshot, _ ->


                    try {
                        if (documentSnapshot.exists() && documentSnapshot != null) {


                            val doctorRequest = documentSnapshot.getBoolean("request")
                            val doctorName = documentSnapshot.getString("username")
                            val doctorID = documentSnapshot.getString("uid")


                            if (doctorRequest) {

                                val fr = WindowAnalyseOrCalling()
                                val bundle = Bundle()
                                val placeActivity = "doctorRequest"
                                bundle.putString("doctorID", doctorID)
                                bundle.putString("drName", doctorName)


                                bundle.putString("placeActivity", placeActivity)
                                fr.arguments = bundle


                                fr.show(fragmentManager, "show")
                            }

                        }


                    } catch (ex: Exception) {
                    }
                }




    }

    private fun prepareAllFragment() {

        navigationDoctor.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {

                R.id.MydoctorTips -> {
                    setFragment(MyDrTipsFragment())
                }

                R.id.requests -> {
                    setFragment(CallingRequestFragment())
                }
                R.id.RecentConsultDoctor -> {
                    setFragment(RecentConsultsWithUserFragment())
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


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_doctor_menu, menu)



        return true
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {

            R.id.profileMenuDoctor -> {

                // go profile settings
                val intent = Intent(this, SetttingsDoctorActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK and Intent.FLAG_ACTIVITY_NEW_TASK)
                finish()
                startActivity(intent)


            }
            R.id.AddTipsDoctor -> {

                val addTip = AddTipsFragment()
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


        // check for doctor and logout
        try {
            mFireStore!!.collection("Doctors")
                    .document(mAuth!!.currentUser!!.uid)
                    .update("state", false)
                    .addOnCompleteListener { task ->

                        if (task.isSuccessful) {

                            AuthUI.getInstance().signOut(this).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    if (task.isComplete) {

                                        val intent = Intent(applicationContext, LoginActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK and
                                                Intent.FLAG_ACTIVITY_NEW_TASK)
                                        // cancel calling if exit
                                        //cancelCallingIfExist()
                                        finish()
                                        startActivity(intent)
                                    } else {
                                        Toast.makeText(applicationContext,
                                                getString(R.string.fail_sigout_msg),
                                                Toast.LENGTH_LONG).show()
                                    }
                                } else {

                                }
                            }

                        } else {
                            Toast.makeText(applicationContext,
                                    getString(R.string.fail_cx_msg),
                                    Toast.LENGTH_LONG).show()
                        }


                    }
        } catch (ex: Exception) {
            Toast.makeText(applicationContext,
                    getString(R.string.check_first),
                    Toast.LENGTH_LONG).show()

        }


    }


}
