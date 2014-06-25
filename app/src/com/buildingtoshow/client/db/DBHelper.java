package com.buildingtoshow.client.db;

/**
 * Created by liudonghua on 14-6-25.
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    // DATABASE building_to_show
    static final String DB_NAME = "building_to_show.db";
    static final int DB_VERSION = 1;

    // TABLE trace_record
    public static final String TABLE_TRACE_RECORD = "trace_record";
    public static final String TRACE_RECORD_ID = "_id";
    public static final String TRACE_RECORD_START_DATETIME = "start_date_time";
    public static final String TRACE_RECORD_DISTANCE = "distance";
    public static final String TRACE_RECORD_TOTAL_TIME = "total_time";
    public static final String TRACE_RECORD_LOCATIONS = "locations";

    // TABLE CREATION STATEMENT
    private static final String CREATE_TABLE = "create table " + TABLE_TRACE_RECORD + "("
            + TRACE_RECORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + TRACE_RECORD_START_DATETIME + " DATETIME NOT NULL, "
            + TRACE_RECORD_DISTANCE + " INTEGER NOT NULL, "
            + TRACE_RECORD_TOTAL_TIME + " INTEGER NOT NULL, "
            + TRACE_RECORD_LOCATIONS + " TEXT NOT NULL "
            + ");";

    public DBHelper(Context context) {
        super(context, DB_NAME, null,DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACE_RECORD);
        onCreate(db);
    }
}
