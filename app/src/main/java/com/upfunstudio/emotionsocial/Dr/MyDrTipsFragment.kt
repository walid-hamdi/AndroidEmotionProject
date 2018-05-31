package com.upfunstudio.emotionsocial.Dr


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.upfunstudio.emotionsocial.Companion.TipContentActivity
import com.upfunstudio.emotionsocial.Helper.RecycleItemTouchHelper
import com.upfunstudio.emotionsocial.R
import kotlinx.android.synthetic.main.fragment_recycleview.view.*
import kotlinx.android.synthetic.main.list_tips.view.*
import java.text.SimpleDateFormat
import java.util.*


class MyDrTipsFragment : Fragment() {


    private var mAuth: FirebaseAuth? = null
    private var mFireStore: FirebaseFirestore? = null
    private var list: ArrayList<Tips>? = null
    private var customAdapter: AdapterRecycle? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
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


        mFireStore!!
                .collection("Tips")
                .whereEqualTo("doctorID", mAuth!!.currentUser!!.uid)
                .addSnapshotListener { querySnapshot, _ ->


                    try {
                        list!!.clear()
                        for (change in querySnapshot.documents) {
                            if (change.exists() && change != null) {


                                val id =  change.id


                                val contentTip = change.getString("contentTip")

                                var timeTip = ""

                                val timeTips = change.get("timeTip")
                                if (timeTips != null) {
                                    val dateTime = timeTips as Date
                                    val date = Date(dateTime.time)
                                    timeTip = SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date)
                                }

                                val titleTip = change.getString("titleTip")


                                if (!contentTip.isNullOrEmpty()
                                        && timeTip != null
                                        && !titleTip.isNullOrEmpty()) {


                                    list!!.add(Tips(id = id,
                                            contentTip = contentTip,
                                            // get specific server time
                                            time = timeTip,
                                            titleTip = titleTip))



                                }


                            } else {
                                Toast.makeText(context, getString(R.string.fail_ccx_msg),
                                        Toast.LENGTH_LONG).show()
                            }
                            customAdapter!!.notifyDataSetChanged()
                        }


                    } catch (ex: Exception) {
                    }

                }
    }

    // class for adapting
    data class Tips(val id: String,
                    val contentTip: String,
                    val time: String,
                    val titleTip: String)


    // appear list main of tips
    inner class AdapterRecycle(val context: Context,
                               val list: ArrayList<Tips>) :
            RecyclerView.Adapter<AdapterRecycle.CustomViewHolder>() {

        var position:Int?=null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {

            val layoutInf = LayoutInflater.from(parent.context)
            val view = layoutInf.inflate(R.layout.list_tips, parent, false)

            return CustomViewHolder(view)

        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            val item = list[position]
            this.position=position

            val title = holder.itemView.tipsTitle.text
            val times = holder.itemView.tipsTime.text


            try {


                if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(times)) {
                    // show  just a list of tips
                    if (title.length > 15) {

                        val slit = item.titleTip.substring(0, 14)
                        holder.itemView.tipsTitle.text = slit.plus("...")

                    } else {

                        holder.itemView.tipsTitle.text = item.titleTip

                    }

                    holder.itemView.tipsTime.text = item.time

                } else {

                    holder.view.visibility = View.GONE

                }
            } catch (ex: Exception) {

                Toast.makeText(context, getString(R.string.error_cx_msg),
                        Toast.LENGTH_LONG).show()

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
                        .collection("Tips")
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