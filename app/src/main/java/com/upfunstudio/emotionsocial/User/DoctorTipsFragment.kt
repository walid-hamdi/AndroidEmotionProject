package com.upfunstudio.emotionsocial.User

import android.app.SearchManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.upfunstudio.emotionsocial.Companion.LoginActivity
import com.upfunstudio.emotionsocial.Companion.TipContentActivity
import com.upfunstudio.emotionsocial.Companion.WindowAnalyseOrCalling
import com.upfunstudio.emotionsocial.R
import kotlinx.android.synthetic.main.all_tips.view.*
import kotlinx.android.synthetic.main.fragment_recycleview.view.*
import java.text.SimpleDateFormat
import java.util.*


class DoctorTipsFragment : Fragment() {


    private var mAuth: FirebaseAuth? = null
    private var mFireStore: FirebaseFirestore? = null
    private var list: ArrayList<Any>? = null
    private var customAdapter: AdapterRecycle? = null
    private var sqlLiteFavorite: SqlLiteFavorite? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_recycleview,
                container, false)


        // prepareInstances
        mFireStore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        sqlLiteFavorite = SqlLiteFavorite(this.context!!)
        setHasOptionsMenu(true)


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

        if (mAuth!!.currentUser != null) {

            activity!!.menuInflater.inflate(R.menu.main_menu, menu)

            val searchView = MenuItemCompat.getActionView(menu!!.findItem(R.id.app_bar_search)) as SearchView

            val searchManager = activity!!.getSystemService(Context.SEARCH_SERVICE)
                    as SearchManager

            searchView.setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))


            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(text: String): Boolean {

                    searchTip(text.substring(0, 1).toUpperCase() + text.substring(1))

                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {


                    return false
                }
            })


        } else {

            activity!!.menuInflater.inflate(R.menu.user_mai, menu)
            val searchView = MenuItemCompat.getActionView(menu!!.findItem(R.id.search_user)) as SearchView

            val searchManager = activity!!.getSystemService(Context.SEARCH_SERVICE)
                    as SearchManager

            searchView.setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))


            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(text: String): Boolean {

                    // weh typing to search for a dr depend his name
                    searchTip(text.substring(0, 1).toUpperCase() + text.substring(1))

                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {


                    return false
                }
            })

        }

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
                    getString(R.string.check_ccx_first),
                    Toast.LENGTH_LONG).show()
        }


    }

    private fun searchTip(tipTitle: String) {


        mFireStore!!
                .collection("Tips")
                .whereEqualTo("titleTip", tipTitle)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                    list!!.clear()
                    try {
                        for (change in querySnapshot.documents) {
                            if (change.exists() && querySnapshot != null) {


                                var timeTip = ""
                                val timeTips = change.get("timeTip")
                                if (timeTips != null) {
                                    val dateTime = timeTips as Date
                                    val date = Date(dateTime.time)
                                    timeTip = SimpleDateFormat("yyyy/MM/dd").format(date)
                                }
                                val contentTip = change
                                        .getString("contentTip")
                                val titleTip = change
                                        .getString("titleTip")
                                val username = change
                                        .getString("username")
                                if (!contentTip.isNullOrEmpty()
                                        && timeTip != null
                                        && !titleTip.isNullOrEmpty()) {

                                    list!!.add(TipsForUsers(
                                            contentTip = contentTip,
                                            time = timeTip,
                                            titleTip = titleTip,
                                            username = username))


                                    customAdapter!!.notifyDataSetChanged()


                                }
                            }
                        }


                    } catch (ex: Exception) {
                    }


                }
    }


    private fun prepareRecycleView() {

        // get all doctors tip

        // get all tip from all drs
        mFireStore!!
                .collection("Tips")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->


                    try {
                        for (change in querySnapshot.documents) {
                            if (change.exists() && querySnapshot != null) {


                                var timeTip = ""
                                val timeTips = change.get("timeTip")
                                if (timeTips != null) {
                                    val dateTime = timeTips as Date
                                    val date = Date(dateTime.time)
                                    timeTip = SimpleDateFormat("yyyy/MM/dd").format(date)
                                }
                                val contentTip = change.getString("contentTip")
                                val titleTip = change.getString("titleTip")
                                val username = change.getString("username")

                                if (!contentTip.isNullOrEmpty()
                                        && timeTip != null
                                        && !titleTip.isNullOrEmpty()) {

                                    list!!.add(TipsForUsers(
                                            contentTip = contentTip,
                                            time = timeTip,
                                            titleTip = titleTip,
                                            username = username))


                                    customAdapter!!.notifyDataSetChanged()


                                }
                            }
                        }


                    } catch (ex: Exception) {
                        Toast.makeText(context, "${ex.message}",
                                Toast.LENGTH_LONG).show()
                    }


                }
    }


    // appear list main of drs for users
    inner class AdapterRecycle(val context: Context,
                               private val list: List<Any>) :
            RecyclerView.Adapter<AdapterRecycle.CustomViewHolder>() {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {


            val layoutInf = LayoutInflater.from(parent.context)
            val view = layoutInf.inflate(R.layout.all_tips, parent, false)

            return CustomViewHolder(view)


        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {

            val item = list[position] as TipsForUsers
            try {

                // full the title for all items for tip
                holder.itemView.titleAlltips.text = item.titleTip
                // full the title and show just first 20 letters
                if (holder.itemView.titleAlltips.text.length > 15) {
                    val split = item.titleTip.substring(0, 14)
                    holder.itemView.titleAlltips.text = split.plus("...")
                } else {
                    holder.itemView.titleAlltips.text = item.titleTip
                }
                // full the publisher doctor for this tip
                holder.itemView.publishedBy.text = "By ".plus(item.username)
                // full the time stamp get it from server
                holder.itemView.dateAllTips.text = item.time
            } catch (ex: Exception) {

            }

            // add to fav list
            holder.itemView.addFavButton.setOnClickListener {


                val values = ContentValues()
                values.put("title", item.titleTip)
                values.put("description", item.contentTip)
                values.put("time", item.time)
                values.put("published", item.username)
                // check if item exist already or not the fav

                try {

                    if (SqlLiteFavorite.itemExits!!) {
                        Toast.makeText(context, getString(R.string.you_add_already_msg),
                                Toast.LENGTH_SHORT).show()

                    } else {
                        val result = sqlLiteFavorite!!.storeFavData(values)
                        if (result > 0) {
                            Toast.makeText(context, getString(R.string.add_to_fav_msg),
                                    Toast.LENGTH_SHORT).show()

                        } else {
                            Toast.makeText(activity, getString(R.string.somethig_wor_msg),
                                    Toast.LENGTH_SHORT).show()

                        }
                    }
                } catch (ex: Exception) {

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


        override fun getItemCount(): Int {

            return list.size

        }


        inner class CustomViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    }


}

// class for adapting
data class TipsForUsers(val contentTip: String,
                        val time: String,
                        val titleTip: String,
                        val username: String)




