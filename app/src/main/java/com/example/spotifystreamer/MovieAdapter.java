package com.example.spotifystreamer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * Created by altair on 18-02-2016.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private Context mContext;
    private ArrayList<Movie> movieList;
    private DiscoveryFragment.OnMovieSelectedListener mListener;

    public MovieAdapter(Context context, ArrayList<Movie> movieList,
                        DiscoveryFragment.OnMovieSelectedListener onMovieSelectedListener) {
        super();
        this.mContext = context;
        this.movieList = movieList;
        this.mListener = onMovieSelectedListener;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater mLayoutInflater =
                (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewHolderItem = mLayoutInflater.inflate(R.layout.item_movie_poster, parent, false);

        return new MovieViewHolder(viewHolderItem);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, final int position) {
        Glide.with(mContext)
                .load(movieList.get(position).getPosterUrl())
                .into(holder.moviePoster);
        holder.moviePoster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onMovieSelected(movieList.get(position), false);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    protected class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView moviePoster;

        public MovieViewHolder(View itemView) {
            super(itemView);
            moviePoster = (ImageView) itemView.findViewById(R.id.movie_poster);
        }
    }
}
