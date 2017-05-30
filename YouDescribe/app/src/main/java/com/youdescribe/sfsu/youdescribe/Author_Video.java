package com.youdescribe.sfsu.youdescribe;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by madhura on 5/3/2017.
 */

public class Author_Video extends AppCompatActivity {

    TextView eMovieInfo;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_info);

        eMovieInfo = (TextView) findViewById(R.id.movieTitle);

        eMovieInfo.setText("Authoring will be implemented here");

        //DvxApi dvxApi = new DvxApi();
        //dvxApi.searchMovie();
    }
}
