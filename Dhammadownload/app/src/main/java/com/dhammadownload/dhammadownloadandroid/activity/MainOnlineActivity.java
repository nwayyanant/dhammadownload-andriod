package com.dhammadownload.dhammadownloadandroid.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dhammadownload.dhammadownloadandroid.R;
import com.dhammadownload.dhammadownloadandroid.common.Constants;
import com.dhammadownload.dhammadownloadandroid.common.SettingManager;
import com.dhammadownload.dhammadownloadandroid.common.Utils;

/**
 * Created by zawlinaung on 9/15/16.
 */
@SuppressWarnings("deprecation")
public class MainOnlineActivity extends Activity {

    private static final String TAG = "MainOnlineActivity";

    private WebView webView;
    ImageButton imgbtnBack;
    ImageButton imgbtnForward;
    ImageButton imgbtnRefresh;
    TextView txtTitle;

    public void onCreate(Bundle savedInstanceState) {

        try {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.main_online_layout);

            Log.d(TAG,"[onCreate] Start.");

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            webView = (WebView) findViewById(R.id.mainWebView);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Log.i(TAG, "Loading URL: " + url);
                    if (Utils.classifyRequestedFileType(
                            Constants.supportedDownloadFiles,
                            url.toUpperCase())) {
                        Utils.gotoDownloadPage(
                                MainOnlineActivity.this,
                                url,
                                view
                        );
                        return true;  // we handled it
                    }
                    view.loadUrl(url);
                    return true;
                }

                @Override
                public void onReceivedError(WebView view,
                                            WebResourceRequest request,
                                            WebResourceError error) {
                    Log.e(TAG, "WebView error on "
                            + request.getUrl() + ": ");
                            //+ error.getDescription());
                    // show an error UI here if you like
                }
            });


            txtTitle = (TextView) findViewById(R.id.txtTitle);
            Typeface face=Typeface.createFromAsset(getAssets(), Constants.standardFont);
            txtTitle.setTypeface(face);
            txtTitle.setText(Constants.dhammadownloadTitile_MM);

            //webView.setInitialScale(70);
            // 2. Enable JavaScript if needed
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setUseWideViewPort(true);
            webSettings.setBuiltInZoomControls(true);

            webSettings.setDomStorageEnabled(true);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setUseWideViewPort(true);
            webView.getSettings().setDisplayZoomControls(false);

            // 3. Allow mixed content on Lollipop and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                webSettings.setMixedContentMode(
                        WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                );
            }


            webView.loadUrl(Constants.mainURL);

            imgbtnBack = (ImageButton) findViewById(R.id.imgbtnBack);
            imgbtnBack.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    webView.goBack();
                }
            });

            imgbtnForward = (ImageButton) findViewById(R.id.imgbtnForward);
            imgbtnForward.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    webView.goForward();
                }
            });

            imgbtnRefresh = (ImageButton) findViewById(R.id.imgbtnRefresh);
            imgbtnRefresh.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    webView.clearCache(true);
                    //webView.loadUrl(webView.getUrl());

                    webView.loadUrl(SettingManager.getInstance().getHomePage());

                    //mkt 2018-09-23 Set P2P Mode On
                    //SettingManager.getInstance().IS_PSP=true;
                }
            });



                webView.setWebViewClient(new WebViewClient() {

                    public void onPageStarted(WebView view, String url, Bitmap favicon)
                    {
                        //
                    }

                    public boolean shouldOverrideUrlLoading(WebView view, String url){
                        Log.i("CatchURL" , url.toString());

                        if(Utils.classifyRequestedFileType(Constants.supportedDownloadFiles,url.toString().toUpperCase())==true){


                            Utils.gotoDownloadPage( MainOnlineActivity.this  ,url.toString(),view);


                        }
                 
                        return false;
                    }
                });

        }catch(Exception e){

            Log.e(TAG, "Exception: "+ Log.getStackTraceString(e));
        }

        Log.d(TAG,"[onCreate] End.");
    }



//    @Override
//    public void onResume() {
//
//        Log.d(TAG,"[onResume] Start.");
//        super.onResume();  // Always call the superclass method first
//
//        webView.loadUrl(SettingManager.getInstance().getHomePage());
//
//        Log.d(TAG,"[onResume] End.");
//
//    }

    @Override
    public void onDestroy() {

        Log.d(TAG,"[onDestroy] Start.");
        super.onDestroy();
        Log.d(TAG,"[onDestroy] End.");

    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainOnlineActivity.this.finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

}
