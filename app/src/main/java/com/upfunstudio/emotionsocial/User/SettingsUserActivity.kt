package com.upfunstudio.emotionsocial.User

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
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
                .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->

                    try {

                        if (documentSnapshot.exists() && documentSnapshot != null) {

                            defaultEmail.text = documentSnapshot.getString("email")
                            defaultPhone.text = documentSnapshot.getString("phone")
                            username.text = documentSnapshot.getString("fullName")
                            languageSpeak.text = documentSnapshot.getString("language")
                            cityPlace.text = documentSnapshot.getString("city")
                            pathPicture = documentSnapshot.getString("profile_picture")
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


                    } catch (ex: Exception) {
                    }


                }


    }

    fun updatePhotoEvent(view: View) {

        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(arrayOf(android.Manifest.permission.CAMERA), PICK)


                return

            }


        }
        // pick picture from camera or gallery phone
        pickPictureFromGalleryOrCamera()


    }


    // pick from camera or gallery

    private fun pickPictureFromGalleryOrCamera() {
        val items = resources.getStringArray(R.array.pick)

        val builder = AlertDialog.Builder(this)
                .setItems(items, { dialog, which ->

                    if (items[which] == "Pick from camera" ||
                            items[which] == "اختر من الكاميرا" ||
                            items[which] == "Seleccionar de la cámara" ||
                            items[which] == "Choisissez parmi la caméra") {


                        val photo = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(photo, REQ_CODE_CAMERA)


                    } else if (items[which] == "Pick from gallery" ||
                            items[which] == "Elija de la galería" ||
                            items[which] == "Choisissez dans la galerie" ||
                            items[which] == "اختر من المعرض") {

                        val intentFromGallery = Intent(Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(Intent.createChooser(intentFromGallery,
                                "Please Pick Picture from gallery"), REQ_CODE_GALLERY)

                    } else {

                        dialog.dismiss()
                    }

                })
        builder.show()

    }

    companion object {
        private const val REQ_CODE_CAMERA = 4
        private const val REQ_CODE_GALLERY = 2
        private const val PICK = 1

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {

                REQ_CODE_GALLERY -> {
                    prepareImage(data!!.data)

                }
                REQ_CODE_CAMERA -> {

                    try {
                        // todo : handle the error of get null value
                        prepareImage(data = data!!.data)

                    } catch (ex: Exception) {
                        Toast.makeText(this, "Problem happened", Toast.LENGTH_LONG).show()

                    }


                }


            }


        }


    }

    // prepare image ad pushed to firease
    private fun prepareImage(data: Uri) {

        val loading = SpotsDialog(this, R.style.loadingToUpload)
        loading.setCanceledOnTouchOutside(false)
        loading.show()


        // store physic picture in storage
        val ref = mFirebaseStorage!!.reference
                .child("Users_Pictures")
                .child("user_profile_pic" + mAuth!!.currentUser!!.uid)
        ref.putFile(data).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                pathPicture = task.result.downloadUrl.toString()

                // update pic path in database
                mFireStore!!.collection("Users")
                        .document(mAuth!!.currentUser!!.uid)
                        .update("profile_picture", pathPicture)
                loadData()

                loading.dismiss()


            } else {
                loading.hide()
                Toast.makeText(applicationContext, getString(R.string.fail_upload)
                        , Toast.LENGTH_LONG).show()

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
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK and Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)


    }

    fun checkAndUpdate(name: String, mail: String, phone: String, city: String, language: String) {

        val userUpdate = mFireStore!!.collection("Users")
                .document(mAuth!!.currentUser!!.uid)




        if (!TextUtils.isEmpty(name)) {
            val usernameCap = name.substring(0, 1).toUpperCase() + name.substring(1)
            username.text = usernameCap
            userUpdate.update("fullName", usernameCap)

        }
        if (!TextUtils.isEmpty(city)) {
            val cityCap = city.substring(0, 1).toUpperCase() + city.substring(1)
            cityPlace.text = cityCap
            userUpdate.update("city", cityCap)

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
            val languageCap = language.substring(0, 1).toUpperCase() + language.substring(1)
            languageSpeak.text = languageCap
            userUpdate.update("language", languageCap)


        }

        Toast.makeText(this, getString(R.string.sucess_upload), Toast.LENGTH_LONG).show()

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {


        when (requestCode) {

            PICK -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickPictureFromGalleryOrCamera()
                } else {

                    Toast.makeText(this,
                            getString(R.string.dey_acess_camera),
                            Toast.LENGTH_LONG).show()
                }

            }

        }




        super.onRequestPermissionsResult(requestCode, permissions, grantResults)


    }


}



