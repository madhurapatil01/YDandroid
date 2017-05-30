package com.youdescribe.sfsu.youdescribe;

import android.os.AsyncTask;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by madhura on 5/3/2017.
 */

public class GetYoutubeDetails {

    String videoID = PlayVideo_new.VIDEO_ID;
    String apiKey = "AIzaSyAI9H-v1Zyt1bN6W7fSz-Zl0jrfU0UYzho";
    String youTubeURLString = "https://www.googleapis.com/youtube/v3/videos?part=snippet&id="+videoID+"&key="+apiKey;
    String videoDescription = new GetVideoDescription().execute(youTubeURLString).get();;

    public GetYoutubeDetails() throws ExecutionException, InterruptedException {
    }

    private class GetVideoDescription extends AsyncTask<String, Void, String> {
        DocumentBuilder db = null;
        String vidDescription= "";

        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0];

            String responseString = "";
            URLConnection connection = null;
            JSONArray items = null;
            JSONObject jObject = null;
            JSONObject innerObject = null;
            JSONObject snippet = null;

            try {
                db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
            InputStream response = new InputStream() {
                @Override
                public int read() throws IOException {
                    return 0;
                }
            };
            try {
                connection = new URL(urlString).openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            try {
                response = connection.getInputStream();
                responseString = IOUtils.toString(response, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                jObject = new JSONObject(responseString);
                items = jObject.getJSONArray("items");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                innerObject = items.getJSONObject(0);
                snippet = innerObject.getJSONObject("snippet");
                vidDescription = snippet.getString("description");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return vidDescription;
        }
    }

}


