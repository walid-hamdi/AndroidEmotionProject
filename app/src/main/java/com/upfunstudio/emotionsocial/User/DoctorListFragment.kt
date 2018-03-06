package com.upfunstudio.emotionsocial.User


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.squareup.picasso.Picasso
import com.upfunstudio.emotionsocial.R
import kotlinx.android.synthetic.main.fragment_doctor_list.view.*
import kotlinx.android.synthetic.main.list_of_drs.*
import kotlinx.android.synthetic.main.list_of_drs.view.*

class DoctorListFragment : Fragment() {


    private var mAuth: FirebaseAuth? = null
    private var mFireStore: FirebaseFirestore? = null
    private var list: ArrayList<Doctor>? = null
    private var customAdapter: AdapterRecycle? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_doctor_list,
                container, false)


        mFireStore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()


        // add offline capability
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()

        mFireStore!!.firestoreSettings = settings


        list = ArrayList()
        customAdapter = AdapterRecycle(context = container!!.context, list = list!!)
        v.recycleview.layoutManager = LinearLayoutManager(this.activity)
        v.recycleview.setHasFixedSize(true)
        v.recycleview.adapter = customAdapter
        prepareRecycleView()


        return v
    }

    override fun onStart() {
        super.onStart()
        customAdapter!!.notifyDataSetChanged()

    }

    override fun onResume() {
        super.onResume()
        customAdapter!!.notifyDataSetChanged()

    }


    private fun prepareRecycleView() {
        // get doctors and appear in list main
        mFireStore!!
                .collection("Doctors")
                .addSnapshotListener { documentSnapshots, _ ->


                    for (change in documentSnapshots.documentChanges) {
                        val state = change.document.getBoolean("state")
                        val picture = change.document.getString("picPath")


                        if (change.type == DocumentChange.Type.ADDED) {


                            try {
                                if (change.document.exists()) {
                                    list!!.add(Doctor(uid = change.document.getString("uid"),
                                            username = change.document.getString("username"),
                                            specialityText = change.document.getString("specialityText"),
                                            picPath = picture,
                                            language = change.document.getString("language"),
                                            state = state,
                                            phone = change.document.getString("phone"),
                                            mail = change.document.getString("mail"),
                                            city = change.document.getString("city")))
                                } else {
                                    list!!.add(Doctor())
                                }


                            } catch (ex: Exception) {

                            }
                            customAdapter!!.notifyDataSetChanged()


                        }
                        // add instance chnage state
                        if (change.type == DocumentChange.Type.MODIFIED) {

                            if (state) {
                                iconState.setBackgroundResource(R.drawable.ic_video_online_24dp)
                            } else {
                                iconState.setBackgroundResource(R.drawable.ic_video_ofline_24dp)

                            }
                            customAdapter!!.notifyDataSetChanged()


                        }
                        // if i delete the doctor form system
                        if (change.type == DocumentChange.Type.REMOVED) {

                            customAdapter!!.notifyDataSetChanged()

                        }


                    }


                }

    }

    // class for adapting
    data class Doctor(val uid: String = "", val username: String = "default name", val city: String = "default city",
                      val language: String = "default language", val phone: String = "default phone",
                      val picPath: String = "default path", val mail: String = "default mail",
                      val specialityText: String = "default speciality", var state: Boolean = false)

    // appear list main of drs for users
    inner class AdapterRecycle(val context: Context,
                               private val list: List<Doctor>) :
            RecyclerView.Adapter<AdapterRecycle.CustomViewHolder>() {


        override fun getItemCount(): Int {

            return list.size

        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CustomViewHolder {
            val layoutInf = LayoutInflater.from(parent?.context)
            val view = layoutInf.inflate(R.layout.list_of_drs, parent, false)

            return CustomViewHolder(view)


        }

        override fun onBindViewHolder(holder: CustomViewHolder?, position: Int) {

            val item = list[position]
            // show just a list of doctors online

            // appear just a doctors with updates for full info
            var fullName = holder!!.itemView.fullNameMainText.text

            if (fullName != "Username") {
                fullName = item.username
                holder.itemView.cityMainText.text = item.city


                // use picasco lib
                if (item.picPath.isNullOrEmpty()) {
                    Picasso.with(context).load(R.drawable.profile_photo)
                            .into(holder.itemView.pictureMain)

                } else {
                    Picasso.with(context).load(item.picPath)
                            .placeholder(R.drawable.profile_photo)
                            .into(holder.itemView.pictureMain)

                }


            } else {
                holder!!.itemView!!.visibility = View.GONE
            }




            holder.itemView.layoutDrList.setOnClickListener {
                val intent = Intent(context, DoctorProfileAppearForUserActivity::class.java)
                // send the details and passing into profile dr appear for users
                intent.putExtra("uid", item.uid)
                intent.putExtra("username", item.username)
                intent.putExtra("city", item.city)
                intent.putExtra("phone", item.phone)
                intent.putExtra("picPath", item.picPath)
                intent.putExtra("mail", item.mail)
                intent.putExtra("language", item.language)
                intent.putExtra("specialityText", item.specialityText)
                intent.putExtra("state", item!!.state)


                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK)
                context.startActivity(intent)


            }

        }


        inner class CustomViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    }


}// Required empty public constructor
