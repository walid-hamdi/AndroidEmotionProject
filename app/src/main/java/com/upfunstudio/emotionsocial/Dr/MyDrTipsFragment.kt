package com.upfunstudio.emotionsocial.Dr


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.upfunstudio.emotionsocial.Companion.TipContentActivity
import com.upfunstudio.emotionsocial.R
import kotlinx.android.synthetic.main.fragment_my_dr_tips.*
import kotlinx.android.synthetic.main.fragment_my_dr_tips.view.*
import kotlinx.android.synthetic.main.list_tips.view.*
import java.text.SimpleDateFormat
import java.util.*


class MyDrTipsFragment : Fragment() {

    private var mAuth: FirebaseAuth? = null
    private var mFireStore: FirebaseFirestore? = null
    private var list: ArrayList<Tips>? = null
    private var customAdapter: AdapterRecycle? = null
    private var doctorID: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_my_dr_tips,
                container, false)


        mFireStore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        doctorID = mAuth!!.currentUser!!.uid


        // add offline capability
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()

        mFireStore!!.firestoreSettings = settings


        list = ArrayList()
        customAdapter = AdapterRecycle(context = container!!.context, list = list!!)
        v.mytipsRecycleview.layoutManager = LinearLayoutManager(this.activity)
        v.mytipsRecycleview.setHasFixedSize(true)
        v.mytipsRecycleview.adapter = customAdapter
        prepareRecycleView()


        // to delete item by swiping
        val itemTouchHelper = ItemTouchHelper(null)
        itemTouchHelper.attachToRecyclerView(mytipsRecycleview)



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

        try {


            mFireStore!!
                    .collection("Doctors")
                    .document(doctorID!!)
                    .collection("Tips")
                    .addSnapshotListener { documentSnapshots, _ ->


                        for (change in documentSnapshots.documentChanges) {


                            if (change.type == DocumentChange.Type.ADDED) {

                                val contentTip = change.document.getString("contentTip")

                                var timeTip = ""

                                val timeTips = change.document.get("timeTip")
                                if (timeTips != null) {
                                    val dateTime = timeTips as Date
                                    val date = Date(dateTime.time)
                                    timeTip = SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date)
                                }

                                val titleTip = change.document.getString("titleTip")


                                if (change.document.exists()
                                        && !contentTip.isNullOrEmpty()
                                        && timeTip != null
                                        && !titleTip.isNullOrEmpty()) {


                                    list!!.add(Tips(
                                            contentTip = contentTip,
                                            // get specific server time
                                            time = timeTip,
                                            titleTip = titleTip))
                                }

                                customAdapter!!.notifyDataSetChanged()


                            }


                        }


                    }

        } catch (ex: Exception) {
        }
    }

    // class for adapting
    data class Tips(val contentTip: String,
                    val time: String,
                    val titleTip: String)

    // appear list main of tips
    inner class AdapterRecycle(val context: Context,
                               private val list: List<Tips>) :
            RecyclerView.Adapter<AdapterRecycle.CustomViewHolder>() {


        override fun getItemCount(): Int {

            return list.size

        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CustomViewHolder {
            val layoutInf = LayoutInflater.from(parent?.context)
            val view = layoutInf.inflate(R.layout.list_tips, parent, false)

            return CustomViewHolder(view)


        }

        override fun onBindViewHolder(holder: CustomViewHolder?, position: Int) {

            val item = list[position]

            var title = holder!!.itemView.tipsTitle.text
            var times = holder.itemView.tipsTime.text


            if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(times)) {
                // show  just a list of tips
                if (title.length > 15) {

                    val slit = item.titleTip.substring(0, 14)
                    holder!!.itemView.tipsTitle.text = slit.plus("...")

                } else {

                    holder!!.itemView.tipsTitle.text = item.titleTip

                }

                holder.itemView.tipsTime.text = item.time

            } else {

                holder!!.view.visibility = View.GONE

            }







            holder.itemView.cardTips.setOnClickListener {
                val intent = Intent(context, TipContentActivity::class.java)
                // send the details and passing into profile dr appear for users
                intent.putExtra("title", item.titleTip)
                intent.putExtra("description", item.contentTip)

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK)
                context.startActivity(intent)


            }

        }


        inner class CustomViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    }


}