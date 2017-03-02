package com.youdescribe.sfsu.youdescribe;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by madhura on 11/28/2016.
 */

public class DvxApi extends Context {

    final Context mContext = this;
    private String defaultAppId = "ydesc";
    private String apiBaseUrl = "http://dvxtest.ski.org:8080/dvx2Api/";
    Document mainDoc = null;
    ArrayList<Movie> searchTableMovies = new ArrayList<>();
    ArrayList<Movie> movies = new ArrayList<Movie>();
    ArrayList<Clip> clips = new ArrayList<Clip>();
    ArrayList<User> user = new ArrayList<User>();
    private String resource = "";
    private BroadcastReceiver receiverDownloadComplete;
    Activity activity;


    public ArrayList searchTable(HashMap<String, String> params){
        String urlString = getConstructedUrl("searchTable", params);
        resource = "movieSearchTable";
        DocumentBuilder db = null;
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        try {
            searchTableMovies = new UpdateTask().execute(urlString).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return searchTableMovies;
    }

    public ArrayList searchMovie(HashMap<String, String> params){
        String urlString = getConstructedUrl("movie", params);
        resource = "searchMovie";
        DocumentBuilder db = null;
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        try {
            movies = new UpdateTask().execute(urlString).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return movies;
    }

    public ArrayList getMovies(HashMap<String, String> params){
        String urlString = getConstructedUrl("movie", params);
        resource = "movie";
        DocumentBuilder db = null;
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        try {
            movies = new UpdateTask().execute(urlString).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return movies;
    }

    public ArrayList getClips(HashMap<String, String> params){
        String urlString = getConstructedUrl("clip/metadata", params);
        resource = "clip/metadata";
        DocumentBuilder db = null;
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        try {
            clips = new UpdateTask().execute(urlString).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return clips;
    }

    public String getAudioClipUrl(HashMap<String, String> params){
        String urlString = getConstructedUrl("clip", params);
        return urlString;
    }

    public ArrayList getUsers(HashMap<String, String> params){
        String urlString = getConstructedUrl("user", params);
        resource = "user";
        DocumentBuilder db = null;
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        try {
            user = new UpdateTask().execute(urlString).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return user;
    }

    private String getConstructedUrl(String query, HashMap<String, String> params){
        String url = apiBaseUrl + query +  "?AppId=" + defaultAppId;
        String paramString = "";
        for ( Map.Entry<String, String> entry : params.entrySet() ) {
            String key = entry.getKey();
            String value = entry.getValue();
            paramString += "&" + key + "=" + value;
        }
        if (paramString != ""){
            url += paramString;
        }
        return url;
    }

    private class UpdateTask extends AsyncTask<String, Void, ArrayList> {

        DocumentBuilder db = null;
        ArrayList dataAsync = new ArrayList();
        JSONArray movies;

        protected ArrayList doInBackground(String... urls) {

            String urlString = urls[0];
            String responseString = "";
            URLConnection connection = null;
            JSONObject jsonObj = null;
            JSONObject outerObj = null;
            JSONArray outerArray = null;
            JSONObject innerObj = null;
            JSONObject outerUserArray = null;

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
                jsonObj = XML.toJSONObject(responseString);
            } catch (JSONException e) {
                Log.e("JSON exception", e.getMessage());
                e.printStackTrace();
            }

            if (resource == "movie") {
                try {
                    outerObj = jsonObj.getJSONObject("movies");
                    outerArray = outerObj.getJSONArray("movie");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dataAsync = getMoviesFromJSON(outerArray);
            }
            else if (resource == "clip/metadata"){
                try {
                    outerObj = jsonObj.getJSONObject("clips");
                    outerArray = outerObj.getJSONArray("clip");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (outerArray != null){
                    dataAsync = getClipsFromJSON(outerArray);
                }else {
                    return null;
                }
            }
            else if (resource == "user"){
                try {
                    outerObj = jsonObj.getJSONObject("users");
                    outerUserArray = outerObj.getJSONObject("user");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dataAsync = getUsersFromJSON(outerUserArray);
            }
            if (resource == "movieSearchTable") {
                try {
                    outerObj = jsonObj.getJSONObject("searchTables");
                    outerArray = outerObj.getJSONArray("searchTable");
                    //innerObj = outerArray.getJSONObject("id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dataAsync = getSearchTableMoviesFromJSON(outerArray);
            }
            if (resource == "searchMovie") {
                try {
                    outerObj = jsonObj.getJSONObject("movies");
                    innerObj = outerObj.getJSONObject("movie");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dataAsync = getSearchedMovieFromJSON(innerObj);
            }
            return dataAsync;
        }

        protected void onPostExecute(ArrayList result) {
            super.onPostExecute(result);
        }

    }

    public static ArrayList<Movie> getMoviesFromJSON(JSONArray movies){
        final ArrayList<Movie> movieList = new ArrayList<>();
        for(int i = 0; i < movies.length(); i++){
            Movie movie = new Movie();
            try{
                movie.movieCreated = movies.getJSONObject(i).getString("movieCreated");
            }catch (JSONException e) {
                    e.printStackTrace();
            }
            try {
                movie.movieDescription = movies.getJSONObject(i).getString("movieDescription");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                movie.movieId = movies.getJSONObject(i).getString("movieId");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                movie.movieLanguageMainFk = movies.getJSONObject(i).getString("movieLanguageMainFk");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                movie.movieMediaId = movies.getJSONObject(i).getString("movieMediaId");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                movie.movieModified = movies.getJSONObject(i).getString("movieModified");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                movie.movieName = movies.getJSONObject(i).getString("movieName");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            /*try {
                movie.movieSource = movies.getJSONObject(i).getString("movieSource");
            } catch (JSONException e) {
                e.printStackTrace();
            }*/

            movieList.add(movie);
            }
        return movieList;
    }

    public static ArrayList<Movie> getSearchedMovieFromJSON(JSONObject movies){
        final ArrayList<Movie> movieList = new ArrayList<>();
        Movie movie = new Movie();
        try{
            movie.movieCreated = movies.getString("movieCreated");
        }catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            movie.movieDescription = movies.getString("movieDescription");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            movie.movieId = movies.getString("movieId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            movie.movieLanguageMainFk = movies.getString("movieLanguageMainFk");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            movie.movieMediaId = movies.getString("movieMediaId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            movie.movieModified = movies.getString("movieModified");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            movie.movieName = movies.getString("movieName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
            /*try {
                movie.movieSource = movies.getJSONObject(i).getString("movieSource");
            } catch (JSONException e) {
                e.printStackTrace();
            }*/

        movieList.add(movie);
        return movieList;
    }

    public static ArrayList<Movie> getSearchTableMoviesFromJSON(JSONArray movies){
        final ArrayList<Movie> movieList = new ArrayList<>();
        for(int i = 0; i < movies.length(); i++){
            Movie movie = new Movie();
            try{
                movie.authorID = movies.getJSONObject(i).getJSONObject("id").getString("clipAuthor");
            }catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                movie.movieMediaId = movies.getJSONObject(i).getJSONObject("id").getString("movieMediaId");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                movie.movieModified = movies.getJSONObject(i).getJSONObject("id").getString("movieModified");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                movie.movieName = movies.getJSONObject(i).getJSONObject("id").getString("movieName");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                movie.authorName = movies.getJSONObject(i).getJSONObject("id").getString("userHandle");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            movie.isDescribed = true;
            movieList.add(movie);
        }
        return movieList;
    }

    public static ArrayList<Clip> getClipsFromJSON(JSONArray clips){
        final ArrayList<Clip> clipList = new ArrayList<>();
        for(int i = 0; i < clips.length(); i++){
            Clip clip = new Clip();
            try {
                clip.clipAuthor = clips.getJSONObject(i).getString("clipAuthor");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                clip.clipChapter = clips.getJSONObject(i).getString("clipChapter");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                clip.clipFilename = clips.getJSONObject(i).getString("clipFilename");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                clip.clipId = clips.getJSONObject(i).getString("clipId");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                clip.clipStartTime = clips.getJSONObject(i).getString("clipStartTime");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                clip.clipType = clips.getJSONObject(i).getString("clipType");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                clip.languageFk = clips.getJSONObject(i).getString("languageFk");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                clip.movieFk = clips.getJSONObject(i).getString("movieFk");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            clipList.add(clip);
        }
        return clipList;
    }

    public static ArrayList<User> getUsersFromJSON(JSONObject users){
        final ArrayList<User> userList = new ArrayList<>();
        //for(int i = 0; i < users.length(); i++){
            User user = new User();
            try {
                user.userCreated = users.getString("userCreated");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                user.userHandle = users.getString("userHandle");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                user.userId = users.getString("userId");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                user.userLastLogin = users.getString("userLastLogin");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                user.userLoggedOn = users.getString("userLoggedOn");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                user.userModified = users.getString("userModified");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                user.userPending = users.getString("userPending");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            userList.add(user);
        //}
        return userList;
    }

    public void downloadAllClips(ArrayList<String> URLS, Activity activity){
        this.activity = activity;
        new DownloadTask().execute(URLS);
    }

    private class DownloadTask extends AsyncTask<ArrayList<String>, Void, Void>{

        @Override
        protected Void doInBackground(ArrayList<String>... params) {

            ArrayList<String> URLS = params[0];
            for (int item=0; item < URLS.size(); item++){
                String url = URLS.get(item);
                String clipID = url.substring(url.lastIndexOf("=") + 1, url.length());
                Uri uri = Uri.parse(URLS.get(item));
                long downloadReference = 0;

                // Create request for android download manager
                final DownloadManager downloadManager = (DownloadManager) activity.getSystemService(activity.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(uri);

                //Setting title of request
                request.setTitle("Data Download");

                //Setting description of request
                request.setDescription("Audio Clips download");

                //Set the local destination for the downloaded file to a path
                //within the application's external files directory
                File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                boolean writable = isExternalStorageWritable();
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, clipID + ".mp3");

                //Make file visible and manageable by system's download app
                request.setVisibleInDownloadsUi(true);

                //Enqueue download and save into referenceId if the file does not already exist
                //downloadReference = downloadManager.enqueue(request);
                final File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), clipID + ".mp3");
                //Log.d("download:", String.valueOf(f)+String.valueOf(downloadReference));
                if (!f.exists()){
                    do {
                        downloadReference = downloadManager.enqueue(request);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.d("download:", String.valueOf(f));
                    }while (!f.exists());
                }
                //return downloadReference;
                /*BroadcastReceiver receiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String action = intent.getAction();
                        while (!DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                            Log.d("downloading:", String.valueOf(f));
                        }
                    }
                };
                registerReceiver(receiver, new IntentFilter(
                        DownloadManager.ACTION_DOWNLOAD_COMPLETE));*/
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            Toast.makeText(activity, "Clips downloaded!", Toast.LENGTH_LONG).show();
            super.onPostExecute(result);
        }
    }

    public void downloadClips(String mediaID, String clipID, Activity activity){
        Uri audioClip_uri = Uri.parse(apiBaseUrl + "clip?AppId=" + defaultAppId + "&Movie=" + mediaID + "&ClipId=" + clipID);
        this.activity = activity;
        //new DownloadTask().execute(mediaID,clipID);
        long downloadReference = downloadData(audioClip_uri, clipID, activity);
    }

    private long downloadData (Uri uri, String clipID, Activity activity) {

        long downloadReference = 0;

        // Create request for android download manager
        final DownloadManager downloadManager = (DownloadManager) activity.getSystemService(activity.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        //Setting title of request
        request.setTitle("Data Download");

        //Setting description of request
        request.setDescription("Audio Clips download");

        //Set the local destination for the downloaded file to a path
        //within the application's external files directory
        File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        boolean writable = isExternalStorageWritable();
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, clipID + ".mp3");

        //Make file visible and manageable by system's download app
        request.setVisibleInDownloadsUi(true);

        //Enqueue download and save into referenceId if the file does not already exist
        //downloadReference = downloadManager.enqueue(request);
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), clipID + ".mp3");
        //Log.d("download:", String.valueOf(f)+String.valueOf(downloadReference));
        if (!f.exists()){
            downloadReference = downloadManager.enqueue(request);
            Log.d("download:", String.valueOf(f)+String.valueOf(downloadReference));
        }

        return downloadReference;
    }


    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    public AssetManager getAssets() {
        return null;
    }

    @Override
    public Resources getResources() {
        return null;
    }

    @Override
    public PackageManager getPackageManager() {
        return null;
    }

    @Override
    public ContentResolver getContentResolver() {
        return null;
    }

    @Override
    public Looper getMainLooper() {
        return null;
    }

    @Override
    public Context getApplicationContext() {
        return null;
    }

    @Override
    public void setTheme(int resid) {

    }

    @Override
    public Resources.Theme getTheme() {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return null;
    }

    @Override
    public String getPackageName() {
        return null;
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        return null;
    }

    @Override
    public String getPackageResourcePath() {
        return null;
    }

    @Override
    public String getPackageCodePath() {
        return null;
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return null;
    }

    @Override
    public boolean moveSharedPreferencesFrom(Context sourceContext, String name) {
        return false;
    }

    @Override
    public boolean deleteSharedPreferences(String name) {
        return false;
    }

    @Override
    public FileInputStream openFileInput(String name) throws FileNotFoundException {
        return null;
    }

    @Override
    public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
        return null;
    }

    @Override
    public boolean deleteFile(String name) {
        return false;
    }

    @Override
    public File getFileStreamPath(String name) {
        return null;
    }

    @Override
    public File getDataDir() {
        return null;
    }

    @Override
    public File getFilesDir() {
        return null;
    }

    @Override
    public File getNoBackupFilesDir() {
        return null;
    }

    @Nullable
    @Override
    public File getExternalFilesDir(String type) {
        return null;
    }

    @Override
    public File[] getExternalFilesDirs(String type) {
        return new File[0];
    }

    @Override
    public File getObbDir() {
        return null;
    }

    @Override
    public File[] getObbDirs() {
        return new File[0];
    }

    @Override
    public File getCacheDir() {
        return null;
    }

    @Override
    public File getCodeCacheDir() {
        return null;
    }

    @Nullable
    @Override
    public File getExternalCacheDir() {
        return null;
    }

    @Override
    public File[] getExternalCacheDirs() {
        return new File[0];
    }

    @Override
    public File[] getExternalMediaDirs() {
        return new File[0];
    }

    @Override
    public String[] fileList() {
        return new String[0];
    }

    @Override
    public File getDir(String name, int mode) {
        return null;
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
        return null;
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
        return null;
    }

    @Override
    public boolean moveDatabaseFrom(Context sourceContext, String name) {
        return false;
    }

    @Override
    public boolean deleteDatabase(String name) {
        return false;
    }

    @Override
    public File getDatabasePath(String name) {
        return null;
    }

    @Override
    public String[] databaseList() {
        return new String[0];
    }

    @Override
    public Drawable getWallpaper() {
        return null;
    }

    @Override
    public Drawable peekWallpaper() {
        return null;
    }

    @Override
    public int getWallpaperDesiredMinimumWidth() {
        return 0;
    }

    @Override
    public int getWallpaperDesiredMinimumHeight() {
        return 0;
    }

    @Override
    public void setWallpaper(Bitmap bitmap) throws IOException {

    }

    @Override
    public void setWallpaper(InputStream data) throws IOException {

    }

    @Override
    public void clearWallpaper() throws IOException {

    }

    @Override
    public void startActivity(Intent intent) {

    }

    @Override
    public void startActivity(Intent intent, Bundle options) {

    }

    @Override
    public void startActivities(Intent[] intents) {

    }

    @Override
    public void startActivities(Intent[] intents, Bundle options) {

    }

    @Override
    public void startIntentSender(IntentSender intent, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws IntentSender.SendIntentException {

    }

    @Override
    public void startIntentSender(IntentSender intent, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, Bundle options) throws IntentSender.SendIntentException {

    }

    @Override
    public void sendBroadcast(Intent intent) {

    }

    @Override
    public void sendBroadcast(Intent intent, String receiverPermission) {

    }

    @Override
    public void sendOrderedBroadcast(Intent intent, String receiverPermission) {

    }

    @Override
    public void sendOrderedBroadcast(Intent intent, String receiverPermission, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {

    }

    @Override
    public void sendBroadcastAsUser(Intent intent, UserHandle user) {

    }

    @Override
    public void sendBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission) {

    }

    @Override
    public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {

    }

    @Override
    public void sendStickyBroadcast(Intent intent) {

    }

    @Override
    public void sendStickyOrderedBroadcast(Intent intent, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {

    }

    @Override
    public void removeStickyBroadcast(Intent intent) {

    }

    @Override
    public void sendStickyBroadcastAsUser(Intent intent, UserHandle user) {

    }

    @Override
    public void sendStickyOrderedBroadcastAsUser(Intent intent, UserHandle user, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {

    }

    @Override
    public void removeStickyBroadcastAsUser(Intent intent, UserHandle user) {

    }

    @Nullable
    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        return null;
    }

    @Nullable
    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler) {
        return null;
    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {

    }

    @Nullable
    @Override
    public ComponentName startService(Intent service) {
        return null;
    }

    @Override
    public boolean stopService(Intent service) {
        return false;
    }

    @Override
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        return false;
    }

    @Override
    public void unbindService(ServiceConnection conn) {

    }

    @Override
    public boolean startInstrumentation(ComponentName className, String profileFile, Bundle arguments) {
        return false;
    }

    @Override
    public Object getSystemService(String name) {
        return null;
    }

    @Override
    public String getSystemServiceName(Class<?> serviceClass) {
        return null;
    }

    @Override
    public int checkPermission(String permission, int pid, int uid) {
        return 0;
    }

    @Override
    public int checkCallingPermission(String permission) {
        return 0;
    }

    @Override
    public int checkCallingOrSelfPermission(String permission) {
        return 0;
    }

    @Override
    public int checkSelfPermission(String permission) {
        return 0;
    }

    @Override
    public void enforcePermission(String permission, int pid, int uid, String message) {

    }

    @Override
    public void enforceCallingPermission(String permission, String message) {

    }

    @Override
    public void enforceCallingOrSelfPermission(String permission, String message) {

    }

    @Override
    public void grantUriPermission(String toPackage, Uri uri, int modeFlags) {

    }

    @Override
    public void revokeUriPermission(Uri uri, int modeFlags) {

    }

    @Override
    public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags) {
        return 0;
    }

    @Override
    public int checkCallingUriPermission(Uri uri, int modeFlags) {
        return 0;
    }

    @Override
    public int checkCallingOrSelfUriPermission(Uri uri, int modeFlags) {
        return 0;
    }

    @Override
    public int checkUriPermission(Uri uri, String readPermission, String writePermission, int pid, int uid, int modeFlags) {
        return 0;
    }

    @Override
    public void enforceUriPermission(Uri uri, int pid, int uid, int modeFlags, String message) {

    }

    @Override
    public void enforceCallingUriPermission(Uri uri, int modeFlags, String message) {

    }

    @Override
    public void enforceCallingOrSelfUriPermission(Uri uri, int modeFlags, String message) {

    }

    @Override
    public void enforceUriPermission(Uri uri, String readPermission, String writePermission, int pid, int uid, int modeFlags, String message) {

    }

    @Override
    public Context createPackageContext(String packageName, int flags) throws PackageManager.NameNotFoundException {
        return null;
    }

    @Override
    public Context createConfigurationContext(Configuration overrideConfiguration) {
        return null;
    }

    @Override
    public Context createDisplayContext(Display display) {
        return null;
    }

    @Override
    public Context createDeviceProtectedStorageContext() {
        return null;
    }

    @Override
    public boolean isDeviceProtectedStorage() {
        return false;
    }

}
