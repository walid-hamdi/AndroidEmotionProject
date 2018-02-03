package com.upfunstudio.emotionsocial

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class ReportDetailsActivity : AppCompatActivity() {

    var firebaseAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_details)
        firebaseAuth = FirebaseAuth.getInstance()

    }

    fun buCancelReport(view: View) {
        Toast.makeText(this, "Report Canceled!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)


    }

    fun buSaveReport(view: View) {
        Toast.makeText(this, "Report saved!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.report_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.profileMenu -> {

                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)


            }

            R.id.logout -> {

                // todo : logout from system
                firebaseAuth!!.signOut()
                checkUser()


            }
            else -> {
                return super.onOptionsItemSelected(item)

            }


        }

        return true
    }
    fun checkUser() {
        if (firebaseAuth!!.currentUser == null) {
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }


}
