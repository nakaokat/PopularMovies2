package com.example.android.popularmovies;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.popularmovies.MovieContract.FavoriteEntry;

/**
 * Created by nakaokataiki on 2016/04/02.
 */
public class MovieDBHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "movie.db";

    public MovieDBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_FAVORITE_TABLE = "CREATE TABLE " + FavoriteEntry.TABLE_NAME + " (" +
                FavoriteEntry.COLUMN_NAME_TITLE + " TEXT NOT NULL, " +
                FavoriteEntry.COLUMN_NAME_OVERVIEW + " TEXT NOT NULL, " +
                FavoriteEntry.COLUMN_NAME_VOTE_AVERAGE + " TEXT NOT NULL, " +
                FavoriteEntry.COLUMN_NAME_RELEASE_DATE + " TEXT NOT NULL, " +
                FavoriteEntry.COLUMN_NAME_POSTER_PATH + " TEXT NOT NULL, " +
                FavoriteEntry.COLUMN_NAME_ID + " TEXT PRIMARY KEY);";

        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion){
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavoriteEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }


}
