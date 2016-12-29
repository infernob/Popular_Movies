package com.example.royma.popularmovies;

/**
 * Created by Royma on 31/05/2016.
 */
class Movie {
    public String id;
    public String title;
    private String posterPath;
    private String overview;
    private String vote_average;
    private String release_date;

    public Movie(){
        super();
    }

    Movie (String id, String title, String posterPath){
        super();
        this.id = id;
        this.title = title;
        this.posterPath = posterPath;
    }

    Movie (String title, String posterPath, String overview, String vote_average,
                  String release_date){
        super();
        this.title = title;
        this.posterPath = posterPath;
        this.overview = overview;
        this.vote_average = vote_average;
        this.release_date = release_date;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    String getPosterPath() {
        return posterPath;
    }

    String getOverview() {
        return overview;
    }

    String getVoteAverage() {
        return vote_average;
    }

    String getReleaseDate() {
        return release_date;
    }
}
