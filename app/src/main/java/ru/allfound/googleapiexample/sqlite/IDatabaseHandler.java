package ru.allfound.googleapiexample.sqlite;

/*
 * DatabaseHandler.java    v.1.0 30.05.2016
 *
 * Copyright (c) 2015-2016 Vladislav Laptev,
 * All rights reserved. Used by permission.
 */

public interface IDatabaseHandler {
    static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "MARKERS_DB";
    static final String TABLE_MARKERS = "markers";

    static final String KEY_ID = "_id";
    static final String KEY_LATITUDE = "latitude";
    static final String KEY_LONGTITUDE = "longitude";
    static final String KEY_NAME = "name";
    static final String KEY_ADDRESS = "address";

    String CREATE_MARKERS_TABLE = "CREATE TABLE "
            + TABLE_MARKERS + "("
            + KEY_ID + " integer primary key autoincrement,"
            + KEY_LATITUDE + " REAL, "
            + KEY_LONGTITUDE + " REAL, "
            + KEY_NAME + " TEXT, "
            + KEY_ADDRESS + " TEXT" + ")";
}
