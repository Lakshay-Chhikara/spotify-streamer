package com.example.spotifystreamer;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
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
public class DiscoveryFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private ArrayList<Movie> movieList;
    private MovieAdapter movieAdapter;

    private OnMovieSelectedListener mListener;

    private static final int SPAN_COUNT = 2;

    private static final String SCHEME = "http";
    private static final String AUTHORITY = "api.themoviedb.org";
    private static final String PATH_1 = "3";
    private static final String PATH_2 = "discover";
    private static final String PATH_3 = "movie";
    private static final String SORT_BY_QUERY_PARAMETER = "sort_by";
    private static final String API_KEY_QUERY_PARAMETER = "api_key";

    private static final String KEY_MOVIE_ARRAY_LIST = "key_movie_list";

    private static final int MOST_POPULAR = 0;
    private static final int HIGHEST_RATED = 1;

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

        if (movieList.isEmpty()) {
            new FetchMovieData().execute(
                    getResources().getStringArray(R.array.sort_by_parameter)[MOST_POPULAR]);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(KEY_MOVIE_ARRAY_LIST, movieList);
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
                new FetchMovieData().execute(sortBy[MOST_POPULAR]);
                return true;

            case R.id.highest_rated:
                new FetchMovieData().execute(sortBy[HIGHEST_RATED]);
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
            Uri uri = new Uri.Builder().scheme(SCHEME)
                    .authority(AUTHORITY)
                    .appendPath(PATH_1)
                    .appendPath(PATH_2)
                    .appendPath(PATH_3)
                    .appendQueryParameter(SORT_BY_QUERY_PARAMETER, params[0])
                    .appendQueryParameter(API_KEY_QUERY_PARAMETER, getString(R.string.api_key))
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
}
