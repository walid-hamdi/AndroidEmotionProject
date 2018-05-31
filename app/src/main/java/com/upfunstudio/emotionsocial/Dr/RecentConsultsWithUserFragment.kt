package com.upfunstudio.emotionsocial.Dr

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.squareup.picasso.Picasso
import com.upfunstudio.emotionsocial.Helper.RecycleItemTouchHelper
import com.upfunstudio.emotionsocial.R
import com.upfunstudio.emotionsocial.User.ListHistoryReportActivity
import com.upfunstudio.emotionsocial.User.RecentConsultsWithDrFragment
import kotlinx.android.synthetic.main.fragment_recycleview.*
import kotlinx.android.synthetic.main.fragment_recycleview.view.*

import kotlinx.android.synthetic.main.list_of_drs.view.*
import kotlinx.android.synthetic.main.recent_consults_ticket.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class RecentConsultsWithUserFragment : Fragment() {

    private var mAuth: FirebaseAuth? = null
    private var mFireStore: FirebaseFirestore? = null
    private var list: ArrayList<Consult>? = null
    private var customAdapter: AdapterRecycle? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        mFireStore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        val v = inflater.inflate(R.layout.fragment_recycleview, container, false)


        // add offline capability
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        mFireStore!!.firestoreSettings = settings



        list = ArrayList()
        customAdapter = AdapterRecycle(context = container!!.context, list = list!!)
        v.recycleview.layoutManager = LinearLayoutManager(this.activity)
        v.recycleview.setHasFixedSize(true)
        v.recycleview.itemAnimator = DefaultItemAnimator()
        v.recycleview.addItemDecoration(DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL))
        v.recycleview.adapter = customAdapter
        prepareRecycleView()

        // to delete
        ItemTouchHelper(RecycleItemTouchHelper(0,
                ItemTouchHelper.LEFT, Listener()))
                .attachToRecyclerView(v.recycleview)


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

            mFireStore!!.collection("Doctors")
                    .document(mAuth!!.currentUser!!.uid)
                    .collection("Consults")
                    .addSnapshotListener { querySnapshot, _ ->


                        try {
                            list!!.clear()
                            for (change in querySnapshot.documents) {

                                if (change.exists() && change != null) {


                                    val id = change.id

                                    val username = change.getString("userName")
                                    val userPic = change.getString("userPic")


                                    var time = ""

                                    val timeConsult = change.get("timeConsult")
                                    if (timeConsult != null) {
                                        val dateTime = timeConsult as Date
                                        val date = Date(dateTime.time)
                                        time = SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date)
                                    }



                                    if (!username.isNullOrEmpty()
                                            && !time.isEmpty()) {


                                        list!!.add(Consult(
                                                id = id,
                                                userName = username,
                                                userPic = userPic,
                                                timeConsult = time))


                                    } else {
                                        Toast.makeText(context, getString(R.string.fail_cx_recetcosilt_msg),
                                                Toast.LENGTH_LONG).show()
                                    }
                                    customAdapter!!.notifyDataSetChanged()


                                }
                            }

                        } catch (ex: Exception) {
                        }


                    }

        } catch (ex: Exception) {

        }
    }

    // class for adapting
    data class Consult(val id: String="",
                       val userName: String="",
                       val userPic: String="",
                       val timeConsult: String="")

    // appear list main of tips
    inner class AdapterRecycle(val context: Context,
                               val list: ArrayList<Consult>) :
            RecyclerView.Adapter<AdapterRecycle.CustomViewHolder>() {

        var position: Int? = null


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {

            val layoutInf = LayoutInflater.from(parent.context)
            val view = layoutInf.inflate(R.layout.recent_consults_ticket, parent, false)

            return CustomViewHolder(view)

        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            val item = list[position]
            this.position = position



            holder.itemView.fullNameConsult.text = "Consulted with ${item.userName}"
            holder.itemView.timeConsult.text = item.timeConsult



            if (item.userPic.isEmpty()) {
                Picasso.with(context).load(R.drawable.profile_photo)
                        .into(holder.itemView.pictureCosulter)

            } else {
                Picasso.with(context).load(item.userPic)
                        .placeholder(R.drawable.profile_photo)
                        .into(holder.itemView.pictureCosulter)


            }

            // go to the profile of the user
            holder.itemView.pictureCosulter
                    .setOnClickListener {

                        // todo : later

                    }


        }
        override fun getItemCount(): Int {

            return list.size

        }

        fun removeItem(position: Int) {

            // remove from list
            list.removeAt(position)
            if (position == -1) {
                position + 1
            }

            // remove from firstore
            try {
                mFireStore!!.collection("Doctors")
                        .document(mAuth!!.currentUser!!.uid)
                        .collection("Consults")
                        .document(list[this.position!! - 1].id)
                        .delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Deleted !", Toast.LENGTH_LONG)
                                        .show()
                            } else {
                                Toast.makeText(context, "Failed !", Toast.LENGTH_LONG)
                                        .show()

                            }


                        }
            } catch (ex: Exception) {
                Toast.makeText(context, ex.message, Toast.LENGTH_LONG)
                        .show()

            }
            notifyItemRemoved(position)
        }


        inner class CustomViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    }


    // impelement the interface to SWIP in recycleview
    inner class Listener : RecycleItemTouchHelper.RecycleItemTouchHelperListenner {
        override fun onSwip(viewHolder: RecyclerView.ViewHolder?, direction: Int, position: Int) {

            if (viewHolder is AdapterRecycle.CustomViewHolder) {

                customAdapter!!.removeItem(viewHolder.adapterPosition)

            }

            // todo : ask to restore "UNDO" the remove by using snackBar

        }

    }



}