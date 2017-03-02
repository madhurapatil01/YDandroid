package com.youdescribe.sfsu.youdescribe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by madhura on 12/24/2016.
 */

public class MovieListAdapter extends BaseAdapter implements Filterable {

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<Movie> mDataSource;
    private ArrayList<Movie> mOriginalValues;
    private ArrayList<Movie> mDisplayedValues;

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
            subtitleTextView.setText("");
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

                /********
                 *
                 *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                 *  else does the Filtering and returns FilteredArrList(Filtered)
                 *
                 ********/
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
                    // set the Filtered result to return
                    results.values = FilteredArrList;
                    results.count = FilteredArrList.size();
                }
                return results;
            }
        };
        return filter;
    }

}