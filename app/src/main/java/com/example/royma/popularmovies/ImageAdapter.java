package com.example.royma.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by Royma on 21/02/2016.
 */
public class ImageAdapter extends ArrayAdapter<Poster>{
    Context context;
    int layoutResourceId;
    ArrayList<Poster> movieList = new ArrayList<>();

    public ImageAdapter(Context context, int layoutResourceId, ArrayList<Poster> objects) {
        super(context, layoutResourceId, objects);
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
        MovieHolder holder = null;

        if(rowView == null)
        {
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(layoutResourceId, parent, false);

            holder = new MovieHolder();
            holder.movieImage = (ImageView)rowView.findViewById(R.id.grid_item_movie_imageView);

            rowView.setTag(holder);
        }
        else
        {
            holder = (MovieHolder)rowView.getTag();
        }

        Poster poster = movieList.get(position);
        holder.movieImage.setImageResource(poster.getMovieImage());

        return rowView;
    }

    static class MovieHolder
    {
        ImageView movieImage;
    }

}
