package com.upfunstudio.emotionsocial

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.update_dialoge.view.*

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)


    }


    fun updatePhotoEvent(view: View) {

        // todo :  and update also in firebase
        val intentFromGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intentFromGallery, REQ_CODE_GALLERY)

        // todo:  and update also in firebase
        // val intentFromCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // startActivityForResult(intentFromCamera, REQ_CODE_CAMERA)


    }

    companion object {
        val REQ_CODE_CAMERA = 1
        val REQ_CODE_GALLERY = 2
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var uri: Uri

        when (requestCode) {
            REQ_CODE_CAMERA -> {
                if (resultCode == Activity.RESULT_OK) {
                    uri = data!!.data
                    profile_picture.setImageURI(uri)
                }


            }
            REQ_CODE_GALLERY -> {
                if (resultCode == Activity.RESULT_OK) {
                    uri = data!!.data
                    profile_picture.setImageURI(uri)
                }


            }


        }


    }


    fun goHomeEvent(view: View) {

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK and Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)

    }

    fun updateInfoEvent(view: View) {

        val adapterUpdateFragementDialoge = AdapterUpdateFragementDialoge()
        val fr = fragmentManager
        adapterUpdateFragementDialoge.show(fr, "show")

    }

    fun historyReportEvent(view: View) {
        val intent = Intent(this, historyDetailsActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK and Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)


    }

    fun checkAndUpdate(name: String, mail: String, phone: String, city: String, language: String) {

        if (!TextUtils.isEmpty(name)) {
            username.text = name
        }
        if (!TextUtils.isEmpty(city)) {
            cityPlace.text = city
        }
        if (!TextUtils.isEmpty(phone)) {
            phoneDefault.text = phone
        }
        if (!TextUtils.isEmpty(mail)) {
            emailDefault.text = mail
        }
        if (!TextUtils.isEmpty(language)) {
            languageSpeak.text = language

        }
        // todo : update online also in firebase

    }


    class AdapterUpdateFragementDialoge : FragmentDialoge() {


        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {

            val view = inflater!!.inflate(R.layout.update_dialoge, container, false)




            view.updateNowButton.setOnClickListener {

                val mainProfileActivity = activity as ProfileActivity


                // update info
                mainProfileActivity.checkAndUpdate(view.changeFullname.text.toString()
                        , view.chngeMail.text.toString(),
                        view.chnagePhone.text.toString(),
                        view.chnageCity.text.toString(),
                        view.chageLanguage.text.toString())





                dismiss()


            }
            view.CancelUpdate.setOnClickListener {
                dismiss()
            }



            return view
        }


    }
}



