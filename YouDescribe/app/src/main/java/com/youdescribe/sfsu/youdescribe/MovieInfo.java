package com.youdescribe.sfsu.youdescribe;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.concurrent.ExecutionException;

/**
 * Created by madhura on 5/3/2017.
 */

public class MovieInfo extends AppCompatActivity {

    TextView eMovieTitle;
    TextView eMovieDescription;
    TextView eAuthorName;
    Intent movieInfoIntent = new Intent(this, GetYoutubeDetails.class);

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_info);

        GetYoutubeDetails getMovieDetails = null;
        try {
            getMovieDetails = new GetYoutubeDetails();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String movieDescription = getMovieDetails.videoDescription;

        eMovieTitle = (TextView) findViewById(R.id.movieTitle);
        eMovieDescription = (TextView) findViewById(R.id.movieDescription);
        eAuthorName = (TextView) findViewById(R.id.authorName);

        eMovieTitle.setText("Title: \n" + PlayVideo_new.VIDEO_TITLE);
        eMovieDescription.setText(movieDescription);
        eAuthorName.setText("Author Name: \n" + PlayVideo_new.AUTHOR_NAME);

        //DvxApi dvxApi = new DvxApi();
        //dvxApi.searchMovie();
    }
}
