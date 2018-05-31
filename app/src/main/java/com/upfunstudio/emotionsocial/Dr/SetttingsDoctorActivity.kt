package com.upfunstudio.emotionsocial.Dr

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
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.upfunstudio.emotionsocial.Companion.AdapterFramgentUpdate
import com.upfunstudio.emotionsocial.Companion.WindowAnalyseOrCalling
import com.upfunstudio.emotionsocial.R
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_profile_doctor.*

class SetttingsDoctorActivity : AppCompatActivity() {

    private var fireStore: FirebaseFirestore? = null
    private var storage: FirebaseStorage? = null
    private var pathPicture = ""
    private var mAuth: FirebaseAuth? = null
    private var state = false
    private var doctorID = ""
    private var check= true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_profile_doctor)
        fireStore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        mAuth = FirebaseAuth.getInstance()


        //  handle connect state
        connectionState()
        // loading all doctor information
        loadDataDoctorProfile()
        // check if there is any request


    }


    override fun onStart() {
        super.onStart()
        // loading all doctor information
        loadDataDoctorProfile()
        // check if there is any call
        check=true
        realTimeRequest()
    }


    // real time request
    private fun realTimeRequest() {
        // check it out if there is any request
        fireStore!!.collection("Doctors")
                .document(mAuth!!.currentUser!!.uid)
                .addSnapshotListener { documentSnapshot, _ ->


                    try {
                        if (documentSnapshot.exists() && documentSnapshot != null) {


                            val doctorRequest = documentSnapshot.getBoolean("request")
                            val doctorName = documentSnapshot.getString("username")
                            val doctorID = documentSnapshot.getString("uid")


                            if (doctorRequest && check) {

                                val fr = WindowAnalyseOrCalling()
                                val bundle = Bundle()
                                val placeActivity = "doctorRequest"
                                bundle.putString("doctorID", doctorID)
                                bundle.putString("drName", doctorName)


                                bundle.putString("placeActivity", placeActivity)
                                fr.arguments = bundle


                                fr.show(fragmentManager, "show")

                            }

                        }


                    } catch (ex: Exception) {
                    }
                }


    }


    //  update info
    fun updateDrNowEvent(view: View) {

        val adapterUpdate = AdapterFramgentUpdate()
        val bundle = Bundle()
        val placeActivity = "updateDoctor"
        bundle.putString("update", placeActivity)
        adapterUpdate.arguments = bundle


        val fr = fragmentManager
        adapterUpdate.show(fr, "show")

    }

    // update pic doctor
    fun updatePictureDoctor(view: View) {


        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(arrayOf(android.Manifest.permission.CAMERA), PICK)


                return

            }


            pickPictureFromGalleryOrCamera()


        }


    }
    // pick picture from gallery phone or camera

    private fun pickPictureFromGalleryOrCamera() {
        val items = resources.getStringArray(R.array.pick)

        val builder = AlertDialog.Builder(this)
                .setItems(items, { dialog, which ->

                    if (items[which] == "Pick from camera" ||
                            items[which] == "اختر من الكميرا" ||
                            items[which] == "Seleccionar de la cámara" ||
                            items[which] == "Choisissez parmi la caméra") {

                        val intentFromCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(intentFromCamera, REQ_CODE_CAMERA)


                    } else if (items[which] == "Pick from gallery" ||
                            items[which] == "Elija de la galería " ||
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
        private const val REQ_CODE_CAMERA = 1
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

    private fun prepareImage(data: Uri) {
        val loading = SpotsDialog(this, R.style.loadingToUpload)
        loading.setCanceledOnTouchOutside(false)
        loading.show()


        // store physic picture in storage
        val ref = storage!!.reference
                .child("Doctors_Pictures")
                .child("dr_profile_pic" + mAuth!!.currentUser!!.uid)
        ref.putFile(data).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                pathPicture = task.result.downloadUrl.toString()

                // update pic path in database

                fireStore!!.collection("Doctors")
                        .document(mAuth!!.currentUser!!.uid)
                        .update("picPath", this.pathPicture)
                loadDataDoctorProfile()
                loading.dismiss()


            } else {
                Toast.makeText(applicationContext, getString(R.string.fail_upload_msfg)
                        , Toast.LENGTH_LONG).show()
                loading.hide()

            }


        }

    }


    // refresh data and load it again
    private fun loadDataDoctorProfile() {


        try {
            // all doctor info to appear for doctor account
            fireStore!!
                    .collection("Doctors")
                    .document(mAuth!!.currentUser!!.uid)
                    .addSnapshotListener { documentSnapshot, _ ->

                        try {
                            if (documentSnapshot.exists()) {

                                val specialityText = documentSnapshot.getString("specialityText")
                                val username = documentSnapshot.getString("username")
                                this.pathPicture = documentSnapshot.getString("picPath")
                                this.state = documentSnapshot.getBoolean("state")
                                val phone = documentSnapshot.getString("phone")
                                val city = documentSnapshot.getString("city")
                                val language = documentSnapshot.getString("language")
                                val mail = documentSnapshot.getString("mail")


                                // save image profile doctor
                                if (pathPicture.isEmpty()) {
                                    Picasso.with(applicationContext)
                                            .load(R.drawable.profile_photo)
                                            .into(profile_picture)

                                } else {
                                    Picasso.with(applicationContext)
                                            .load(pathPicture)
                                            .placeholder(R.drawable.profile_photo)
                                            .into(profile_picture)

                                }

                                dr_name.text = username
                                cityPlace.text = city
                                speciality.text = specialityText
                                languageDr.text = language
                                phoneDr.text = phone
                                mailDr.text = mail

                                // save switch in the same change state
                                connectStateSwitch.isChecked = this.state


                            }
                        } catch (ex: Exception) {
                        }


                    }
        } catch (ex: IllegalArgumentException) {
            Toast.makeText(this, "Error ${ex.message}",
                    Toast.LENGTH_SHORT).show()
        }

    }


    fun goHomeDrEvent(view: View) {
        // return to home dr
        check=false
        val intent = Intent(this, MainDoctors::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK and Intent.FLAG_ACTIVITY_NEW_TASK )
        finish()
        startActivity(intent)
    }

    fun checkUpdate(name: String, mail: String, city: String,
                    language: String, specialist: String) {


        doctorID = mAuth!!.currentUser!!.uid
        val doctorUpdate = fireStore!!.collection("Doctors")
                .document(doctorID)


        if (!TextUtils.isEmpty(name)) {
            val usernameCap = name.substring(0, 1).toUpperCase() + name.substring(1)

            dr_name.text = usernameCap
            doctorUpdate.update("username", usernameCap)

        }
        if (!TextUtils.isEmpty(city)) {
            val cityCap = city.substring(0, 1).toUpperCase() + city.substring(1)
            cityPlace.text = cityCap
            doctorUpdate.update("city", cityCap)

        }
        if (!TextUtils.isEmpty(mail)) {
            mailDr.text = mail
            doctorUpdate.update("mail", mail)

        }
        if (!TextUtils.isEmpty(language)) {
            val languageCap = language.substring(0, 1).toUpperCase() + language.substring(1)
            languageDr.text = languageCap
            doctorUpdate.update("language", languageCap)


        }
        if (!TextUtils.isEmpty(specialist)) {
            val specialityTextCap = specialist.substring(0, 1).toUpperCase() + specialist.substring(1)
            speciality.text = specialityTextCap
            doctorUpdate.update("specialityText", specialityTextCap)

        }

        doctorUpdate.get().addOnCompleteListener { task ->

            if (task.isSuccessful) {
                Toast.makeText(this, getString(R.string.sucess_update), Toast.LENGTH_LONG).show()

            } else {
                Toast.makeText(this, getString(R.string.fail_upload), Toast.LENGTH_LONG).show()

            }

        }


    }


    private fun connectionState() {

        connectStateSwitch.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                try {
                    // that mean change state to online
                    fireStore!!.collection("Doctors")
                            .document(mAuth!!.currentUser!!.uid)
                            .update("state", true)

                } catch (ex: Exception) {
                }

            } else {
                try {
                    // that mean change state to offline
                    fireStore!!.collection("Doctors")
                            .document(mAuth!!.currentUser!!.uid)
                            .update("state", false)

                } catch (ex: Exception) {
                }
            }

        }
    }
}
