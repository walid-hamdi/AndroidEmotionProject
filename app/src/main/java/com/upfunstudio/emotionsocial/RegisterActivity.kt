package com.upfunstudio.emotionsocial

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private var loading: SpotsDialog? = null
    private var firebaseAuth: FirebaseAuth? = null

    companion object {
        private const val fullNameRegularExpression = "[a-zA-Z]"
        private const val emailRegularExpression = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        private const val passRegularExpression = "[a-zA-Z]"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        firebaseAuth = FirebaseAuth.getInstance()
    }

    fun goToAnalyse(view: View) {
        registerToAccount()

    }


    private fun registerToAccount() {
        val fullname = editUsername.text.toString()
        val email = editEmail.text.toString()
        val password = editPass.text.toString()
        val passwordConf = editConfirmPass.text.toString()


        // todo : need to use regular expression to avoid the invalid mail
        if (!TextUtils.isEmpty(fullname)) {

            if (!TextUtils.isEmpty(email) && email.matches(Regex(emailRegularExpression))) {

                if (!TextUtils.isEmpty(password)) {
                    if (!TextUtils.isEmpty(passwordConf)) {

                        if (password != passwordConf) {
                            editConfirmPass.error = "Password not correct as the same"
                        }
                        loading = SpotsDialog(this)
                        loading!!.setTitle("Loading to Sign up...")
                        loading!!.setCanceledOnTouchOutside(true)
                        loading!!.show()




                        firebaseAuth!!.createUserWithEmailAndPassword(email, password).addOnCompleteListener {

                            task ->
                            if (task.isSuccessful) {

                                loading!!.dismiss()
                                val fr = FragmentDialoge()
                                val bundle = Bundle()
                                // to know the analyse dialoge from main or form login & register
                                val placeActivity = "register"
                                bundle.putString("placeActivity", placeActivity)
                                fr.arguments = bundle

                                val frman = fragmentManager
                                fr.show(frman, "Show")



                            } else {

                                // something wrong
                                Toast.makeText(this,
                                        "Something wrong!", Toast.LENGTH_SHORT).show()
                                loading!!.hide()


                            }


                        }

                    } else {
                        editConfirmPass.error = "Please enter this field also!!"


                    }

                } else {
                    editPass.error = "Please enter the password!"

                }


            } else {
                editEmail.error = "Please enter the email!"


            }
        } else {
            editUsername.error = "Plase enter the full name"
        }


    }


    fun goLoginEvent(view: View) {


        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)

    }

    override fun onBackPressed() {
        alreadyBack.setTextColor(Color.RED)
    }


}
