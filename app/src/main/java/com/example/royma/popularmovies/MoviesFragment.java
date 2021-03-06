package com.example.royma.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MoviesFragment extends Fragment {
    // Adapter must be initialised outside of methods
    ImageAdapter mMoviesAdapter;

    public MoviesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // ArrayAdapter takes data from a source and creates a view that represents
        // each data entry (populates gridView)
        mMoviesAdapter = new ImageAdapter(
                getActivity(),  // Context (fragment's parent activity)
                R.layout.grid_item_movie,    // ID of grid item layout
                new ArrayList<Movie>());

        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);

        GridView gridView = (GridView) rootView.findViewById(R.id.movies_gridView);
        gridView.setAdapter(mMoviesAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Retrieves movie ID from object at current within adapter
                String movieId = ((Movie)mMoviesAdapter.getItem(position)).getId();
                // Launches Detail activity with selected movie's ID passed as an extra
                Intent detailIntent = new Intent(getContext(), DetailsActivity.class);
                detailIntent.putExtra(Intent.EXTRA_TEXT, movieId);
                startActivity(detailIntent);
            }
        });

        return rootView;
    }

    // Populates or refreshes the view of movie posters
    private void updateMovies(){
        FetchMovieTask movieTask = new FetchMovieTask();
        // Retrieve user preferred sort. Use default if none found
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String sortPref = sharedPref.getString(getString(R.string.pref_sortBy_key),
                getString(R.string.pref_sortBy_default));
        movieTask.execute(sortPref);

    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }


    public class FetchMovieTask extends AsyncTask<String, Void, Movie[]> {

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        private Movie[] getMovieDataFromJson(String movieJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String MDB_RESULTS = "results";
            final String MDB_ID = "id";
            final String MDB_TITLE = "original_title";
            final String MDB_POSTER = "poster_path";

            JSONObject moviesJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = moviesJson.getJSONArray(MDB_RESULTS);


            Movie[] resultMovs = new Movie[movieArray.length()];
            for(int i = 0; i < movieArray.length(); i++) {

                String movie_id;
                String title;
                String poster_path;

                // Get the JSON object representing an individual movie
                JSONObject movie = movieArray.getJSONObject(i);

                // extract movie ID from object.
                movie_id = movie.getString(MDB_ID);

                // extract title
                title = movie.getString(MDB_TITLE);

                // extract poster path
                poster_path = movie.getString(MDB_POSTER);

                resultMovs[i] = new Movie(movie_id, title, poster_path);

            }

            return resultMovs;

        }

        @Override
        protected Movie[] doInBackground(String... sortBy) {
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
                final String MOVIES_BASE_URL =
                        "https://api.themoviedb.org/3/movie";
                final String APIKEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                        .appendPath(sortBy[0])
                        .appendQueryParameter(APIKEY_PARAM, BuildConfig.MOVIE_DATABASE_API_KEY)
                        .build();

                // Built URL for Movie Database query
                URL moviesURL= new URL(builtUri.toString());

                // Verbose log of created URL
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
        protected void onPostExecute(Movie[] result) {
            if (result != null){
                mMoviesAdapter.clear();
                for (Movie movie : result){
                    // Add complete movie object to adapter
                    mMoviesAdapter.add(movie);
                }
                // New data has returned from server
            }
        }
    }
}
