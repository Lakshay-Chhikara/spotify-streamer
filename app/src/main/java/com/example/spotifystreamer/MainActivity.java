package com.example.spotifystreamer;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity
        implements DiscoveryFragment.OnMovieSelectedListener {
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (findViewById(R.id.content) != null) {
            if (savedInstanceState != null) {
                return;
            }

            DiscoveryFragment discoveryFragment = new DiscoveryFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, discoveryFragment)
                    .commit();
        }
    }

    @Override
    public void onMovieSelected(Movie movie, boolean skipForOnePaneLayout) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        MovieDetailFragment movieDetailFragment = (MovieDetailFragment) fragmentManager
                .findFragmentById(R.id.fragment_movie_detail);

        if (movieDetailFragment != null) {
            movieDetailFragment.updateMovieDetailsView(movie);
        } else {
            if (!skipForOnePaneLayout) {
                MovieDetailFragment newMovieDetailFragment = new MovieDetailFragment();
                Bundle mBundle = new Bundle();
                mBundle.putParcelable(MovieDetailFragment.KEY_MOVIE, movie);
                newMovieDetailFragment.setArguments(mBundle);
                fragmentManager.beginTransaction()
                        .replace(R.id.content, newMovieDetailFragment)
                        .addToBackStack(null)
                        .commit();
            }
        }
    }
}
