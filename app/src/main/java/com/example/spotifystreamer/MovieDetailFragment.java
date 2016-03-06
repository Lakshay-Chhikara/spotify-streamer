package com.example.spotifystreamer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;


public class MovieDetailFragment extends Fragment {
    private static final double MAX_RATING = 10;
    public static final String KEY_MOVIE = "key_movie";
    private Movie movie;

    private ImageView moviePoster;
    private TextView movieOriginalTitle, movieReleaseDate, movieRating, movieSynopsis;

    public MovieDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_movie_detail, container, false);
        moviePoster = (ImageView) rootView.findViewById(R.id.movie_poster);
        movieOriginalTitle = (TextView) rootView.findViewById(R.id.movie_original_title);
        movieReleaseDate = (TextView) rootView.findViewById(R.id.movie_release_date);
        movieRating = (TextView) rootView.findViewById(R.id.movie_rating);
        movieSynopsis = (TextView) rootView.findViewById(R.id.movie_synopsis);
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(KEY_MOVIE, movie);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            movie = savedInstanceState.getParcelable(KEY_MOVIE);
            updateMovieDetailsView(movie);
        } else {
            Bundle arguments = getArguments();
            if (arguments != null) {
                updateMovieDetailsView((Movie) arguments.getParcelable(KEY_MOVIE));
            }
        }
    }

    public void updateMovieDetailsView(Movie movie) {
        if (movie != null) {
            this.movie = movie;
            Glide.with(getActivity()).load(movie.getPosterUrl()).into(moviePoster);
            movieOriginalTitle.setText(movie.getTitle());
            movieReleaseDate.setText(movie.getReleaseDate());
            movieRating.setText(getString(R.string.rating_format, movie.getRating(), MAX_RATING));
            movieSynopsis.setText(movie.getSynopsis());
        }
    }
}
