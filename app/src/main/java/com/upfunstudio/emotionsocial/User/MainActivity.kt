package com.upfunstudio.emotionsocial.User

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.upfunstudio.emotionsocial.Companion.LoginActivity
import com.upfunstudio.emotionsocial.Companion.RecentConsultsFragment
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
        setFragment(DoctorListFragment())


        // if the user does not have a phone that mean this user is a doctor
        if (!mAuth!!.currentUser!!.phoneNumber.isNullOrEmpty()) {

            val intentDoctorAhead = Intent(this, MainDoctors::class.java)
            intentDoctorAhead.putExtra("userID", mAuth!!.currentUser!!.uid)
            startActivity(intentDoctorAhead)
        }

        // adapter all fragment
        adapterAllFragment()


    }


    private fun adapterAllFragment() {

        navigation.setOnNavigationItemSelectedListener { item ->

            when (item.itemId) {
                R.id.doctorList -> {

                    setFragment(DoctorListFragment())

                }
                R.id.DoctorsTips -> {
                    setFragment(DoctorTipsFragment())


                }

                R.id.RecentConsult -> {
                    setFragment(RecentConsultsFragment())


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


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.main_menu, menu)
        // todo : search


        return true
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.profileMenu -> {

                val intent = Intent(this, SettingsUserActivity::class.java)
                startActivity(intent)


            }
            R.id.fav -> {

                startActivity(Intent(this, FavActivity::class.java))

            }
            R.id.AnalyseMenu -> {


                val fr = WindowAnalyseOrCalling()
                val bundle = Bundle()
                // to know the analyse dialogue from main or form login & register
                val placeActivity = "MainActivity"
                bundle.putString("placeActivity", placeActivity)
                fr.arguments = bundle
                val frman = fragmentManager
                fr.show(frman, "Show")


            }
            R.id.logoutMenu -> {

                mAuth!!.signOut()
                checkUser()


            }

            else -> {
                return super.onOptionsItemSelected(item)

            }


        }

        return true
    }


    private fun checkUser() {

        // check for user
        if (mAuth!!.currentUser == null) {
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
        }


    }


}
