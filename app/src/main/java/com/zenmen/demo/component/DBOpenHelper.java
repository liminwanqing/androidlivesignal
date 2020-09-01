package com.zenmen.demo.component;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "people.db";
    public static final String DB_TABLE = "peopleinfo";
    public static final int DB_VERSION = 1;

    private static final String DB_CREATE = "create table " +
            DB_TABLE + "(" + People.KEY_ID + " integer primary key autoincrement, " +
            People.KEY_NAME + " text not null, " + People.KEY_AGE + " integer, " +
            People.KEY_HEIGHT + " float);";

    public DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
        onCreate(db);
    }
}
