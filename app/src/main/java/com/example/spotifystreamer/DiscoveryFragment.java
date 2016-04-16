package com.example.spotifystreamer;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DiscoveryFragment.OnMovieSelectedListener} interface
 * to handle interaction events.
 */
public class DiscoveryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private RecyclerView mRecyclerView;
    private ArrayList<Movie> movieList;
    private MovieAdapter movieAdapter;

    private OnMovieSelectedListener mListener;

    private static final int SPAN_COUNT = 2;

    private static final String KEY_MOVIE_ARRAY_LIST = "key_movie_list";
    private static final String KEY_SHOWING_FAVOURITES = "key_showing_favourites";

    private static final int MOST_POPULAR = 0;
    private static final int HIGHEST_RATED = 1;

    private static final int FAVOURITE_MOVIES_LOADER = 0;
    private boolean showingFavourites = false;

    public DiscoveryFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMovieSelectedListener) {
            mListener = (OnMovieSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMovieSelectedListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            showingFavourites = savedInstanceState.getBoolean(KEY_SHOWING_FAVOURITES);
            movieList = savedInstanceState.getParcelableArrayList(KEY_MOVIE_ARRAY_LIST);
            movieAdapter = new MovieAdapter(getActivity(), movieList, mListener);
            //movieAdapter.notifyItemRangeInserted(0, movieList.size());

            // Don't do this here
            //mRecyclerView.setAdapter(movieAdapter);
        } else {
            movieList = new ArrayList<>();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_discovery, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_movies);
        mRecyclerView.setHasFixedSize(true);

        GridLayoutManager mGridLayoutManager = new GridLayoutManager(getActivity(),
                SPAN_COUNT, GridLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mGridLayoutManager);

        movieAdapter = new MovieAdapter(getActivity(), movieList, mListener);
        mRecyclerView.setAdapter(movieAdapter);

        if (movieList.isEmpty() && !showingFavourites) {
            new FetchMovieData().execute(
                    getResources().getStringArray(R.array.sort_by_parameter)[MOST_POPULAR]);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(KEY_MOVIE_ARRAY_LIST, movieList);
        outState.putBoolean(KEY_SHOWING_FAVOURITES, showingFavourites);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_discovery, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        String[] sortBy = getResources().getStringArray(R.array.sort_by_parameter);
        switch (itemId) {
            case R.id.most_popular:
                showingFavourites = false;
                new FetchMovieData().execute(sortBy[MOST_POPULAR]);
                return true;

            case R.id.highest_rated:
                showingFavourites = false;
                new FetchMovieData().execute(sortBy[HIGHEST_RATED]);
                return true;

            case R.id.favourites:
                showingFavourites = true;
                getLoaderManager().initLoader(FAVOURITE_MOVIES_LOADER, null, DiscoveryFragment.this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private class FetchMovieData extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(final String... params) {
            Uri uri = new Uri.Builder().scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("discover")
                    .appendPath("movie")
                    .appendQueryParameter("sort_by", params[0])
                    .appendQueryParameter("api_key", getString(R.string.api_key))
                    .build();
            String response = null;
            try {
                StringBuilder stringBuilder = new StringBuilder();
                URLConnection urlConnection = new URL(uri.toString()).openConnection();
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream()));
                /* TODO: See if there is any better way so that don't have to
                 * declare a variable out of the scope it is required
                 */
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                bufferedReader.close();
                response = stringBuilder.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                Snackbar.make(mRecyclerView, R.string.check_internet_connection, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new FetchMovieData().execute(params);
                            }
                        })
                        .show();
            }

            if (response != null) {
                movieList.clear();
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray movieResults = jsonResponse.getJSONArray("results");
                    for (int i = 0; i < movieResults.length(); ++i) {
                        try {
                            movieList.add(new Movie(movieResults.getJSONObject(i)));
                        } catch (JSONException e) {
                            // TODO : Log.e()
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            movieAdapter.notifyDataSetChanged();
            //movieAdapter.notifyItemRangeChanged(0, movieList.size());
        }
    }

    public interface OnMovieSelectedListener {
        void onMovieSelected(Movie movie, boolean skipForOnePaneLayout);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(),
                FavouriteContract.FavouriteEntry.buildFavouriteMoviesUri(),
                new String[] {FavouriteContract.FavouriteEntry._ID,
                        FavouriteContract.FavouriteEntry.COLUMN_NAME,
                        FavouriteContract.FavouriteEntry.COLUMN_POSTER_URI,
                        FavouriteContract.FavouriteEntry.COLUMN_SYNOPSIS,
                        FavouriteContract.FavouriteEntry.COLUMN_RELEASE_DATE,
                        FavouriteContract.FavouriteEntry.COLUMN_RATING},
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!showingFavourites) {
            return;
        }

        movieList.clear();
        if (data.moveToFirst()) {
            do {
                movieList.add(new Movie(data.getInt(0), data.getString(1), data.getString(2),
                        data.getString(3), data.getString(4), data.getDouble(5)));
            } while (data.moveToNext());
        }
        movieAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
