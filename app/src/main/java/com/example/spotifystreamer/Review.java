package com.example.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by altair on 4/17/16.
 */
public class Review implements Parcelable {
    private String author, content;

    public Review(String author, String content) {
        this.author = author;
        this.content = content;
    }

    private Review(Parcel in) {
        this.author = in.readString();
        this.content = in.readString();
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public static final Parcelable.Creator<Review> CREATOR = new Parcelable.Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel source) {
            return new Review(source);
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(author);
        dest.writeString(content);
    }
}
