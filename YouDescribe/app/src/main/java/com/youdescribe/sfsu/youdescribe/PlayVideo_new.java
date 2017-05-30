package com.youdescribe.sfsu.youdescribe;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NavUtils;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class PlayVideo_new extends AppCompatActivity implements YouTubePlayer.OnInitializedListener {
    public static final String API_KEY = "AIzaSyDrZV4nWCVsTSHFP-7PXbJYPE9ynNOTiQ0";
    private String defaultAppId = "ydesc";
    private String apiBaseUrl = "http://dvx.ski.org:8080/dvx2Api/";
    public static String VIDEO_ID = "";
    public static String MEDIA_ID = "";
    public static String AUTHOR_ID = "";
    public static String VIDEO_TITLE = "";
    public static String VIDEO_DESCRIPTION = "";
    public static String AUTHOR_NAME = "";
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
    AudioManager audioManager;
    Toolbar mActionBar;

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
    boolean paused = false;
    boolean isClipPresent = true;
    private ShareActionProvider mShareActionProvider;

    Button mPlay;
    Button mPause;
    Button mStop;
    Intent movieInfoIntent;

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_video);

        movieInfoIntent = new Intent(PlayVideo_new.this, MovieInfo.class);

        //YouTubePlayerSupportFragment frag =(YouTubePlayerSupportFragment) getSupportFragmentManager().findFragmentById(R.id.youtube_fragment);

        mPlay = (Button) findViewById(R.id.playButton);
        mPause = (Button) findViewById(R.id.pauseButton);
        mStop = (Button) findViewById(R.id.stopButton);

        mActionBar = (Toolbar) findViewById(R.id.toolbar1);
        if (mActionBar != null)
        {
            setSupportActionBar(mActionBar);
        }

        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);

        /** Initializing YouTube Player View **/
        YouTubePlayerSupportFragment youTubePlayerFragment = (YouTubePlayerSupportFragment) getSupportFragmentManager()
                .findFragmentById(R.id.youtube_fragment);
        youTubePlayerFragment.initialize(API_KEY, this);
        Bundle bundle = getIntent().getExtras();
        VIDEO_ID = bundle.getString("videoID");
        MEDIA_ID = bundle.getString("movieID");
        AUTHOR_ID = bundle.getString("authorId");
        VIDEO_TITLE = bundle.getString("videoTitle");
        VIDEO_DESCRIPTION = bundle.getString("videoDescription");
        AUTHOR_NAME = bundle.getString("authorName");

        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        SeekBar volControl = (SeekBar)findViewById(R.id.volumeSeekBar);
        volControl.setMax(maxVolume);
        volControl.setProgress(curVolume);
        volControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, arg1, 0);
            }
        });

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
                clip.downloadAllClips(downloadURLs, PlayVideo_new.this);
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
    public void onInitializationSuccess(Provider provider, YouTubePlayer YPlayer, boolean wasRestored) {
        player = YPlayer;
        isInitialized = true;
        //this.player = player;
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
                player.play();
            }
        });

        mPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.pause();
                if(playing == true){
                    mp.pause();
                }
            }
        });

        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.pause();
                player.seekToMillis(0);
                if(playing == true){
                    mp.stop();
                }
                numberOfClipsRemaining = clipStartTime.size();
            }
        });
        player.setManageAudioFocus(true);
        player.getFullscreenControlFlags();
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
            //playMusic();
        }

        @Override
        public void onPaused() {
            // Called when playback is paused, either due to user action or call to pause().
            //showMessage("Paused");
            //pauseMusic();
            if(playing == true){
                mp.pause();
            }
        }

        @Override
        public void onStopped() {
            // Called when playback stops for a reason other than being paused.
            //showMessage("Stopped");
            //stopMusic();
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
            if (isDescribed && (numberOfClipsRemaining > 0)) {
                nextClipStartTime = getNextClipStartTime();
                nextClipID = getNextClipID();
            }
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
        if ((mp != null)&&(playing)) {
            mp.stop();
        }
        handler.removeCallbacks(runnable);
    }

    public void playMusic(){
        //showMessage("Play Music");
        if(paused&&(mp!=null)){
            mp.setLooping(true);
            mp.start();
            playing = true;
        }
    }

    public void pauseMusic(){
        //showMessage("Pause Music");
        if(playing&&(mp!=null)){
            mp.pause();
            playing = false;
            paused = true;
        }
    }

    public void stopMusic(){
        //showMessage("Stop Music");
        if(playing&&(mp!=null)){
            mp.stop();
            playing = false;
        }
    }

    public void playAudio(){
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int volume_level = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume_level, 0);

        String audioFilePath = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + nextClipID + ".mp3");
        Uri mediaURI = Uri.parse(audioFilePath);
        mp = MediaPlayer.create(this,mediaURI);
        //mp.setVolume(0.09f, 0.09f);
        mp.setLooping(false);
        showMessage("Audio Start");
        mp.start();
        playing = true;

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
        playing = false;
    }

    public void pauseAndPlayAudio(){
        player.pause();

        //audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int volume_level = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume_level, 0);

        String audioFilePath = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + nextClipID + ".mp3");
        Uri mediaURI = Uri.parse(audioFilePath);
        mp = MediaPlayer.create(this,mediaURI);
        //mp.setVolume(0.09f, 0.09f);
        mp.setLooping(false);
        showMessage("Audio Start");
        mp.start();
        playing = true;

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
        playing = false;
    }

    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.playvideo_actionbar_menu, menu);
        MenuItem item = menu.findItem(R.id.shareMenu);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        return true;
    }

    // Call to update the share intent
    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.infoButton:
                Bundle bundle = new Bundle();
                bundle.putString("videoTitle", VIDEO_TITLE);
                bundle.putString("videoDescription", VIDEO_DESCRIPTION);
                bundle.putString("authorName", AUTHOR_NAME);
                startActivity(movieInfoIntent);
                return true;

            case R.id.shareMenu:
                Intent iShare = new Intent(android.content.Intent.ACTION_SEND);
                iShare.setType("text/plain");
                iShare.putExtra(Intent.EXTRA_SUBJECT, "YouTube link:");
                String youTubeLink = "https://www.youtube.com/watch?v=" + VIDEO_ID;
                iShare.putExtra(Intent.EXTRA_TEXT, youTubeLink);
                startActivity(Intent.createChooser(iShare,"Share via"));
                return true;

        }
        return super.onOptionsItemSelected(item);
    }
}