package com.example.jason.video_player_1;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class MySqlite extends SQLiteOpenHelper {

        public static final String Create_contact = "create table danmu(id integer primary key autoincrement, "
                + "content char(100), " + "color integer(20), " + "size float(20),"+"height float(20),"+"level integer(20),"+"video char(100),"+"time interger(20))";

        private Context mcontext;

        public MySqlite(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
            mcontext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // 当创建数据库的时候会调用此方法在此方法中创建表
            db.execSQL(Create_contact);
            Toast.makeText(mcontext, "弹幕数据库创建完成", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }


}


