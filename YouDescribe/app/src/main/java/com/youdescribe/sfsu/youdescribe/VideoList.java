package com.youdescribe.sfsu.youdescribe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by madhura on 11/30/2016.
 */

public class VideoList extends Activity {
    ListView listView;
    ArrayList<Movie> movies = new ArrayList<>();
    ArrayList<Movie> moviesInList = new ArrayList<>();
    ArrayList<Movie> finalMovies = new ArrayList<>();
    ArrayList<Clip> clips = new ArrayList<>();
    ArrayList<User> users = new ArrayList<>();
    HashMap<String, String> authorIDForMovie = new HashMap<String, String>();
    EditText inputSearch;
    MovieListAdapter adapter;
    ArrayList<String> movieNames = new ArrayList<String>();
    int maxYouTubeResults = 5;
    String apiKey = "AIzaSyAI9H-v1Zyt1bN6W7fSz-Zl0jrfU0UYzho";
    String youTubeURLString = "https://www.googleapis.com/youtube/v3/search?part=snippet&fields=items(id,snippet(title,channelTitle))&type=video&maxResults=" + maxYouTubeResults + "&key=" + apiKey;
    String youTubeURL = new String();
    Handler handler;
    Runnable runnable;
    int startPoint;
    int stopPoint;

    public VideoList() throws MalformedURLException {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videos_list);
        final Intent videoMediaIdIntent = new Intent(this, PlayVideo.class);

        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.list);
        inputSearch = (EditText) findViewById(R.id.inputSearch);

        final DvxApi movie = new DvxApi();
        final HashMap<String, String> mMovies = new HashMap<String, String>() {{
            put("Language", "English");
        }};

        // Retrieve list of movies
        movies = movie.getMovies(mMovies);
        //adapter = new MovieListAdapter(this, movies);
        //listView.setAdapter(adapter);
        moviesInList = movie.searchTable(mMovies);

        for (int i = 0; i<movies.size(); i++){
            Boolean matchFlag = false;
            Movie movieTemp = movies.get(i);
            for(Movie m : moviesInList){
                if(m.movieMediaId.equals(movieTemp.movieMediaId)){
                    matchFlag = true;
                }
            }
            if (matchFlag == false){
                moviesInList.add(movieTemp);
            }
        }


        adapter = new MovieListAdapter(this, moviesInList);

        // Assign adapter to ListView
        listView.setAdapter(adapter);
        listView.setTextFilterEnabled(true);

        findViewById(R.id.loadingPanel).setVisibility(View.GONE);

        // Locate the EditText in listview_main.xml
        inputSearch = (EditText) findViewById(R.id.inputSearch);

        // Enabling Search Filter
        inputSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {

                // When user changes the Text
                VideoList.this.adapter.getFilter().filter(cs.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {

            }
        });

        // ListView Item Click Listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // ListView Clicked item index
                int itemPosition = position;
                // ListView Clicked item value
                Movie selectedVideo = (Movie) listView.getItemAtPosition(position);
                String movieMediaID = selectedVideo.movieMediaId;
                mMovies.put("MediaId",movieMediaID);

                ArrayList<Movie> tempMovie = movie.searchMovie(mMovies);
                String movieID = tempMovie.get(0).movieId;

                String authorId = selectedVideo.authorID;

                Toast.makeText(getApplicationContext(), movieMediaID, Toast.LENGTH_LONG).show();
                Bundle bundle = new Bundle();
                bundle.putString("videoID", movieMediaID);
                bundle.putString("movieID", movieID);
                bundle.putString("authorId", authorId);
                videoMediaIdIntent.putExtras(bundle);

                startActivity(videoMediaIdIntent);
            }
        });

    }

}