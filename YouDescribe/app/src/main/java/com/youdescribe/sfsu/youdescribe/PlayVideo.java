package com.youdescribe.sfsu.youdescribe;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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
    ArrayList<String> allAuthorIDs = new ArrayList<String>();
    ArrayList<String> authorNames = new ArrayList<String>();
    ArrayList<User> users = new ArrayList<>();
    ArrayList<String> clipID = new ArrayList<String>();
    ArrayList<String> clipStartTime = new ArrayList<String>();
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
    int activeAudioIndex = 0;
    int numberOfClipsRemaining = 0;
    boolean isDescribed = false;
    boolean isLoaded = false;
    boolean isInitialized = false;

    private MediaPlayer mp;
    private MyPlayerStateChangeListener mPlayerStateChangeListener;
    private MyPlaybackEventListener mPlaybackEventListener;
    private YouTubePlayer player;
    private static final int RECOVERY_REQUEST = 1;
    boolean playing = false;

    Button mPlay;
    Button mPause;
    Button mStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_video);
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

        int j = 0;
        int value = 0;
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

                    //get author IDs of all the clips for the loaded movie
                    allAuthorIDs.add(j, clipAuthor);
                    //value = Integer.parseInt(clipsAuthors.get(temp));
                    //clipsAuthors.put(temp, String.valueOf(value++));

                    //retrieve unique author IDs
                    if(!authorIDs.contains(clipAuthor)){
                        authorIDs.add(clipAuthor);
                    }
                    j++;
                }
            }
        }

        /*if (authorIDs != null){
            for(int i=0;i<authorIDs.size();i++){
                final String author_ID = authorIDs.get(i);
                HashMap<String, String> mUsers = new HashMap<String, String>() {{
                    put("UserId", author_ID);
                }};
                users = clip.getUsers(mUsers);
                String temp = null;
                temp = users.get(0).userHandle;
                authorNames.add(i,temp);
            }
        }*/

        for(String authorId : authorIDForAuthorName.keySet()){
            authorNames.add(authorIDForAuthorName.get(authorId));
        }

        /*for(int i=0; i<authorNames.size(); i++){
            String name = authorNames.get(i);
            if (name == AUTHOR_NAME){
                authorPosition = i;
            }
        }*/

        List<String> indexes = new ArrayList<String>(authorIDForAuthorName.keySet()); // <== Set to List
        for (String authorId : indexes){
            if (Integer.parseInt(authorId) == Integer.parseInt(AUTHOR_ID)){
                authorPosition = indexes.indexOf(authorId);
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
                chosenAuthor = authorIDs.get(position);
                int j = 0;
                for (int i=0; i<clips.size(); i++){
                    String clipAuthor = null;
                    String clipIDString;
                    String clipStartTimeString;
                    clipAuthor = clips.get(i).clipAuthor;
                    if ((clipAuthor != "") && (clipAuthor != null)) {
                        if(Integer.parseInt(clipAuthor) == Integer.parseInt(chosenAuthor)){
                            clipIDString = clips.get(i).clipId;
                            clipID.add(j, clipIDString);
                            clipStartTimeString = clips.get(i).clipStartTime;
                            clipStartTime.add(j, clipStartTimeString);
                            //Log.d("downloading:", clipID.get(j));
                            //clip.downloadClips(MEDIA_ID, clipID.get(j), PlayVideo.this);
                            //Toast.makeText(PlayVideo.this, "Please wait for clips to be downloaded", Toast.LENGTH_LONG).show();
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
                            pauseAndPlayAudio();
                            numberOfClipsRemaining--;
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

    public void pauseAndPlayAudio(){
        player.pause();

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 18, 0);

        String audioFilePath = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + nextClipID + ".mp3");
        Uri mediaURI = Uri.parse(audioFilePath);
        mp = MediaPlayer.create(this,mediaURI);
        //mp = new MediaPlayer();
        /**try {
            mp.setDataSource(audioFilePath);
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }**/
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