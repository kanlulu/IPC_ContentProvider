package com.kanlulu.ipc_contentprovider.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by kanlulu
 * DATE: 2018/12/13 10:58
 */
public class ProviderDbHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "ipc_provider.db";
    public static final String TABLE_NAME = "book";
    public static final int DB_VERSION = 1;

    private final String SQL_CREATE_TABLE = "create table if not exists " + TABLE_NAME + " (_id integer primary key, name TEXT, description TEXT)";

    public ProviderDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
