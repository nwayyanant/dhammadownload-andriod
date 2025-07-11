package com.dhammadownload.dhammadownloadandroid.common;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.dhammadownload.dhammadownloadandroid.R;
import com.dhammadownload.dhammadownloadandroid.activity.AudioPlayerActivity;
import com.dhammadownload.dhammadownloadandroid.entity.MediaInfo;

import java.io.File;

public class MediaPlayerService extends Service {

    public interface MediaPlayerServiceListener{
        public void onStart();
        public void onPause();
        public void onCompletion(MediaPlayer mp);
    }


    private static final String TAG = "MediaPlayerService";

    private final IBinder mBinder = new MyBinder();
    private Handler mHandler;
    private int mProgress, mMaxValue;
    private Boolean mIsPaused;

    public void stop(){
        Log.d(TAG,"stop()");
        stopForeground(true);
        stopSelf();

        releaseMediaPlayer();
    }

    private void releaseMediaPlayer(){
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            }
        }catch (IllegalStateException ise){
            Log.d(TAG,"Exception:" + Log.getStackTraceString(ise));
        }
    }

    public Boolean getIsPlaying() {
        boolean result=false;
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                result = true;
            }

        return result;
    }

    public void setmIsPlaying(Boolean mIsPlaying) {
        this.mIsPlaying = mIsPlaying;
    }

    private Boolean mIsPlaying;

    private MediaPlayer mediaPlayer;
    private String mAuthorProfilePhoto;
    private MediaInfo mMediaInfo;
    private static final int DLMediaPlayerServiceForegroundServiceId = 101;

    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_PLAY = "com.dhammadownload.dhammadownloadandroid.common.action.PLAY";
    private static final String ACTION_PAUSE = "com.dhammadownload.dhammadownloadandroid.common.action.PAUSE";
    private static final String ACTION_STOP = "com.dhammadownload.dhammadownloadandroid.common.action.STOP";

    // TODO: Rename parameters
    private static final String EXTRA_MEDIAPATH = "com.dhammadownload.dhammadownloadandroid.common.extra.PARAM1";
    private static final String EXTRA_MEDIATITLE = "com.dhammadownload.dhammadownloadandroid.common.extra.PARAM2";
    private static final String EXTRA_MEDIAAUTHOR = "com.dhammadownload.dhammadownloadandroid.common.extra.PARAM3";
    private static final String EXTRA_MEDIAAUTHORIMAGE = "com.dhammadownload.dhammadownloadandroid.common.extra.PARAM4";

    public interface ACTION {
        public static String MAIN_ACTION = "com.marothiatechs.customnotification.action.main";
        public static String INIT_ACTION = "com.marothiatechs.customnotification.action.init";
        public static String PREV_ACTION = "com.marothiatechs.customnotification.action.prev";
        public static String PLAY_ACTION = "com.marothiatechs.customnotification.action.play";
        public static String NEXT_ACTION = "com.marothiatechs.customnotification.action.next";
        public static String STARTFOREGROUND_ACTION = "com.marothiatechs.customnotification.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "com.marothiatechs.customnotification.action.stopforeground";

    }


    // TODO: Customize helper method
    public static void startActionPlay(Context context, MediaInfo mediaInfo, String authorImagePath) {
        Intent intent = new Intent(context, MediaPlayerService.class);
        intent.setAction(ACTION_PLAY);
        intent.putExtra(EXTRA_MEDIAPATH, mediaInfo.getLocalStoragePath());
        intent.putExtra(EXTRA_MEDIAAUTHOR,mediaInfo.getAuthorname());
        intent.putExtra(EXTRA_MEDIATITLE,mediaInfo.getTitle());
        intent.putExtra(EXTRA_MEDIAAUTHORIMAGE,authorImagePath);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        }
    }

    private  void sendNotification(MediaInfo mediaInfo,String authorProfilePhoto){
        Log.d(TAG,"Start sendNotification :" + mediaInfo.getAuthorname() + " :" + mediaInfo.getTitle());
        Bitmap largeImage = BitmapFactory.decodeFile(authorProfilePhoto);

        // Using RemoteViews to bind custom layouts into Notification
        RemoteViews views = new RemoteViews(getPackageName(),
                R.layout.status_bar);
        RemoteViews bigViews = new RemoteViews(getPackageName(),
                R.layout.status_bar_expanded);

// showing default album image
        views.setViewVisibility(R.id.status_bar_icon, View.VISIBLE);
        views.setViewVisibility(R.id.status_bar_album_art, View.GONE);
        views.setImageViewBitmap(R.id.status_bar_icon,largeImage);

        bigViews.setImageViewBitmap(R.id.status_bar_album_art,
                largeImage);

        Intent notificationIntent = new Intent(this, AudioPlayerActivity.class);
        notificationIntent.setAction(Intent.ACTION_VIEW);
        notificationIntent.putExtra("authorProfileImage", mediaInfo.getProfileimage());
        notificationIntent.putExtra("authorName", mediaInfo.getAuthorname());
        notificationIntent.putExtra("title", mediaInfo.getFilename());
        notificationIntent.putExtra("openMediaFile", mediaInfo.getPhysicallocation());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent playIntent = new Intent(this, MediaPlayerService.class);
        playIntent.setAction(MediaPlayerService.ACTION.PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, PendingIntent.FLAG_IMMUTABLE);


        Intent closeIntent = new Intent(this, MediaPlayerService.class);
        closeIntent.setAction(MediaPlayerService.ACTION.STOPFOREGROUND_ACTION);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, PendingIntent.FLAG_IMMUTABLE);

        views.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);

        //views.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);
        //bigViews.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);

        views.setImageViewResource(R.id.status_bar_play,
                R.drawable.apollo_holo_dark_pause);
        bigViews.setImageViewResource(R.id.status_bar_play,
                R.drawable.apollo_holo_dark_pause);

        views.setTextViewText(R.id.status_bar_track_name, mediaInfo.getTitle());
        bigViews.setTextViewText(R.id.status_bar_track_name, mediaInfo.getTitle());

        views.setTextViewText(R.id.status_bar_artist_name, mediaInfo.getAuthorname());
        bigViews.setTextViewText(R.id.status_bar_artist_name, mediaInfo.getAuthorname());

        bigViews.setTextViewText(R.id.status_bar_album_name, "");

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this,Dhammadownload.CHANNEL_ID_1 );

        mBuilder.setSmallIcon(R.drawable.noti_icon)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle())
                .setCustomContentView(views)
                .setContent(views);

        final Notification status =mBuilder.build();
        status.flags = Notification.FLAG_ONGOING_EVENT;
        status.contentIntent = pendingIntent;

        final int id=DLMediaPlayerServiceForegroundServiceId;
        startForeground(id, status);

        Log.d(TAG,"End sendNotification :" + mediaInfo.getAuthorname() + " :" + mediaInfo.getTitle());

    }

    private  void sendNotificationOld(MediaInfo mediaInfo,String authorProfilePhoto){
        Log.d(TAG,"Start sendNotification :" + mediaInfo.getAuthorname() + " :" + mediaInfo.getTitle());
        Bitmap largeImage = BitmapFactory.decodeFile(authorProfilePhoto);

        Intent stopIntent=new Intent(getApplicationContext(), MediaPlayerService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent pstopIntent = PendingIntent.getService(this, 0,
                stopIntent, PendingIntent.FLAG_IMMUTABLE);


        Notification channel=new NotificationCompat.Builder(getApplicationContext(), Dhammadownload.CHANNEL_ID_1)
                .setSmallIcon(R.drawable.dhammadownload_logo)
                .setContentTitle(mediaInfo.getAuthorname())
                .setContentText(mediaInfo.getTitle())
                .setLargeIcon(largeImage)
                .addAction(R.drawable.apollo_holo_dark_pause,this.getResources().getString(R.string.icon_back),null)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0,1))
                .build();

        channel.flags = Notification.FLAG_ONGOING_EVENT;

        startForeground(DLMediaPlayerServiceForegroundServiceId,channel);

        Log.d(TAG,"End sendNotification :" + mediaInfo.getAuthorname() + " :" + mediaInfo.getTitle());

    }

    @Override
    public void onCreate() {
        Log.d(TAG,"onCreate");
        super.onCreate();
        mHandler = new Handler();
        mProgress = 0;
        mIsPaused = true;
        mMaxValue = 5000;
    }

    private void preparePlayer(){
        try {
            if(this.mediaPlayer ==null) {
                mediaPlayer = new MediaPlayer();
            }

            String mOpenMediaFile=mMediaInfo.getLocalStoragePath();//To change
            if(mOpenMediaFile.startsWith(Constants.mainURLProcol)){
                mediaPlayer.setDataSource(this, Uri.parse(mOpenMediaFile));
            }else{
                File openfile = new File(mOpenMediaFile);
                mediaPlayer.setDataSource(this,Uri.fromFile(openfile));
            }


            mediaPlayer.prepare();
            mMaxValue = this.mediaPlayer.getDuration();

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.seekTo(0);
                    mp.pause();

                    MyBinder myBinder=(MyBinder)mBinder;

                    if (myBinder != null && myBinder.isBinderAlive() && myBinder.getMediaPlayerServiceListener() != null) {
                        myBinder.getMediaPlayerServiceListener().onCompletion(mediaPlayer);
                    }

                    stop();
                }

            });



        }catch (Exception e) {
            Log.e(TAG, "Exception: "+ Log.getStackTraceString(e));
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand");

       mMediaInfo=new MediaInfo();
            final String action = intent.getAction();
            if (ACTION_PLAY.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_MEDIAPATH);
                final String param2 = intent.getStringExtra(EXTRA_MEDIAAUTHOR);
                final String param3 = intent.getStringExtra(EXTRA_MEDIATITLE);
                final String param4 = intent.getStringExtra(EXTRA_MEDIAAUTHORIMAGE);
                mMediaInfo.setLocalStoragePath(param1);
                mMediaInfo.setAuthorname(param2);
                mMediaInfo.setTitle(param3);
                mAuthorProfilePhoto=param4;

                //sendNotification(mMediaInfo,mAuthorProfilePhoto);
                preparePlayer();
            } else if (ACTION_PAUSE.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_MEDIAPATH);
                //handleActionPause(param1);
            }else if (ACTION_STOP.equals(action)) {
                Log.i(TAG, "Received Stop Foreground Intent");
                Toast.makeText(this, "Service Stoped", Toast.LENGTH_SHORT).show();
                stop();
            }

        return START_STICKY;
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public class MyBinder extends Binder{
        public void setMediaPlayerServiceListener(MediaPlayerServiceListener mediaPlayerServiceListener) {
            this.mediaPlayerServiceListener = mediaPlayerServiceListener;
        }

        public MediaPlayerServiceListener getMediaPlayerServiceListener() {
            return mediaPlayerServiceListener;
        }

        private MediaPlayerServiceListener mediaPlayerServiceListener;

        public MediaPlayerService getService(){
            return MediaPlayerService.this;
        }

    }

    public Boolean getIsPaused(){
        return mIsPaused;
    }

    public int getProgress(){
        return mProgress;
    }

    public int getMaxValue(){
        return mMaxValue;
    }

    public void pausePretendLongRunningTask(){
        mIsPaused = true;
        onPause();
    }

    private void onPause() {
        //NotificationManagerCompat notifyManager = NotificationManagerCompat.from(this);
        //notifyManager.cancel(DLMediaPlayerServiceForegroundServiceId);
        stopForeground(true);
    }

    public void seekTo(int msec){
        if(mediaPlayer!=null && msec<mMaxValue){
            mediaPlayer.seekTo(msec);
        }
    }

    public void unPausePretendLongRunningTask(){
        mIsPaused = false;
        startPretendLongRunningTask();
    }

    public void startPretendLongRunningTask(){
        sendNotification(mMediaInfo,mAuthorProfilePhoto);

        mediaPlayer.start();

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {

                if(mProgress >= mMaxValue || mIsPaused){
                    Log.d(TAG, "run: removing callbacks");
                    mHandler.removeCallbacks(this); // remove callbacks from runnable

                    try {
                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            mProgress = mediaPlayer.getCurrentPosition();
                            mediaPlayer.pause();
                        } else {
                            mProgress = 0;
                        }
                    }catch (IllegalStateException ise){
                        Log.d(TAG,Log.getStackTraceString(ise));
                        mProgress=0;
                    }

                    pausePretendLongRunningTask();
                }
                else{
                    //Log.d(TAG, "run: progress: " + mProgress);
                    //mProgress += 100; // increment the progress

                    try {
                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            mProgress = mediaPlayer.getCurrentPosition();
                        } else {
                            mProgress = 0;
                        }
                        mHandler.postDelayed(this, 50); // continue incrementing
                    }catch (IllegalStateException ise){
                        Log.d(TAG,Log.getStackTraceString(ise));
                        mProgress=0;
                    }


                }
            }
        };
        mHandler.postDelayed(runnable, 50);
    }

    public void resetTask(){
        mProgress = 0;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(TAG, "onTaskRemoved: called.");
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: called.");

        releaseMediaPlayer();
    }
}