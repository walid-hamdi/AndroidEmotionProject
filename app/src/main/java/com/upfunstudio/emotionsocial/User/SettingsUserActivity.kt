package com.upfunstudio.emotionsocial.User

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.upfunstudio.emotionsocial.Companion.AdapterFramgentUpdate
import com.upfunstudio.emotionsocial.R
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_profile.*

class SettingsUserActivity : AppCompatActivity() {

    // use firebase now
    private var mFireStore: FirebaseFirestore? = null
    private var mAuth: FirebaseAuth? = null
    private var mFirebaseStorage: FirebaseStorage? = null
    private var pathPicture: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        mFireStore = FirebaseFirestore.getInstance()
        // add offline capability
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        mFireStore!!.firestoreSettings = settings


        mAuth = FirebaseAuth.getInstance()
        mFirebaseStorage = FirebaseStorage.getInstance()


    }

    override fun onStart() {
        super.onStart()
        loadData()

    }

    private fun loadData() {


        // get all user info
        mFireStore!!.collection("Users")
                .document(mAuth!!.currentUser!!.uid)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        // read data to appear in profile user
                        if (task.result.exists()) {
                            defaultEmail.text = task.result.getString("email")
                            defaultPhone.text = task.result.getString("phone")
                            username.text = task.result.getString("fullName")
                            languageSpeak.text = task.result.getString("language")
                            cityPlace.text = task.result.getString("city")
                            pathPicture = task.result.getString("profile_picture")
                            // use picasso lib to adapate image in imageview
                            if (pathPicture.isNullOrEmpty()) {
                                Picasso.with(applicationContext)
                                        .load(R.drawable.profile_photo)
                                        .placeholder(R.drawable.profile_photo)
                                        .into(profile_picture)
                            } else {
                                Picasso.with(applicationContext)
                                        .load(pathPicture)
                                        .placeholder(R.drawable.profile_photo)
                                        .into(profile_picture)
                            }

                        }
                    }


                }

    }


    fun updatePhotoEvent(view: View) {

        // pick picture from gallery phone
        val intentFromGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(Intent.createChooser(intentFromGallery,
                "Please Pick Picture from gallery"), REQ_CODE_GALLERY)


    }

    companion object {
        private const val REQ_CODE_GALLERY = 2
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_CODE_GALLERY -> {
                if (resultCode == Activity.RESULT_OK) {
                    val loading = SpotsDialog(this)
                    loading!!.setTitle("Loading to upload...")
                    loading!!.setCanceledOnTouchOutside(false)
                    loading!!.show()

                    // store physic picture in storage
                    val ref = mFirebaseStorage!!.reference
                            .child("Users_Pictures")
                            .child(mAuth!!.currentUser!!.uid)
                    ref.putFile(data!!.data).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            pathPicture = task.result.downloadUrl.toString()

                            // update pic path in database
                            mFireStore!!.collection("Users")
                                    .document(mAuth!!.currentUser!!.uid)
                                    .update("profile_picture", pathPicture)
                            loadData()
                            loading!!.dismiss()


                        } else {
                            loading!!.hide()
                            Toast.makeText(applicationContext, "Failed upload image "
                                    , Toast.LENGTH_LONG).show()

                        }


                    }


                }


            }


        }


    }


    fun goHomeEvent(view: View) {

        finish()

    }

    fun updateInfoEvent(view: View) {

        val adapterUpdate = AdapterFramgentUpdate()
        val bundle = Bundle()
        val placeActivity = "updateUser"
        bundle.putString("update", placeActivity)
        adapterUpdate.arguments = bundle


        val fr = fragmentManager
        adapterUpdate.show(fr, "show")

    }

    fun historyReportEvent(view: View) {
        val intent = Intent(this, ListHistoryReportActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK and Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)


    }

    fun checkAndUpdate(name: String, mail: String, phone: String, city: String, language: String) {

        val userUpdate = mFireStore!!.collection("Users")
                .document(mAuth!!.currentUser!!.uid)


        if (!TextUtils.isEmpty(name)) {
            username.text = name
            userUpdate.update("fullName", name)

        }
        if (!TextUtils.isEmpty(city)) {
            cityPlace.text = city
            userUpdate.update("city", city)

        }
        if (!TextUtils.isEmpty(phone)) {
            defaultPhone.text = phone
            userUpdate.update("phone", phone)

        }
        if (!TextUtils.isEmpty(mail)) {
            // update texttview offline
            defaultEmail.text = mail
            // update in database online
            userUpdate.update("email", mail)
            // update auth email
            mAuth!!.currentUser!!.updateEmail(mail)

        }
        if (!TextUtils.isEmpty(language)) {
            languageSpeak.text = language
            userUpdate.update("language", language)


        }

        Toast.makeText(this, "Updated successfully", Toast.LENGTH_LONG).show()

    }


}



