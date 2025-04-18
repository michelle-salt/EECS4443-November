package com.example.novemberproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tracking.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_TRACKING = "tracking_data";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_TRACKING + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "session_start INTEGER, " +
                "session_end INTEGER, " +
                "total_session_time INTEGER, " +
                "avg_time_per_post INTEGER, " +
                "total_likes INTEGER, " +
                "pulsing_on INTEGER, " +
                "pulse_interval INTEGER" +
                ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACKING);
        onCreate(db);
    }

    public void insertTrackingData(long sessionStart, long sessionEnd, long totalSessionTime, long avgTimePerPost, int totalLikes,
                                   boolean pulsingOn, int pulseInterval) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("session_start", sessionStart);
        values.put("session_end", sessionEnd);
        values.put("total_session_time", totalSessionTime);
        values.put("avg_time_per_post", avgTimePerPost);
        values.put("total_likes", totalLikes);
        values.put("pulsing_on", pulsingOn ? 1 : 0);
        values.put("pulse_interval", pulseInterval);
        db.insert(TABLE_TRACKING, null, values);
        db.close();
    }

    public Cursor getAllTrackingData() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_TRACKING, null);
    }
}
