package com.example.android.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Movie;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.android.popularmovies.MovieContract.FavoriteEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements MainActivityFragment.OnItemSelectedListener {

    @Override
    public void updateDetail(Map<String, String> map) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("data", ((HashMap) map));
        //For Handsets
        if (findViewById(R.id.fragment_container) != null) {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }
        //For Tablets
        else {
            DetailActivityFragment detailActivityFragment = new DetailActivityFragment();
            detailActivityFragment.setArguments(bundle);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.tablet_detail_container, detailActivityFragment).commit();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Fort Handsets
        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            MainActivityFragment mainActivityFragment = new MainActivityFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.fragment_container, mainActivityFragment).commit();
        }
        //For Tablets
        if (findViewById(R.id.tablet_main_container) != null){
            MainActivityFragment mainActivityFragment = new MainActivityFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.tablet_main_container, mainActivityFragment).commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.highest_rated:
                showHighestRated();
                return true;
            case R.id.most_popular:
                showMostPopular();
                return true;
            case R.id.favorites:
                showFavorite();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showHighestRated(){
        reloadView(getString(R.string.select_top_rated));
    }

    private void showMostPopular() {
        reloadView(getString(R.string.select_most_popular));
    }

    private void showFavorite(){
        reloadView(getString(R.string.select_favorite));
    }

    private void reloadView(String selectData){
        Bundle bundle = new Bundle();
        bundle.putString("selectData", selectData);
        // For Handsets
        if (findViewById(R.id.fragment_container) != null){
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment fragment = new MainActivityFragment();
            fragment.setArguments(bundle);
            fragmentTransaction.replace(R.id.fragment_container, fragment).commit();
        }
        //For Tablets
        else if (findViewById(R.id.tablet_main_container) != null){
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment fragment = new MainActivityFragment();
            fragment.setArguments(bundle);
            fragmentTransaction.replace(R.id.tablet_main_container, fragment).commit();
        }
    }
}
