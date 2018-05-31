package com.upfunstudio.emotionsocial.User

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.squareup.picasso.Picasso
import com.upfunstudio.emotionsocial.Companion.LoginActivity
import com.upfunstudio.emotionsocial.Companion.WindowAnalyseOrCalling
import com.upfunstudio.emotionsocial.Helper.RecycleItemTouchHelper
import com.upfunstudio.emotionsocial.R
import kotlinx.android.synthetic.main.fragment_recycleview.*
import kotlinx.android.synthetic.main.fragment_recycleview.view.*
import kotlinx.android.synthetic.main.recent_consults_ticket.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class RecentConsultsWithDrFragment : Fragment() {

    private var mAuth: FirebaseAuth? = null
    private var mFireStore: FirebaseFirestore? = null
    private var list: ArrayList<Consult>? = null
    private var customAdapter: AdapterRecycle? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        mFireStore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        setHasOptionsMenu(true)


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

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {

        activity!!.menuInflater.inflate(R.menu.main_menu, menu)
        // search for dr deped his name

        val searchView = MenuItemCompat.getActionView(menu!!.findItem(R.id.app_bar_search)) as SearchView

        val searchManager = activity!!.getSystemService(Context.SEARCH_SERVICE)
                as SearchManager

        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))


        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(text: String): Boolean {

                searchDrCosult(text.substring(0, 1).toUpperCase() + text.substring(1))
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {


                return false
            }
        })

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.profileMenu -> {

                val intent = Intent(activity, SettingsUserActivity::class.java)
                startActivity(intent)


            }
            R.id.fav -> {

                startActivity(Intent(activity, FavActivity::class.java))

            }
            R.id.AnalyseMenu -> {


                val fr = WindowAnalyseOrCalling()
                val bundle = Bundle()
                // to know the analyse dialogue from main or form login & register
                val placeActivity = "MainActivity"
                bundle.putString("placeActivity", placeActivity)
                fr.arguments = bundle
                fr.show(activity!!.fragmentManager, "Show")


            }
            R.id.logoutMenu -> {

                try {

                    mAuth!!.signOut()
                    checkUser()

                } catch (ex: Exception) {
                }

            }

            else -> {
                return super.onOptionsItemSelected(item)

            }


        }

        return true
    }

    private fun checkUser() {

        // check for user
        if (mAuth!!.currentUser == null) {
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
        } else {
            Toast.makeText(activity,
                    getString(R.string.check_first),
                    Toast.LENGTH_LONG).show()
        }


    }


    private fun searchDrCosult(search: String) {


        // search depend dr name
        mFireStore!!.collection("Users")
                .document(mAuth!!.currentUser!!.uid)
                .collection("Consults")
                .whereEqualTo("doctorName", search)
                .addSnapshotListener { querySnapshot, _ ->

                    try {
                        list!!.clear()
                        for (change in querySnapshot.documents) {

                            if (change.exists() && change != null) {

                                val id = change.id


                                val username = change.getString("doctorName")
                                val drPic = change.getString("doctorPic")
                                val drID = change.getString("doctorID")


                                var time = ""

                                val timeConsult = change.get("timeConsult")
                                if (timeConsult != null) {
                                    val dateTime = timeConsult as Date
                                    val date = Date(dateTime.time)
                                    time = SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date)
                                }



                                if (username != null
                                        && !time.isEmpty()) {


                                    list!!.add(Consult(
                                            id = id,
                                            doctorID = drID,
                                            doctorName = username,
                                            doctorPic = drPic,
                                            timeConsult = time))

                                }

                            }
                            customAdapter!!.notifyDataSetChanged()

                        }
                    } catch (ex: Exception) {
                    }


                }


    }


    private fun prepareRecycleView() {



        mFireStore!!.collection("Users")
                .document(mAuth!!.currentUser!!.uid)
                .collection("Consults")
                .addSnapshotListener { querySnapshot, _ ->

                    try {
                        list!!.clear()
                        for (change in querySnapshot.documents) {

                            if (change.exists() && change != null) {


                                val id = change.id

                                val username = change.getString("doctorName")
                                val drPic = change.getString("doctorPic")
                                val drID = change.getString("doctorID")


                                var time = ""

                                val timeConsult = change.get("timeConsult")
                                if (timeConsult != null) {
                                    val dateTime = timeConsult as Date
                                    val date = Date(dateTime.time)
                                    time = SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date)
                                }



                                if (username != null
                                        && !time.isEmpty()) {


                                    list!!.add(Consult(
                                            id = id,
                                            doctorID = drID,
                                            doctorName = username,
                                            doctorPic = drPic,
                                            timeConsult = time))

                                }

                            }
                            customAdapter!!.notifyDataSetChanged()


                        }
                    } catch (ex: Exception) {
                    }


                }


    }

    // class for adapting
    data class Consult(val id: String="",
                       val doctorID: String="",
                       val doctorName: String="",
                       val doctorPic: String="",
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
                mFireStore!!.collection("Users")
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



        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            val item = list[position]
            this.position = position


            holder.itemView.fullNameConsult.text = "Consulted with Dr ${item.doctorName}"
            holder.itemView.timeConsult.text = item.timeConsult


            if (item.doctorPic.isEmpty()) {
                Picasso.with(context).load(R.drawable.profile_photo)
                        .into(holder.itemView.pictureCosulter)

            } else {
                Picasso.with(context).load(item.doctorPic)
                        .placeholder(R.drawable.profile_photo)
                        .into(holder.itemView.pictureCosulter)


            }

            // go to the doctor profile
            holder.itemView.pictureCosulter
                    .setOnClickListener {

                        val intent = Intent(context, DoctorProfileAppearForUserActivity::class.java)
                        // send the details and passing into profile dr appear for users

                        intent.putExtra("fromConsultInfo", true)
                        intent.putExtra("doctorID", item.doctorID)

                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        context.startActivity(intent)


                    }


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