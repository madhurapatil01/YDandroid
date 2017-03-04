package com.youdescribe.sfsu.youdescribe;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by madhura on 12/24/2016.
 */

public class MovieListAdapter extends BaseAdapter implements Filterable {

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<Movie> mDataSource;
    private ArrayList<Movie> mOriginalValues;
    private ArrayList<Movie> mDisplayedValues;
    int maxYouTubeResults = 5;
    String apiKey = "AIzaSyAI9H-v1Zyt1bN6W7fSz-Zl0jrfU0UYzho";
    String youTubeURLString = "https://www.googleapis.com/youtube/v3/search?part=snippet&fields=items(id,snippet(title,channelTitle))&type=video&maxResults=" + maxYouTubeResults + "&key=" + apiKey + "&q=";


    public MovieListAdapter(Context context, ArrayList<Movie> items) {
        mContext = context;
        mDataSource = items;
        mOriginalValues = items;
        mDisplayedValues = items;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mDisplayedValues.size();
    }

    @Override
    public Object getItem(int position) {
        return mDisplayedValues.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get view for row item
        View rowView = mInflater.inflate(R.layout.list_item, parent, false);

        // Get title element
        TextView titleTextView = (TextView) rowView.findViewById(R.id.video_name);

        // Get subtitle element
        TextView subtitleTextView = (TextView) rowView.findViewById(R.id.movie_list_subtitle);

        // Get thumbnail element
        ImageView thumbnailImageView = (ImageView) rowView.findViewById(R.id.video_list_thumbnail);

        // 1
        final Movie movie = (Movie) getItem(position);

        // 2
        if (movie.movieName == ""){
            movie.movieName = "test";
        }
        titleTextView.setText(movie.movieName);

        //3
        if (movie.isDescribed){
            subtitleTextView.setText("Described by " + movie.authorName);
        }else{
            subtitleTextView.setText("Not Described");
        }

        //4
        String img_url="http://img.youtube.com/vi/"+movie.movieMediaId+"/0.jpg";
        Picasso.with(mContext).load(img_url).placeholder(R.mipmap.ic_launcher).into(thumbnailImageView);

        return rowView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,FilterResults results) {

                mDisplayedValues = (ArrayList<Movie>) results.values; // has the filtered values
                notifyDataSetChanged();  // notifies the data with new filtered values
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values

                if (mOriginalValues == null) {
                    mOriginalValues = new ArrayList<Movie>(mDataSource); // saves the original data in mOriginalValues
                }

                if (constraint == null || constraint.length() == 0) {
                    // set the Original result to return
                    results.values = mOriginalValues;
                    results.count = mOriginalValues.size();
                } else {
                    ArrayList<Movie> FilteredArrList = new ArrayList<Movie>();
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < mOriginalValues.size(); i++) {
                        String data = mOriginalValues.get(i).movieName;
                        if ((data!= null) && (data.toLowerCase().contains(constraint.toString()))) {
                            Movie tempObj = new Movie();
                            tempObj.authorName = mOriginalValues.get(i).authorName;
                            tempObj.isDescribed = mOriginalValues.get(i).isDescribed;
                            tempObj.movieMediaId = mOriginalValues.get(i).movieMediaId;
                            tempObj.movieCreated = mOriginalValues.get(i).movieCreated;
                            tempObj.movieDescription = mOriginalValues.get(i).movieDescription;
                            tempObj.movieId = mOriginalValues.get(i).movieId;
                            tempObj.movieLanguageMainFk = mOriginalValues.get(i).movieLanguageMainFk;
                            tempObj.movieModified = mOriginalValues.get(i).movieModified;
                            tempObj.movieName = mOriginalValues.get(i).movieName;
                            tempObj.movieSource = mOriginalValues.get(i).movieSource;
                            FilteredArrList.add(tempObj);
                        }
                    }

                    //youtube search
                    ArrayList<Movie> youTubeMovies = new ArrayList<Movie>();
                    try {
                        if(constraint.toString().contains(" ")){
                            constraint.toString().replace(" ","+");
                        }
                        youTubeMovies = new YouTubeVideosSearch().execute(youTubeURLString+constraint).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    for (int i=0; i<youTubeMovies.size();i++){
                        FilteredArrList.add(youTubeMovies.get(i));
                    }

                    // set the Filtered result to return
                    results.values = FilteredArrList;
                    results.count = FilteredArrList.size();
                }
                return results;
            }
        };
        return filter;
    }

    private class YouTubeVideosSearch extends AsyncTask<String, Void, ArrayList> {

        DocumentBuilder db = null;
        ArrayList<Movie> ytSearchedMovie = new ArrayList<Movie>();

        @Override
        protected ArrayList doInBackground(String... params) {
            String urlString = params[0];

            String responseString = "";
            URLConnection connection = null;
            JSONArray items = null;
            JSONObject jObject = null;

            try {
                db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
            InputStream response = new InputStream() {
                @Override
                public int read() throws IOException {
                    return 0;
                }
            };
            try {
                connection = new URL(urlString).openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            try {
                response = connection.getInputStream();
                responseString = IOUtils.toString(response, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                jObject  = new JSONObject(responseString);
                items = jObject.getJSONArray("items");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            for(int i = 0; i<maxYouTubeResults ; i++){
                Movie searchedYTVideo = new Movie();
                try {
                    JSONObject id = items.getJSONObject(i).getJSONObject("id");
                    searchedYTVideo.movieMediaId = id.getString("videoId");
                    JSONObject snippet = items.getJSONObject(i).getJSONObject("snippet");
                    searchedYTVideo.movieName = snippet.getString("title");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ytSearchedMovie.add(searchedYTVideo);
            }

            return ytSearchedMovie;

        }

        protected void onPostExecute(ArrayList result) {
            super.onPostExecute(result);
        }
    }

}