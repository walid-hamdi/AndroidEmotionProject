package com.upfunstudio.emotionsocial.User

import android.app.DialogFragment
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.upfunstudio.emotionsocial.Companion.ConsultRoomActivity
import com.upfunstudio.emotionsocial.R
import kotlinx.android.synthetic.main.scan_dialoge.view.*


/**
 * Created by walido on 1/30/2018.
 */
open class WindowAnalyseOrCalling : DialogFragment() {

    private var mFireStore: FirebaseFirestore? = null


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {

        mFireStore = FirebaseFirestore.getInstance()
        val view = inflater!!.inflate(R.layout.scan_dialoge, container, false)


        try {


            val place: String = this.arguments.getString("placeActivity", "")

            // show dialog for calling in this case
            if (place == "calling") {

                // design new layout by coding
                view.textDisplay.text = "Calling now ..."
                view.scanButtonDialoge.visibility = View.GONE
                view.pictureCallingDr.visibility = View.VISIBLE
                //view.cancelButtonDialoge.setBackgroundResource(R.drawable.button_refuse_background)
                //view.cancelButtonDialoge.setTextColor(resources.getColor(android.R.color.white))
                val pictureDoctor: String = this.arguments.getString("pictureDoctor", "")

                if (!pictureDoctor.isNullOrEmpty()) {

                    Picasso.with(activity)
                            .load(pictureDoctor)
                            .into(view.pictureCallingDr)

                } else {
                    Picasso.with(activity)
                            .load(R.drawable.profile_photo)
                            .into(view.pictureCallingDr)

                }
                val media = MediaPlayer.create(activity,
                        R.raw.calling)
                // send request to doctor and if accept , pass to char room
                val myEmail = this.arguments.getString("myEmail", "")
                if (!myEmail.isNullOrEmpty()) {
                    val doctorID = this.arguments.getString("doctorID", "")
                    val map = HashMap<String, Any>()
                    map["email"] = myEmail
                    map["response"] = false
                    map["canceled"] = false
                    map["timeRequest"] = FieldValue.serverTimestamp()
                    val myPicture = this.arguments.getString("myPicture", "")
                    if (!myPicture.isNullOrEmpty()) {
                        map["pictureRequested"] = myPicture

                    }
                    if (!doctorID.isNullOrEmpty()) {
                        mFireStore!!.collection("Doctors")
                                .document(doctorID)
                                .collection("Requests")
                                .add(map as Map<String, Any>)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        // success to send request
                                        // start song to call

                                        media.start()

                                    }
                                }

                    }


                }
                /*
                 todo : test if canceled dismiss & if request==true do to consult room
                 else wait 10 s and send message for the user call it again later


                  */



                view.cancelButtonDialoge.setOnClickListener {

                    dismiss()
                    media.stop()
                    // todo : stop request doctor
                    activity.finish()

                }


                // this dialog appear for doctor main when someone send request
            } else if (place == "doctorRequest") {

                // todo : put the specific name later
                view.textDisplay.text = "Someone want to call you ..."
                view.scanButtonDialoge.background = resources.getDrawable(R.drawable.button_background)
                view.scanButtonDialoge.text = "Accept"
                view.scanButtonDialoge.setTextColor(resources.getColor(R.color.grey))

                view.pictureCallingDr.visibility = View.VISIBLE

                val pictureDoctor: String = this.arguments.getString("myPicture", "")

                if (!pictureDoctor.isNullOrEmpty()) {

                    Picasso.with(activity)
                            .load(pictureDoctor)
                            .into(view.pictureCallingDr)

                } else {
                    Picasso.with(activity)
                            .load(R.drawable.profile_photo)
                            .into(view.pictureCallingDr)

                }

                val media = MediaPlayer.create(activity,
                        R.raw.calling)
                media.start()
                view.scanButtonDialoge.setOnClickListener {
                    media.stop()
                    startActivity(Intent(activity,
                            ConsultRoomActivity::class.java))
                    this.dismiss()


                }
                view.cancelButtonDialoge.setOnClickListener {

                    media.stop()
                    this.dismiss()


                }


            }


            // this mean that dialog for analyse
            else {

                view.scanButtonDialoge.setOnClickListener {


                    // todo : scan and get result detect real time camera
                    val detectRealTime = Intent(activity, CameraAnalyse::class.java)
                    startActivity(detectRealTime)
                    dismiss()


                }
                view.cancelButtonDialoge.setOnClickListener {


                    // to avoid enter open activity many time
                    if (place == "MainActivity") {

                        dismiss()

                    } else if (place == "loginUser" || place == "register") {
                        dismiss()
                        val intent = Intent(activity, MainActivity::class.java)
                        startActivity(intent)
                    }


                }

            }
        } catch (ex: Exception) {
        }
        return view
    }


}