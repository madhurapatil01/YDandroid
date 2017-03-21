package com.youdescribe.sfsu.youdescribe;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class PlayVideo extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {
    public static final String API_KEY = "AIzaSyDrZV4nWCVsTSHFP-7PXbJYPE9ynNOTiQ0";
    private String defaultAppId = "ydesc";
    private String apiBaseUrl = "http://dvxtest.ski.org:8080/dvx2Api/";
    public static String VIDEO_ID = "";
    public static String MEDIA_ID = "";
    public static String AUTHOR_ID = "";
    public int authorPosition;
    ArrayList<Clip> clips = new ArrayList<>();
    ArrayList<String> authorIDs = new ArrayList<String>();
    ArrayList<String> authorNames = new ArrayList<String>();
    ArrayList<User> users = new ArrayList<>();
    ArrayList<String> clipID = new ArrayList<String>();
    ArrayList<String> clipStartTime = new ArrayList<String>();
    ArrayList<String> clipFunction =  new ArrayList<String>();
    ArrayList<String> downloadURLs = new ArrayList<String>();
    HashMap<String, String> authorIDForAuthorName = new HashMap<String, String>();
    String chosenAuthor = new String();
    Spinner spinner2;
    ArrayAdapter<String> adapter;
    Handler handler;
    Runnable runnable;

    double currentTime = 0;
    double nextClipStartTime = 0;
    String nextClipID;
    String nextClipFunction;
    int activeAudioIndex = 0;
    int numberOfClipsRemaining = 0;
    boolean isDescribed = false;
    boolean isLoaded = false;
    boolean isInitialized = false;
    boolean stopProgressBar = true;

    private MediaPlayer mp;
    private MyPlayerStateChangeListener mPlayerStateChangeListener;
    private MyPlaybackEventListener mPlaybackEventListener;
    private YouTubePlayer player;
    private static final int RECOVERY_REQUEST = 1;
    boolean playing = false;
    boolean isClipPresent = true;

    Button mPlay;
    Button mPause;
    Button mStop;

    //private ProgressDialog progressBar;
    //private int progressBarStatus = 0;
    //private Handler progressBarbHandler = new Handler();

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_video);

        /*progressBar = new ProgressDialog(this);
        progressBar.setCancelable(true);
        progressBar.setMessage("File downloading ...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.show();
        progressBarStatus = 0;

        new Thread(new Runnable() {
            public void run() {
                while (stopProgressBar) {

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    progressBarbHandler.post(new Runnable() {
                        public void run() {
                            progressBar.setIndeterminate(true);
                            progressBar.setMessage("File downloading..");
                        }
                    });
                }

                if (!stopProgressBar) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    progressBar.dismiss();
                }
            }
        }).start();*/

        mPlay = (Button) findViewById(R.id.playButton);
        mPause = (Button) findViewById(R.id.pauseButton);
        mStop = (Button) findViewById(R.id.stopButton);

        /** Initializing YouTube Player View **/
        YouTubePlayerView youTubePlayerView = (YouTubePlayerView) findViewById(R.id.youtube_player);
        youTubePlayerView.initialize(API_KEY, this);
        Bundle bundle = getIntent().getExtras();
        VIDEO_ID = bundle.getString("videoID");
        MEDIA_ID = bundle.getString("movieID");
        AUTHOR_ID = bundle.getString("authorId");

        mPlay.setEnabled(false);

        final DvxApi clip = new DvxApi();
        HashMap<String, String> mClips = new HashMap<String, String>() {{
            put("Movie", MEDIA_ID);
        }};


        clips = clip.getClips(mClips);

        if (clips != null){
            for (int i=0;i<clips.size();i++) {
                String clipAuthor = null;
                clipAuthor = clips.get(i).clipAuthor;
                if ((clipAuthor != "") && (clipAuthor != null)) {
                    if (!authorIDForAuthorName.containsKey(clipAuthor)) {
                        final String finalClipAuthor = clipAuthor;
                        HashMap<String, String> mUsers = new HashMap<String, String>() {{
                            put("UserId", finalClipAuthor);
                        }};
                        users = clip.getUsers(mUsers);
                        authorIDForAuthorName.put(clipAuthor, users.get(0).userHandle);
                    }
                }
            }
            for(String authorId : authorIDForAuthorName.keySet()){
                authorNames.add(authorIDForAuthorName.get(authorId));
            }
            List<String> indexes = new ArrayList<String>(authorIDForAuthorName.keySet());
            for (String authorId : indexes) {
                if (Integer.parseInt(authorId) == Integer.parseInt(AUTHOR_ID)) {
                    authorPosition = indexes.indexOf(authorId);
                }
            }
        }

        spinner2 = (Spinner) findViewById(R.id.spinner2);

        adapter = new ArrayAdapter<String>(
                this, R.layout.spinner_item, authorNames);
        adapter.setDropDownViewResource(R.layout.spinner_item);

        spinner2.setAdapter(adapter);
        spinner2.setSelection(authorPosition);

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                downloadURLs.clear();
                clipID.clear();
                clipStartTime.clear();
                List<String> indexes = new ArrayList<String>(authorIDForAuthorName.keySet());
                chosenAuthor = indexes.get(position);
                int j = 0;
                for (int i=0; i<clips.size(); i++){
                    Clip clipMovie = clips.get(i);
                    if ((clipMovie.clipAuthor != "") && (clipMovie.clipAuthor != null)) {
                        if(Integer.parseInt(clipMovie.clipAuthor) == Integer.parseInt(chosenAuthor)){
                            clipID.add(j, clipMovie.clipId);
                            clipStartTime.add(j, clipMovie.clipStartTime);
                            clipFunction.add(j, clipMovie.clipFunction);
                            downloadURLs.add(j,apiBaseUrl + "clip?AppId=" + defaultAppId + "&Movie=" + MEDIA_ID + "&ClipId=" + clipID.get(j));
                            j++;
                        }
                    }
                }
                clip.downloadAllClips(downloadURLs, PlayVideo.this);
                numberOfClipsRemaining = clipStartTime.size();
                isDescribed = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        for (int i=0; i<downloadURLs.size();i++) {
            String url = downloadURLs.get(i);
            String fileClipID = url.substring(url.lastIndexOf("=") + 1, url.length());
            File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileClipID + ".mp3");
            if (f.exists()){
                isClipPresent = isClipPresent && true;
            }
            else {
                isClipPresent = isClipPresent && false;
            }
        }

        if (isClipPresent == true){
            mPlay.setEnabled(true);
            mPause.setEnabled(true);
            mStop.setEnabled(true);
        }

        stopProgressBar = false;

        mPlayerStateChangeListener = new MyPlayerStateChangeListener();
        mPlaybackEventListener = new MyPlaybackEventListener();

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if ((isInitialized) && (isDescribed) && (isLoaded)) {
                    if (player.isPlaying()) {
                        currentTime = (player.getCurrentTimeMillis()) / 1000;
                        Log.d("Current time is:", String.valueOf(currentTime));
                        if (currentTime == Math.ceil(nextClipStartTime)) {
                            nextClipFunction = getNextClipFunction();
                            if (nextClipFunction != "desc_inline") {
                                pauseAndPlayAudio();
                                numberOfClipsRemaining--;
                            }
                            else {
                                playAudio();
                                numberOfClipsRemaining--;
                            }
                        }
                    }
                }
                handler.postDelayed(this, 500);
            }
        };

        handler.postDelayed(runnable, 500);
    }

    @Override
    public void onInitializationFailure(Provider provider, YouTubeInitializationResult errorReason) {
        //Toast.makeText(this, "Failed to Initialize!", Toast.LENGTH_LONG).show();
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_REQUEST).show();
        } else {
            String error = String.format("Error initializing YouTube player", errorReason.toString());
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onInitializationSuccess(Provider provider, final YouTubePlayer player, boolean wasRestored) {
        isInitialized = true;
        this.player = player;
        /** add listeners to YouTubePlayer instance **/
        player.setPlayerStateChangeListener(mPlayerStateChangeListener);
        player.setPlaybackEventListener(mPlaybackEventListener);

        /** Start buffering **/
        if (!wasRestored) {
            player.cueVideo(VIDEO_ID);
        }

        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDescribed && (numberOfClipsRemaining > 0)) {
                    nextClipStartTime = getNextClipStartTime();
                    nextClipID = getNextClipID();
                }
                player.play();
            }
        });

        mPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.pause();
            }
        });

        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.pause();
                player.seekToMillis(0);
            }
        });
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public double getNextClipStartTime(){
        activeAudioIndex = clipStartTime.size() - numberOfClipsRemaining;
        return Double.parseDouble(clipStartTime.get(activeAudioIndex));
    }

    public String getNextClipID(){
        activeAudioIndex = clipStartTime.size() - numberOfClipsRemaining;
        return clipID.get(activeAudioIndex);
    }

    public String getNextClipFunction(){
        activeAudioIndex = clipStartTime.size() - numberOfClipsRemaining;
        return clipFunction.get(activeAudioIndex);
    }

    private final class MyPlaybackEventListener implements YouTubePlayer.PlaybackEventListener {

        @Override
        public void onPlaying() {
            // Called when playback starts, either due to user action or call to play().
            showMessage("Playing");

        }

        @Override
        public void onPaused() {
            // Called when playback is paused, either due to user action or call to pause().
            //showMessage("Paused");
        }

        @Override
        public void onStopped() {
            // Called when playback stops for a reason other than being paused.
            //showMessage("Stopped");
        }

        @Override
        public void onBuffering(boolean b) {
            // Called when buffering starts or ends.
        }

        @Override
        public void onSeekTo(int i) {
            // Called when a jump in playback position occurs, either
            // due to user scrubbing or call to seekRelativeMillis() or seekToMillis()
        }
    }

    private final class MyPlayerStateChangeListener implements YouTubePlayer.PlayerStateChangeListener {

        @Override
        public void onLoading() {
            // Called when the player is loading a video
            // At this point, it's not ready to accept commands affecting playback such as play() or pause()
        }

        @Override
        public void onLoaded(String s) {
            // Called when a video is done loading.
            // Playback methods such as play(), pause() or seekToMillis(int) may be called after this callback.
            isLoaded = true;
            mPlay.setEnabled(true);
            showMessage("Video Loaded");
        }

        @Override
        public void onAdStarted() {
            // Called when playback of an advertisement starts.
        }

        @Override
        public void onVideoStarted() {
            // Called when playback of the video starts.
        }

        @Override
        public void onVideoEnded() {
            // Called when the video reaches its end.
        }

        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {
            // Called when an error occurs.
        }
    }

    protected void onDestroy(){
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    public void playMusic(){
        if(!playing){
            mp.setLooping(true);
            mp.start();
            playing = true;
        }
    }

    public void pauseMusic(){
        if(playing){
            mp.pause();
            playing = false;
        }
    }

    public void stopMusic(){
        if(playing){
            mp.stop();
            playing = false;
        }
    }

    public void playAudio(){
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 18, 0);

        String audioFilePath = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + nextClipID + ".mp3");
        Uri mediaURI = Uri.parse(audioFilePath);
        mp = MediaPlayer.create(this,mediaURI);
        mp.setLooping(false);
        showMessage("Audio Start");
        mp.start();

        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                mp.release();
                showMessage("Audio Stopped");
                if (isDescribed && (numberOfClipsRemaining > 0)) {
                    nextClipStartTime = getNextClipStartTime();
                    nextClipID = getNextClipID();
                }
                //player.play();
            }
        });
    }

    public void pauseAndPlayAudio(){
        player.pause();

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 18, 0);

        String audioFilePath = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + nextClipID + ".mp3");
        Uri mediaURI = Uri.parse(audioFilePath);
        mp = MediaPlayer.create(this,mediaURI);
        mp.setLooping(false);
        showMessage("Audio Start");
        mp.start();

        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                mp.release();
                showMessage("Audio Stopped");
                if (isDescribed && (numberOfClipsRemaining > 0)) {
                    nextClipStartTime = getNextClipStartTime();
                    nextClipID = getNextClipID();
                }
                player.play();
            }
        });
    }
}