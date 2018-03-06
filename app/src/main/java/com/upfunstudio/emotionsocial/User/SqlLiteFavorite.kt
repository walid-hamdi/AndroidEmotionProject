package com.upfunstudio.emotionsocial.User

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder

/**
 * Created by walido on 2/28/2018.
 */
class SqlLiteFavorite(var context: Context) {

    companion object {
        var context: Context? = null
        const val db = "DB"
        const val version = 1
        const val tableName = "tips"
        const val createTable = "create table if not exists tips "
                .plus("(id integer primary key AUTOINCREMENT ,")
                .plus("title text,description text,time text,published text);")
        const val dropTable = "drop table if exists tips;"
        var itemExits: Boolean? = null


    }

    private var dbInstance: SQLiteDatabase? = null

    init {
        this.context = context
        var db = SqlLiteFavoriteHelper(context)
        dbInstance = db.writableDatabase

    }

    class SqlLiteFavoriteHelper(context: Context) :
            SQLiteOpenHelper(context, db, null, version) {

        override fun onCreate(db: SQLiteDatabase?) {
            db!!.execSQL(createTable)


        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

            db!!.execSQL(dropTable)
            onCreate(db)
        }


    }


    fun storeFavData(contextValue: ContentValues): Long {

        val id = dbInstance!!.insert(tableName, null, contextValue)
        return id


    }

    fun retriveFavData(projection: Array<String>, selection: String,
                       selectionArgs: Array<String>, orderBy: String): Cursor {

        SQLiteQueryBuilder().tables = tableName

        val cursor = dbInstance!!.query(tableName, projection, selection,
                selectionArgs, null, null, orderBy)
        itemExits = cursor.count > 0

        return cursor


    }

    fun deleteData() {

    }

}