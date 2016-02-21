package com.example.royma.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MoviesFragment extends Fragment {

    ArrayList <Poster> movieList = new ArrayList<>();


    public MoviesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        movieList.add(new Poster(R.drawable.sample_0));
        movieList.add(new Poster(R.drawable.sample_1));
        movieList.add(new Poster(R.drawable.sample_2));
        movieList.add(new Poster(R.drawable.sample_3));
        movieList.add(new Poster(R.drawable.sample_4));
        movieList.add(new Poster(R.drawable.sample_5));
        movieList.add(new Poster(R.drawable.sample_6));
        movieList.add(new Poster(R.drawable.sample_7));

        ImageAdapter mMoviesAdapter = new ImageAdapter(
                getActivity(),  // Context (fragment's parent activity)
                R.layout.grid_item_movie,    // ID of list item layout
                movieList);

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
}
