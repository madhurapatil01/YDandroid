package com.youdescribe.sfsu.youdescribe;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by madhura on 2/16/2017.
 */

public class AdapterRecyclerView extends RecyclerView.Adapter<AdapterRecyclerView.ViewHolder> {

    private Context mContext;
    public LayoutInflater mInflater;
    private ArrayList<Movie> mDataSource;

    public AdapterRecyclerView (Context context, ArrayList<Movie> movies){
        super();
        mContext = context;
        mDataSource = movies;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView title, subtitle;
        public ImageView thumbnail;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.video_name);
            subtitle = (TextView) itemView.findViewById(R.id.movie_list_subtitle);
            thumbnail = (ImageView) itemView.findViewById(R.id.video_list_thumbnail);
        }
    }

    @Override
    public AdapterRecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent);
        //v.setOnClickListener(mOnClickListener);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Movie movie = (Movie) getItem(position);
        holder.title.setText(movie.movieName);
        holder.subtitle.setText(movie.authorName);

        String img_url="http://img.youtube.com/vi/"+movie.movieMediaId+"/0.jpg";
        Picasso.with(mContext).load(img_url).placeholder(R.mipmap.ic_launcher).into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return mDataSource.size();
    }

    public Object getItem(int position) {
        return mDataSource.get(position);
    }

}
