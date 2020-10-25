package com.example.hya;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public DBHelper(Context context){
        super(context, "DBdM4.db",null,DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table if not exists main (num integer primary key autoincrement, lon text, lat text, title text, content text, image text)"; //테이블이 없으면 만듬.
        String sql2 = "create table if not exists checking (cch integer)";
        try{
            db.execSQL(sql);
            db.execSQL(sql2);
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
