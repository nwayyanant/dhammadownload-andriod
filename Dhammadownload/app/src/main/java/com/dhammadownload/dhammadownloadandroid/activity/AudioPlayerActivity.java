/*
Audio Player Activity to play MP3 and MP4 file . Only Audio
Chnage History
--------------
25/Sep/2016 - Initial version
 */

package com.dhammadownload.dhammadownloadandroid.activity;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.dhammadownload.dhammadownloadandroid.R;
import com.dhammadownload.dhammadownloadandroid.common.Constants;
import com.dhammadownload.dhammadownloadandroid.common.MediaPlayerService;
import com.dhammadownload.dhammadownloadandroid.entity.MediaInfo;


import java.io.File;
import java.util.concurrent.TimeUnit;

public class AudioPlayerActivity extends Activity {

    private static final String TAG = "AudioPlayerActivity";

    private TextView textMaxTime;
    private TextView textCurrentPosition;
    private Button buttonStart;
    private SeekBar seekBar;
    private Handler threadHandler = new Handler();
    UpdateSeekBarThread updateSeekBarThread;

    //private MediaPlayer mediaPlayer;

    Typeface mmfontface;
    ImageView imgAuthor;
    TextView txtAuthorNme;
    TextView txtTitle;
    int duration=0;

    String mAuthorProfilePhoto;
    String mAuthorName ;
    String mTitle;
    String mOpenMediaFile;

    private NotificationManagerCompat mNotificationManagerCompat;

    private MediaPlayerService mService;

    private boolean mIsFirstLoad=true;

    private void setBinder(MediaPlayerService.MyBinder binder) {
        this.mBinder = binder;
        onBinderChanged();
    }

    private MediaPlayerService.MyBinder mBinder;
    private MediaInfo mMediaInfo;

    private static final String ACTION_PLAY = "com.dhammadownload.dhammadownloadandroid.common.action.PLAY";
    private static final String ACTION_PAUSE = "com.dhammadownload.dhammadownloadandroid.common.action.PAUSE";

    // TODO: Rename parameters
    private static final String EXTRA_MEDIAPATH = "com.dhammadownload.dhammadownloadandroid.common.extra.PARAM1";
    private static final String EXTRA_MEDIATITLE = "com.dhammadownload.dhammadownloadandroid.common.extra.PARAM2";
    private static final String EXTRA_MEDIAAUTHOR = "com.dhammadownload.dhammadownloadandroid.common.extra.PARAM3";
    private static final String EXTRA_MEDIAAUTHORIMAGE = "com.dhammadownload.dhammadownloadandroid.common.extra.PARAM4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try{

        Log.d(TAG,"[onCreate] Start.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_player);
        setFinishOnTouchOutside(false);
            mmfontface =Typeface.createFromAsset(getAssets(), Constants.standardFont);

        //get extras

        Bundle extras = getIntent().getExtras();
        mAuthorProfilePhoto = extras.getString("authorProfileImage");
         mAuthorName = extras.getString("authorName");
         mTitle = extras.getString("title");
         mOpenMediaFile = extras.getString("openMediaFile");

            mMediaInfo=new MediaInfo();
            mMediaInfo.setAuthorname(mAuthorName);
            mMediaInfo.setTitle(mTitle);
            mMediaInfo.setLocalStoragePath(mOpenMediaFile);

        txtAuthorNme = (TextView) findViewById(R.id.txtAuthorName);
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        imgAuthor = (ImageView) findViewById(R.id.imgViewAuthor);


            txtAuthorNme.setTypeface(mmfontface);
            txtAuthorNme.setText(mAuthorName);

            txtTitle.setTypeface(mmfontface);
            txtTitle.setText(mTitle);

            if ((new File(mAuthorProfilePhoto)).exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(mAuthorProfilePhoto);
                imgAuthor.setImageBitmap(myBitmap);
            }else{
                imgAuthor.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.dhammadownload_logo));
            }

        this.textCurrentPosition = (TextView)this.findViewById(R.id.textView_currentPosion);
        this.textMaxTime=(TextView) this.findViewById(R.id.textView_maxTime);
        this.buttonStart= (Button) this.findViewById(R.id.button_start);


        this.seekBar= (SeekBar) this.findViewById(R.id.seekBar);
        this.seekBar.setClickable(true);


            Log.d(TAG,"[onCreate--- [openMediaFile].\");] " + mOpenMediaFile);


            textCurrentPosition.setText("00:00:00");
        this.seekBar.setOnSeekBarChangeListener(new UpdateMediaPlayerPosition());

            mIsFirstLoad=true;
            startMyService();

        }catch(Exception e)
        {
            Log.e(TAG, "Exception: "+ Log.getStackTraceString(e));
        }

        mNotificationManagerCompat=NotificationManagerCompat.from(this);

        Log.d(TAG,"[onCreate] End.");
    }

    private void toggleUpdates(){
        Button mButton=buttonStart;
        if(mService != null){
            if(mService.getProgress() == mService.getMaxValue()){
                mService.resetTask();
                mButton.setText(R.string.Play);
            }
            else{
                if(mService.getIsPaused()){
                    mService.unPausePretendLongRunningTask();
                    //mViewModel.setIsProgressBarUpdating(true);
                    mButton.setText(R.string.Pause);
                }
                else{
                    mService.pausePretendLongRunningTask();
                    //mViewModel.setIsProgressBarUpdating(false);
                    mButton.setText(R.string.Play);
                }
            }

        }
    }


    // Convert millisecond to string.
    private String millisecondsToString(int milliseconds)  {

        String result = "";

        try {
                result = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(milliseconds),
                TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliseconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));




        }catch(Exception e){
            Log.e(TAG, "Exception: "+ Log.getStackTraceString(e));
        }
        return result;

    }


    public void doStart(View view)  {

        Log.d(TAG,"[doStart] Start.");

        toggleUpdates();

        Log.d(TAG,"[doStart] End.");
    }

    // Thread to Update position for SeekBar.
    class UpdateSeekBarThread implements Runnable {

        volatile boolean stop = false;
        public void run()  {

            try{

                if(mService!=null) {
                    if (mService.getIsPlaying()) {
                        int currentPosition = mService.getProgress();
                        String currentPositionStr = millisecondsToString(currentPosition);
                        textCurrentPosition.setText(currentPositionStr);
                        seekBar.setProgress(currentPosition);

                    }
                }

                // Delay thread 50 milisecond.
                threadHandler.postDelayed(this, 50);

            }
            catch(Exception e){
                Log.e(TAG, "Exception: "+ Log.getStackTraceString(e));
            }


        }

    }


    public void doClose(View view){

        finish();

    }

    private class UpdateMediaPlayerPosition implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {

            try{

                if (fromUser == true) {

                    int currentPosition = progress;
                    String currentPositionStr = millisecondsToString(currentPosition);
                    textCurrentPosition.setText(currentPositionStr);
                    seekBar.setProgress(currentPosition);


                    if(mService!=null) {
                        mService.seekTo(progress);
                    }
                }

            }catch(Exception e){
                Log.e(TAG, "Exception: "+ Log.getStackTraceString(e));
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {}

    }



    @Override
    public void onDestroy() {

        Log.d(TAG,"[onDestroy] Start.");

        super.onDestroy();
        updateSeekBarThread = null;
        if(mService !=null){
            mService.stop();
        }

        Log.d(TAG,"[onDestroy] End.");

    }

    private  void startActionPlay(Context context, MediaInfo mediaInfo, String authorImagePath) {
        Intent intent = new Intent(context, MediaPlayerService.class);
        intent.setAction(ACTION_PLAY);
        intent.putExtra(EXTRA_MEDIAPATH, mediaInfo.getLocalStoragePath());
        intent.putExtra(EXTRA_MEDIAAUTHOR,mediaInfo.getAuthorname());
        intent.putExtra(EXTRA_MEDIATITLE,mediaInfo.getTitle());
        intent.putExtra(EXTRA_MEDIAAUTHORIMAGE,authorImagePath);

        ContextCompat.startForegroundService(this,intent);

    }


    private void startMyService(){
        startActionPlay(this,mMediaInfo,mAuthorProfilePhoto);

        bindMyService();
    }

    private void bindMyService(){
        Intent serviceBindIntent =  new Intent(this, MediaPlayerService.class);
        bindService(serviceBindIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void onBinderChanged(){
        if(mBinder == null){
            Log.d(TAG, "onChanged: unbound from service");
            updateSeekBarThread=null;
        }
        else{
            Log.d(TAG, "onChanged: bound to service.");
            mService = mBinder.getService();

                    // The duration in milliseconds
                    int currentPosition = mService.getProgress();
                    int duration=mService.getMaxValue();
                    if (currentPosition == 0) {
                        this.seekBar.setMax(duration);
                        String maxTimeString = this.millisecondsToString(duration);
                        this.textMaxTime.setText(maxTimeString);
                    }

                                // Create a thread to update position of SeekBar.
                    if (updateSeekBarThread == null) {
                        updateSeekBarThread = new UpdateSeekBarThread();
                    }
                    threadHandler.postDelayed(updateSeekBarThread, 50);

                    mBinder.setMediaPlayerServiceListener(mediaPlayerServiceListener);

                    if(mIsFirstLoad){
                        mIsFirstLoad=false;
                        doStart(null);
                    }
        }
    }

    MediaPlayerService.MediaPlayerServiceListener mediaPlayerServiceListener=new MediaPlayerService.MediaPlayerServiceListener() {
        @Override
        public void onStart() {

        }

        @Override
        public void onPause() {

        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            if(buttonStart != null && textCurrentPosition != null && seekBar != null) {
                        buttonStart.setText(R.string.Play);
                        textCurrentPosition.setText("00:00:00");
                        seekBar.setProgress(0);
            }
        }
    };


    // Keeping this in here because it doesn't require a context
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder iBinder) {
            Log.d(TAG, "ServiceConnection: connected to service.");
            // We've bound to MyService, cast the IBinder and get MyBinder instance
            MediaPlayerService.MyBinder binder = (MediaPlayerService.MyBinder) iBinder;
            //mBinder=binder;
            setBinder(binder);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "ServiceConnection: disconnected from service.");
            //mBinder=null;
            setBinder(null);
        }
    };



}