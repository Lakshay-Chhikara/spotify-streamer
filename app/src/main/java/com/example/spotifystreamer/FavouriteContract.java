package com.example.spotifystreamer;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by altair on 4/6/16.
 */
public class FavouriteContract {

    public static final String CONTENT_AUTHORITY = "com.example.spotifystreamer";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_FAVOURITE = "favourite";

    public static final class FavouriteEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVOURITE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVOURITE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVOURITE;

        public static final String TABLE_NAME = "movie";

        public static final String COLUMN_NAME = "name";

        public static final String COLUMN_POSTER_URI = "poster_uri";

        public static final String COLUMN_RATING = "rating";

        public static final String COLUMN_RELEASE_DATE = "release_date";

        public static final String COLUMN_SYNOPSIS = "synopsis";

        public static Uri buildFavouriteMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildFavouriteMoviesUri() {
            return CONTENT_URI.buildUpon().build();
        }

        public static Uri buildFavouriteMovieWithDetailsUri(int id) {
            return CONTENT_URI.buildUpon().appendQueryParameter(_ID, Integer.toString(id)).build();
        }
    }
}
