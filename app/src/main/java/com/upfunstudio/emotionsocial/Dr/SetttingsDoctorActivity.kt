package com.upfunstudio.emotionsocial.Dr

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
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.upfunstudio.emotionsocial.Companion.AdapterFramgentUpdate
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_doctor)
        fireStore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        mAuth = FirebaseAuth.getInstance()


        //  handle connect state
        connectionState()
        loadDataDoctorProfile()


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
                    val ref = storage!!.reference
                            .child("Doctors_Pictures")
                            .child("dr_profile_pic")
                    ref.putFile(data!!.data).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            pathPicture = task.result.downloadUrl.toString()

                            // update pic path in database

                            fireStore!!.collection("Doctors")
                                    .document(mAuth!!.currentUser!!.uid)
                                    .update("picPath", this.pathPicture)
                            loadDataDoctorProfile()
                            loading!!.dismiss()


                        } else {
                            Toast.makeText(applicationContext, "Failed upload image "
                                    , Toast.LENGTH_LONG).show()
                            loading!!.hide()

                        }


                    }


                }


            }


        }


    }

    override fun onStart() {
        super.onStart()
        loadDataDoctorProfile()
    }


    // refresh data and load it again
    private fun loadDataDoctorProfile() {


        try {
            // all doctor info to appear for doctor account
            fireStore!!
                    .collection("Doctors")
                    .document(mAuth!!.currentUser!!.uid)
                    .addSnapshotListener { documentSnapshot, _ ->

                        if (documentSnapshot.exists()) {

                            val specialityText = documentSnapshot.getString("specialityText")
                            val username = documentSnapshot.getString("username")
                            this.pathPicture = documentSnapshot.getString("picPath")
                            this.state = documentSnapshot.getBoolean("state")
                            val phone = documentSnapshot.getString("phone")
                            val city = documentSnapshot.getString("city")
                            val language = documentSnapshot.getString("language")
                            val mail = documentSnapshot.getString("mail")
                            this.doctorID = documentSnapshot.getString("uid")


                            // save image profile doctor
                            if (pathPicture.isNullOrEmpty()) {
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


                    }
        } catch (ex: IllegalArgumentException) {
            Toast.makeText(this, "Error ${ex.message}",
                    Toast.LENGTH_SHORT).show()
        }

    }


    fun goHomeDrEvent(view: View) {
        // return to home dr
        finish()
    }

    fun checkUpdate(name: String, mail: String, city: String,
                    language: String, specialist: String) {


        val userUpdate = fireStore!!.collection("Doctors")
                .document(doctorID)


        if (!TextUtils.isEmpty(name)) {
            dr_name.text = name
            userUpdate.update("username", name)

        }
        if (!TextUtils.isEmpty(city)) {
            cityPlace.text = city
            userUpdate.update("city", city)

        }
        if (!TextUtils.isEmpty(mail)) {
            mailDr.text = mail
            userUpdate.update("mail", mail)

        }
        if (!TextUtils.isEmpty(language)) {
            languageDr.text = language
            userUpdate.update("language", language)


        }
        if (!TextUtils.isEmpty(specialist)) {
            speciality.text = specialist
            userUpdate.update("specialityText", specialist)

        }

        Toast.makeText(this, "Updated successfully", Toast.LENGTH_LONG).show()


    }


    private fun connectionState() {

        connectStateSwitch.setOnCheckedChangeListener { buttonView, isChecked ->

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
