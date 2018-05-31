package com.upfunstudio.emotionsocial.Companion

import android.app.DialogFragment
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.upfunstudio.emotionsocial.R
import com.upfunstudio.emotionsocial.User.CameraAnalyse
import com.upfunstudio.emotionsocial.User.MainActivity
import kotlinx.android.synthetic.main.scan_dialoge.view.*
import java.util.*


/**
 * Created by walido on 1/30/2018.
 */
open class WindowAnalyseOrCalling : DialogFragment() {

    private var mFireStore: FirebaseFirestore? = null
    private var mAuth: FirebaseAuth? = null
    private var media: MediaPlayer? = null


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {

        mFireStore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        media = MediaPlayer.create(activity, R.raw.calling)
        val doctorID = this.arguments.getString("doctorID", "")

        returnValueDrForDefault(doctorID)


        val view = inflater!!.inflate(R.layout.scan_dialoge, container, false)


        try {


            val place: String = this.arguments.getString("placeActivity", "")

            // check the connection


            // show dialog for calling in this case
            if (place == "calling") {


                // design new layout by coding
                view.textDisplay.text = getString(R.string.call_now)
                view.scanButtonDialoge.visibility = View.GONE
                view.pictureCallingDr.visibility = View.VISIBLE
                //view.cancelButtonDialoge.setBackgroundResource(R.drawable.button_refuse_background)
                //view.cancelButtonDialoge.setTextColor(resources.getColor(android.R.color.white))
                val pictureDoctor: String = this.arguments.getString("pictureDoctor", "")
                val doctorID = this.arguments.getString("doctorID", "")
                val doctorName = this.arguments.getString("doctorName", "")

                val userID = mAuth!!.currentUser!!.uid



                if (!pictureDoctor.isEmpty()) {

                    Picasso.with(activity)
                            .load(pictureDoctor)
                            .into(view.pictureCallingDr)

                } else {
                    Picasso.with(activity)
                            .load(R.drawable.profile_photo)
                            .into(view.pictureCallingDr)

                }


                // getting user information to push into request info
                mFireStore!!.collection("Users")
                        .document(userID)
                        .get()
                        .addOnCompleteListener { task ->

                            try {

                                if (task.isSuccessful) {

                                    val pictureUser = task.result.getString("profile_picture")

                                    val userEmail = task.result.getString("email")

                                    val userUsername = task.result.getString("fullName")


                                    if (!userEmail.isNullOrEmpty()
                                            && !pictureUser.isNullOrEmpty()
                                            && !userUsername.isNullOrEmpty()) {


                                        // send request
                                        sendRequestFromUser(
                                                userID,
                                                userEmail,
                                                userUsername,
                                                doctorID,
                                                pictureUser)
                                        // check there is any response
                                        getResponseToUser(
                                                doctorID = doctorID,
                                                doctorName = doctorName,
                                                userID = userID,
                                                userName = userUsername,
                                                doctorPic = pictureDoctor,
                                                userPic = pictureUser)


                                    }

                                }


                            } catch (ex: Exception) {

                                Toast.makeText(activity, "Connection went down!",
                                        Toast.LENGTH_LONG).show()

                            }

                        }


                // the user cancel the request
                view.cancelButtonDialoge.setOnClickListener {


                    mFireStore!!.collection("Doctors")
                            .document(doctorID)
                            .update("canceled", true)
                            .addOnFailureListener {
                                Toast.makeText(activity, "Connection went down!",
                                        Toast.LENGTH_LONG).show()

                            }


                }


                // this dialog appear for doctor main when someone send request
            } else if (place == "doctorRequest") {


                view.scanButtonDialoge.visibility = View.GONE
                view.accept.visibility = View.VISIBLE
                view.accept.setTextColor(Color.WHITE)
                view.pictureCallingDr.visibility = View.VISIBLE


                // put and show the specific name of the user
                // read the fullName a picture for the requests
                // todo : handle it later for user ad pic
                val userName = this.arguments.getString("usernameRequest")
                val pictureUser = this.arguments.getString("pictureRequests")
                val pictureDoctor: String = this.arguments.getString("myPicture", "")
                val doctorID = this.arguments.getString("doctorID", "")
                val doctorName = this.arguments.getString("drName", "")


                if (!userName.isNullOrEmpty()) {
                    view.textDisplay.text = "$userName want to call you ..."

                } else {
                    view.textDisplay.text = getString(R.string.want_to_call)

                }

                //view.cancelButtonDialoge.background=resources.getDrawable(R.drawable.button_refuse_background)

                if (!pictureDoctor.isEmpty()) {

                    // show picture of the user
                    Picasso.with(activity)
                            .load(pictureUser)
                            .into(view.pictureCallingDr)

                } else {
                    Picasso.with(activity)
                            .load(R.drawable.profile_photo)
                            .into(view.pictureCallingDr)

                }


                media!!.start()

                /*

                     if the dr accept
                     dr and user enter the room

                     if the dr cancel or the user
                     user and dr dismiss dialog


                      */

                // doctor accept the request
                view.accept.setOnClickListener {

                    mFireStore!!.collection("Doctors")
                            .document(doctorID)
                            .update("accept", true)


                }
                // doctor cancel request
                view.cancelButtonDialoge.setOnClickListener {

                    mFireStore!!.collection("Doctors")
                            .document(doctorID)
                            .update("canceled", true)


                }
                // check repose from user appear to dr
                mFireStore!!.collection("Doctors")
                        .document(doctorID)
                        .addSnapshotListener { documentSnapshot, _ ->

                            try {

                                if (documentSnapshot.exists() && documentSnapshot != null) {


                                    val canceled = documentSnapshot.getBoolean("canceled")

                                    if (canceled) {
                                        this.dismiss()
                                        media!!.stop()
                                        //cancelRequest(doctorID)
                                    }
                                    val accept = documentSnapshot.getBoolean("accept")
                                    if (accept) {


                                        this.dismiss()
                                        media!!.stop()
                                        cancelRequest(doctorID)
                                        // when the dr enter the chat room change the state as connect now to true
                                        allowConnectNowDr(doctorID)

                                        // enter to the room as dr
                                        val intent = Intent(activity,
                                                ConsultRoomActivity::class.java)
                                        intent.putExtra("roomName", doctorID)

                                        // prepare random value
                                        val random =Random().nextInt(1000 - 0) + 0

                                        // send UserEntered
                                        intent.putExtra("userEntered", "Doctor${doctorName.split(" ")[0]}$random")



                                        startActivity(intent)


                                    }


                                }
                            } catch (ex: Exception) {

                            }


                        }


            }


            // this mean that dialog for analyse
            else {

                view.scanButtonDialoge.setOnClickListener {


                    //  go to camera analyse activity
                    val detectRealTime = Intent(activity, CameraAnalyse::class.java)
                    startActivity(detectRealTime)
                    activity.finish()

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

    private fun sendRequestFromUser(userID: String, userEmail: String, userUsername: String, doctorID: String, userPicture: String) {

        val map = HashMap<String, Any>()
        map["id"] = userID // user id
        map["email"] = userEmail
        map["response"] = false
        map["username"] = userUsername
        map["timeRequest"] = FieldValue.serverTimestamp()
        if (!userPicture.isEmpty()) {
            map["pictureRequested"] = userPicture

        }
        // get doctor request state
        var doctorRequest: Boolean
        mFireStore!!.collection("Doctors")
                .document(doctorID)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (task.result.exists()) {
                            doctorRequest = task.result.getBoolean("request")

                            // send request only when request false
                            if (!doctorID.isEmpty() && !doctorRequest) {

                                // change canceled to default value and send request


                                // send request to dr
                                mFireStore!!.collection("Doctors")
                                        .document(doctorID)
                                        .collection("Requests")
                                        .add(map as Map<String, Any>)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {

                                                // success to send request
                                                // start song to call
                                                media!!.start()
                                                // to avoid any one else call at the same time
                                                mFireStore!!.collection("Doctors")
                                                        .document(doctorID)
                                                        .update("request", true)


                                            }

                                        }


                            } else {

                                dismiss()
                                Toast.makeText(activity, getString(R.string.please_try_to_call_later),
                                        Toast.LENGTH_LONG).show()
                                activity.finish()

                            }
                        } else {
                            dismiss()
                            Toast.makeText(activity, getString(R.string.please_try_to_call_later),
                                    Toast.LENGTH_LONG).show()
                            activity.finish()

                        }


                    } else {
                        dismiss()
                        Toast.makeText(activity, getString(R.string.please_try_to_call_later),
                                Toast.LENGTH_LONG).show()
                        activity.finish()

                    }
                }

    }


    private fun getResponseToUser(doctorID: String, userID: String, doctorName: String, userName: String, doctorPic: String, userPic: String) {
        // check if there is any response from dr
        mFireStore!!.collection("Doctors")
                .document(doctorID)
                .addSnapshotListener { documentSnapshot, _ ->

                    try {

                        if (documentSnapshot.exists() && documentSnapshot != null) {


                            val canceled = documentSnapshot.getBoolean("canceled")


                            if (canceled) {
                                this.dismiss()
                                media!!.stop()
                                cancelRequest(doctorID)
                            }
                            val accept = documentSnapshot.getBoolean("accept")
                            if (accept) {
                                this.dismiss()
                                media!!.stop()
                                cancelRequest(doctorID)

                                // user delete request cause dr accept request ad connect with you
                                //deleteRequest(doctorID,CallingRequestFragment().list[].id)
                                // enter room as a user
                                val intent = Intent(activity,
                                        ConsultRoomActivity::class.java)
                                intent.putExtra("roomName", doctorID)

                                // prepare random value
                                val random =Random().nextInt(1000 - 0) + 0
                                // adding random value to avoid the same name
                                intent.putExtra("userEntered", "User${userName.split(" ")[0]}$random")

                                // sending the information of the consulting
                                intent.putExtra("doctorID", doctorID)
                                intent.putExtra("userID", userID)
                                intent.putExtra("doctorName", doctorName)
                                intent.putExtra("userName", userName)
                                intent.putExtra("doctorPic", doctorPic)
                                intent.putExtra("userPic", userPic)




                                startActivity(intent)


                            }


                        }
                    } catch (ex: Exception) {

                    }

                }


    }

    private fun cancelRequest(doctorID: String) {
        mFireStore!!.collection("Doctors")
                .document(doctorID)
                .update("request", false)


    }

    private fun allowConnectNowDr(doctorID: String) {
        mFireStore!!.collection("Doctors")
                .document(doctorID)
                .update("connectNow", true)


    }

    private fun deleteRequest(doctorID: String, documentID: String) {


        try {
            mFireStore!!
                    .collection("Doctors")
                    .document(doctorID)
                    .collection("Requests")
                    .document(documentID)
                    .delete()

        } catch (ex: Exception) {
            Toast.makeText(activity, ex.message, Toast.LENGTH_LONG)
                    .show()

        }


    }

    private fun returnValueDrForDefault(doctorID: String) {

        try {
            mFireStore!!.collection("Doctors")
                    .document(doctorID)
                    .update("request", false,
                            "accept", false,
                            "canceled", false)

        } catch (ex: Exception) {


        }
    }


}