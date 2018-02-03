package com.upfunstudio.emotionsocial

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {


    var firebaseAuth: FirebaseAuth? = null
    var loading: SpotsDialog? = null

    companion object {
        private const val emailRegularExpression = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        firebaseAuth = FirebaseAuth.getInstance()


    }

    fun loginEventLogin(view: View) {

        loginAccount()


    }

    fun loginAccount() {
        val email = editEmail.text.toString()
        val password = editPass.text.toString()

        if (!TextUtils.isEmpty(email) && email.matches(Regex(emailRegularExpression))) {


            if (!TextUtils.isEmpty(password)) {

                loading = SpotsDialog(this)
                loading!!.setTitle("Loading to login...")
                loading!!.setCanceledOnTouchOutside(false)
                loading!!.show()





                firebaseAuth!!.signInWithEmailAndPassword(email, password).addOnCompleteListener {

                    task ->
                    if (task.isSuccessful) {

                        loading!!.dismiss()
                        val fr = FragmentDialoge()
                        val bundle = Bundle()
                        // to know the analyse dialoge from main or form login & register
                        val placeActivity = "login"
                        bundle.putString("placeActivity", placeActivity)
                        fr.arguments = bundle

                        val frman = fragmentManager
                        fr.show(frman, "Show")


                        editEmail.setText("")
                        editPass.setText("")

                    } else {

                        // something wrong
                        Toast.makeText(this,
                                "Something wrong!", Toast.LENGTH_SHORT).show()

                        loading!!.hide()

                    }


                }

            } else {
                editPass.error = "Please enter the password"

            }


        } else {
            editEmail.error = "Please enter the email!"


        }


    }

    fun goRegisterActivityEvent(view: View) {

        val intent = Intent(this, RegisterActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()

    }

    override fun onBackPressed() {

    }


}
