package com.dhammadownload.dhammadownloadandroid.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.dhammadownload.dhammadownloadandroid.R;
import com.dhammadownload.dhammadownloadandroid.common.Constants;
import com.dhammadownload.dhammadownloadandroid.common.Utils;

@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity implements ActivityCompat.OnRequestPermissionsResultCallback{

    private static final int PERMISSION_REQUEST_STORAGE = 0;
    private static final String TAG = "MainActivity";



    TabHost tabHost;
    Typeface mmfontface;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        Utils.setContext(this.getApplicationContext());

        Log.d(TAG,"[onCreate] Start.");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try{

            tabHost = getTabHost();
            mmfontface =Typeface.createFromAsset(getAssets(), Constants.standardFont);

            // Tab for Online Web
            TabHost.TabSpec onlinespec = tabHost.newTabSpec("Online");
            onlinespec.setIndicator(getTabIndicator(tabHost.getContext(), Constants.tabLabelOnline, R.drawable.icon_online_config));
            Intent onlineIntent = new Intent(this, MainOnlineActivity.class);
            onlinespec.setContent(onlineIntent);

            // Tab for MP3
            TabHost.TabSpec mp3spec = tabHost.newTabSpec("MP3");
            mp3spec.setIndicator(getTabIndicator(tabHost.getContext(), Constants.tabLabelMP3, R.drawable.icon_mp3_config));
            Intent mp3Intent = new Intent(this, LocalMP3Activity.class);
            mp3spec.setContent(mp3Intent);

            // Tab for MP4
            TabHost.TabSpec mp4spec = tabHost.newTabSpec("MP4");
            mp4spec.setIndicator(getTabIndicator(tabHost.getContext(), Constants.tabLabelMP4, R.drawable.icon_mp4_config));
            Intent mp4Intent = new Intent(this, LocalMP4Activity.class);
            mp4spec.setContent(mp4Intent);

            // Tab for EBOOK
            TabHost.TabSpec ebookspec = tabHost.newTabSpec("EBOOK");
            ebookspec.setIndicator(getTabIndicator(tabHost.getContext(), Constants.tabLabelEBOOK, R.drawable.icon_ebook_config)); // new function to inject our own tab layout
            Intent ebookIntent = new Intent(this, LocalEbookActivity.class);
            ebookspec.setContent(ebookIntent);

            // Tab for MediaPlayer
            TabHost.TabSpec playerspec = tabHost.newTabSpec("PLAY");
            playerspec.setIndicator(getTabIndicator(tabHost.getContext(), Constants.tabLabelPLAYER, R.drawable.icon_player_config));
            Intent playerIntent = new Intent(this, AudioPlayerActivity.class);
            playerspec.setContent(playerIntent);

            // Tab for SETTING
            TabHost.TabSpec settingspec = tabHost.newTabSpec("SETTING");
            settingspec.setIndicator(getTabIndicator(tabHost.getContext(), Constants.tabLabelSetting, R.drawable.icon_setting_config)); // new function to inject our own tab layout
            Intent settingIntent = new Intent(this, SettingActivity.class);
            settingspec.setContent(settingIntent);

            // Adding all TabSpec to TabHost
            tabHost.addTab(onlinespec); // Adding online tab
            tabHost.addTab(mp3spec); // Adding mp3 tab
            tabHost.addTab(mp4spec); // Adding mp4 tab
            tabHost.addTab(ebookspec); // Adding ebook tab
            //tabHost.addTab(playerspec);//Adding Player tab
            tabHost.addTab(settingspec); // Adding setting tab

            for(int i=0;i<tabHost.getTabWidget().getChildCount();i++)
            {
                tabHost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor(Constants.tabBackgroundColor));
                TextView tabLabel = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(R.id.textView);
                if ( i == 0){
                    tabLabel.setTextColor(Color.parseColor(Constants.tabActiveFontColor));
                }else{
                    tabLabel.setTextColor(Color.parseColor(Constants.tabDeactiveFontColor));
                }


            }

            tabHost.getTabWidget().setCurrentTab(0);



            getTabHost().setOnTabChangedListener(new TabHost.OnTabChangeListener() {
                @Override
                public void onTabChanged (String tabId){

                    for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
                        TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(R.id.textView); //Unselected Tabs
                        tv.setTextColor(Color.parseColor(Constants.tabDeactiveFontColor));
                    }

                    TextView tv = (TextView) tabHost.getCurrentTabView().findViewById(R.id.textView); //for Selected Tab
                    tv.setTextColor(Color.parseColor(Constants.tabActiveFontColor));

                }
            });

            handleBundleData(getIntent().getExtras());

            //Check for Storage Permission Granted and
            //proceed to create Folder, download Author Image, Main Config, Media Config
            // only when permission is granted
            //Otherwise Request the permission
            if(isStoragePermissionGranted()){
                Utils.createNoMediaFile();
            }else{



                AlertDialog.Builder errorDialog = new AlertDialog.Builder(MainActivity.this);
                errorDialog
                        .setTitle("Dhammadownload")
                        .setMessage(Constants.MainStoragePermissionRequestMsg)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                errorDialog.show();


                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }

        }catch(Exception e){

            Log.e(TAG, "Exception: "+ Log.getStackTraceString(e));
        }

        Log.d(TAG,"[onCreate] End.");

    }

    private View getTabIndicator(Context context, String title, int icon) {

        View view= null;

        try{

            Log.d(TAG,"[getTabIndicator] End.");

            view = LayoutInflater.from(context).inflate(R.layout.tab_layout, null);
            ImageView iv = (ImageView) view.findViewById(R.id.imageView);
            iv.setImageResource(icon);
            TextView tv = (TextView) view.findViewById(R.id.textView);
            Typeface face=Typeface.createFromAsset(getAssets(), Constants.standardFont);
            tv.setTypeface(face);
            tv.setText(title);
            tv.setTextSize(Constants.tabLabelFontSize);
            Log.d(TAG,"[getTabIndicator] End.");

        } catch(Exception e){

            Log.e(TAG, "Exception: "+ Log.getStackTraceString(e));

        }

        return view;

    }



    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.this.finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private  void handleBundleData(Bundle data){
        Log.d(TAG,"handleBundleData");


            Bundle args = data;
            String url=args.getString(Constants.INTENT_URL);


            if(url!=null &&
                    Utils.classifyRequestedFileType(Constants.supportedDownloadFiles,url.toString().toUpperCase())==true) {

                Utils.gotoDownloadPage(this, url.toString(), null);

            }

    }

    /***Permission Requesting Code***/
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                Utils.createNoMediaFile();

            } else {
                // Permission request was denied.
                /*
                Snackbar.make(mLayout, "Camera permission request was denied.",
                        Snackbar.LENGTH_SHORT)
                        .show();
                */

                Snackbar.make(tabHost, "Storage permission request was denied.",
                        Snackbar.LENGTH_SHORT)
                        .show();

            }
        }
        // END_INCLUDE(onRequestPermissionsResult)
    }

    private  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

}
