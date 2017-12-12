package com.jshaz.daigo.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jshaz on 2017/11/26.
 */

public class UserDatabaseHelper extends SQLiteOpenHelper {

    public static final String CREATE_USER = "create table user_cache ("
            + "id integer primary key autoincrement, "
            + "userid text, "
            + "discode text, "
            + "phonenum text, "
            + "nickname text, "
            + "headicon text, "
            + "campuscode integer)";

    private Context mContext;

    public UserDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_USER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
