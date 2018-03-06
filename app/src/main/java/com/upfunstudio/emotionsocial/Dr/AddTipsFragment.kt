package com.upfunstudio.emotionsocial.Dr


import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.upfunstudio.emotionsocial.R
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.fragment_add_tips.view.*


class AddTipsFragment : DialogFragment() {

    private var mFireStore: FirebaseFirestore? = null
    private var mAuth: FirebaseAuth? = null
    private var doctorID: String? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_add_tips, container, false)
        prepareInstance()


        v.cancel.setOnClickListener {
            this.dismiss()
        }

        v.addNewTip.setOnClickListener {

            if (TextUtils.isEmpty(v.titleAddTips.text)) {
                v.titleAddTips.error = "Title of the tip"
            } else if (TextUtils.isEmpty(v.contentTips.text)) {
                v.contentTips.error = "Description of the tip"
            } else {


                try {
                    mFireStore!!.collection("Doctors")
                            .document(doctorID!!)
                            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->

                                val username = documentSnapshot.getString("username")
                                // save all info for tip
                                addTipInfo(title = v.titleAddTips.text.toString(),
                                        content = v.contentTips.text.toString(),
                                        username = username)


                            }


                } catch (ex: Exception) {

                }


            }

        }


        return v
    }

    private fun prepareInstance() {

        mFireStore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        doctorID = mAuth!!.currentUser!!.uid

    }

    private fun addTipInfo(title: String, content: String, username: String) {

        val map = HashMap<String, Any>()
        map["titleTip"] = title
        map["contentTip"] = content
        // get specific server time
        map["timeTip"] = FieldValue.serverTimestamp()
        map["username"] = username


        val loading = SpotsDialog(activity)
        loading!!.setTitle("Adding...")
        loading!!.setCanceledOnTouchOutside(true)
        loading!!.show()


        // adding new tips to database
        mFireStore!!.collection("Doctors")
                .document(doctorID!!)
                .collection("Tips")
                .add(map as Map<String, Any>)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(activity, "You Added Tip! Thank you!",
                                Toast.LENGTH_SHORT).show()
                        loading.dismiss()
                        this.dismiss()


                    } else {
                        Toast.makeText(activity, "Failed Add!",
                                Toast.LENGTH_SHORT).show()

                        loading.hide()
                    }
                }


    }


}