package com.buildingtoshow.client.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

import com.buildingtoshow.client.utils.Utils;

import java.util.Date;
import java.util.List;

/**
 * Created by liudonghua on 14-6-25.
 */
public class TraceRecordHelper {
    
    private static TraceRecordHelper mTraceRecordHelper;
    private DBHelper mDBHelper;
    private SQLiteDatabase database;
    
    private TraceRecordHelper(Context context) {
        mDBHelper = new DBHelper(context);
        database = mDBHelper.getWritableDatabase();
    }
    
    public static TraceRecordHelper getInstance(Context context) {
        if(mTraceRecordHelper == null) {
            mTraceRecordHelper = new TraceRecordHelper(context);
        }
        return mTraceRecordHelper;
    }

    public void insert(Date startDateTime, int distance, int totalTime, List<Location> locations, String snapshotPath) {
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.TRACE_RECORD_START_DATETIME, Utils.formatDateTime(startDateTime));
        cv.put(DBHelper.TRACE_RECORD_DISTANCE, distance);
        cv.put(DBHelper.TRACE_RECORD_TOTAL_TIME, totalTime);
        cv.put(DBHelper.TRACE_RECORD_LOCATIONS, Utils.locationsToString(locations));
        cv.put(DBHelper.TRACE_RECORD_SNAPSHOT_PATH, snapshotPath);
        database.insert(DBHelper.TABLE_TRACE_RECORD, null, cv);
    }

    public Cursor query() {
        String[] selections = null;
        String orderBy = DBHelper.TRACE_RECORD_START_DATETIME + " DESC";
        Cursor c = database.query(DBHelper.TABLE_TRACE_RECORD, selections, null,null, null, null, orderBy);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public int update(long traceRecordId, int distance) {
        ContentValues cvUpdate = new ContentValues();
        cvUpdate.put(DBHelper.TRACE_RECORD_DISTANCE, distance);
        int i = database.update(DBHelper.TABLE_TRACE_RECORD, cvUpdate,
                DBHelper.TRACE_RECORD_ID + " = " + traceRecordId, null);
        return i;
    }

    public void delete(long traceRecordId) {
        database.delete(DBHelper.TABLE_TRACE_RECORD, DBHelper.TRACE_RECORD_ID + "="
                + traceRecordId, null);
    }
}
