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


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_add_tips, container, false)
        prepareInstance()


        v.cancel.setOnClickListener {
            this.dismiss()
        }

        v.addNewTip.setOnClickListener {

            if (TextUtils.isEmpty(v.titleAddTips.text)) {
                v.titleAddTips.error = getString(R.string.tip_msg)
            } else if (TextUtils.isEmpty(v.contentTips.text)) {
                v.contentTips.error = getString(R.string.dsc_msg)
            } else {


                // to get info from tip's publisher
                val doctorID = mAuth!!.currentUser!!.uid
                mFireStore!!.collection("Doctors")
                        .document(doctorID)
                        .addSnapshotListener { documentSnapshot, _ ->


                            try {
                                if (documentSnapshot.exists() && documentSnapshot != null) {
                                    if (documentSnapshot.getString("username") != null) {
                                        val username = documentSnapshot.getString("username")

                                        // save all info for tip
                                        addTipInfo(doctorID = doctorID, title = v.titleAddTips.text.toString(),
                                                content = v.contentTips.text.toString(),
                                                username = username)
                                    } else {
                                        Toast.makeText(activity,
                                                getString(R.string.update_first),
                                                Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, getString(R.string.tip_exit_msg),
                                            Toast.LENGTH_LONG).show()
                                }

                            } catch (ex: Exception) {

                            }

                        }
            }

        }


        return v
    }

    private fun prepareInstance() {

        mFireStore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()


    }

    private fun addTipInfo(doctorID: String, title: String, content: String, username: String) {

        val map = HashMap<String, Any>()

        map["doctorID"] = doctorID
        // add title tip with uppercase the first letter
        map["titleTip"] = title.substring(0,1).toUpperCase()+title.substring(1)
        map["contentTip"] = content.substring(0,1).toUpperCase()+content.substring(1)
        // get specific server time
        map["timeTip"] = FieldValue.serverTimestamp()
        map["username"] = username


        val loading = SpotsDialog(activity,R.style.loadingToAdd)
        loading.setCanceledOnTouchOutside(true)
        loading.show()


        // adding new tips to database
        mFireStore!!.collection("Tips")
                .add(map as Map<String, Any>)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        Toast.makeText(activity, getString(R.string.add_tip_sucess),
                                Toast.LENGTH_SHORT).show()
                        loading.dismiss()
                        this.dismiss()


                    } else {
                        Toast.makeText(activity, getString(R.string.fail_to_add_tip),
                                Toast.LENGTH_SHORT).show()

                        loading.hide()
                    }
                }


    }


}