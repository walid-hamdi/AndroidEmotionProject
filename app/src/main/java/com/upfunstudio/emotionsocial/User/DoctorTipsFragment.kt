package com.upfunstudio.emotionsocial.User

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.upfunstudio.emotionsocial.Companion.TipContentActivity
import com.upfunstudio.emotionsocial.R
import kotlinx.android.synthetic.main.all_tips.view.*
import kotlinx.android.synthetic.main.fragment_doctor_tips.view.*
import java.text.SimpleDateFormat
import java.util.*


class DoctorTipsFragment : Fragment() {


    private var mAuth: FirebaseAuth? = null
    private var mFireStore: FirebaseFirestore? = null
    private var list: ArrayList<TipsForUsers>? = null
    private var customAdapter: AdapterRecycle? = null
    private var sqlLiteFavorite: SqlLiteFavorite? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_doctor_tips,
                container, false)


        // prepareInstances
        mFireStore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        sqlLiteFavorite = SqlLiteFavorite(this.context!!)


        // add offline capability
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()

        mFireStore!!.firestoreSettings = settings


        list = ArrayList()
        customAdapter = AdapterRecycle(context = container!!.context, list = list!!)
        v.tipsRecycleview.layoutManager = LinearLayoutManager(this.activity)
        v.tipsRecycleview.setHasFixedSize(true)
        v.tipsRecycleview.adapter = customAdapter
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

        //todo : get all doctors tip
        mFireStore!!
                .collection("Doctors")
                .document("IXCK6eH6NlbONFPxG8OMWtyiLsu2")
                .collection("Tips")
                .addSnapshotListener { documentSnapshots, _ ->

                    for (change in documentSnapshots.documentChanges) {
                        if (change.type == DocumentChange.Type.ADDED) {

                            if (change.document.exists()) {
                                var timeTip = ""
                                val timeTips = change.document.get("timeTip")
                                if (timeTips != null) {
                                    val dateTime = timeTips as Date
                                    val date = Date(dateTime.time)
                                    timeTip = SimpleDateFormat("yyyy/MM/dd").format(date)
                                }
                                val contentTip = change
                                        .document
                                        .getString("contentTip")
                                val titleTip = change
                                        .document
                                        .getString("titleTip")
                                val username = change
                                        .document
                                        .getString("username")
                                if (!contentTip.isNullOrEmpty()
                                        && timeTip != null
                                        && !titleTip.isNullOrEmpty()) {

                                    list!!.add(TipsForUsers(
                                            contentTip = contentTip,
                                            time = timeTip,
                                            titleTip = titleTip,
                                            username = username))


                                }
                            }
                        } else if (change.type == DocumentChange.Type.REMOVED) {

                            customAdapter!!.notifyDataSetChanged()

                        }


                    }
                    customAdapter!!.notifyDataSetChanged()


                }


    }

    // class for adapting
    data class TipsForUsers(val contentTip: String,
                            val time: String,
                            val titleTip: String,
                            val username: String)


    // appear list main of drs for users
    inner class AdapterRecycle(val context: Context,
                               private val list: List<TipsForUsers>) :
            RecyclerView.Adapter<AdapterRecycle.CustomViewHolder>() {


        override fun getItemCount(): Int {

            return list.size

        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CustomViewHolder {
            val layoutInf = LayoutInflater.from(parent?.context)
            val view = layoutInf.inflate(R.layout.all_tips, parent, false)

            return CustomViewHolder(view)


        }

        override fun onBindViewHolder(holder: CustomViewHolder?, position: Int) {

            val item = list[position]


            // full the title for all items for tip
            holder!!.itemView.titleAlltips.text = item.titleTip
            // full the title and show just first 20 letters
            if (holder!!.itemView.titleAlltips.text.length > 10) {
                val split = item.contentTip.substring(0, 9)
                holder!!.itemView.titleAlltips.text = split.plus("...")
            } else {
                holder!!.itemView.titleAlltips.text = item.contentTip
            }
            // full the publisher doctor for this tip
            holder.itemView.publishedBy.text = "By ".plus(item.username)
            // full the time stamp get it from server
            holder.itemView.dateAllTips.text = item.time


            // add to fav list
            holder!!.itemView.addFavButton.setOnClickListener {

                val values = ContentValues()
                values.put("title", item.titleTip)
                values.put("description", item.contentTip)
                values.put("time", item.time)
                values.put("published", item.username)
                // check if item exist already or not the fav
                if (SqlLiteFavorite.itemExits!!) {
                    Toast.makeText(activity, "You added this item already!",
                            Toast.LENGTH_SHORT).show()

                } else {
                    val result = sqlLiteFavorite!!.storeFavData(values)
                    if (result > 0) {
                        Toast.makeText(activity, "You added it to Favorite List",
                                Toast.LENGTH_SHORT).show()

                    } else {
                        Toast.makeText(activity, "Something went wrong to add it!",
                                Toast.LENGTH_SHORT).show()

                    }
                }

            }

            // pass the info to details psycho tip content
            holder.itemView.layoutAllTips.setOnClickListener {
                val intent = Intent(context, TipContentActivity::class.java)
                // send the details and passing to appear for users
                intent.putExtra("title", item.titleTip)
                intent.putExtra("description", item.contentTip)

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK)
                context.startActivity(intent)


            }


        }


        inner class CustomViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    }


}