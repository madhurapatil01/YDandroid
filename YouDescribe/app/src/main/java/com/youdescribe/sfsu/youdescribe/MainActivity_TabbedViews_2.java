package com.youdescribe.sfsu.youdescribe;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.EditText;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity_TabbedViews_2 extends AppCompatActivity {

    ArrayList<Movie> movies = new ArrayList<>();
    public static ArrayList<Movie> moviesInList = new ArrayList<>();
    ArrayList<Movie> tempMoviesList = new ArrayList<>();
    ArrayList<Movie> describedMovies = new ArrayList<>();
    MovieListAdapter adapter;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    EditText searchTextBox;
    Bundle movieBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main__tabbed_views);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        searchTextBox = (EditText) findViewById(R.id.searchEditText);
        searchTextBox.setEnabled(true);
        // Create the adapter that will return a fragment for each of the two
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        final DvxApi movie = new DvxApi();
        final HashMap<String, String> mMovies = new HashMap<String, String>() {{
            put("Language", "English");
        }};

        Log.d("Process", "Getting Movies");
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
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            //Returning the current tabs
            switch (position){
                case 0:
                    Tab1 tab1 = null;
                    try {
                        tab1 = new Tab1();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    return tab1;
                case 1:
                    Tab2 tab2 = null;
                    try {
                        tab2 = new Tab2();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    return tab2;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Decribed Videos";
                case 1:
                    return "Not Described Videos";
            }
            return null;
        }
    }
}
