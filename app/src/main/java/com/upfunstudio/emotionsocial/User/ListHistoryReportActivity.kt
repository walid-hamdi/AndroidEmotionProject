package com.upfunstudio.emotionsocial.User

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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
import com.upfunstudio.emotionsocial.Dr.MyDrTipsFragment
import com.upfunstudio.emotionsocial.Helper.RecycleItemTouchHelper
import com.upfunstudio.emotionsocial.R
import kotlinx.android.synthetic.main.fragment_recycleview.*
import kotlinx.android.synthetic.main.fragment_recycleview.view.*
import kotlinx.android.synthetic.main.history_report.view.*
import java.text.SimpleDateFormat
import java.util.*

class ListHistoryReportActivity : AppCompatActivity() {

    private var mFireStore: FirebaseFirestore? = null
    private var list: ArrayList<Report>? = null
    private var customAdapter: AdapterRecycle? = null
    private var mAuth: FirebaseAuth? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_recycleview)
        mFireStore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        /*
        get all recent activities (camera analyses, consults) with the specific time
        and you can clear it

         */

        list = ArrayList()
        customAdapter = AdapterRecycle(context = this, list = list!!)
        recycleview.layoutManager = LinearLayoutManager(this)
        recycleview.setHasFixedSize(true)
        recycleview.itemAnimator = DefaultItemAnimator()
        recycleview.addItemDecoration(DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL))

        recycleview.adapter = customAdapter
        prepareRecycleView()

        // to delete
        ItemTouchHelper(RecycleItemTouchHelper(0,
                ItemTouchHelper.LEFT, Listener()))
                .attachToRecyclerView(recycleview)


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


        // appear all user history report
        mFireStore!!.collection("Users")
                .document(mAuth!!.currentUser!!.uid)
                .collection("ReportDay")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->


                    try {
                        list!!.clear()
                        for (change in querySnapshot.documents) {
                            if (change.exists() && change != null) {

                                val id =  change.id


                                val typeEmotion = change.getString("emotion")
                                val tip = change.getString("tip")
                                val uid = change.getString("uid")

                                val fear = change.getDouble("fear")
                                val joy = change.getDouble("joy")
                                val anger = change.getDouble("anger")
                                val sadness = change.getDouble("sadness")
                                val surprise = change.getDouble("surprise")


                                var time = ""


                                val timeTips = change.get("time")
                                if (timeTips != null) {
                                    val dateTime = timeTips as Date
                                    val date = Date(dateTime.time)
                                    time = SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date)
                                }


                                if (!typeEmotion.isNullOrEmpty()
                                        && time != null
                                        && !tip.isNullOrEmpty()) {


                                    list!!.add(Report(id=id,
                                            emotion = typeEmotion,
                                            fear = fear,
                                            joy = joy,
                                            anger = anger,
                                            sadness = sadness,
                                            surprise = surprise,
                                            time = time,
                                            tip = tip,
                                            uid = uid))

                                        }

                            }
                            customAdapter!!.notifyDataSetChanged()



                        }

                    } catch (ex: Exception) {
                    }
                }


    }

    // class for adapting
    data class Report(val id:String,
                      val emotion: String,
                      val fear: Double,
                      val joy: Double,
                      val anger: Double,
                      val sadness: Double,
                      val surprise: Double,
                      val time: String,
                      val tip: String,
                      val uid: String)

    // appear list main of report
    inner class AdapterRecycle(val context: Context,
                               val list: ArrayList<Report>) :
            RecyclerView.Adapter<AdapterRecycle.CustomViewHolder>() {

        var position:Int?=null


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {

            val layoutInf = LayoutInflater.from(parent.context)
            val view = layoutInf.inflate(R.layout.history_report, parent, false)

            return CustomViewHolder(view)

        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {

            val item = list[position]
            this.position=position


            holder.itemView.typeEmotion.text = "Emotion : " + item.emotion
            holder.itemView.tip.text = "Tip : ${item.tip}"
            holder.itemView.time.text = item.time

            // to show part of the tip if it log
            if (holder.itemView.tip.text.length > 20) {
                val split = item.tip.substring(0, 19)
                holder.itemView.tip.text = split.plus(getString(R.string.read_more_msg))
            } else {
                holder.itemView.tip.text = item.tip
            }

            holder.itemView.setOnClickListener {

                val intent = Intent(context, HistoryReportDetails::class.java)
                // send the details and passing to appear for users
                intent.putExtra("emotion", item.emotion)
                intent.putExtra("tip", item.tip)
                intent.putExtra("time", item.time)

                intent.putExtra("fear", item.fear)
                intent.putExtra("joy", item.joy)
                intent.putExtra("sadness", item.sadness)
                intent.putExtra("surprise", item.surprise)
                intent.putExtra("anger", item.anger)




                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK)
                context.startActivity(intent)

            }


        }

        fun removeItem(position: Int) {

            // remove from list
            list.removeAt(position)
            if(position==-1){
                position+1
            }
            // remove from firstore

            try {
                mFireStore!!.collection("Users")
                        .document(mAuth!!.currentUser!!.uid)
                        .collection("ReportDay")
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
