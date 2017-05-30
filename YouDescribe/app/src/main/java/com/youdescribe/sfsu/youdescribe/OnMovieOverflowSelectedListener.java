package com.youdescribe.sfsu.youdescribe;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

/**
 * Created by madhura on 5/3/2017.
 */

public class OnMovieOverflowSelectedListener implements View.OnClickListener {

    private Movie mMovie;
    private Context mContext;
    private Intent authorMovieIdIntent;

    public OnMovieOverflowSelectedListener(Context context, Movie movie) {
        mContext = context;
        mMovie = movie;
    }

    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onClick(View v) {
        PopupMenu popupMenu = new PopupMenu(mContext, v);
        popupMenu.inflate(R.menu.listview_items_menu);
        popupMenu.show();

        authorMovieIdIntent = new Intent(mContext,Author_Video.class);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.author_movies:
                        mContext.startActivity(authorMovieIdIntent);
                        return true;

                    default:
                        return true;
                }
            }
        });
    }
}
