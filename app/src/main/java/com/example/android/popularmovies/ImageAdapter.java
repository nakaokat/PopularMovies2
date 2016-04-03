package com.example.android.popularmovies;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

/**
 * Created by nakaokataiki on 2016/02/27.
 */
public class ImageAdapter extends BaseAdapter {
    private Context context;
    private List movieData;

    public ImageAdapter(Context context, List data) {
        this.context = context;
        this.movieData = data;
    }

    public int getCount() {
        return movieData.size();
    }

    public Object getItem(int position) {
        return movieData.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    //create a new ImageView for each item referenced by the Adapter
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(0, 0, 0, 0);
        } else {
            imageView = (ImageView) convertView;
        }
        String imageBase = context.getString(R.string.image_base_url);
        String url = imageBase + ((Map) movieData.get(position)).get("poster_path").toString();
        Picasso.with(context).load(url).into(imageView);
        return imageView;
    }
}
