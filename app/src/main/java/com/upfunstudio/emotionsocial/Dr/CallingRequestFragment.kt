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
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.squareup.picasso.Picasso
import com.upfunstudio.emotionsocial.Helper.RecycleItemTouchHelper
import com.upfunstudio.emotionsocial.R
import kotlinx.android.synthetic.main.calling_requests.view.*
import kotlinx.android.synthetic.main.fragment_recycleview.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class CallingRequestFragment : Fragment() {

    private var mAuth: FirebaseAuth? = null
    private var mFireStore: FirebaseFirestore? = null
    var list: ArrayList<Requests>? = null
    private var customAdapter: AdapterRecycle? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_recycleview,
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
        v.recycleview.layoutManager = LinearLayoutManager(activity)

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


    private fun prepareRecycleView() {


        mFireStore!!
                .collection("Doctors")
                .document(mAuth!!.currentUser!!.uid)
                .collection("Requests")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {


                        for (change in task.result.documentChanges) {
                            if (change.document.exists()) {

                                if (change.type == DocumentChange.Type.ADDED) {


                                    val id =  change.document.id


                                    val pictureRequested = change.document.getString("pictureRequested")
                                    val username = change.document.getString("username")
                                    val email = change.document.getString("email")
                                    val response = change.document.getBoolean("response")


                                    var timeCallFormat = ""

                                    val timeCall = change.document.get("timeRequest")
                                    if (timeCall != null) {
                                        val dateTime = timeCall as Date
                                        val date = Date(dateTime.time)
                                        timeCallFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date)
                                    }



                                    if (!username.isNullOrEmpty() &&
                                            !email.isNullOrEmpty()
                                            && timeCallFormat != null
                                            && pictureRequested != null) {


                                        list!!.add(Requests(id=id,
                                                username = username,
                                                email = email,
                                                time = timeCallFormat,
                                                response = response,
                                                pictureRequested = pictureRequested))


                                    }
                                    customAdapter!!.notifyDataSetChanged()


                                }


                            }
                        }

                    } else {

                        Toast.makeText(context, getString(R.string.please_refresh),
                                Toast.LENGTH_LONG).show()

                    }
                }


    }

    // class for adapting
    data class Requests(val id:String,
                        val username: String,
                        val email: String,
                        val time: String,
                        val response: Boolean,
                        val pictureRequested: String)

    // appear list main of requests
    inner class AdapterRecycle(val context: Context,
                               val list: ArrayList<Requests>) :
            RecyclerView.Adapter<AdapterRecycle.CustomViewHolder>() {

        var position:Int?=null



        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {

            val layoutInf = LayoutInflater.from(parent.context)
            val view = layoutInf.inflate(R.layout.calling_requests, parent, false)

            return CustomViewHolder(view)

        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            val item = list[position]
            this.position=position

            holder.itemView.usernameCall.text = item.username
            holder.itemView.emailCall.text = item.email
            holder.itemView.timeCall.text = item.time

            // appear response state
            if (item.response) {
                holder.itemView.responseCall.text = getString(R.string.accept)
            } else {
                holder.itemView.responseCall.text = getString(R.string.fail_accept)
            }

            // appear picture request
            if (item.pictureRequested.isEmpty()) {
                Picasso.with(context).load(R.drawable.profile_photo)
                        .into(holder.itemView.picRequester)

            } else {
                Picasso.with(context).load(item.pictureRequested)
                        .placeholder(R.drawable.profile_photo)
                        .into(holder.itemView.picRequester)


            }
        }

        // to remove
        fun removeItem(position: Int) {

            // remove from list
            list.removeAt(position)
            if(position==-1){
                position+1
            }
            // remove from firstore

            try {
                mFireStore!!
                        .collection("Doctors")
                        .document(mAuth!!.currentUser!!.uid)
                        .collection("Requests")
                        .document(list[this.position!!-1].id)
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
            }catch (ex:Exception){
                Toast.makeText(context, ex.message, Toast.LENGTH_LONG)
                        .show()

            }
            notifyItemRemoved(position)
        }


        override fun getItemCount(): Int {

            return list.size

        }


        inner class CustomViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    }

    // implement the interface to SWIP in recycleview
    inner class Listener : RecycleItemTouchHelper.RecycleItemTouchHelperListenner {
        override fun onSwip(viewHolder: RecyclerView.ViewHolder?, direction: Int, position: Int) {

            if (viewHolder is AdapterRecycle.CustomViewHolder) {

                customAdapter!!.removeItem(viewHolder.adapterPosition)

            }

            // todo : ask to restore "UNDO" the remove by using snackBar

        }

    }



}
