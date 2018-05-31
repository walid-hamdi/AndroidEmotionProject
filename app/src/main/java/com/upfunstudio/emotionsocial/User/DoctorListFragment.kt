package com.upfunstudio.emotionsocial.User


import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.upfunstudio.emotionsocial.Companion.LoginActivity
import com.upfunstudio.emotionsocial.Companion.WindowAnalyseOrCalling
import com.upfunstudio.emotionsocial.R
import kotlinx.android.synthetic.main.fragment_recycleview.view.*
import kotlinx.android.synthetic.main.list_of_drs.view.*
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.DefaultItemAnimator




class DoctorListFragment : Fragment() {


    private var mAuth: FirebaseAuth? = null
    private var mFireStore: FirebaseFirestore? = null
    private var list: ArrayList<Doctor>? = null
    private var customAdapter: AdapterRecycle? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


        val v = inflater.inflate(R.layout.fragment_recycleview,
                container, false)


        mFireStore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        setHasOptionsMenu(true)



        list = ArrayList()

        prepareRecycleView()
        customAdapter = AdapterRecycle(context = container!!.context, list = list!!)
        v.recycleview.layoutManager = LinearLayoutManager(this.activity!!, LinearLayoutManager.VERTICAL, false)
        v.recycleview.setHasFixedSize(true)
        v.recycleview.itemAnimator = DefaultItemAnimator()
        v.recycleview.addItemDecoration(DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL))
        v.recycleview.adapter = customAdapter





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

                    // weh typing to search for a dr depend his name
                    searchDr(text.substring(0, 1).toUpperCase() + text.substring(1))

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
                    searchDr(text.substring(0, 1).toUpperCase() + text.substring(1))

                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {


                    return false
                }
            })

        }
        // search for dr deped his name


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
                    getString(R.string.check_cx_first),
                    Toast.LENGTH_LONG).show()

        }


    }


    private fun prepareRecycleView() {
        // get doctors and appear in list main
        // todo : handle real time state later


        mFireStore!!
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

    private fun searchDr(search: String) {
        // get doctors and appear in list main


        mFireStore!!
                .collection("Doctors")
                .whereEqualTo("username", search)
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


    // appear list main of drs for users
    inner class AdapterRecycle(val context: Context,
                               private val list: List<Doctor>) :
            RecyclerView.Adapter<AdapterRecycle.CustomViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            val layoutInf = LayoutInflater.from(parent.context)


            val view = layoutInf.inflate(R.layout.list_of_drs, parent, false)


            return CustomViewHolder(view)


        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {


            val item = list[position]


            /*
             show  a list of doctors that exist in the system
             and updated they're information
              */


            holder.itemView.fullNameMainText.text = item.username
            holder.itemView.cityMainText.text = item.city


            // use picasso lib
            if (item.picPath.isEmpty()) {
                Picasso.with(context).load(R.drawable.profile_photo)
                        .into(holder.itemView.pictureMain)

            } else {
                Picasso.with(context).load(item.picPath)
                        .placeholder(R.drawable.profile_photo)
                        .into(holder.itemView.pictureMain)


            }

            if (item.state) {
                holder.itemView.iconState.setBackgroundResource(R.drawable.ic_video_online_24dp)

            } else {
                holder.itemView.iconState.setBackgroundResource(R.drawable.ic_video_ofline_24dp)

            }



            holder.itemView.layoutDrList.setOnClickListener {
                if (mAuth!!.currentUser == null) {

                    val builder = AlertDialog.Builder(activity)
                            .setMessage(getString(R.string.use_accout_msg))
                            .setNegativeButton(getString(R.string.use_accout_text), { dialog, which ->

                                startActivity(Intent(activity, LoginActivity::class.java))
                                activity!!.finish()

                            })
                            .setPositiveButton(getString(R.string.get_ack), { dialog, which ->
                                dialog.dismiss()


                            })
                    builder.show()


                } else {

                    val intent = Intent(context, DoctorProfileAppearForUserActivity::class.java)
                    // send the details and passing into profile dr appear for users
                    intent.putExtra("doctorID", item.uid)

                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    context.startActivity(intent)


                }


            }

        }

        override fun getItemCount(): Int {

            return list.size

        }


        inner class CustomViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    }


}
