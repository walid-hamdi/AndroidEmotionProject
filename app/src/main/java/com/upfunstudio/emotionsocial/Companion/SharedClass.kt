package com.upfunstudio.emotionsocial.Companion

import android.content.Context
import android.content.SharedPreferences

class SharedClass(context: Context) {

    private var sharedPreferences: SharedPreferences? = null

    init {

        sharedPreferences = context.getSharedPreferences("myRef", Context.MODE_PRIVATE)

    }

    fun saveData(random: Int) {

        val edit = sharedPreferences!!.edit()
        edit.putInt("random", random)
        edit.commit()

    }

    fun loadData(): Int {

        return sharedPreferences!!.getInt("random", 0)


    }


}