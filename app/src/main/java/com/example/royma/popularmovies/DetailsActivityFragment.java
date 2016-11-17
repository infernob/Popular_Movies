package com.example.royma.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Fragment containing more detailed information about a selected movie.
 */
public class DetailsActivityFragment extends Fragment {

    private static final String LOG_TAG = DetailsActivityFragment.class.getSimpleName();
    private String mMovieId;

    public DetailsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Receives intent sent from main Movies Activity. Includes movieID.
        Intent intent = getActivity().getIntent();

        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            mMovieId = intent.getStringExtra(Intent.EXTRA_TEXT);
        }

        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    private void updateDetails(){
        FetchMovieTask movieTask = new FetchMovieTask();
        movieTask.execute(mMovieId);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateDetails();
    }

    public class FetchMovieTask extends AsyncTask<String, Void, Movie> {

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        private Movie getMovieDataFromJson(String movieJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String MDB_TITLE = "original_title";
            final String MDB_POSTER = "poster_path";
            final String MDB_SYNOPSIS = "overview";
            final String MDB_RATING = "vote_average";
            final String MDB_DATE = "release_date";

            JSONObject movie = new JSONObject(movieJsonStr);

            String title;
            String poster_path;
            String synopsis;
            String rating;
            String release_date;

            // Get the JSON object representing an individual movie
            // JSONObject movie = movieArray.getJSONObject(i);

            // extract title
            title = movie.getString(MDB_TITLE);

            // extract poster path
            poster_path = movie.getString(MDB_POSTER);

            synopsis = movie.getString(MDB_SYNOPSIS);

            rating = movie.getString(MDB_RATING);

            release_date = movie.getString(MDB_DATE);

            return new Movie(title, poster_path, synopsis, rating, release_date);

        }

        @Override
        protected Movie doInBackground(String... movieId) {
            /*
            NETWORK CALLS TO TheMovieDatabase API
            */

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;

            try {
                // Construct the URL for the MovieDatabase query
                // Possible parameters are available at MDB's API page, at
                // http://docs.themoviedb.apiary.io/#
                final String MOVIE_BASE_URL =
                        "https://api.themoviedb.org/3/movie";
                final String APIKEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendPath(movieId[0])
                        .appendQueryParameter(APIKEY_PARAM, BuildConfig.MOVIE_DATABASE_API_KEY)
                        .build();

                // Built URL for Movie Database query
                URL moviesURL= new URL(builtUri.toString());

                // Verbose log of created URL for testing
                Log.v(LOG_TAG, String.valueOf(moviesURL));

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) moviesURL.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                return getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(Movie result) {
            // For poster path creation
            final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
            final String SIZE ="w500";

            if (result != null) {
                // Updates title
                TextView title = (TextView)getView().findViewById(R.id.details_movieTitle_textView);
                title.setText(result.getTitle());
                // Updates release date
                TextView date = (TextView)getView().findViewById(R.id.details_releaseDate_textView);
                date.setText(result.getReleaseDate());
                // Updates user rating
                TextView rating = (TextView)getView().findViewById(R.id.details_userRating_textView);
                rating.setText(result.getVoteAverage());
                // Updates movie synopsis
                TextView synopsis = (TextView)getView().findViewById(R.id.details_movieSynopsis_textView);
                synopsis.setText(result.getOverview());
                // Updates movie poster image
                Picasso
                        .with(getContext())
                        .load(POSTER_BASE_URL + SIZE + result.getPosterPath())
                        .placeholder(R.drawable.picture_unavailable)
                        .into((ImageView) getView().findViewById(R.id.details_poster_imageView));
            }
        }
    }
}
