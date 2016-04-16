package com.example.spotifystreamer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by altair on 4/6/16.
 */
public class MovieDBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "movie.db";

    public MovieDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + FavouriteContract.FavouriteEntry.TABLE_NAME
                + " (" + FavouriteContract.FavouriteEntry._ID + " INTEGER," +
                FavouriteContract.FavouriteEntry.COLUMN_NAME + " TEXT NOT NULL," +
                FavouriteContract.FavouriteEntry.COLUMN_POSTER_URI + " TEXT NOT NULL," +
                FavouriteContract.FavouriteEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL," +
                FavouriteContract.FavouriteEntry.COLUMN_RATING + " REAL NOT NULL," +
                FavouriteContract.FavouriteEntry.COLUMN_SYNOPSIS + " TEXT NOT NULL, " +
                " UNIQUE (" + FavouriteContract.FavouriteEntry._ID + ") ON CONFLICT REPLACE );";

        db.execSQL(SQL_CREATE_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FavouriteContract.FavouriteEntry.TABLE_NAME);
        onCreate(db);
    }
}
