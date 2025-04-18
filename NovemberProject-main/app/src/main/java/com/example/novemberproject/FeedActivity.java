package com.example.novemberproject;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MenuItem;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FeedActivity extends AppCompatActivity {

    private static final String TAG = "FeedActivity";
    private static final String TUMBLR_API_KEY = "9VGKz0EG6FGBMtYtQLFxxJ66Cpy3LfIqBzK110Ux5tIwgn2G00";

    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private ArrayList<Post> postList = new ArrayList<>();
    private ArrayList<String> selectedTags;

    // Used for pagination
    private long beforeTimestamp = 0;

    // used for session tracking
    private long sessionStartTime;
    private int totalLikes = 0;
    private long totalTimeOnPosts = 0;
    private int viewedPostCount = 0;
    private long postEnterTime = 0;
    private int lastVisibleItemPosition = -1;

    // tune pulsing settings
    private boolean pulsingEnabled; // on or off
    private int pulseInterval; // number of posts between triggering pulses
    private int lastPulseIndexTriggered = -1;
    private static final int PULSE_ANIMATION_DURATION = 1200;

    // flag for infinite scrolling
    private boolean isLoading = false;
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        // toolbar at the top
        Toolbar toolbar = findViewById(R.id.toolbarFeed);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Record session start time
        sessionStartTime = SystemClock.elapsedRealtime();

        // Get selected hashtags from selection screen
        selectedTags = getIntent().getStringArrayListExtra("selectedTags");
        setTitle("Feed: " + selectedTags.toString());

        recyclerView = findViewById(R.id.recyclerViewFeed);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PostAdapter(postList, new PostAdapter.OnLikeClickListener() {
            @Override
            public void onLikeClicked(Post post) {
                totalLikes++;
            }
            @Override
            public void onUnlikeClicked(Post post) {
                totalLikes--;
            }
        });
        recyclerView.setAdapter(adapter);

        // Load current pulsing settings
        loadSettings();

        // Scroll listener to track time per post and trigger pagination and pulsing
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private long lastScrollTime = SystemClock.elapsedRealtime();
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                long currentTime = SystemClock.elapsedRealtime();
                long deltaTime = currentTime - lastScrollTime;
                lastScrollTime = currentTime;

                int firstVisible = layoutManager.findFirstCompletelyVisibleItemPosition();
                if (firstVisible != RecyclerView.NO_POSITION && firstVisible != lastVisibleItemPosition) {
                    if (lastVisibleItemPosition != -1) {
                        long timeSpent = currentTime - postEnterTime;
                        totalTimeOnPosts += timeSpent;
                        viewedPostCount++;
                    }
                    lastVisibleItemPosition = firstVisible;
                    postEnterTime = currentTime;

                    // Trigger pulsing if enabled
                    if (pulsingEnabled && firstVisible >= pulseInterval &&
                            (firstVisible % pulseInterval == 0) && firstVisible != lastPulseIndexTriggered) {
                        triggerPulseAnimation();
                        lastPulseIndexTriggered = firstVisible;
                    }
                }

                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                if (!isLoading && totalItemCount <= (lastVisibleItem + 3)) {
                    loadMorePosts();
                    isLoading = true;
                }
            }
        });

        loadMorePosts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSettings();
    }

    private void loadSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        pulsingEnabled = prefs.getBoolean("pulsing_enabled", false);
        pulseInterval = prefs.getInt("pulse_interval", 5);
    }

    // Handle playing the pulsing animation
    private void triggerPulseAnimation() {
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(recyclerView,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 1.05f, 1f, 1.05f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 1.05f, 1f, 1.05f, 1f));
        animator.setDuration(PULSE_ANIMATION_DURATION);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    private void loadMorePosts() {
        new FetchTumblrPostsTask().execute(selectedTags.toArray(new String[0]));
    }

    private class FetchTumblrPostsTask extends AsyncTask<String, Void, ArrayList<Post>> {
        @Override
        protected ArrayList<Post> doInBackground(String... tags) {
            ArrayList<Post> newPosts = new ArrayList<>();
            try {
                // Loop through each selected tag
                for (String tag : tags) {
                    String apiUrl = "https://api.tumblr.com/v2/tagged?tag=" + tag + "&api_key=" + TUMBLR_API_KEY;
                    if (beforeTimestamp > 0) {
                        apiUrl += "&before=" + beforeTimestamp;
                    }
                    URL url = new URL(apiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder jsonSb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        jsonSb.append(line);
                    }
                    reader.close();
                    String jsonStr = jsonSb.toString().trim();

                    JSONArray jsonPostsArray;
                    if (!jsonStr.startsWith("[")) {
                        JSONObject jsonResponse = new JSONObject(jsonStr);
                        JSONObject meta = jsonResponse.optJSONObject("meta");
                        if (meta != null) {
                            int status = meta.optInt("status", 200);
                            if (status != 200) {
                                continue;
                            }
                        }
                        jsonPostsArray = jsonResponse.getJSONArray("response");
                    } else {
                        jsonPostsArray = new JSONArray(jsonStr);
                    }

                    for (int i = 0; i < jsonPostsArray.length(); i++) {
                        JSONObject postJson = jsonPostsArray.getJSONObject(i);
                        String type = postJson.optString("type", "");
                        String imageUrl = "";
                        if (type.equals("photo")) {
                            JSONArray photos = postJson.optJSONArray("photos");
                            if (photos != null && photos.length() > 0) {
                                JSONObject photoObj = photos.getJSONObject(0);
                                JSONObject originalSize = photoObj.optJSONObject("original_size");
                                if (originalSize != null) {
                                    imageUrl = originalSize.optString("url", "");
                                }
                            }
                        } else if (type.equals("text") && postJson.has("body")) {
                            String body = postJson.optString("body", "");
                            imageUrl = extractImageUrl(body);
                        }

                        // Only show posts with an image
                        if (!imageUrl.isEmpty()) {
                            String summary = postJson.optString("summary", "No summary available");
                            String postUrl = postJson.optString("post_url", "");
                            long postTimestamp = postJson.optLong("timestamp", System.currentTimeMillis());
                            Post post = new Post(summary, postUrl, imageUrl, postTimestamp);
                            newPosts.add(post);
                            if (beforeTimestamp == 0 || postTimestamp < beforeTimestamp) {
                                beforeTimestamp = postTimestamp;
                            }
                        }
                    }
                }
                // Sort posts by timestamp
                Collections.sort(newPosts, new Comparator<Post>() {
                    @Override
                    public int compare(Post p1, Post p2) {
                        return Long.compare(p2.getTimestamp(), p1.getTimestamp());
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error fetching: " + e.getMessage());
            }
            return newPosts;
        }

        @Override
        protected void onPostExecute(ArrayList<Post> newPosts) {
            if (!newPosts.isEmpty()) {
                int currentSize = postList.size();
                if (currentSize == 0) {
                    postList.addAll(newPosts);
                    adapter.notifyDataSetChanged();
                } else {
                    postList.addAll(newPosts);
                    adapter.notifyItemRangeInserted(currentSize, newPosts.size());
                }
            } else {
                Toast.makeText(FeedActivity.this, "No more posts found.", Toast.LENGTH_SHORT).show();
            }
            isLoading = false;
        }
    }

    // Extract the image URL
    private String extractImageUrl(String html) {
        String imageUrl = "";
        try {
            Pattern pattern = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");
            Matcher matcher = pattern.matcher(html);
            if (matcher.find()) {
                imageUrl = matcher.group(1);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting URL: " + e.getMessage());
        }
        return imageUrl;
    }

    // Handle ending the session
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            endSession();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Handle ending the session
    @Override
    public void onBackPressed() {
        endSession();
        super.onBackPressed();
    }

    // Save data to DB
    private void endSession() {
        long sessionEndTime = SystemClock.elapsedRealtime();
        long totalSessionTime = sessionEndTime - sessionStartTime;
        long avgTimePerPost = viewedPostCount > 0 ? totalTimeOnPosts / viewedPostCount : 0;

        DBHelper dbHelper = new DBHelper(this);
        dbHelper.insertTrackingData(sessionStartTime, sessionEndTime, totalSessionTime, avgTimePerPost, totalLikes,
                pulsingEnabled, pulseInterval);
        Toast.makeText(this, "Session data saved", Toast.LENGTH_SHORT).show();
    }
}
