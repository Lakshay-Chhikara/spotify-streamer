package com.example.spotifystreamer;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

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


public class MovieDetailFragment extends Fragment implements View.OnClickListener {
    private static final double MAX_RATING = 10;
    public static final String KEY_MOVIE = "key_movie";
    public static final String KEY_TRAILER_ARRAY_LIST = "key_trailer_array_list";
    public static final String KEY_REVIEW_ARRAY_LIST = "key_review_array_list";
    private Movie movie;

    private ImageView moviePoster, addToFavorites;
    private TextView movieOriginalTitle, movieReleaseDate, movieRating, movieSynopsis;
    private RecyclerView trailersRecyclerView, reviewsRecyclerView;
    private RecyclerView.Adapter trailerAdapter, reviewAdapter;
    private ArrayList<Trailer> trailerArrayList;
    private ArrayList<Review> reviewArrayList;
    private ShareActionProvider mShareActionProvider;

    public MovieDetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_movie_detail, container, false);
        moviePoster = (ImageView) rootView.findViewById(R.id.movie_poster);
        addToFavorites = (ImageView) rootView.findViewById(R.id.add_to_favourites);
        movieOriginalTitle = (TextView) rootView.findViewById(R.id.movie_original_title);
        movieReleaseDate = (TextView) rootView.findViewById(R.id.movie_release_date);
        movieRating = (TextView) rootView.findViewById(R.id.movie_rating);
        movieSynopsis = (TextView) rootView.findViewById(R.id.movie_synopsis);
        trailersRecyclerView = (RecyclerView) rootView.findViewById(R.id.trailers_recycler_view);
        reviewsRecyclerView = (RecyclerView) rootView.findViewById(R.id.reviews_recycler_view);

        trailersRecyclerView.setHasFixedSize(true);
        reviewsRecyclerView.setHasFixedSize(true);

        LinearLayoutManager trailersLinearLayoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false);
        trailersRecyclerView.setLayoutManager(trailersLinearLayoutManager);
        LinearLayoutManager reviewsLinearLayoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false);
        reviewsRecyclerView.setLayoutManager(reviewsLinearLayoutManager);

        if (savedInstanceState == null) {
            trailerArrayList = new ArrayList<>();
            reviewArrayList = new ArrayList<>();
        } else {
            trailerArrayList = savedInstanceState.getParcelableArrayList(KEY_TRAILER_ARRAY_LIST);
            reviewArrayList = savedInstanceState.getParcelableArrayList(KEY_REVIEW_ARRAY_LIST);
        }

        trailerAdapter = new TrailerAdapter();
        trailersRecyclerView.setAdapter(trailerAdapter);
        reviewAdapter = new ReviewAdapter();
        reviewsRecyclerView.setAdapter(reviewAdapter);

        addToFavorites.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(KEY_MOVIE, movie);
        outState.putParcelableArrayList(KEY_TRAILER_ARRAY_LIST, trailerArrayList);
        outState.putParcelableArrayList(KEY_REVIEW_ARRAY_LIST, reviewArrayList);
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
            Glide.with(getActivity()).load(movie.getPosterUri()).into(moviePoster);
            movieOriginalTitle.setText(movie.getTitle());
            movieReleaseDate.setText(movie.getReleaseDate());
            movieRating.setText(getString(R.string.rating_format, movie.getRating(), MAX_RATING));
            movieSynopsis.setText(movie.getSynopsis());
        }

        updateAddToFavouritesButton();
        fetchTrailersAndReviews();
    }

    private void fetchTrailersAndReviews() {
        new FetchMovieTrailersAndReviews().execute();
    }

    private void addMovieToFavourites(Movie movie) {
        ContentValues movieContentValues = new ContentValues();
        movieContentValues.put(FavouriteContract.FavouriteEntry._ID, movie.getId());
        movieContentValues.put(FavouriteContract.FavouriteEntry.COLUMN_NAME, movie.getTitle());
        movieContentValues.put(FavouriteContract.FavouriteEntry.COLUMN_POSTER_URI, movie.getPosterUri());
        movieContentValues.put(FavouriteContract.FavouriteEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
        movieContentValues.put(FavouriteContract.FavouriteEntry.COLUMN_RATING, movie.getRating());
        movieContentValues.put(FavouriteContract.FavouriteEntry.COLUMN_SYNOPSIS, movie.getSynopsis());

        getContext().getContentResolver().insert(
                FavouriteContract.FavouriteEntry.buildFavouriteMoviesUri(), movieContentValues);

        updateAddToFavouritesButton();
    }

    private void deleteMovieFromFavourites(Movie movie) {
        getContext().getContentResolver().delete(
                FavouriteContract.FavouriteEntry.buildFavouriteMoviesUri(),
                FavouriteContract.FavouriteEntry._ID + "= ?",
                new String[] {Integer.toString(movie.getId())});

        updateAddToFavouritesButton();
    }

    private void updateAddToFavouritesButton() {
        if (movieExistInDatabase()) {
            addToFavorites.setImageResource(android.R.drawable.star_big_on);
        } else {
            addToFavorites.setImageResource(android.R.drawable.star_big_off);
        }
    }

    private boolean movieExistInDatabase() {
        Cursor mCursor = getContext().getContentResolver().query(
                FavouriteContract.FavouriteEntry.buildFavouriteMoviesUri(),
                new String[] {FavouriteContract.FavouriteEntry._ID},
                FavouriteContract.FavouriteEntry._ID + "= ?",
                new String[] {Integer.toString(movie.getId())},
                null);

        if (mCursor.moveToFirst()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.add_to_favourites:
                if (!movieExistInDatabase()) {
                    addMovieToFavourites(this.movie);
                } else {
                    deleteMovieFromFavourites(this.movie);
                }
                break;
        }
    }

    private class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerViewHolder> {
        public TrailerAdapter() {
            super();
        }

        @Override
        public TrailerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater mLayoutInflater =
                    (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View trailerItemView = mLayoutInflater.inflate(R.layout.item_trailer, parent, false);
            return new TrailerViewHolder(trailerItemView);
        }

        @Override
        public void onBindViewHolder(TrailerViewHolder holder, final int position) {
            Glide.with(getActivity())
                    .load("http://img.youtube.com/vi/" +
                            trailerArrayList.get(position).getSource() + "/0.jpg")
                    .into(holder.thumbnail);
            holder.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + trailerArrayList.get(position).getSource()));
                    if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                        getContext().startActivity(intent);
                    } else {
                        Intent viewIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://www.youtube.com/watch?v=" + trailerArrayList.get(position).getSource()));
                        if (viewIntent.resolveActivity(getContext().getPackageManager()) != null) {
                            getContext().startActivity(viewIntent);
                        } else {
                            Snackbar.make(movieOriginalTitle,
                                    R.string.no_app_found_to_show_trailer,
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return trailerArrayList.size();
        }

        class TrailerViewHolder extends RecyclerView.ViewHolder {
            ImageView thumbnail;

            public TrailerViewHolder(View itemView) {
                super(itemView);
                this.thumbnail = (ImageView) itemView.findViewById(R.id.image);
            }
        }
    }

    private class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
        public ReviewAdapter() {
            super();
        }

        @Override
        public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater mLayoutInflater =
                    (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View reviewItemView = mLayoutInflater.inflate(R.layout.item_review, parent, false);
            return new ReviewViewHolder(reviewItemView);
        }

        @Override
        public void onBindViewHolder(ReviewViewHolder holder, int position) {
            holder.author.setText(reviewArrayList.get(position).getAuthor());
            holder.content.setText(reviewArrayList.get(position).getContent());
        }

        @Override
        public int getItemCount() {
            return reviewArrayList.size();
        }

        class ReviewViewHolder extends RecyclerView.ViewHolder {
            TextView author, content;

            public ReviewViewHolder(View itemView) {
                super(itemView);
                this.author = (TextView) itemView.findViewById(R.id.author);
                this.content = (TextView) itemView.findViewById(R.id.content);
            }
        }
    }

    private class FetchMovieTrailersAndReviews extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(final String... params) {
            Uri uri = new Uri.Builder().scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("movie")
                    .appendPath(Integer.toString(movie.getId()))
                    .appendQueryParameter("api_key", getString(R.string.api_key))
                    .appendQueryParameter("append_to_response",
                            getString(R.string.additional_information_required))
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
                Snackbar.make(movieOriginalTitle, R.string.check_internet_connection, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new FetchMovieTrailersAndReviews().execute();
                            }
                        })
                        .show();
            }

            if (response != null) {
                trailerArrayList.clear();
                reviewArrayList.clear();
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray trailers = jsonResponse.getJSONObject("trailers").getJSONArray("youtube");
                    for (int i = 0; i < trailers.length(); ++i) {
                        try {
                            JSONObject trailer = trailers.getJSONObject(i);
                            trailerArrayList.add(new Trailer(
                                    trailer.getString("name"), trailer.getString("source")));
                        } catch (JSONException e) {
                            // TODO : Log.e()
                            e.printStackTrace();
                        }
                    }
                    JSONArray reviews = jsonResponse.getJSONObject("reviews").getJSONArray("results");
                    for (int i = 0; i < reviews.length(); ++i) {
                        try {
                            JSONObject review = reviews.getJSONObject(i);
                            reviewArrayList.add(new Review(
                                    review.getString("author"), review.getString("content")));
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
            trailerAdapter.notifyDataSetChanged();
            reviewAdapter.notifyDataSetChanged();
            if (mShareActionProvider != null) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                if (trailerArrayList.size() < 1) {
                    // TODO: reset shareactionprovider's intent ?
                    return;
                }
                shareIntent.putExtra(Intent.EXTRA_TEXT, "http://www.youtube.com/watch?v=" +
                        trailerArrayList.get(0).getSource());
                mShareActionProvider.setShareIntent(shareIntent);
            }
            //movieAdapter.notifyItemRangeChanged(0, movieList.size());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_movie_details, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider)
                MenuItemCompat.getActionProvider(menuItem);
    }
}
