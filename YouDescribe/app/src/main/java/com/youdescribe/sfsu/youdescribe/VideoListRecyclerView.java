package com.youdescribe.sfsu.youdescribe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;
import android.widget.ListView;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by madhura on 2/12/2017.
 */

public class VideoListRecyclerView extends Activity {
    RecyclerView rv;
    ListView listView;
    ArrayList<Movie> movies = new ArrayList<>();
    ArrayList<Movie> moviesInList = new ArrayList<>();
    ArrayList<Clip> clips = new ArrayList<>();
    ArrayList<User> users = new ArrayList<>();
    //ArrayList<String> authorNames = new ArrayList<>();
    HashMap<String, String> authorIDForMovie = new HashMap<String, String>();
    //ArrayList<Movie> moviesReset = new ArrayList<>();
    EditText inputSearch;
    //ArrayAdapter<String> adapter;
    AdapterRecyclerView adapter;
    ArrayList<String> movieNames = new ArrayList<String>();
    //ArrayList<String> movieMediaIDs = new ArrayList<String>();
    //ArrayList<String> movieIDs = new ArrayList<String>();
    //final String[] refinedMovieMediaIDs = new String[0];
    int maxYouTubeResults = 5;
    String apiKey = "AIzaSyAI9H-v1Zyt1bN6W7fSz-Zl0jrfU0UYzho";
    String youTubeURLString = "https://www.googleapis.com/youtube/v3/search?part=snippet&fields=items(id,snippet(title,channelTitle))&type=video&maxResults=" + maxYouTubeResults + "&key=" + apiKey;
    String youTubeURL = new String();

    public VideoListRecyclerView() throws MalformedURLException {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videos_recycler_view);
        final Intent videoMediaIdIntent = new Intent(this, PlayVideo.class);

        // Get RecyclerView object
        rv = (RecyclerView) findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);

        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.list);
        inputSearch = (EditText) findViewById(R.id.inputSearch);

        final DvxApi movie = new DvxApi();
        HashMap<String, String> mMovies = new HashMap<String, String>() {{
            put("Language", "English");
        }};

        // Retrieve list of movies
        movies = movie.getMovies(mMovies);

        for (int i = 0; i < 70; i++) {
            ArrayList<String> authorIDArrayForMovie = new ArrayList<>();
            final String movieId = movies.get(i).movieId;
            HashMap<String, String> mClips = new HashMap<String, String>() {{
                put("Movie", movieId);
            }};
            clips = movie.getClips(mClips);
            if (clips != null){
                for (int j = 0; j < clips.size(); j++) {
                    int k = 0;
                    String clipAuthor = null;
                    clipAuthor = clips.get(j).clipAuthor;
                    if ((clipAuthor != "") && (clipAuthor != null)) {
                        //retrieve unique author IDs
                        movies.get(i).isDescribed = true;
                        Movie tempObj = new Movie();
                        if (!authorIDForMovie.containsKey(clipAuthor)) {
                            final String finalClipAuthor = clipAuthor;
                            HashMap<String, String> mUsers = new HashMap<String, String>() {{
                                put("UserId", finalClipAuthor);
                            }};
                            users = movie.getUsers(mUsers);
                            authorIDForMovie.put(clipAuthor, users.get(0).userHandle);
                        }
                        if (!authorIDArrayForMovie.contains(clipAuthor)){
                            authorIDArrayForMovie.add(clipAuthor);
                            //authorNames.add(authorIDForMovie.get(clipAuthor));
                            //tempObj.authorName = users.get(0).userHandle;
                            tempObj.authorID = clipAuthor;
                            tempObj.authorName = authorIDForMovie.get(clipAuthor);
                            tempObj.isDescribed = movies.get(i).isDescribed;
                            tempObj.movieMediaId = movies.get(i).movieMediaId;
                            tempObj.movieCreated = movies.get(i).movieCreated;
                            tempObj.movieDescription = movies.get(i).movieDescription;
                            tempObj.movieId = movies.get(i).movieId;
                            tempObj.movieLanguageMainFk = movies.get(i).movieLanguageMainFk;
                            tempObj.movieModified = movies.get(i).movieModified;
                            tempObj.movieName = movies.get(i).movieName;
                            tempObj.movieSource = movies.get(i).movieSource;
                            moviesInList.add(tempObj);
                            k++;
                        }
                    }
                }
            }
            else {
                /*Movie tempObj = new Movie();
                tempObj.authorName = movies.get(i).authorName;
                tempObj.isDescribed = movies.get(i).isDescribed;
                tempObj.movieMediaId = movies.get(i).movieMediaId;
                tempObj.movieCreated = movies.get(i).movieCreated;
                tempObj.movieDescription = movies.get(i).movieDescription;
                tempObj.movieId = movies.get(i).movieId;
                tempObj.movieLanguageMainFk = movies.get(i).movieLanguageMainFk;
                tempObj.movieModified = movies.get(i).movieModified;
                tempObj.movieName = movies.get(i).movieName;
                tempObj.movieSource = movies.get(i).movieSource;
                moviesInList.add(tempObj);*/
                moviesInList.add((movies.get(i)));
                //authorNames.add("");
            }
        }

        /*for (int i=0; i<moviesInList.size(); i++){
            moviesInList.get(i).authorName = authorNames.get(i);
        }*/

        Collections.sort(moviesInList, Movie.MovieDateComparator );
        /*Collections.sort(moviesInList, new Comparator<Movie>() {
                    @Override
                    public int compare(Movie o1, Movie o2) {
                        int movieCreated1 = Integer.parseInt(o1.movieCreated);
                        int movieCreated2 = Integer.parseInt(o2.movieCreated);
                        return movieCreated1 - movieCreated2;
                    }
                });*/

        /*try {
            int j= 0;
            for (int i=0;i<movies.size();i++) {
                String temp = movies.get(i).getString("movieName");
                if((temp != "") && (temp != null)) {
                    movieNames.add(j, movies.get(i).getString("movieName"));
                    movieMediaIDs.add(j, movies.get(i).getString("movieMediaId"));
                    movieIDs.add(j, movies.get(i).getString("movieId"));
                    j++;
                }
                else{
                    movieNames.add(j, "test");
                    movieMediaIDs.add(j, movies.get(i).getString("movieMediaId"));
                    movieIDs.add(j, movies.get(i).getString("movieId"));
                    j++;
                }
            }
        } catch (JSONException e) {
                e.printStackTrace();
            }*/

        //moviesReset = movie.getMovies(m);


        adapter = new AdapterRecyclerView(this, moviesInList);
        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data


        //adapter = new ArrayAdapter<String>(this, R.layout.list_item, R.id.video_name, movieNames);
        //int count = adapter.getCount();

        //MovieListAdapter adapter = new MovieListAdapter(this, movieNames);

        //adapter = new SimpleAdapter(this, movieNames, android.R.layout.simple_list_item_2);

        // Assign adapter to RecyclerView
        rv.setAdapter(adapter);

        // Assign adapter to ListView
        //listView.setAdapter(adapter);
        //listView.setTextFilterEnabled(true);

        /*for (int i = 100; i < movies.size(); i++) {
            ArrayList<String> authorIDArrayForMovie = new ArrayList<>();
            final String movieId = movies.get(i).movieId;
            HashMap<String, String> mClips = new HashMap<String, String>() {{
                put("Movie", movieId);
            }};
            clips = movie.getClips(mClips);
            if (clips != null){
                for (int j = 0; j < clips.size(); j++) {
                    int k = 0;
                    String clipAuthor = null;
                    clipAuthor = clips.get(j).clipAuthor;
                    if ((clipAuthor != "") && (clipAuthor != null)) {
                        //retrieve unique author IDs
                        movies.get(i).isDescribed = true;
                        Movie tempObj = new Movie();
                        if (!authorIDForMovie.containsKey(clipAuthor)) {
                            final String finalClipAuthor = clipAuthor;
                            HashMap<String, String> mUsers = new HashMap<String, String>() {{
                                put("UserId", finalClipAuthor);
                            }};
                            users = movie.getUsers(mUsers);
                            authorIDForMovie.put(clipAuthor, users.get(0).userHandle);
                        }
                        if (!authorIDArrayForMovie.contains(clipAuthor)){
                            authorIDArrayForMovie.add(clipAuthor);
                            //authorNames.add(authorIDForMovie.get(clipAuthor));
                            //tempObj.authorName = users.get(0).userHandle;
                            tempObj.authorID = clipAuthor;
                            tempObj.authorName = authorIDForMovie.get(clipAuthor);
                            tempObj.isDescribed = movies.get(i).isDescribed;
                            tempObj.movieMediaId = movies.get(i).movieMediaId;
                            tempObj.movieCreated = movies.get(i).movieCreated;
                            tempObj.movieDescription = movies.get(i).movieDescription;
                            tempObj.movieId = movies.get(i).movieId;
                            tempObj.movieLanguageMainFk = movies.get(i).movieLanguageMainFk;
                            tempObj.movieModified = movies.get(i).movieModified;
                            tempObj.movieName = movies.get(i).movieName;
                            tempObj.movieSource = movies.get(i).movieSource;
                            moviesInList.add(tempObj);
                            k++;
                        }
                    }
                }
            }
            else {
                moviesInList.add((movies.get(i)));
                //authorNames.add("");
            }
        }

        Collections.sort(moviesInList, Movie.MovieDateComparator );
        adapter.notifyDataSetChanged();*/

        //findViewById(R.id.loadingPanel).setVisibility(View.GONE);

        // Enabling Search Filter

        /*inputSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {

                // When user changes the Text
                VideoListRecyclerView.this.adapter.getFilter().filter(cs.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {

            }
        });*/



        // ListView Item Click Listener
        /*listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // ListView Clicked item index
                int itemPosition = position;
                // ListView Clicked item value
                Movie selectedVideo = (Movie) listView.getItemAtPosition(position);
                String movieMediaID = selectedVideo.movieMediaId;
                String movieID = selectedVideo.movieId;

                //String  itemValue = (String) listView.getItemAtPosition(position);
                // Show Alert
                //Toast.makeText(getApplicationContext(), movieMediaIDs.get(position), Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), movieMediaID, Toast.LENGTH_LONG).show();
                Bundle bundle = new Bundle();
                bundle.putString("videoID", movieMediaID);
                bundle.putString("movieID", movieID);
                videoMediaIdIntent.putExtras(bundle);

                startActivity(videoMediaIdIntent);
            }
        });*/

    }

    /*class getSearchResults extends AsyncTask<String, Void, JSONObject> {

        JSONObject json;
        @Override
        protected JSONObject doInBackground(String... params) {

            try {
                json = new JSONObject(IOUtils.toString(new URL(youTubeURL), Charset.forName("UTF-8")));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return json;
        }

        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
        }
    }

    /*public void resetMovieNames (){
        try {
            int j= 0;
            for (int i=0;i<moviesReset.size();i++) {
                String temp = moviesReset.get(i).getString("movieName");
                if((temp != "") && (temp != null)) {
                    movieMediaIDs.add(j, moviesReset.get(i).getString("movieMediaId"));
                    movieNames.add(j, moviesReset.get(i).getString("movieName"));
                    movieIDs.add(j, movies.get(i).getString("movieId"));
                    j++;
                }
                else{
                    movieNames.add(j, "test");
                    movieMediaIDs.add(j, moviesReset.get(i).getString("movieMediaId"));
                    movieIDs.add(j, movies.get(i).getString("movieId"));
                    j++;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }*/

}