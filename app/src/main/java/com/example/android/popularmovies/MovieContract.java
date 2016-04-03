package com.example.android.popularmovies;

import android.provider.BaseColumns;

/**
 * Created by nakaokataiki on 2016/04/02.
 */
public class MovieContract {

    public MovieContract(){

    }

    public static abstract class FavoriteEntry implements BaseColumns {

        public static final String TABLE_NAME = "favorite";

        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_OVERVIEW = "overview";
        public static final String COLUMN_NAME_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_NAME_RELEASE_DATE = "release_date";
        public static final String COLUMN_NAME_POSTER_PATH = "poster_path";

    }

}
