package com.upfunstudio.emotionsocial.Companion

import android.app.Activity
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
import com.upfunstudio.emotionsocial.User.RegisterActivity
import com.upfunstudio.emotionsocial.User.WindowAnalyseOrCalling
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


    }

    fun loginEventLogin(view: View) {

        loginAccount()


    }

    fun loginDoctorEvent(view: View) {


        // simple code to make number auth from google to include all country +216 ( tunis)
        val providers = Arrays.asList(
                AuthUI.IdpConfig.Builder(AuthUI
                        .PHONE_VERIFICATION_PROVIDER)
                        .build())

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN)


    }

    // return with result to verify the phone number exist in database or not to allow to enter as a doctor in the system
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)



        try {

            // get the id and phone entered from user
            val uid = mAuth!!.currentUser!!.uid
            val phoneEntered = IdpResponse.fromResultIntent(data)!!.phoneNumber

            if (requestCode == RC_SIGN_IN) {

                // just for showing loading dialogue for the waiting result
                loading = SpotsDialog(this)
                loading!!.setTitle("Loading to login...")
                loading!!.setCanceledOnTouchOutside(false)
                loading!!.show()

                if (resultCode == Activity.RESULT_OK) {

                    val query = mFireStore!!.collection("Verify")
                            .whereEqualTo("phone", phoneEntered)
                            .get()
                    query.addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            for (phones in task.result.documentChanges) {
                                val phoneExit = phones.document.getString("phone")

                                if (phoneEntered == phoneExit) {
                                    // save doctor info
                                    saveInDatabase(uid, phoneEntered!!)

                                    loading!!.dismiss()

                                    val intent = Intent(this,
                                            MainDoctors::class.java)
                                    intent.putExtra("doctorID", uid)
                                    startActivity(intent)


                                } else {

                                    mAuth!!.currentUser!!.delete()
                                    Toast.makeText(applicationContext,
                                            "Your phone ${phoneEntered!!.length} does not exits , phone exists ${phoneExit.length}",
                                            Toast.LENGTH_LONG).show()
                                    loading!!.hide()

                                }

                            }

                        }
                    }


                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(this, "", Toast.LENGTH_LONG).show()
                }
            }
        } catch (ex: Exception) {
        }
    }

    private fun saveInDatabase(uid: String, phone: String) {

        val doctor = HashMap<String, Any>()
        doctor["uid"] = uid
        doctor["username"] = "Username"
        doctor["city"] = "City"
        doctor["language"] = "Language"
        doctor["mail"] = "Email"
        doctor["phone"] = phone
        doctor["picPath"] = ""
        doctor["specialityText"] = "Speciality"
        doctor["state"] = true
        doctor["request"] = false
        doctor["response"] = false


        try {
            // avoid change data every entered if the info already exist in fireStore
            if (!mFireStore!!.collection("Doctors").document(uid).get().result.exists()) {
                mFireStore!!.collection("Doctors")
                        .document(uid)
                        .set(doctor as Map<String, Any>)
                        .addOnCompleteListener {

                            task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this,
                                        "Welcome to your main page",
                                        Toast.LENGTH_LONG).show()

                            } else {
                                Toast.makeText(this,
                                        "Your not allowed to be here",
                                        Toast.LENGTH_LONG).show()
                            }


                        }
            }
        } catch (ex: Exception) {
        }
    }


    private fun loginAccount() {
        val email = editEmail.text.toString()
        val password = editPass.text.toString()

        if (!TextUtils.isEmpty(email) && email.matches(Regex(emailRegularExpression))) {


            if (!TextUtils.isEmpty(password)) {

                loading = SpotsDialog(this)
                loading!!.setTitle("Loading to login...")
                loading!!.setCanceledOnTouchOutside(false)
                loading!!.show()





                mAuth!!.signInWithEmailAndPassword(email, password).addOnCompleteListener {

                    task ->
                    if (task.isSuccessful) {

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
