package com.upfunstudio.emotionsocial.User

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.Toast
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.upfunstudio.emotionsocial.Companion.LoginActivity
import com.upfunstudio.emotionsocial.R
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_report_details.*
import kotlinx.android.synthetic.main.fragment_recycleview.view.*
import kotlinx.android.synthetic.main.list_suggest_drs.view.*
import java.util.*
import kotlin.collections.ArrayList


class ReportDetailsActivity : AppCompatActivity() {

    private var firebaseAuth: FirebaseAuth? = null
    private var list: ArrayList<Doctor>? = null
    private var fireStore: FirebaseFirestore? = null
    private var customAdapter: AdapterRecycleSuggest? = null
    private var emotion: String? = null


    private var anger: Double = 0.0
    private var fear: Double = 0.0
    private var joy: Double = 0.0
    private var sadness: Double = 0.0
    private var surprise: Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_details)
        firebaseAuth = FirebaseAuth.getInstance()
        fireStore = FirebaseFirestore.getInstance()

        // todo : add more details ask questios to get accurate result
        fullChartState()
        appearEmotion()
        appearTipsDependAnalyse()
        appearSuggestDrs()


    }

    override fun onStart() {
        super.onStart()

        customAdapter!!.notifyDataSetChanged()
    }


    private fun fullChartState() {

        try {
            // gettig data
            anger = intent.extras.getDouble("anger")
            fear = intent.extras.getDouble("fear")
            joy = intent.extras.getDouble("joy")
            sadness = intent.extras.getDouble("sadness")
            surprise = intent.extras.getDouble("surprise")

            chartPie.setUsePercentValues(true)
            chartPie.description.isEnabled = false
            chartPie.setExtraOffsets(5f, 10f, 5f, 5f)
            chartPie.dragDecelerationFrictionCoef = 0.99f
            chartPie.isDrawHoleEnabled = true
            chartPie.setHoleColor(Color.WHITE)
            chartPie.transparentCircleRadius = 61f

            val arr = ArrayList<PieEntry>()
            arr.add(PieEntry(anger.toFloat() * 10, getString(R.string.anger_type)))
            arr.add(PieEntry(fear.toFloat() * 10, getString(R.string.fear_type)))
            arr.add(PieEntry(joy.toFloat() * 10, getString(R.string.joy_type)))
            arr.add(PieEntry(sadness.toFloat() * 10, getString(R.string.sadanger_tyoe)))
            arr.add(PieEntry(surprise.toFloat() * 10, getString(R.string.surprise_type)))

            chartPie.animateY(1000, Easing.EasingOption.EaseInCubic)

            val set = PieDataSet(arr, getString(R.string.emotio_type))
            set.sliceSpace = 1.5f
            set.selectionShift = 5f
            set.colors = ColorTemplate.MATERIAL_COLORS.toMutableList()

            val data = PieData(set)
            data.setValueTextSize(10f)
            data.setValueTextColor(Color.WHITE)

            chartPie.data = data


        } catch (ex: Exception) {


        }


    }


    private fun appearEmotion() {

        try {
            emotion = intent.extras.getString("emotion")

            //emotion = "Anger"

            if (!emotion!!.isEmpty()) {
                emotioType.text = getString(R.string.emotio_msg) + emotion
            } else {
                emotion = getString(R.string.default_emotio)
                emotioType.text = getString(R.string.emotio_msg) + emotion
            }


        } catch (ex: Exception) {

        }
    }

    private fun appearTipsDependAnalyse() {

        val arr = ArrayList<String>()


        // get random tip to show it in specific report depend analyse
        fireStore!!
                .collection("Tips")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        arr.clear()
                        if (task.result != null) {

                            for (change in task.result.documentChanges) {
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
        viewPagerDoctorSuggest.itemAnimator = DefaultItemAnimator()
        viewPagerDoctorSuggest.addItemDecoration(DividerItemDecoration(applicationContext,
                DividerItemDecoration.VERTICAL))

        // prepare arrayList and adapter
        list = ArrayList()
        customAdapter = AdapterRecycleSuggest(this, list!!)
        viewPagerDoctorSuggest.adapter = customAdapter


        // get data from firestore and store it in arrayList
        fireStore!!
                .collection("Doctors")
                .addSnapshotListener { querySnapshot, _ ->

                    try {
                        list!!.clear()

                        for (change in querySnapshot.documents) {

                            if (change.exists() && change != null) {

                                // use all items and add those to arrlist
                                val items = change.toObject(Doctor::class.java)
                                list!!.add(items)


                            }


                        }
                        customAdapter!!.notifyDataSetChanged()


                    } catch (ex: Exception) {

                    }

                }


    }

    fun buCancelReport(view: View) {
        Toast.makeText(this, getString(R.string.report_cacel_msg), Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)


    }

    fun buSaveReport(view: View) {
        // saving report data (type emotion and tips) in firebase
        if (firebaseAuth!!.currentUser != null) {

            if (emotion != null) {
                val reportInfo = HashMap<String, Any>()
                reportInfo["uid"] = firebaseAuth!!.currentUser!!.uid
                reportInfo["emotion"] = emotion!!

                reportInfo["tip"] = tip.text
                reportInfo["joy"] = joy
                reportInfo["fear"] = fear
                reportInfo["anger"] = anger
                reportInfo["sadness"] = sadness
                reportInfo["surprise"] = surprise

                reportInfo["time"] = FieldValue.serverTimestamp()

                val loading = SpotsDialog(this)
                loading.setTitle("Loading to add report...")
                loading.setCanceledOnTouchOutside(false)
                loading.show()

                fireStore!!.collection("Users")
                        .document(firebaseAuth!!.currentUser!!.uid)
                        .collection("ReportDay")
                        .add(reportInfo as Map<String, String>)
                        .addOnCompleteListener { task ->

                            if (task.isSuccessful) {

                                loading.dismiss()
                                Toast.makeText(this, getString(R.string.repore_saved), Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)

                            } else {
                                loading.hide()
                                Toast.makeText(this, getString(R.string.some_we_msg), Toast.LENGTH_SHORT).show()

                            }


                        }


            } else {
                Toast.makeText(this, getString(R.string.you_do_have_complet_report),
                        Toast.LENGTH_LONG).show()

            }


        } else {
            if (firebaseAuth!!.currentUser == null) {

                val builder = AlertDialog.Builder(this)
                        .setMessage(getString(R.string.use_accout_msg))
                        .setNegativeButton(getString(R.string.use_accout_text), { dialog, which ->

                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()

                        })
                        .setPositiveButton(getString(R.string.get_ack), { dialog, which ->
                            dialog.dismiss()


                        })
                builder.show()
                // register to allow save report

            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        if (firebaseAuth!!.currentUser != null)
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
        } else {
            Toast.makeText(this,
                    getString(R.string.check_first),
                    Toast.LENGTH_LONG).show()
        }
    }

    inner class AdapterRecycleSuggest(val context: Context,
                                      private val list: List<Doctor>) :
            RecyclerView.Adapter<AdapterRecycleSuggest.CustomViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            val layoutInf = LayoutInflater.from(parent.context)
            val view = layoutInf.inflate(R.layout.list_suggest_drs, parent, false)

            return CustomViewHolder(view)


        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {

            val item = list[position]


            if (item.picPath.isEmpty()) {
                Picasso.with(context).load(R.drawable.profile_photo)
                        .into(holder.itemView.pictureSuggest)

            } else {
                Picasso.with(context).load(item.picPath)
                        .placeholder(R.drawable.profile_photo)
                        .into(holder.itemView.pictureSuggest)

            }
            /*
             todo  : later display only online drs
                         if (item.state) {
            } else {
                // to hide list of drs not online :D
                holder!!.itemView.visibility = View.GONE

            }

              */






            holder.itemView.pictureSuggest.setOnClickListener {


                // if the user entered with his account already
                if (firebaseAuth!!.currentUser != null) {


                    val intent = Intent(context, DoctorProfileAppearForUserActivity::class.java)
                    // send the details and passing into profile dr appear for users

                    intent.putExtra("fromReport", true)
                    intent.putExtra("doctorID", item.uid)


                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    context.startActivity(intent)


                    // if the user entered as anonymose user
                } else {


                    AlertDialog.Builder(context)
                            .setMessage(getString(R.string.use_accout_msg))
                            .setNegativeButton(getString(R.string.use_accout_text), { dialog, which ->

                                startActivity(Intent(context, LoginActivity::class.java))
                                finish()

                            })
                            .setPositiveButton(getString(R.string.get_ack), { dialog, which ->
                                dialog.dismiss()


                            }).show()
                }


            }

        }


        override fun getItemCount(): Int {

            return list.size

        }


        inner class CustomViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    }

    // class for adapting
    data class Doctor(val uid: String = "",
                      val username: String = "",
                      val city: String = "",
                      val language: String = "",
                      val phone: String = "",
                      val picPath: String = "",
                      val mail: String = "",
                      val specialityText: String = "",
                      var state: Boolean = false,
                      var connectionNow: Boolean = false,
                      var request: Boolean = false)

}
