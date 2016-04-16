package com.example.spotifystreamer;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by altair on 4/12/16.
 */
public class MovieProvider extends ContentProvider {

    public static final UriMatcher mUriMatcher = buildUriMatcher();
    private MovieDBHelper mMovieDBHelper;

    static final int FAVOURITE_MOVIES = 100;
    static final int FAVOURITE_MOVIE_WITH_DETAILS = 101;

    static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = FavouriteContract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority, FavouriteContract.PATH_FAVOURITE, FAVOURITE_MOVIES);
        uriMatcher.addURI(authority, FavouriteContract.PATH_FAVOURITE + "/#", FAVOURITE_MOVIE_WITH_DETAILS);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mMovieDBHelper = new MovieDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = mUriMatcher.match(uri);

        switch (match) {
            case FAVOURITE_MOVIES:
                return FavouriteContract.FavouriteEntry.CONTENT_TYPE;
            case FAVOURITE_MOVIE_WITH_DETAILS:
                return FavouriteContract.FavouriteEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor returnCursor;
        switch (mUriMatcher.match(uri)) {
            case FAVOURITE_MOVIES: {
                returnCursor = mMovieDBHelper.getReadableDatabase().query(
                        FavouriteContract.FavouriteEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case FAVOURITE_MOVIE_WITH_DETAILS: {
                returnCursor = mMovieDBHelper.getReadableDatabase().query(
                        FavouriteContract.FavouriteEntry.TABLE_NAME,
                        null,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return returnCursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase sqLiteDatabase = mMovieDBHelper.getWritableDatabase();
        final int match = mUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case FAVOURITE_MOVIES: {
                long _id = sqLiteDatabase.insert(FavouriteContract.FavouriteEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = FavouriteContract.FavouriteEntry.buildFavouriteMovieUri(_id);
                } else {
                    throw new SQLiteException("Failed to insert row into " + uri);
                }
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        sqLiteDatabase.close();
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase sqLiteDatabase = mMovieDBHelper.getWritableDatabase();
        final int match = mUriMatcher.match(uri);
        int rowsDeleted;

        if (null == selection) {
            selection = "1";
        }

        switch (match) {
            case FAVOURITE_MOVIES:
                rowsDeleted = sqLiteDatabase.delete(
                        FavouriteContract.FavouriteEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        sqLiteDatabase.close();
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase sqLiteDatabase = mMovieDBHelper.getWritableDatabase();
        final int match = mUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case FAVOURITE_MOVIES:
                rowsUpdated = sqLiteDatabase.update(
                        FavouriteContract.FavouriteEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        sqLiteDatabase.close();
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase sqLiteDatabase = mMovieDBHelper.getWritableDatabase();
        final int match = mUriMatcher.match(uri);
        switch (match) {
            case FAVOURITE_MOVIES:
                sqLiteDatabase.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = sqLiteDatabase.insert(FavouriteContract.FavouriteEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            ++returnCount;
                        }
                    }
                    sqLiteDatabase.setTransactionSuccessful();
                } finally {
                    sqLiteDatabase.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
