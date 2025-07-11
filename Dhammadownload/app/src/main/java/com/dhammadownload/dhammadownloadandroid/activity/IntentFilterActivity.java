package com.dhammadownload.dhammadownloadandroid.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import androidx.core.app.ActivityCompat;
import android.os.Bundle;
import android.util.Log;

import com.dhammadownload.dhammadownloadandroid.common.Constants;
import com.dhammadownload.dhammadownloadandroid.common.Dhammadownload;
import com.dhammadownload.dhammadownloadandroid.common.Utils;

public class IntentFilterActivity extends Activity {

    private static final String TAG = "IntentFilterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String DHAMMADOWNLOAD_HOST="dhammadownload.com";
        String DHAMMADOWNLOAD_HOST2="www.dhammadownload.com";

        Intent intent=getIntent();
        if ( intent.getData()!=null &&
                intent.getData().getHost()!=null &&
                (
                        intent.getData().getHost().equals(DHAMMADOWNLOAD_HOST)||
                                intent.getData().getHost().equals(DHAMMADOWNLOAD_HOST2))
        ) {
            Uri data = intent.getData();
            String url= data.toString();

//            Bundle args = new Bundle();
//            args.putString(Constants.INTENT_URL, url);
//            Dhammadownload application = (Dhammadownload) getApplication();
//            application.setIntentDataBundle(args);

            Log.i("IntentURL" , url.toString());

            if (isTaskRoot())
            {
                // Start the app before finishing
                Intent startAppIntent = new Intent(getApplicationContext(), MainActivity.class);
                startAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startAppIntent.putExtra(Constants.INTENT_URL,url);
                startActivity(startAppIntent);
            }else{
                if(Utils.classifyRequestedFileType(Constants.supportedDownloadFiles,url.toString().toUpperCase())==true) {

                    Utils.gotoDownloadPage(this, url.toString(), null);

                }
            }

        }



        finish();
    }


}
