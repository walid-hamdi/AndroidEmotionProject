package com.upfunstudio.emotionsocial.User

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.upfunstudio.emotionsocial.Companion.LoginActivity
import com.upfunstudio.emotionsocial.R
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private var loading: SpotsDialog? = null
    private var mAuth: FirebaseAuth? = null
    private var mFireStore: FirebaseFirestore? = null

    companion object {
        private const val fullNameRegularExpression = "[a-zA-Z]"
        private const val emailRegularExpression = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        private const val passRegularExpression = "[a-zA-Z]"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        mAuth = FirebaseAuth.getInstance()
        mFireStore = FirebaseFirestore.getInstance()
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




                        mAuth!!.createUserWithEmailAndPassword(email, password).addOnCompleteListener {

                            task ->
                            if (task.isSuccessful) {

                                // save all fields in database
                                saveInDatabase()


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
            editUsername.error = "Please enter the full name"
        }


    }

    // function to save in firebase
    private fun saveInDatabase() {
        val users = HashMap<String, Any>()



        users["email"] = editEmail.text.toString()
        users["fullName"] = editUsername.text.toString()
        users["language"] = "Please update your Language Speak"
        users["password"] = editPass.text.toString()
        users["phone"] = "Please update your Phone"
        users["profile_picture"] = ""



        mFireStore!!.collection("Users")
                .document(mAuth!!.currentUser!!.uid)
                .set(users as Map<String, String>)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        // appear analyse window
                        loading!!.dismiss()
                        val fr = WindowAnalyseOrCalling()
                        val bundle = Bundle()
                        // to know the analyse dialoge from main or form login & register
                        val placeActivity = "register"
                        bundle.putString("placeActivity", placeActivity)
                        fr.arguments = bundle

                        val frman = fragmentManager
                        fr.show(frman, "Show")
                        registerLayout.visibility = View.GONE


                    } else {
                        Toast.makeText(applicationContext,
                                "Please check the connection!",
                                Toast.LENGTH_LONG).show()

                    }

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
