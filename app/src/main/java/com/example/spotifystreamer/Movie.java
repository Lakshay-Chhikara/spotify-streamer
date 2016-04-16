package com.example.spotifystreamer;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by altair on 18-02-2016.
 */
public class Movie implements Parcelable {
    private static final String SCHEME_FOR_MOVIE_POSTER = "http";
    private static final String AUTHORITY_FOR_MOVIE_POSTER = "image.tmdb.org";
    private static final String PATH_1_FOR_MOVIE_POSTER = "t";
    private static final String PATH_2_FOR_MOVIE_POSTER = "p";
    // For Poster Size
    private static final String PATH_3_FOR_MOVIE_POSTER = "w185";

    private int id;
    private String title, posterUrl, synopsis, releaseDate;
    private double rating;

    public Movie(JSONObject movie) throws JSONException {
        this.id = movie.getInt("id");
        this.title = movie.getString("original_title");
        this.posterUrl = new Uri.Builder().scheme(SCHEME_FOR_MOVIE_POSTER)
                .authority(AUTHORITY_FOR_MOVIE_POSTER)
                .appendPath(PATH_1_FOR_MOVIE_POSTER)
                .appendPath(PATH_2_FOR_MOVIE_POSTER)
                .appendPath(PATH_3_FOR_MOVIE_POSTER).toString() + movie.getString("poster_path");
        this.synopsis = movie.getString("overview");
        this.rating = movie.getDouble("vote_average");
        this.releaseDate = movie.getString("release_date");
    }

    public Movie(int id, String title, String posterUrl, String synopsis, String releaseDate,
                 double rating) {
        this.id = id;
        this.title = title;
        this.posterUrl = posterUrl;
        this.synopsis = synopsis;
        this.releaseDate = releaseDate;
        this.rating = rating;
    }

    private Movie(Parcel in) {
        this.title = in.readString();
        this.posterUrl = in.readString();
        this.synopsis = in.readString();
        this.releaseDate = in.readString();
        this.rating = in.readDouble();
    }

    public int getId() {
        return id;
    }

    public String getPosterUri() {
        return posterUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public double getRating() {
        return rating;
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(posterUrl);
        dest.writeString(synopsis);
        dest.writeString(releaseDate);
        dest.writeDouble(rating);
    }
}
