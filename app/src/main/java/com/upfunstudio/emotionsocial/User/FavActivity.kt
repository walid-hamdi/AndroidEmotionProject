package com.upfunstudio.emotionsocial.User

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.upfunstudio.emotionsocial.Companion.TipContentActivity
import com.upfunstudio.emotionsocial.R
import kotlinx.android.synthetic.main.activity_fav.*
import kotlinx.android.synthetic.main.all_tips.view.*
import java.util.*

class FavActivity : AppCompatActivity() {
    private var list: ArrayList<TipsForUsers>? = null
    private var customAdapter: AdapterRecycle? = null
    private var sqlLiteFavorite: SqlLiteFavorite? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fav)

        list = ArrayList()
        customAdapter = AdapterRecycle(context = this, list = list!!)
        recycleFav.layoutManager = LinearLayoutManager(this)
        recycleFav.setHasFixedSize(true)
        recycleFav.adapter = customAdapter
        sqlLiteFavorite = SqlLiteFavorite(this)
        appearData("%")

        // todo : delete from SqLlite when swap left item


    }

    override fun onStart() {
        super.onStart()
        customAdapter!!.notifyDataSetChanged()

    }

    override fun onRestart() {
        super.onRestart()
        customAdapter!!.notifyDataSetChanged()

    }

    override fun onResume() {
        super.onResume()
        customAdapter!!.notifyDataSetChanged()

    }

    private fun appearData(title: String) {

        // retrieve data from SqlLite
        val sql = SqlLiteFavorite(this)


        val projection = arrayOf("title", "description", "time", "published")
        val selection = "title like ?"
        val selectionArgs = arrayOf(title)

        val cursor =
                sql.retriveFavData(
                        projection = projection,
                        selection = selection,
                        selectionArgs = selectionArgs,
                        orderBy = "title")

        list!!.clear()
        if (cursor.moveToFirst()) {
            do {
                val title = cursor.getString(cursor.getColumnIndex("title"))
                val des = cursor.getString(cursor.getColumnIndex("description"))
                val time = cursor.getString(cursor.getColumnIndex("time"))
                val published = cursor.getString(cursor.getColumnIndex("published"))

                list!!.add(TipsForUsers(
                        titleTip = title,
                        contentTip = des,
                        time = time,
                        username = published
                ))

            } while (cursor.moveToNext())
        }
        customAdapter!!.notifyDataSetChanged()



    }

    // class for adapting
    data class TipsForUsers(val contentTip: String,
                            val time: String,
                            val titleTip: String,
                            val username: String)


    // appear list main of drs for users
    inner class AdapterRecycle(val context: Context,
                               private val list: List<TipsForUsers>) :
            RecyclerView.Adapter<AdapterRecycle.CustomViewHolder>() {


        override fun getItemCount(): Int {

            return list.size

        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CustomViewHolder {
            val layoutInf = LayoutInflater.from(parent?.context)
            val view = layoutInf.inflate(R.layout.all_tips, parent, false)

            return CustomViewHolder(view)


        }

        override fun onBindViewHolder(holder: CustomViewHolder?, position: Int) {

            val item = list[position]


            // full the title for all items for tip
            holder!!.itemView.titleAlltips.text = item.titleTip
            // full the title and show just first 20 letters
            if (holder!!.itemView.titleAlltips.text.length > 20) {
                val split = item.contentTip.substring(0, 19)
                holder!!.itemView.titleAlltips.text = split.plus("...")
            } else {
                holder!!.itemView.titleAlltips.text = item.contentTip
            }
            // full the publisher doctor for this tip
            holder.itemView.publishedBy.text = "By ".plus(item.username)
            // full the time stamp get it from server
            holder.itemView.dateAllTips.text = item.time


            // add to fav list
            holder!!.itemView.addFavButton.visibility = View.GONE

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


        inner class CustomViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    }


}
