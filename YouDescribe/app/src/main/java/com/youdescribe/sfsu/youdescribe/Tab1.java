package com.youdescribe.sfsu.youdescribe;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by madhura on 4/3/2017.
 */

public class Tab1 extends Fragment{

    ListView listView;
    ArrayList<? extends Movie> madhuraMovies = new ArrayList<>();
    ArrayList<Movie> movies = new ArrayList<>();
    ArrayList<? extends Movie> moviesInList = new ArrayList<>();
    ArrayList<Movie> tempMoviesList = new ArrayList<>();
    ArrayList<Movie> describedMovies = new ArrayList<>();
    EditText inputSearch;
    MovieListAdapter adapter;
    int maxYouTubeResults = 5;
    int loadMoviesBy = 10;
    String apiKey = "AIzaSyAI9H-v1Zyt1bN6W7fSz-Zl0jrfU0UYzho";
    String youTubeURLString = "https://www.googleapis.com/youtube/v3/search?part=snippet&fields=items(id,snippet(title,channelTitle))&type=video&maxResults=" + maxYouTubeResults + "&key=" + apiKey + "&q=";

    public Tab1() throws MalformedURLException {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //moviesInList = getArguments().getParcelableArrayList("valuesArray");

        /*Bundle args = getArguments();
        if (args != null) {
            madhuraMovies = args.getParcelableArrayList("arraylist");
        }*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab1, container, false);
        //setHasOptionsMenu(true);
        //return v;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                    PackageManager.PERMISSION_GRANTED) {
                Log.d("TAG", "onCreate: permission granted");
            }
            else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        2);
            }
        }

        final Intent videoMediaIdIntent = new Intent(getActivity(), PlayVideo_new.class);

        // Get ListView object from xml
        listView = (ListView) v.findViewById(R.id.list1);
        //inputSearch = (EditText) v.findViewById(R.id.inputSearch);

        final DvxApi movie = new DvxApi();
        final HashMap<String, String> mMovies = new HashMap<String, String>() {{
            put("Language", "English");
        }};


        /*Log.d("Process", "Getting Movies");
        // Retrieve list of movies
        movies = movie.getMovies(mMovies);

        Log.d("Process", "Getting Movies Search Table");
        // Retrieve list of movies from the search table
        moviesInList = movie.searchTable(mMovies);

        Log.d("Process", "Comparing Movies");
        for (Movie movie1 : movies){
            for (Movie movie2 : moviesInList){
                if(movie1.movieMediaId.equals(movie2.movieMediaId)){
                    tempMoviesList.add(movie1);
                }
            }
        }

        for (Movie movie1:tempMoviesList){
            moviesInList.add(movie1);
        }*/

        moviesInList = MainActivity_TabbedViews_2.moviesInList;

        Log.d("Process", "Separating described movies");
        for (int i=0; i<moviesInList.size(); i++){
            Movie newMovie = new Movie();
            if (moviesInList.get(i).isDescribed == true){
                newMovie = moviesInList.get(i);
                describedMovies.add(newMovie);
            }
        }

        adapter = new MovieListAdapter(getActivity(), describedMovies);

        // Assign adapter to ListView
        listView.setAdapter(adapter);
        listView.setTextFilterEnabled(true);

        //findViewById(R.id.loadingPanel).setVisibility(View.GONE);

        // EditText in listview_main.xml for search functionality
        //inputSearch = (EditText) v.findViewById(R.id.inputSearch);


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

                String movieID = null;
                String authorId = null;
                String authorName = null;

                if(selectedVideo.isDescribed) {
                    ArrayList<Movie> tempMovie = movie.searchMovie(mMovies);
                    movieID = tempMovie.get(0).movieId;
                    if (selectedVideo.authorID==null){
                        authorId = tempMovie.get(0).authorID;
                        authorName = tempMovie.get(0).authorName;
                    }
                    else {
                        authorId = selectedVideo.authorID;
                        authorName = selectedVideo.authorName;
                    }
                }

                //Toast.makeText(getApplicationContext(), movieMediaID, Toast.LENGTH_LONG).show();
                Bundle bundle = new Bundle();
                bundle.putString("videoID", movieMediaID);
                bundle.putString("movieID", movieID);
                bundle.putString("authorId", authorId);
                bundle.putString("authorName", authorName);
                bundle.putString("videoTitle", selectedVideo.movieName);
                bundle.putString("videoDescription", selectedVideo.movieDescription);
                videoMediaIdIntent.putExtras(bundle);

                startActivity(videoMediaIdIntent);
            }
        });

        // EditText in listview_main.xml for search functionality
        inputSearch = (EditText) getActivity().findViewById(R.id.searchEditText);

        inputSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {

                // When user changes the Text
                adapter.getFilter().filter(cs.toString());

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

        /*if (listView.getLastVisiblePosition() == listView.getAdapter().getCount() -1 &&
                listView.getChildAt(listView.getChildCount() - 1).getBottom() <= listView.getHeight())
        {
            Log.d("Reached:", "Last Position");
            //It is scrolled all the way down here

        }*/

        /*listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                addItems();
            }
        });*/

        return v;
    }

    public static Tab1 newInstance(int index) throws MalformedURLException {
        Tab1 f = new Tab1();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("index1", index);
        f.setArguments(args);

        return f;
    }
}
