package com.example.royma.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

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
                new ArrayList<String>());

        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);

        GridView gridView = (GridView) rootView.findViewById(R.id.movies_gridView);
        gridView.setAdapter(mMoviesAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Toast.makeText(getContext(), String.valueOf(position), Toast.LENGTH_SHORT).show();
            }
        });


        return rootView;
    }

    // TODO: Uncomment once preferences settings have been added
    private void updateMovies(){
        FetchMovieTask movieTask = new FetchMovieTask();
        // Retrieve user preferred sort. Use default if none found
//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
//        String locationPref = sharedPref.getString(getString(R.string.pref_location_key),
//                getString(R.string.pref_location_default));
        movieTask.execute();

    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }


    public class FetchMovieTask extends AsyncTask <Void, Void, String[]>{

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        private String[] getMovieDataFromJson(String movieJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String MDB_RESULTS = "results";
            final String MDB_TITLE = "original_title";
            final String MDB_POSTER = "poster_path";
            final String MDB_SYNOPSIS = "overview";
            final String MDB_RATING = "vote_average";
            final String MDB_DATE = "release_date";

            JSONObject moviesJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = moviesJson.getJSONArray(MDB_RESULTS);


            String[] resultStrs = new String[movieArray.length() + 1];
            for(int i = 0; i < movieArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String title;
                String synopsis;
                String poster_path;

                // Get the JSON object representing an individual movie
                JSONObject movie = movieArray.getJSONObject(i);

                // extract original title from object.
                title = movie.getString(MDB_TITLE);

                // extract synopsis
                synopsis = movie.getString(MDB_SYNOPSIS);

                // extract poster path
                poster_path = movie.getString(MDB_POSTER);


                resultStrs[i] = poster_path;
            }

            return resultStrs;

        }

        @Override
        protected String[] doInBackground(Void... params) {
            /*
            NETWORK CALLS TO TheMovieDatabase API
            */

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;

//            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
//            String tempUnitPref = sharedPref.getString(getString(R.string.pref_tempUnit_key),
//                    getString(R.string.pref_tempUnit_default));
            // TODO: initialise "sortBy" using shared preference once created
            String sortBy = "popularity.desc";

            try {
                // Construct the URL for the MovieDatabase query
                // Possible parameters are available at MDB's API page, at
                // http://docs.themoviedb.apiary.io/#
                final String MOVIES_BASE_URL =
                        "https://api.themoviedb.org/3/discover/movie?";
                final String SORT_PARAM = "sort_by";
                final String APIKEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_PARAM, sortBy)
                        .appendQueryParameter(APIKEY_PARAM, BuildConfig.MOVIE_DATABASE_API_KEY)
                        .build();

                // Built URL for Open Weather Map query
                URL weatherURL= new URL(builtUri.toString());

                Log.v(LOG_TAG, String.valueOf(weatherURL));

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) weatherURL.openConnection();
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
        protected void onPostExecute(String[] result) {
            // For poster path creation
            final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
            final String SIZE ="w500";

            if (result != null){
                mMoviesAdapter.clear();
                for (String posterPath : result){
                    // Add complete poster path to adapter
                    mMoviesAdapter.add(POSTER_BASE_URL + SIZE + posterPath);
                }
                // New data has returned from server
            }
        }
    }
}
