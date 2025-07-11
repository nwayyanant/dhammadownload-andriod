package com.dhammadownload.dhammadownloadandroid.common;

import com.onesignal.OneSignal;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Chaw on 1/5/2017.
 */

public class Dhammadownload extends Application {

    public static String CHANNEL_ID_1="CHANNEL_ID_1";
    public static String CHANNEL_ID_NAME="Media Player";


    public Bundle getIntentDataBundle() {
        return intentDataBundle;
    }

    public void setIntentDataBundle(Bundle intentDataBundle) {
        this.intentDataBundle = intentDataBundle;
    }

    private Bundle intentDataBundle =  null;

    @Override
    public void onCreate() {
        super.onCreate();

        //a1ab7203-cbdf-4514-9640-daa5653677be
        final String ONESIGNAL_APP_ID = "a1ab7203-cbdf-4514-9640-daa5653677be";


        Log.d("Application","onCreate");

        createNotificationChannel();

        //OneSignal.setLogLevel(OneSignal.LOG_LEVEL.DEBUG, OneSignal.LOG_LEVEL.DEBUG);
        //OneSignal.startInit(this).init();

//        OneSignal.startInit(this)
//                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
//                .unsubscribeWhenNotificationsAreDisabled(true)
//                .init();
        // Enable logging (for debugging)
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // Initialize OneSignal
        OneSignal.initWithContext(this);

        // Set your App ID
        OneSignal.setAppId(ONESIGNAL_APP_ID);

        // Optional: Customize behavior
        OneSignal.setNotificationWillShowInForegroundHandler(notificationReceivedEvent -> {
            notificationReceivedEvent.complete(notificationReceivedEvent.getNotification());
        });





        Log.d("Application","OneSignal.startInit Done");

        // Sync hashed email if you have a login system or collect it.
        //   Will be used to reach the user at the most optimal time of day.
        // OneSignal.syncHashedEmail(userEmail);
    }

    private void createNotificationChannel() {
        NotificationChannel notificationChannel = new NotificationChannel(
                CHANNEL_ID_1, CHANNEL_ID_NAME, NotificationManager.IMPORTANCE_HIGH
        );
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationChannel.enableLights(true);

        NotificationManager manager=getSystemService(NotificationManager.class);
        manager.createNotificationChannel(notificationChannel);
    }


}
