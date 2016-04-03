package com.example.android.popularmovies;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    final String LOG_TAG = MainActivity.class.getSimpleName();
    private String selectData = "movieData";

    OnItemSelectedListener callback;

    public interface OnItemSelectedListener{
        public void updateDetail(Map<String, String> map);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        Bundle bundle = getArguments();
        if(bundle != null){
            String selectDataArg = bundle.getString("selectData");
            if (selectDataArg != null){
                selectData = selectDataArg;
            }
        }

        View rootView = inflater.inflate(R.layout.movie_grid, container, false);

        FetchMovieTask fetchMovieTask = new FetchMovieTask(rootView, selectData);
        fetchMovieTask.execute();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_fragment_main, menu);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            callback = (OnItemSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnItemSelectedListener");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class FetchMovieTask extends AsyncTask<String, Void, Void> {

        private View rootView;
        private List<Map> movieData;
        private List<Map> topRatedData;
        private String selectData = "movieData";

        public FetchMovieTask(View rootView, String selectData) {
            this.rootView = rootView;
            this.movieData = new ArrayList<>();
            this.topRatedData = new ArrayList<>();
            this.selectData = selectData;
        }

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        /**
         * make json string data into array
         *
         * @param movieJsonStr
         * @return
         * @throws JSONException
         */
        private String[] getMovieDataFromJson(String movieJsonStr)
                throws JSONException {

            //the name of the Json object that need to be extract
            final String RESULTS = "results";

            if(movieJsonStr == null){
                movieJsonStr="";
            }

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(RESULTS);

            String[] resultStrs = new String[20];

            for (int i = 0; i < movieArray.length(); i++) {
                JSONObject respectiveMovieObject = movieArray.getJSONObject(i);
                resultStrs[i] = respectiveMovieObject.getString("title");

                Map<String, String> map = new HashMap<>();
                map.put("title", respectiveMovieObject.getString("title"));
                map.put("poster_path", respectiveMovieObject.get("poster_path").toString());
                map.put("overview", respectiveMovieObject.get("overview").toString());
                map.put("release_date", respectiveMovieObject.get("release_date").toString());
                map.put("vote_average", respectiveMovieObject.get("vote_average").toString());
                map.put("id", respectiveMovieObject.get("id").toString());
                movieData.add(map);
            }

            return resultStrs;
        }

        /**
         * make json string data into array
         *
         * @param movieJsonStr
         * @return
         * @throws JSONException
         */
        private void getTopRatedDataFromJson(String movieJsonStr)
                throws JSONException {

            //the name of the Json object that need to be extract
            final String RESULTS = "results";

            if(movieJsonStr == null){
                movieJsonStr = "";
            }

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(RESULTS);

            for (int i = 0; i < movieArray.length(); i++) {
                JSONObject respectiveMovieObject = movieArray.getJSONObject(i);
                Map<String, String> map = new HashMap<>();
                map.put("title", respectiveMovieObject.getString("title"));
                map.put("poster_path", respectiveMovieObject.get("poster_path").toString());
                map.put("overview", respectiveMovieObject.get("overview").toString());
                map.put("release_date", respectiveMovieObject.get("release_date").toString());
                map.put("vote_average", respectiveMovieObject.get("vote_average").toString());
                map.put("id", respectiveMovieObject.get("id").toString());
                topRatedData.add(map);
            }
        }

        @Override
        protected Void doInBackground(String... params) {
            final String POPULAR_URL = "http://api.themoviedb.org/3/movie/popular";
            final String TOP_RATED_URL = "http://api.themoviedb.org/3/movie/top_rated";

            try {
                getMovieDataFromJson(getJsonStr(POPULAR_URL));
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            try {
                getTopRatedDataFromJson(getJsonStr(TOP_RATED_URL));
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        private String getJsonStr(String arg_url){

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonStr = null;

            try{
                Uri uri = Uri.parse(arg_url);
                Uri.Builder builder = uri.buildUpon()
                        .appendQueryParameter("api_key", getString(R.string.api_key));
                Uri builtUri = builder.build();
                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    jsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    jsonStr = null;
                }
                jsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return jsonStr;
        }

        @Override
        protected void onPostExecute(Void v) {
            // Get images and put it into a grid view.
            GridView gridview = (GridView) rootView.findViewById(R.id.gridview_movies);
            if(selectData.equals(getString(R.string.select_most_popular))){
                gridview.setAdapter(new ImageAdapter(getActivity(), movieData));
                gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                        callback.updateDetail((Map) movieData.get(position));
                    }
                });
            }else if(selectData.equals(getString(R.string.select_top_rated))){
                gridview.setAdapter(new ImageAdapter(getActivity(), topRatedData));
                gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                        callback.updateDetail((Map) topRatedData.get(position));
                    }
                });
            }else if(selectData.equals(getString(R.string.select_favorite))){
                final List favoriteList = loadFavorite();
                gridview.setAdapter(new ImageAdapter(getActivity(), favoriteList));
                gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                        callback.updateDetail((Map) favoriteList.get(position));
                    }
                });


            }
        }

        private List<Map> loadFavorite(){
            MovieDBHelper movieDBHelper = new MovieDBHelper(getContext());
            SQLiteDatabase db = movieDBHelper.getReadableDatabase();
            String[] projection = {
                    MovieContract.FavoriteEntry.COLUMN_NAME_ID,
                    MovieContract.FavoriteEntry.COLUMN_NAME_TITLE,
                    MovieContract.FavoriteEntry.COLUMN_NAME_OVERVIEW,
                    MovieContract.FavoriteEntry.COLUMN_NAME_POSTER_PATH,
                    MovieContract.FavoriteEntry.COLUMN_NAME_RELEASE_DATE,
                    MovieContract.FavoriteEntry.COLUMN_NAME_VOTE_AVERAGE
            };
            String sortOrder =
                    MovieContract.FavoriteEntry.COLUMN_NAME_TITLE + " DESC";
            Cursor c = db.query(
                    MovieContract.FavoriteEntry.TABLE_NAME,  // The table to query
                    projection,                               // The columns to return
                    null,                                // The columns for the WHERE clause
                    null,                            // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    sortOrder                                 // The sort order
            );
            List<Map> favoriteList = new ArrayList<>();
            Integer integer = c.getCount();
            Boolean hasNext = c.moveToFirst();
            while (hasNext) {
                Map<String, String> map = new HashMap<>();
                String title = c.getString(c.getColumnIndex(MovieContract.FavoriteEntry.COLUMN_NAME_TITLE));
                map.put("title", title);
                String movie_id = c.getString(c.getColumnIndex(MovieContract.FavoriteEntry.COLUMN_NAME_ID));
                map.put("id", movie_id);
                String overview = c.getString(c.getColumnIndex(MovieContract.FavoriteEntry.COLUMN_NAME_OVERVIEW));
                map.put("overview", overview);
                String vote_average = c.getString(c.getColumnIndex(MovieContract.FavoriteEntry.COLUMN_NAME_VOTE_AVERAGE));
                map.put("vote_average", vote_average);
                String release_date = c.getString(c.getColumnIndex(MovieContract.FavoriteEntry.COLUMN_NAME_RELEASE_DATE));
                map.put("release_date", release_date);
                String poster_path = c.getString(c.getColumnIndex(MovieContract.FavoriteEntry.COLUMN_NAME_POSTER_PATH));
                map.put("poster_path", poster_path);
                favoriteList.add(map);
                hasNext = c.moveToNext();
            }
            c.close();
            return favoriteList;
        }
    }
}
