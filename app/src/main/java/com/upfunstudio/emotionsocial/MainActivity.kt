package com.upfunstudio.emotionsocial

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.list_of_drs.view.*


class MainActivity : AppCompatActivity() {


    var firebaseAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        firebaseAuth = FirebaseAuth.getInstance()

        recycleview.layoutManager = LinearLayoutManager(this)
        recycleview.setHasFixedSize(true)
        recycleview.adapter = AdapterRecycle(this)


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.main_menu, menu)

        return true
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.profileMenu -> {

                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)


            }
            R.id.AnalyseMenu -> {

                val fr = FragmentDialoge()
                val bundle = Bundle()
                // to know the analyse dialoge from main or form login & register
                val placeActivity = "MainActivity"
                bundle.putString("placeActivity", placeActivity)
                fr.arguments = bundle
                val frman = fragmentManager
                fr.show(frman, "Show")

            }
            R.id.logoutMenu -> {

                // todo : logout from system
                firebaseAuth!!.signOut()
                checkUser()


            }
            else -> {
                return super.onOptionsItemSelected(item)

            }


        }

        return true
    }

    fun checkUser() {
        if (firebaseAuth!!.currentUser == null) {
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
        }
    }


    class AdapterRecycle(val context: Context) : RecyclerView.Adapter<AdapterRecycle.CustomViewHolder>() {

        val listNamesDr = arrayListOf<String>("Ahmed Ali", "Walid Hamdi", "Ali Saif")
        val listCitiesDr = arrayListOf<String>("Kaiouan", "Nebel", "Sidi bouzid")
        val listPicturesDr = arrayListOf<Int>(R.drawable.dr_photo, R.drawable.dr_photo, R.drawable.dr_photo)

        override fun getItemCount(): Int {

            return listNamesDr.size

        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CustomViewHolder {
            val layoutInf = LayoutInflater.from(parent?.context)
            val view = layoutInf.inflate(R.layout.list_of_drs, parent, false)

            return CustomViewHolder(view)


        }

        override fun onBindViewHolder(holder: CustomViewHolder?, position: Int) {

            holder!!.itemView.fullNameMainText.setText(listNamesDr[position])
            holder!!.itemView.cityMainText.setText(listCitiesDr[position])
            holder!!.itemView.pictureMain.setImageResource(listPicturesDr[position])

            holder!!.itemView.layoutDrList.setOnClickListener {
                val intent = Intent(context, DoctorProfileActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK)
                context.startActivity(intent)


            }

        }


        class CustomViewHolder(val view: View) : RecyclerView.ViewHolder(view) {


        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }


}
