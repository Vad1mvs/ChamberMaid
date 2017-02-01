package com.utis.chambermaid.tables;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.utis.chambermaid.DBSchemaHelper;

public class IpTables extends SQLiteOpenHelper {
    DBSchemaHelper dbSchemaHelper;
    public static final String _ID = "_id";
    public static final String HOTEL = "hotel";
    public static final String IP_NAME = "ip_name";

    public static final String TABLE_NAME = "ip";
    static final int DB_VERSION = 1;
    static final String DB_NAME = "DB1";


    private IpTables dbhelper;
    private Context ourcontext;
    private SQLiteDatabase database;
    public IpTables(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    private static final String CREATE_TABLE = "create table "
            + TABLE_NAME + "(" + _ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + HOTEL + " TEXT,"
            + IP_NAME + " TEXT NOT NULL);";

    public IpTables(Context context) {
        super(context, DB_NAME, null,DB_VERSION);
        ourcontext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public IpTables abrirBaseData() throws SQLException {
        dbhelper = new IpTables(ourcontext);
        database = dbhelper.getWritableDatabase();
        return this;
    }

    public void cerrar() {
        dbhelper.close();
    }

    public void insertData( String hotel, String name) {
        ContentValues cv = new ContentValues();
        cv.put(IpTables.HOTEL, hotel);
        cv.put(IpTables.IP_NAME, name);
        database.insert(IpTables.TABLE_NAME, null, cv);
    }

    public Cursor listData() {
        String[] todasLasColumnas = new String[] {
                IpTables._ID,
                IpTables.IP_NAME,
                IpTables.HOTEL
        };
        Cursor c = database.query(IpTables.TABLE_NAME, todasLasColumnas, null,
                null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public int actualData(long ID, String memberName, String hotel) {
        ContentValues cv = new ContentValues();
        cv.put(IpTables.IP_NAME, memberName);
        cv.put(IpTables.HOTEL, hotel);
        int i = database.update(IpTables.TABLE_NAME, cv,
                IpTables._ID + " = " + ID, null);
        return i;
    }

    public void deleteData(long memberID) {
        database.delete(IpTables.TABLE_NAME, IpTables._ID + "="
                + memberID, null);
    }
}
