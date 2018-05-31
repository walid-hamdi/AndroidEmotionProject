package com.upfunstudio.emotionsocial.Companion

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.upfunstudio.emotionsocial.Dr.MainDoctors
import com.upfunstudio.emotionsocial.R
import com.upfunstudio.emotionsocial.User.MainActivity
import com.upfunstudio.emotionsocial.User.RegisterActivity
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*
import kotlin.collections.HashMap


class LoginActivity : AppCompatActivity() {


    private var mAuth: FirebaseAuth? = null
    private var loading: SpotsDialog? = null
    private var mFireStore: FirebaseFirestore? = null


    companion object {
        private const val RC_SIGN_IN = 1
        private const val emailRegularExpression = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mAuth = FirebaseAuth.getInstance()
        mFireStore = FirebaseFirestore.getInstance()

        infoDialog()


    }
    private fun infoDialog(){
        // show info dialog to decide if you want to create account or use app without
        val builder = AlertDialog.Builder(this)
                .setTitle(getString(R.string.Info_text))
                .setMessage(getString(R.string.details_Info_text))
                .setNegativeButton(getString(R.string.start_text), { dialog, which ->

                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    saveUser()
                })
                .setPositiveButton(getString(R.string.use_accout_text), { dialog, which ->
                    dialog.dismiss()


                })
        try{
            val fromRegister=intent.extras.getInt("fromRegister",0)
            if(fromRegister!=1){
                builder.show()
            }
        }catch (ex:Exception){}


    }

    // save user
    private fun saveUser() {
        // use ay umer to save user
        SharedClass(this).saveData(12)

    }

    fun loginEventLogin(view: View) {

        try {
            loginAccount()
        } catch (ex: Exception) {
            Toast.makeText(this, getString(R.string.update_wor_text),
                    Toast.LENGTH_SHORT).show()
        }


    }

    fun loginDoctorEvent(view: View) {


        try {
            // simple code to make number auth from google to include all country +216(tunis)
            val providers = Arrays.asList(
                    AuthUI.IdpConfig.Builder(AuthUI
                            .PHONE_VERIFICATION_PROVIDER)
                            .build())

            // assign auth only when the dr register the info

            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN)

        } catch (ex: Exception) {
            Toast.makeText(this,  getString(R.string.update_wor_text), Toast.LENGTH_SHORT).show()

        }

    }

    // return with result to verify the phone number exist in database or not to allow to enter as a doctor in the system

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)





        try {
            if (requestCode == RC_SIGN_IN) {
                val phoneEntered = IdpResponse.fromResultIntent(data)!!.phoneNumber


                if (resultCode == Activity.RESULT_OK) {

                    // get the phone entered from user
                    // just for showing loading dialog to waiting result
                    loading = SpotsDialog(this, R.style.loadingDrSign)
                    loading!!.setCanceledOnTouchOutside(true)
                    loading!!.show()

                    // allow only the phone already entered
                    mFireStore!!
                            .collection("Verify")
                            .whereEqualTo("phone", phoneEntered)
                            .get()
                            .addOnCompleteListener { task ->


                                if (task.isSuccessful) {

                                    // save the dr info
                                    saveInDatabase(phoneEntered!!)
                                    loading!!.dismiss()


                                } else {

                                    mAuth!!.currentUser!!.delete()
                                    Toast.makeText(applicationContext,
                                            getString(R.string.meg_verify_phone),
                                            Toast.LENGTH_LONG).show()
                                    loading!!.hide()

                                }


                            }
                }
            }

        } catch (ex: Exception) {

        }


    }

    private fun saveInDatabase(phone: String) {
        val doctor = HashMap<String, Any>()
        doctor["uid"] = mAuth!!.currentUser!!.uid
        doctor["username"] = "Username"
        doctor["city"] = "City"
        doctor["language"] = "Language"
        doctor["mail"] = "Email"
        doctor["phone"] = phone
        doctor["picPath"] = ""
        doctor["specialityText"] = "Speciality"
        doctor["state"] = true
        doctor["request"] = false
        doctor["accept"] = false
        doctor["connectNow"] = false

        // we can add init info just in the first entering
        /*
         add check value to notice the dr to
         update his profile if he entered at the first time
          */



        mFireStore!!
                .collection("Doctors")
                .document(mAuth!!.currentUser!!.uid)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (task.result.exists()) {


                            val intent = Intent(this, MainDoctors::class.java)

                            startActivity(intent)

                            Toast.makeText(this,
                                    getString(R.string.welcome_ack_msg),
                                    Toast.LENGTH_LONG).show()
                        } else {

                            mFireStore!!.collection("Doctors")
                                    .document(mAuth!!.currentUser!!.uid)
                                    .set(doctor as Map<String, String>)
                                    .addOnCompleteListener { task ->

                                        if (task.isSuccessful) {


                                            Toast.makeText(this,
                                                    getString(R.string.msg_icorge_to_update),
                                                    Toast.LENGTH_LONG).show()


                                            val intent = Intent(this, MainDoctors::class.java)

                                            startActivity(intent)

                                        } else {
                                            Toast.makeText(this,
                                                    getString(R.string.try_agai),
                                                    Toast.LENGTH_LONG).show()

                                        }

                                    }


                        }


                    } else {
                        Toast.makeText(applicationContext, getString(R.string.connection_down),
                                Toast.LENGTH_LONG).show()
                        loading!!.hide()
                    }

                }

    }


    private fun loginAccount() {
        val email = editEmail.text.toString()
        val password = editPass.text.toString()


        if (!TextUtils.isEmpty(email) && email.matches(Regex(emailRegularExpression))) {


            if (!TextUtils.isEmpty(password)) {


                loading = SpotsDialog(this)
                loading!!.setTitle(getString(R.string.loadig_to_load))
                loading!!.setCanceledOnTouchOutside(false)
                loading!!.show()





                mAuth!!.signInWithEmailAndPassword(email, password).addOnCompleteListener {

                    task ->
                    if (task.isSuccessful) {

                        if (mAuth!!.currentUser!!.isEmailVerified) {

                            loading!!.dismiss()
                            val fr = WindowAnalyseOrCalling()
                            val bundle = Bundle()
                            // to know the analyse dialoge from main or form login & register
                            val placeActivity = "loginUser"
                            bundle.putString("placeActivity", placeActivity)

                            fr.arguments = bundle

                            val frman = fragmentManager
                            fr.show(frman, "Show")
                            // to invisible login layout after login
                            loginLayout.visibility = View.GONE




                            editEmail.setText("")
                            editPass.setText("")


                        } else {

                            // asking him to verify his email
                            Toast.makeText(applicationContext, getString(R.string.verify_email_msg),
                                    Toast.LENGTH_LONG).show()
                            loading!!.hide()

                        }


                    } else {

                        // something wrong
                        Toast.makeText(this,
                                getString(R.string.somethig_wor), Toast.LENGTH_SHORT).show()

                        loading!!.hide()

                    }


                }

            } else {
                editPass.error = getString(R.string.msg_ente_the_password)

            }


        } else {
            editEmail.error = getString(R.string.please_enter_the_email)


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
