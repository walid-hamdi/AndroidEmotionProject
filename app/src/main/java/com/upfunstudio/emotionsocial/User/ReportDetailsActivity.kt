package com.upfunstudio.emotionsocial.User

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.upfunstudio.emotionsocial.Companion.LoginActivity
import com.upfunstudio.emotionsocial.R
import kotlinx.android.synthetic.main.activity_report_details.*
import kotlinx.android.synthetic.main.list_suggest_drs.view.*
import java.util.*


class ReportDetailsActivity : AppCompatActivity() {

    private var firebaseAuth: FirebaseAuth? = null
    private var list: ArrayList<Doctor>? = null
    private var fireStore: FirebaseFirestore? = null
    private var customAdapter: AdapterRecycleSuggest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_details)
        firebaseAuth = FirebaseAuth.getInstance()
        fireStore = FirebaseFirestore.getInstance()


        appearTipsDependAnalyse()
        appearSuggestDrs()


    }

    override fun onStart() {
        super.onStart()

        customAdapter!!.notifyDataSetChanged()
    }

    private fun appearTipsDependAnalyse() {

        val arr = ArrayList<String>()





        fireStore!!
                .collection("Doctors")
                .document("IXCK6eH6NlbONFPxG8OMWtyiLsu2")
                .collection("Tips")
                .addSnapshotListener { documentSnapshots, _ ->

                    arr.clear()
                    for (change in documentSnapshots.documentChanges) {

                        if (change.document.exists()) {

                            val contentTip = change
                                    .document
                                    .getString("contentTip")
                            arr.add(contentTip)
                            val random = rand(0, arr.size)
                            tip.text = arr[random]



                        }


                    }


                }


    }

    private val random = Random()
    private fun rand(from: Int, to: Int): Int {
        return random.nextInt(to - from) + from
    }


    private fun appearSuggestDrs() {

        // appear doctor list horizontally
        viewPagerDoctorSuggest.layoutManager = LinearLayoutManager(applicationContext,
                LinearLayoutManager.HORIZONTAL, false)
        viewPagerDoctorSuggest.setHasFixedSize(true)
        // prepare arrayList and adapter
        list = ArrayList()
        customAdapter = AdapterRecycleSuggest(this, list!!)
        viewPagerDoctorSuggest.adapter = customAdapter


        // get data from firestore and store it in arrayList
        fireStore!!
                .collection("Doctors")
                .addSnapshotListener { documentSnapshots, _ ->


                    for (change in documentSnapshots.documentChanges) {


                        if (change.type == DocumentChange.Type.ADDED) {


                            if (change.document.exists()) {
                                list!!.add(Doctor(username = change.document.getString("username"),
                                        specialityText = change.document.getString("specialityText"),
                                        picPath = change.document.getString("picPath"),
                                        language = change.document.getString("language"),
                                        state = change.document.getBoolean("state"),
                                        phone = change.document.getString("phone"),
                                        mail = change.document.getString("mail"),
                                        city = change.document.getString("city")))
                            } else {
                                list!!.add(Doctor())
                            }

                            customAdapter!!.notifyDataSetChanged()


                        }


                    }


                }


    }

    fun buCancelReport(view: View) {
        Toast.makeText(this, "Report Canceled!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)


    }

    fun buSaveReport(view: View) {
        // todo : change it later by saving report data (graph,type emotion and tips) in firebase
        Toast.makeText(this, "Report saved!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.report_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.profileMenu -> {

                // go to profile user for setting
                val intent = Intent(this, SettingsUserActivity::class.java)
                startActivity(intent)


            }

            R.id.logout -> {

                // logout from system
                firebaseAuth!!.signOut()
                checkUser()


            }
            else -> {
                return super.onOptionsItemSelected(item)

            }


        }

        return true
    }

    private fun checkUser() {
        if (firebaseAuth!!.currentUser == null) {
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    inner class AdapterRecycleSuggest(val context: Context,
                                      private val list: List<Doctor>) :
            RecyclerView.Adapter<AdapterRecycleSuggest.CustomViewHolder>() {


        override fun getItemCount(): Int {

            return list.size

        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CustomViewHolder {
            val layoutInf = LayoutInflater.from(parent?.context)
            val view = layoutInf.inflate(R.layout.list_suggest_drs, parent, false)

            return CustomViewHolder(view)


        }

        override fun onBindViewHolder(holder: CustomViewHolder?, position: Int) {

            val item = list[position]


            if (item.state) {
                if (item.picPath.isNullOrEmpty()) {
                    Picasso.with(context).load(R.drawable.profile_photo)
                            .into(holder!!.itemView.pictureSuggest)

                } else {
                    Picasso.with(context).load(item.picPath)
                            .placeholder(R.drawable.profile_photo)
                            .into(holder!!.itemView.pictureSuggest)

                }
            } else {
                // to hide list of drs not online :D
                holder!!.itemView.visibility = View.GONE

            }





            holder.itemView.pictureSuggest.setOnClickListener {
                val intent = Intent(context, DoctorProfileAppearForUserActivity::class.java)
                // send the details and passing into profile dr appear for users
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

    // class for adapting
    data class Doctor(val uid: String = "", val username: String = "default name", val city: String = "default city",
                      val language: String = "default language", val phone: String = "default phone",
                      val picPath: String = "default path", val mail: String = "default mail",
                      val specialityText: String = "default speciality", var state: Boolean = false)


}
