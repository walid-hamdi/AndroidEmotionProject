package com.upfunstudio.emotionsocial.Companion

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.upfunstudio.emotionsocial.Dr.MainDoctors
import com.upfunstudio.emotionsocial.R
import com.upfunstudio.emotionsocial.User.MainActivity
import com.vidyo.VidyoClient.Connector.Connector
import com.vidyo.VidyoClient.Connector.ConnectorPkg
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_consult_room.*
import org.jsoup.Jsoup
import java.util.*


class ConsultRoomActivity : AppCompatActivity() {


    private var connector: Connector? = null
    private var mAuth: FirebaseAuth? = null
    private var mFireStore: FirebaseFirestore? = null
    private var doctorID: String? = null
    private var token: String? = null
    private val CHATPERSMISSIONS = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consult_room)
        // pass vidyo : 2FQS52OZ8ayadcQn6wVXV

        chackPermissions()

    }


    private fun chackPermissions() {

        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(
                            this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(arrayOf(android.Manifest.permission.CAMERA,
                        android.Manifest.permission.RECORD_AUDIO), CHATPERSMISSIONS)


                return
            }


        }


        consult()

    }


    private fun consult() {

        // ini the firebase instances
        mAuth = FirebaseAuth.getInstance()
        doctorID = mAuth!!.currentUser!!.uid
        mFireStore = FirebaseFirestore.getInstance()

        // ini the connector instance
        ConnectorPkg.setApplicationUIContext(this)
        ConnectorPkg.initialize()


        // connect
        // must use unique user name
        // must use unique token
        // same room name (note : i putted dr id as room name)

        try {


            val userEntered = intent.extras.getString("userEntered")
            val roomName = intent.extras.getString("roomName")
            val developer_key = "6c5def8d325a457fa7440e69d6a76844"
            val app_id = "cf1fe1.vidyo.io"
            val expires = 10000


            if (!roomName.isNullOrEmpty() && !userEntered.isNullOrEmpty()) {

                startConnect(developer_key, app_id, userEntered, roomName, expires)

            } else {

                Toast.makeText(this,
                        "Something went wrong!!!", Toast.LENGTH_LONG).show()


            }


        } catch (ex: Exception) {
            Toast.makeText(this, "${ex.message} ", Toast.LENGTH_LONG).show()

        }

    }

    private fun startConnect(developer_key: String, app_id: String, userEntered: String, roomName: String, expires: Int) {

        val dialog = SpotsDialog(this)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()


        // getting token from url
        val thread = Thread(Runnable {

            // read from user passed
            try {

                val doc = Jsoup.connect("https://vidyocreatetoken.appspot.com/on_get_token?developer_Key=$developer_key&app_Id=$app_id&userName=$userEntered&expires_In_Secs=$expires").get()
                val p = doc.getElementsByTag("p")
                token = p[3].text().split(":")[1].trim()

            } catch (ex: Exception) {


            }


        })

        thread.start()
        thread.join()
        dialog.dismiss()

        connector!!.connect(
                "prod.vidyo.io",
                token,
                userEntered,
                roomName,
                Consult())
        Toast.makeText(this, "Welcome to connect!", Toast.LENGTH_SHORT).show()


    }

    fun startConversation(view: View) {


        connector = Connector(videoFrame,
                Connector.ConnectorViewStyle.VIDYO_CONNECTORVIEWSTYLE_Tiles,
                16,
                "",
                "",
                0)

        connector!!.showViewAt(
                videoFrame,
                0,
                0,
                videoFrame!!.width,
                videoFrame!!.height)


    }

    fun finishReturnEvent(view: View) {


        try {
            mFireStore!!.collection("Doctors")
                    .document(doctorID!!)
                    .update("request", false,
                            "connectNow", false,
                            "accept", false)
                    .addOnCompleteListener { task ->

                        // the doctor entered and change the states fields
                        if (task.isSuccessful) {

                            // disconnect
                            try {
                                connector!!.disconnect()
                            } catch (ex: Exception) {
                            }
                            Toast.makeText(applicationContext, "Have great day!",
                                    Toast.LENGTH_LONG).show()
                            val intent = Intent(applicationContext,
                                    MainDoctors::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                            finish()
                            // that mean the user entered
                        } else {
                            // save consult info from user
                            saveConsultInfo()

                        }
                    }
        } catch (ex: Exception) {
        }


    }

    private fun saveConsultInfo() {

        try {


            val doctorID = intent.extras.getString("doctorID")
            val userID = intent.extras.getString("userID")
            val doctorName = intent.extras.getString("doctorName")
            val userName = intent.extras.getString("userName")
            val doctorPic = intent.extras.getString("doctorPic")
            val userPic = intent.extras.getString("userPic")



            if (!doctorID.isNullOrEmpty() &&
                    !userID.isNullOrEmpty() &&
                    !doctorName.isNullOrEmpty() &&
                    !userName.isNullOrEmpty() &&
                    !doctorPic.isNullOrEmpty() &&
                    !userPic.isNullOrEmpty()) {

                val consult = HashMap<String, Any>()
                consult["doctorID"] = doctorID
                consult["userID"] = userID
                consult["doctorName"] = doctorName
                consult["userName"] = userName
                consult["doctorPic"] = doctorPic
                consult["userPic"] = userPic
                consult["timeConsult"] = FieldValue.serverTimestamp()

                // push infos to the user and to the doctor
                mFireStore!!.collection("Users")
                        .document(userID)
                        .collection("Consults")
                        .add(consult as Map<String, String>)
                        .addOnCompleteListener { task ->

                            if (task.isSuccessful) {

                                // push it to the doctor
                                mFireStore!!.collection("Doctors")
                                        .document(doctorID)
                                        .collection("Consults")
                                        .add(consult as Map<String, String>)
                                        .addOnCompleteListener { task ->

                                            if (task.isSuccessful) {

                                                val intent = Intent(applicationContext,
                                                        MainActivity::class.java)
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                                startActivity(intent)
                                                finish()
                                                Toast.makeText(applicationContext, "Don't forget to rate Doctor!", Toast.LENGTH_SHORT).show()


                                            } else {
                                                Toast.makeText(applicationContext, "Check the connection!", Toast.LENGTH_SHORT).show()
                                            }
                                        }

                            } else {
                                Toast.makeText(applicationContext, "Check the connection!", Toast.LENGTH_SHORT).show()
                            }
                        }


            }


        } catch (ex: Exception) {

        }

    }


    private inner class Consult : Connector.IConnect {


        override fun onSuccess() {

            Toast.makeText(applicationContext, "Success to connect ", Toast.LENGTH_LONG).show()

        }

        override fun onFailure(p0: Connector.ConnectorFailReason?) {
            Toast.makeText(applicationContext, "Failed to connect", Toast.LENGTH_LONG).show()


        }

        override fun onDisconnected(p0: Connector.ConnectorDisconnectReason?) {


        }


    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when (requestCode) {
            CHATPERSMISSIONS -> {

                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,
                            "Deny to use Camera",
                            Toast.LENGTH_LONG)

                } else if (grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,
                            "Deny to use Audio",
                            Toast.LENGTH_LONG)
                } else {
                    consult()
                }
            }
        }



        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


}