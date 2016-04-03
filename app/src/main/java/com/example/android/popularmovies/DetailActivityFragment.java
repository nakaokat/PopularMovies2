package com.example.android.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Movie;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

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
public class DetailActivityFragment extends Fragment {

    public DetailActivityFragment() {
    }

    Map<String, String> movieData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getActivity().getIntent();
        Bundle bundle;
        View rootView;
        // For Tablets
        if(intent.getExtras() == null && getArguments() != null){
            bundle = getArguments();
            movieData = (Map) bundle.getSerializable("data");
            rootView = inflater.inflate(R.layout.fragment_detail, null);
        }
        // For Handsets
        else{
            bundle = intent.getExtras();
            if (bundle != null){
                movieData = (Map) bundle.getSerializable("data");
            }
            rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        }

        if (bundle == null){
            return rootView;
        }
        else{
            TextView titleTextView = (TextView) rootView.findViewById(R.id.movie_title);
            titleTextView.setText(movieData.get("title"));

            TextView overviewTextView = (TextView) rootView.findViewById(R.id.overview);
            overviewTextView.setText(movieData.get("overview"));

            String vote_average = movieData.get("vote_average");
            TextView voteAverageTextView = (TextView) rootView.findViewById(R.id.vote_average);
            voteAverageTextView.setText("Vote Average: " + vote_average + "/10.00");

            TextView releaseDateTextView = (TextView) rootView.findViewById(R.id.release_date);
            releaseDateTextView.setText("Release Date: " + movieData.get("release_date"));

            String poster_path = movieData.get("poster_path");
            ImageView imageView = (ImageView) rootView.findViewById(R.id.poster_image);
            imageView.setAdjustViewBounds(true);
            Picasso.with(getActivity()).load(getString(R.string.image_base_url) + poster_path).into(imageView);

            Button button = (Button) rootView.findViewById(R.id.add_favorite_button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MovieDBHelper movieDBHelper = new MovieDBHelper(getActivity());
                    SQLiteDatabase db = movieDBHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put(MovieContract.FavoriteEntry.COLUMN_NAME_ID, movieData.get("id"));
                    values.put(MovieContract.FavoriteEntry.COLUMN_NAME_TITLE, movieData.get("title"));
                    values.put(MovieContract.FavoriteEntry.COLUMN_NAME_OVERVIEW, movieData.get("overview"));
                    values.put(MovieContract.FavoriteEntry.COLUMN_NAME_POSTER_PATH, movieData.get("poster_path"));
                    values.put(MovieContract.FavoriteEntry.COLUMN_NAME_RELEASE_DATE, movieData.get("release_date"));
                    values.put(MovieContract.FavoriteEntry.COLUMN_NAME_VOTE_AVERAGE, movieData.get("vote_average"));
                    db.insert(MovieContract.FavoriteEntry.TABLE_NAME, null, values);
                    Toast.makeText(getContext(), "MARK AS FAVORITE", Toast.LENGTH_SHORT).show();
                }
            });

            FetchDetailTask fetchDetailTask = new FetchDetailTask(rootView, movieData.get("id"));
            fetchDetailTask.execute();

            return rootView;
        }
    }


    class FetchDetailTask extends AsyncTask<String, Void, Void>{
        private View rootView;
        private List<Map> videoData;
        private List<Map> reviewData;
        private String id;
        private final String LOG_TAG = FetchDetailTask.class.getSimpleName();

        public FetchDetailTask(View rootView, String id){
            this.rootView = rootView;
            this.videoData = new ArrayList<>();
            this.reviewData = new ArrayList<>();
            this.id = id;
        }

        private void getVideoDataFromJson(String videoJsonStr)
        throws JSONException{
            if (videoJsonStr == null){
                videoJsonStr = "";
            }

            //the name of the Json object that need to be extract
            final String RESULTS = "results";
            JSONObject videoJson = new JSONObject(videoJsonStr);
            JSONArray videoArray = videoJson.getJSONArray(RESULTS);

            for (int i = 0; i < videoArray.length(); i++){
                JSONObject itemJson = videoArray.getJSONObject(i);
                Map<String, String> map = new HashMap<>();
                if (itemJson.getString("site").equals("YouTube") && itemJson.getString("type").equals("Trailer")){
                    map.put("youtube_key", itemJson.getString("key"));
                    videoData.add(map);
                }
            }

        }

        private void getReviewDataFromJson(String reviewJsonStr)
                throws JSONException{
            if(reviewJsonStr == null){
                reviewJsonStr = "";
            }

            //the name of the Json object that need to be extract
            final String RESULTS = "results";
            JSONObject reviewJson = new JSONObject(reviewJsonStr);
            JSONArray reviewArray = reviewJson.getJSONArray(RESULTS);

            for (int i = 0; i < reviewArray.length(); i++){
                JSONObject itemJson = reviewArray.getJSONObject(i);
                Map<String, String> map = new HashMap<>();
                map.put("content", itemJson.getString("content"));
                map.put("author", itemJson.getString("author"));
                map.put("id", itemJson.getString("id"));
                map.put("url", itemJson.getString("url"));
                reviewData.add(map);
            }
        }


        @Override
        protected Void doInBackground(String... strings) {
            final String BASE_URL = "http://api.themoviedb.org/3/movie";

            String video_url = BASE_URL + "/"+ id + "/videos";
            String review_url = BASE_URL + "/"+ id + "/reviews";

            try{
                getVideoDataFromJson(getJsonStr(video_url));
            } catch(JSONException e){
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            try{
                getReviewDataFromJson(getJsonStr(review_url));
            } catch(JSONException e){
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
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            LinearLayout videoContainer = (LinearLayout) rootView.findViewById(R.id.video_container);
            for (int i = 0; i < videoData.size(); i++){
                final String YOUTUBE_KEY = videoData.get(i).get("youtube_key").toString();
                TextView textView = (TextView) new TextView(getContext());
                textView.setText("Trailer " + (i + 1));
                videoContainer.addView(textView);

                Button button = new Button(getContext());
                button.setText("Play Video");
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("vnd.youtube:" + YOUTUBE_KEY));
                        intent.putExtra("VIDEO_ID", YOUTUBE_KEY);
                        startActivity(intent);
                    }
                });
                videoContainer.addView(button);
            }

            LinearLayout reviewContainer = (LinearLayout) rootView.findViewById(R.id.review_container);
            for (int i = 0; i < reviewData.size(); i++){
                String content = reviewData.get(i).get("content").toString();
                TextView textView = new TextView(getContext());
                textView.setText(content);
                reviewContainer.addView(textView);
                String author = reviewData.get(i).get("author").toString();
                TextView authorTextView = new TextView(getContext());
                authorTextView.setText("Reviewed by " + author);
                authorTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_TEXT_END);
                reviewContainer.addView(authorTextView);
            }
        }
    }
}
