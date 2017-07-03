package com.youdescribe.sfsu.youdescribe;

import java.util.Comparator;

/**
 * Created by madhura on 12/24/2016.
 */

public class Movie {
    public String movieCreated;
    public String movieDescription;
    public String movieId;
    public String movieLanguageMainFk;
    public String movieMediaId;
    public String movieModified;
    public String movieName;
    public String movieSource;
    public String authorID;
    public String authorName;
    public Boolean isDescribed = false;
    public Boolean ytSearched = false;

    /*Comparator for sorting the list by Movie Created date*/
    public static Comparator<Movie> MovieDateComparator = new Comparator<Movie>() {

        public int compare(Movie s1, Movie s2) {
            //long movieCreated1 = Long.parseLong(s1.movieCreated);
            //long movieCreated2 = Long.parseLong(s2.movieCreated);

            String movieCreated1 = s1.movieCreated;
            String movieCreated2 = s2.movieCreated;
            //String returnNull = "";
            if (movieCreated1!= null && movieCreated2!= null){
                //ascending order
                long movie1 = Long.parseLong(movieCreated1);
                long movie2 = Long.parseLong(movieCreated2);
                //return movieCreated1.compareTo(movieCreated2);
                return (int) (movie2-movie1);
            }
            else {
                return 0;
            }
            //descending order
            //return StudentName2.compareTo(StudentName1);
        }};

}
