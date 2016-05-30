package ru.allfound.googleapiexample.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import ru.allfound.googleapiexample.model.Marker;

/*
 * DatabaseHandler.java    v.1.0 30.05.2016
 *
 * Copyright (c) 2015-2016 Vladislav Laptev,
 * All rights reserved. Used by permission.
 */

public class DatabaseHandler extends SQLiteOpenHelper implements IDatabaseHandler {

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_MARKERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXIST " + TABLE_MARKERS);
        onCreate(db);
    }

    public void addMarker(Marker marker) {
        long rowID = -1;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        putMarker2DB(contentValues, marker);
        try {
            rowID = db.insert(TABLE_MARKERS, null, contentValues);
        } catch (SQLiteConstraintException ex) {
            ex.printStackTrace();
        }
        marker.setId(rowID);
        db.close();
    }

    /**
     * Add new department to database.
     * @return The row ID of the newly inserted department, or -1 if an error occurred.
     * */
    public long addMarker(ContentValues contentValues) {
        long rowID = -1;

        SQLiteDatabase db = this.getWritableDatabase();
        try {
            rowID = db.insert(TABLE_MARKERS, null, contentValues);
        } catch (SQLiteConstraintException ex) {
            ex.printStackTrace();
        }
        return rowID;
    }

    // updates an existing marker in the database
    public void updateMarker(Marker marker) {
        ContentValues contentValues = new ContentValues();
        putMarker2DB(contentValues, marker);
        SQLiteDatabase db = this.getWritableDatabase();
        db.update(TABLE_MARKERS, contentValues, KEY_ID + "=" + marker.getId(), null);
        db.close();
    }

    public List<Marker> fetchMarkers() {
        ArrayList<Marker> markers = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_MARKERS, null, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Marker marker = new Marker();
            marker.setId(cursor.getLong(0));
            marker.setLatitude(cursor.getDouble(1));
            marker.setLongitude(cursor.getDouble(2));
            marker.setName(cursor.getString(3));
            marker.setAddress(cursor.getString(4));
            markers.add(0, marker);
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return markers;
    }

    public Marker findById(long id) {
        Marker marker = null;
        String sql = "SELECT * FROM " + TABLE_MARKERS
                + " WHERE " + KEY_ID + " = ?";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(sql, new String[] { id + "" });
        if (cursor.moveToNext()) {
            marker = new Marker();
            marker.setId(cursor.getLong(0));
            marker.setLatitude(cursor.getDouble(1));
            marker.setLongitude(cursor.getDouble(2));
            marker.setName(cursor.getString(3));
            marker.setAddress(cursor.getString(4));
        }
        cursor.close();
        db.close();
        return marker;
    }

    public boolean deleteMarker(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int status = db.delete(TABLE_MARKERS, KEY_ID + "=" + id, null);
        db.close();
        if (status == 1) return true;
        return false;
    }

    public void deleteMarkers() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MARKERS, null, null);
        close();
    }

    private void putMarker2DB(ContentValues contentValues, Marker marker) {
        contentValues.put(KEY_LATITUDE, marker.getLatitude());
        contentValues.put(KEY_LONGTITUDE, marker.getLongitude());
        contentValues.put(KEY_NAME, marker.getName());
        contentValues.put(KEY_ADDRESS, marker.getAddress());
    }
}
