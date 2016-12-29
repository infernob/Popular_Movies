package com.example.royma.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Roy on 21/02/2016.
 * Used to easily manage and load images within the app
 */
public class ImageAdapter extends ArrayAdapter{
    Context context;
    int layoutResourceId;
    ArrayList<Movie> movieList = new ArrayList<>();

    public ImageAdapter(Context context, int layoutResourceId, ArrayList<Movie> objects) {
        super(context, layoutResourceId, objects);
        this.context = context;
        movieList = objects;
        this.layoutResourceId = layoutResourceId;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        // For poster path creation
        final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
        final String SIZE ="w500";

        if(rowView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(layoutResourceId, parent, false);
        }

        Picasso
                .with(context)
                .load(POSTER_BASE_URL + SIZE + movieList.get(position).getPosterPath())
                .into((ImageView) rowView.findViewById(R.id.grid_item_movie_imageView));

        return rowView;
    }

}
