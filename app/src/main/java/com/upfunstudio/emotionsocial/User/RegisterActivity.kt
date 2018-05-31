package com.upfunstudio.emotionsocial.User

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.upfunstudio.emotionsocial.Companion.LoginActivity
import com.upfunstudio.emotionsocial.R
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.webview_layout.view.*


class RegisterActivity : AppCompatActivity() {

    private var loading: SpotsDialog? = null
    private var mAuth: FirebaseAuth? = null
    private var mFireStore: FirebaseFirestore? = null

    companion object {
        // use it later to avoid incorrect error
        private const val fullNameRegularExpression = "^[\\p{L} .'-]+$"
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


        // avoid the invalid mail
        if (!TextUtils.isEmpty(fullname) &&
                fullname.matches(Regex(fullNameRegularExpression))) {

            if (!TextUtils.isEmpty(email) &&
                    email.matches(Regex(emailRegularExpression))) {

                // todo :  && password.matches(Regex(password)

                if (!TextUtils.isEmpty(password)) {
                    if (!TextUtils.isEmpty(passwordConf)) {

                        if (password != passwordConf) {
                            editConfirmPass.error = getString(R.string.pass_fail_msg)
                        }
                        loading = SpotsDialog(this, R.style.loadingRegister)
                        loading!!.setCanceledOnTouchOutside(true)
                        loading!!.show()




                        mAuth!!.createUserWithEmailAndPassword(email, password).addOnCompleteListener {

                            task ->
                            if (task.isSuccessful) {

                                // send verification to make sure the real email
                                sentVerification()


                            } else {

                                // something wrong
                                Toast.makeText(this,
                                        getString(R.string.wet_wor_msg), Toast.LENGTH_SHORT).show()
                                loading!!.hide()


                            }


                        }

                    } else {
                        editConfirmPass.error = getString(R.string.co_msg)


                    }

                } else {
                    editPass.error = getString(R.string.pass_eter_msfg)

                }


            } else {
                editEmail.error = getString(R.string.enter_email_msg)


            }
        } else {
            editUsername.error = getString(R.string.enter_fullame_msg)
        }


    }

    // to verify email
    private fun sentVerification() {
        mAuth!!.currentUser!!.sendEmailVerification()
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {


                        // save all fields in database
                        saveInDatabase()


                    } else {

                        Toast.makeText(this,
                                getString(R.string.faild_email),
                                Toast.LENGTH_LONG)
                                .show()
                        loading!!.hide()


                    }

                }


    }

    // to make life easier :p
    private fun displayWidowToVerify() {


        val alert = AlertDialog.Builder(this)
        alert.setTitle(getString(R.string.verify_with_email))

        val view = LayoutInflater.from(this).inflate(R.layout.webview_layout, null)

        view.webVieww.loadUrl("https://www.google.com/")
        view.webVieww.settings.javaScriptEnabled = true
        view.edit.requestFocus()
        view.edit.isFocusable = true

        view.back.setOnClickListener {
            view.webVieww.goBack()
        }
        view.go.setOnClickListener {
            view.webVieww.goForward()
        }


        view.webVieww.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)

                return true
            }
        }


        alert.setView(view)


        alert.setNegativeButton("Close", { dialog, _ ->
            dialog.dismiss()
            returnToLogin()
        })

        alert.create().window.setLayout(600, 400)
        alert.create().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        alert.create().show()
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

                        Toast.makeText(this,
                                getString(R.string.check_email_verify),
                                Toast.LENGTH_LONG)
                                .show()
                        displayWidowToVerify()


                    } else {
                        Toast.makeText(applicationContext,
                                getString(R.string.check_first),
                                Toast.LENGTH_LONG).show()

                    }

                }


    }


    fun goLoginEvent(view: View) {


        returnToLogin()


    }

    private fun returnToLogin() {

        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.putExtra("fromRegister",1)
        startActivity(intent)

    }


    override fun onBackPressed() {
        alreadyBack.setTextColor(Color.RED)
    }


}
