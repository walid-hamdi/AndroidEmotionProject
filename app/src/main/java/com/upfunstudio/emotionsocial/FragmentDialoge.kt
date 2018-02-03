package com.upfunstudio.emotionsocial

import android.app.Activity
import android.app.DialogFragment
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.scan_dialoge.view.*

/**
 * Created by walido on 1/30/2018.
 */
open class FragmentDialoge : DialogFragment() {


    private var imageStore: Uri? = null
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val view = inflater!!.inflate(R.layout.scan_dialoge, container, false)

        view.scanButtonDialoge.setOnClickListener {


            // getting the pic from camera
            val intentFromCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intentFromCamera, REQ_CODE_CAMERA)

            // todo : take time until process the image
            Thread.sleep(5000)

            dismiss()
            // todo : passing to report activity and send all the info from image process
            val intent = Intent(activity, ReportDetailsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)


        }
        view.cancelButtonDialoge.setOnClickListener {


            try {
                val place: String = this.arguments.getString("placeActivity", "")


                // to avoid enter open activity many time
                if (place == "MainActivity") {
                    dismiss()

                } else if (place == "login" || place == "register") {
                    dismiss()
                    startActivity(Intent(activity, MainActivity::class.java))
                }
            } catch (ex: Exception) {
            }

        }



        return view
    }


    companion object {
        private const val REQ_CODE_CAMERA = 1

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        when (requestCode) {
            REQ_CODE_CAMERA -> {
                if (resultCode == Activity.RESULT_OK) {

                    imageStore = data!!.data


                }


            }


        }


    }


}