package com.mob2.tubeexplorer.network;

import android.os.AsyncTask;

import com.mob2.tubeexplorer.model.VideoItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * FetchVideosTask
 * ---------------
 * AsyncTask (required by the project) that fetches videos from the
 * YouTube Data API v3 "search" endpoint IN THE BACKGROUND, then delivers
 * the parsed result back on the UI thread through a callback.
 *
 *  - doInBackground(): performs the HTTP request + JSON parsing (worker thread)
 *  - onPreExecute() / onPostExecute(): run on the UI thread
 */
public class FetchVideosTask extends AsyncTask<String, Void, ArrayList<VideoItem>> {

    /** YouTube Data API v3 key. */
    private static final String API_KEY = "AIzaSyAEk7F_bbhTFUWxwJXDn5fzxviwCJYk7EY";

    private static final String BASE_URL =
            "https://www.googleapis.com/youtube/v3/search"
                    + "?part=snippet&type=video&maxResults=25";

    /** Callback so the Fragment can react to success / failure on the UI thread. */
    public interface VideosCallback {
        void onPreLoad();                              // show ProgressBar
        void onVideosFetched(ArrayList<VideoItem> videos); // success
        void onError(String message);                  // failure -> error notification
    }

    private final VideosCallback callback;
    private String errorMessage = null;

    public FetchVideosTask(VideosCallback callback) {
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (callback != null) callback.onPreLoad();
    }

    @Override
    protected ArrayList<VideoItem> doInBackground(String... params) {
        String query = (params != null && params.length > 0 && params[0] != null)
                ? params[0] : "android development";

        HttpURLConnection connection = null;
        try {
            String urlString = BASE_URL
                    + "&q=" + URLEncoder.encode(query, "UTF-8")
                    + "&key=" + API_KEY;

            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                errorMessage = "Server returned HTTP " + responseCode;
                return null;
            }

            // Read the response body
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
            }

            return parseJson(sb.toString());

        } catch (Exception e) {
            errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown network error";
            return null;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    /** Parse the JSON response from the YouTube API into VideoItem objects. */
    private ArrayList<VideoItem> parseJson(String json) throws Exception {
        ArrayList<VideoItem> result = new ArrayList<>();
        JSONObject root = new JSONObject(json);
        JSONArray items = root.optJSONArray("items");
        if (items == null) return result;

        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);

            JSONObject id = item.optJSONObject("id");
            String videoId = (id != null) ? id.optString("videoId", "") : "";
            if (videoId.isEmpty()) continue; // skip channels/playlists

            JSONObject snippet = item.getJSONObject("snippet");
            String title        = snippet.optString("title", "No title");
            String description  = snippet.optString("description", "No description available.");
            String channelTitle = snippet.optString("channelTitle", "Unknown channel");
            String publishedAt  = snippet.optString("publishedAt", "");
            if (publishedAt.length() >= 10) publishedAt = publishedAt.substring(0, 10);

            String thumbnailUrl = "";
            JSONObject thumbnails = snippet.optJSONObject("thumbnails");
            if (thumbnails != null) {
                JSONObject high = thumbnails.optJSONObject("high");
                if (high == null) high = thumbnails.optJSONObject("medium");
                if (high == null) high = thumbnails.optJSONObject("default");
                if (high != null) thumbnailUrl = high.optString("url", "");
            }

            result.add(new VideoItem(videoId, title, description,
                    channelTitle, publishedAt, thumbnailUrl));
        }
        return result;
    }

    @Override
    protected void onPostExecute(ArrayList<VideoItem> videos) {
        super.onPostExecute(videos);
        if (callback == null) return;

        if (videos != null && !videos.isEmpty()) {
            callback.onVideosFetched(videos);
        } else {
            callback.onError(errorMessage != null ? errorMessage : "No videos found");
        }
    }
}
