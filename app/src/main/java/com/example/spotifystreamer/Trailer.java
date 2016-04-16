package com.example.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by altair on 4/17/16.
 */
public class Trailer implements Parcelable {
    private String name, source;

    public Trailer(String name, String source) {
        this.name = name;
        this.source = source;
    }

    private Trailer(Parcel in) {
        this.name = in.readString();
        this.source = in.readString();
    }

    public String getName() {
        return name;
    }

    public String getSource() {
        return source;
    }

    public static final Parcelable.Creator<Trailer> CREATOR = new Parcelable.Creator<Trailer>() {
        @Override
        public Trailer createFromParcel(Parcel source) {
            return new Trailer(source);
        }

        @Override
        public Trailer[] newArray(int size) {
            return new Trailer[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(source);
    }
}
