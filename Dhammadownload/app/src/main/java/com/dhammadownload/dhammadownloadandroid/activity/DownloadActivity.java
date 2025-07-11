/*
Download Activity to donwload MP3,MP4 and PDF etc.....
Chnage History
--------------
25/Sep/2016 - Initial version
 */

package com.dhammadownload.dhammadownloadandroid.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dhammadownload.dhammadownloadandroid.R;
import com.dhammadownload.dhammadownloadandroid.common.Constants;
import com.dhammadownload.dhammadownloadandroid.common.SettingManager;
import com.dhammadownload.dhammadownloadandroid.common.StorageLocation;
import com.dhammadownload.dhammadownloadandroid.common.Utils;
import com.dhammadownload.dhammadownloadandroid.entity.MediaInfo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by zawlinaung on 9/17/16.
 */
@SuppressWarnings("deprecation")
public class DownloadActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int PERMISSION_REQUEST_CAMERA = 0;

    private static final String TAG = "DownloadActivity";

    private Boolean isP2P=false;

    private MediaInfo mMediaInfo;

    String downloadURL = "";
    ImageView imgAuthor;
    TextView txtAuthorNme;
    TextView txtTitle;
    TextView txtDownloadSize;
    TextView txtDescription;
    TextView txtDownloadStausMessage;

    Button btnClose;
    Button btnDownload_Delete;
    Button btnOpen;

    String authorName = "";
    String authorRemoteURL = "";
    String folderPath = "";
    String authorImageURL = "";
    String authorImageLocalPath = "";
    String authorMainConfigURL = "";
    String authorMainConfigLocalPath = "";
    String authorMediaConfigURL = "";
    String authorMediaConfigLocalPath = "";
    String absoluteLocalFilePath = "";

    String strFileDownloadMessage = "";
    Boolean mainFileDownload = true;
    Exception downloadAsyncTaskError;
    // Progress Dialog
    private ProgressDialog pDialog;
    // Progress dialog type (0 - for Horizontal progress bar)
    public static final int progress_bar_type = 0;


    Typeface mmfontface;
    static DownloadFileFromURL downloadActivity;
    AlertDialog.Builder confirmDialog;


    public void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "[onCreate] Start.");

        try {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.download_layout);

            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                downloadURL = extras.getString("downloadURL");
                Log.d(TAG, "[onCreate] Download UR[." + downloadURL + "]");

                isP2P=(extras.containsKey("isP2P"))? extras.getBoolean("isP2P"):SettingManager.getInstance().IS_PSP;
                Log.d(TAG, "[onCreate] isP2P[." + isP2P + "]");
            }

            btnClose = (Button) findViewById(R.id.btnDownloadClose);
            btnDownload_Delete = (Button) findViewById(R.id.btnDownloadDownload_Delete);
            btnOpen = (Button) findViewById(R.id.btnDownloadOpen);
            txtAuthorNme = (TextView) findViewById(R.id.txtAuthorName);
            txtTitle = (TextView) findViewById(R.id.txtTitle);
            imgAuthor = (ImageView) findViewById(R.id.imgViewAuthor);
            txtDownloadSize = (TextView) findViewById(R.id.txtDownloadSize);
            txtDescription = (TextView) findViewById(R.id.Desc);
            txtDownloadStausMessage = (TextView) findViewById(R.id.txtDownloadStatusMsg);

            mmfontface = Typeface.createFromAsset(getAssets(), Constants.standardFont);
            Log.d(TAG, "[onCreate] Initialization controls.");

            mMediaInfo=Utils.convertToMeidaInfoFromUrl(downloadURL);

            //authorName = Utils.getAuthorNameFromLocalURL(downloadURL);
            authorName=mMediaInfo.getAuthorname();
            authorRemoteURL = Utils.getMainAuthorFolder(downloadURL, authorName);

            txtAuthorNme.setText(authorName);
            txtTitle.setText(Utils.getFileNameFromAnyURL(downloadURL));

            txtDescription.setTypeface(mmfontface);
            if (Utils.classifyRequestedFileType(Constants.supportedAudioFiles, downloadURL)) {

                txtDescription.setText(Constants.txtAudioFileDesc);

            } else if (Utils.classifyRequestedFileType(Constants.supportedVideoFiles, downloadURL)) {
                txtDescription.setText(Constants.txtVideoFileDesc);
            } else {
                txtDescription.setText(Constants.txtEbookFileDesc);
            }

            txtDownloadSize.setTypeface(mmfontface);
            txtDownloadSize.setText(Utils.getDownloadSize(downloadURL) + "-MB");

            txtDownloadStausMessage.setTypeface(mmfontface);

            initListenserAndDialog();

            //Check for Storage Permission Granted and
            //proceed to create Folder, download Author Image, Main Config, Media Config
            // only when permission is granted
            //Otherwise Request the permission
            if(isStoragePermissionGranted()){
                createFolderAndDownloadConfigFiles();
            }else{
                txtDownloadStausMessage.setText(Constants.StoragePermissionRequestMsg);
                btnDownload_Delete.setEnabled(false);

                /*
                AlertDialog.Builder errorDialog = new AlertDialog.Builder(DownloadActivity.this);
                errorDialog
                        .setTitle("Error")
                        .setMessage(Constants.StoragePermissionRequestMsg)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                errorDialog.show();
                */


                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }


        } catch (Exception e) {
            Log.e(TAG, "Exception: " + Log.getStackTraceString(e));


            showDownloadError(e);

        }
        Log.d(TAG, "[onCreate] End.");

    }

    private void createFolderAndDownloadConfigFiles() {

        boolean isSDCardAvailable=Utils.isSDCardAvailable();
        if(SettingManager.getInstance().getStorageLocation()==StorageLocation.SDCARD
                && (!isSDCardAvailable)){
            AlertDialog.Builder msgDialog = new AlertDialog.Builder(DownloadActivity.this);
            msgDialog
                    .setTitle("Download")
                    .setMessage(Constants.SDCardNotWritableMsg)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SettingManager.getInstance().setStorageLocation(StorageLocation.SDCARD);
                        }
                    });

            msgDialog.show();
        }

        folderPath = Utils.getLocalAppFolderPathFromWebURL(downloadURL);
        Utils.createFolder(folderPath);
        Utils.createNoMediaFile();
        Log.d(TAG, "[onCreate] Folders are created.[" + folderPath + "]");
        //=====================================================================================
        //download author image | main config | media info

        //=====================================================================================
        authorImageURL = authorRemoteURL + "/" + authorName + Constants.authorImageExt;
        //authorImageLocalPath = authorImageURL.replace(Constants.mainURLProcol, "");

        String authorLocalFolder=Utils.getLocalAppFolderPathFromWebURL(authorImageURL);

        authorImageLocalPath=authorLocalFolder + authorName + Constants.authorImageExt;
        Utils.downloadFile(authorImageURL, authorImageLocalPath);
        //=====================================================================================

        authorMainConfigURL = authorRemoteURL + "/" + Constants.authorMainConfigFile;
        //authorMainConfigLocalPath = authorMainConfigURL.replace(Constants.mainURLProcol, "");
        authorMainConfigLocalPath=authorLocalFolder  + Constants.authorMainConfigFile;
        Utils.downloadFile(authorMainConfigURL, authorMainConfigLocalPath);
        //=====================================================================================

        authorMediaConfigURL = authorRemoteURL + "/" + Constants.authorMediaConfig;
        //authorMediaConfigLocalPath = authorMediaConfigURL.replace(Constants.mainURLProcol, "");
        authorMediaConfigLocalPath = authorLocalFolder + Constants.authorMediaConfig;
        Utils.downloadFile(authorMediaConfigURL, authorMediaConfigLocalPath);
        //=====================================================================================


        /***Update UI based on Downloaded Config File***/
        File authorMainConfigFile = new File(Utils.getDocumentDirectory() + authorMainConfigLocalPath);
        if (authorMainConfigFile.exists()) {
            txtAuthorNme.setTypeface(mmfontface);
            txtAuthorNme.setText(Utils.getProfileInfo(authorMainConfigLocalPath));
        } else {
            Log.d(TAG, "[onCreate] File does not exist.[" + authorMainConfigLocalPath + "]");
        }

        File mediaInfoConfigFile = new File(Utils.getDocumentDirectory() + authorMediaConfigLocalPath);
        String internetRemoteUrl=mMediaInfo.getInternetRemoteUrl();
        if (mediaInfoConfigFile.exists()) {
            txtTitle.setTypeface(mmfontface);
            //txtTitle.setText(Utils.getMediaTitle(authorMediaConfigLocalPath, downloadURL));
            txtTitle.setText(Utils.getMediaTitle(authorMediaConfigLocalPath, internetRemoteUrl));
        } else {
            Log.d(TAG, "[onCreate] File does not exist.[" + authorMediaConfigLocalPath + "]");
        }

        File authorProfileImage = new File(Utils.getDocumentDirectory() + authorImageLocalPath);
        if (authorProfileImage.exists()) {

            Bitmap myBitmap = BitmapFactory.decodeFile(authorProfileImage.getAbsolutePath());
            imgAuthor.setImageBitmap(myBitmap);

        } else {
            Log.d(TAG, "[onCreate] File does not exist.[" + authorImageLocalPath + "]");
            imgAuthor.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.dhammadownload_logo));
        }

        //absoluteLocalFilePath = Utils.getLocalAbosoluteFilePathWithNameFromWebRUL(downloadURL);
        absoluteLocalFilePath = Utils.getLocalAbosoluteFilePathWithNameFromWebRUL(downloadURL);

        txtDownloadStausMessage.setTypeface(mmfontface);

        File localFile = new File(absoluteLocalFilePath);
        if (localFile.exists()) {

            btnDownload_Delete.setText("DELETE");

            if (SettingManager.getInstance().getStorageLocation() == StorageLocation.DEVICE) {
                txtDownloadStausMessage.setText(Constants.fileDownloadedMessage);
            } else {
                txtDownloadStausMessage.setText(Constants.fileDownloadedMessageForSDCard);
            }

        } else {

            btnDownload_Delete.setText("DOWNLOAD");

            if (SettingManager.getInstance().getStorageLocation() == StorageLocation.DEVICE) {
                txtDownloadStausMessage.setText(Constants.fileToDownloadedMessage);
            } else {
                txtDownloadStausMessage.setText(Constants.fileToDownloadedMessageForSDCard);
            }

        }
    }

    /***Initialize UI***/
    private void initListenserAndDialog(){
                btnClose.setOnClickListener(new View.OnClickListener()

    {
        public void onClick (View v){

        finish();
    }
    });

                        btnDownload_Delete.setOnClickListener(new View.OnClickListener()

    {
        public void onClick (View v){

        File localFile = new File(absoluteLocalFilePath);
        if (localFile.exists()) {

            //Put up the Yes/No message box
            confirmDialog = new AlertDialog.Builder(DownloadActivity.this);
            confirmDialog
                    .setTitle("Delete")
                    .setMessage("Are you sure?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            Utils.deleteFileFromLocal(absoluteLocalFilePath);
                            btnDownload_Delete.setText("DOWNLOAD");
                            txtDownloadStausMessage.setText(Constants.fileToDownloadedMessage);

                        }
                    })
                    .setNegativeButton("No", null);

            confirmDialog.show();


        } else {

            downloadActivity = new DownloadFileFromURL();
            mainFileDownload = true;
            strFileDownloadMessage = "Downloading. Please wait...";
            downloadActivity.execute(downloadURL);

        }


    }
    });

                        btnOpen.setOnClickListener(new View.OnClickListener()

    {
        public void onClick (View v){

        ///Opening local files
        File localFile = new File(absoluteLocalFilePath);
        if (localFile.exists()) {

            if (Utils.classifyRequestedFileType(Constants.supportedAudioFiles, downloadURL.toString().toUpperCase()) == true) {

                Intent audioPlayer = new Intent(DownloadActivity.this, AudioPlayerActivity.class);
                audioPlayer.setAction(Intent.ACTION_VIEW);
                audioPlayer.putExtra("authorProfileImage", Utils.getDocumentDirectory() + authorImageLocalPath);
                audioPlayer.putExtra("authorName", txtAuthorNme.getText());
                audioPlayer.putExtra("title", txtTitle.getText());
                audioPlayer.putExtra("openMediaFile", absoluteLocalFilePath);
                startActivity(audioPlayer);

            } else if (Utils.classifyRequestedFileType(Constants.supportedVideoFiles, downloadURL.toString().toUpperCase()) == true) {

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(localFile), "video/mp4");
                startActivity(intent);


            } else if (Utils.classifyRequestedFileType(Constants.supportedEbookFiles, downloadURL.toString().toUpperCase()) == true) {


                try {
//                    Intent intent = new Intent();
//                    intent.setAction(Intent.ACTION_VIEW);
//                    intent.setDataAndType(Uri.parse("file://" + absoluteLocalFilePath), "application/pdf");
//                    startActivity(intent);

                    Intent pdfViewer = new Intent(DownloadActivity.this, PdfViewerActivity.class);
                    pdfViewer.setAction(Intent.ACTION_VIEW);
                    pdfViewer.putExtra("openMediaFile", absoluteLocalFilePath);
                    startActivity(pdfViewer);

                } catch (ActivityNotFoundException e) {
                    Toast.makeText(DownloadActivity.this, "No PDF Viewer Installed", Toast.LENGTH_LONG).show();
                }

            }


        } else {

            ///Opening online files
            if (Utils.classifyRequestedFileType(Constants.supportedAudioFiles, downloadURL.toString().toUpperCase()) == true) {

                Intent audioPlayer = new Intent(DownloadActivity.this, AudioPlayerActivity.class);
                audioPlayer.setAction(Intent.ACTION_VIEW);
                audioPlayer.putExtra("authorProfileImage", Utils.getDocumentDirectory() + authorImageLocalPath);
                audioPlayer.putExtra("authorName", txtAuthorNme.getText());
                audioPlayer.putExtra("title", txtTitle.getText());
                audioPlayer.putExtra("openMediaFile", downloadURL);
                startActivity(audioPlayer);

            } else if (Utils.classifyRequestedFileType(Constants.supportedVideoFiles, downloadURL.toString().toUpperCase()) == true) {

                try{
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(downloadURL), "video/mp4");
                startActivity(intent);
                }catch(ActivityNotFoundException ae){
                    String errorMsg="No video player App installed. Please install App that can play video. \n" +
                            absoluteLocalFilePath +
                            "\n" + ae.getMessage();
                    Toast.makeText(DownloadActivity.this, errorMsg,Toast.LENGTH_LONG).show();
                    Log.e(TAG,errorMsg,ae);
                }

            } else if (Utils.classifyRequestedFileType(Constants.supportedEbookFiles, downloadURL.toString().toUpperCase()) == true) {

                try {
                    strFileDownloadMessage = "Loading file. Please wait...";
                    mainFileDownload = false;
                    downloadActivity = new DownloadFileFromURL();
                    downloadActivity.execute(downloadURL);

                } catch (ActivityNotFoundException e) {
                    Toast.makeText(DownloadActivity.this, "No PDF Viewer Installed. Please install.", Toast.LENGTH_LONG).show();
                }


            }

        }
    }
    });

    pDialog =new

    ProgressDialog(this);
                        pDialog.setButton(ProgressDialog.BUTTON_NEGATIVE,"Cancel",new DialogInterface.OnClickListener()

    {

        @Override
        public void onClick (DialogInterface dialog,int which){


        downloadActivity.cancel(true);

        File downloadFile = new File(absoluteLocalFilePath);
        if (downloadFile.exists()) {
            downloadFile.delete();
        }
        pDialog.cancel();
    }
    });

}

                /**
                 * Showing Dialog
                 * */
                @Override
                protected Dialog onCreateDialog(int id) {
                    switch (id) {
                        case progress_bar_type: // we set this to 0
                            pDialog.setMessage(strFileDownloadMessage);
                            pDialog.setIndeterminate(false);
                            pDialog.setMax(100);
                            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            pDialog.setCancelable(false);
                            pDialog.show();
                            return pDialog;
                        default:
                            return null;
                    }
    }

    private void showDownloadError(Exception downloadException){
        if(downloadException!=null){
            AlertDialog.Builder errorDialog = new AlertDialog.Builder(DownloadActivity.this);
            errorDialog
                    .setTitle("Error")
                    .setMessage("Unexpected Error Occur. \n" + "Error Details: " + downloadAsyncTaskError.getMessage())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            txtDownloadStausMessage.setText(Constants.DownloadErrorMsg);
                            btnDownload_Delete.setEnabled(false);
                            btnOpen.setEnabled(false);

                        }
                    });

            errorDialog.show();
        }
    }

    /***Permission Requesting Code***/
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start camera preview Activity.
                /*
                Snackbar.make(mLayout, "Camera permission was granted. Starting preview.",
                        Snackbar.LENGTH_SHORT)
                        .show();
                startCamera();
                */
                btnDownload_Delete.setEnabled(true);

                createFolderAndDownloadConfigFiles();

            } else {
                // Permission request was denied.
                /*
                Snackbar.make(mLayout, "Camera permission request was denied.",
                        Snackbar.LENGTH_SHORT)
                        .show();
                */
                txtDownloadStausMessage.setText(Constants.StoragePermissionRequestMsg);
                btnDownload_Delete.setEnabled(false);
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



    /**
     * Background Async Task to download file
     * */
    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progress_bar_type);
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) {

            Log.d(TAG,"[doInBackground] Start.");
            int count;
            try {
                downloadAsyncTaskError=null;//Set to no Error at beginning

                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();
                // this will be useful so that you can show a tipical 0-100% progress bar
                int lenghtOfFile = conection.getContentLength();
                Log.d(TAG,"[doInBackground--- Log.d(TAG,\"[lenghtOfFile] Start.\");] " + lenghtOfFile);

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                // Output stream
                OutputStream output = new FileOutputStream(absoluteLocalFilePath);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress(""+(int)((total*100)/lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);

                }
                //conection = null;
                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

                File downloadedFile = new File(absoluteLocalFilePath);
                if (downloadedFile.exists()){
                downloadedFile.length();

                    Log.d(TAG,"[doInBackground--- Log.d(TAG,\"[downloadedFile].\");] " + downloadedFile.length());
                }

            } catch (Exception e) {

                mainFileDownload=false;
                downloadAsyncTaskError=e;

                Log.e(TAG, "Exception: "+ Log.getStackTraceString(e));
                File downloadedFile = new File("absoluteLocalFilePath");
                if (downloadedFile.exists()){
                    downloadedFile.delete();
                }
            }

            Log.d(TAG,"[doInBackground] End.");
            return null;
        }

        /**
         * Updating progress bar
         * */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        /**
         * After completing background task
         * Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String file_url) {

            Log.d(TAG,"[onPostExecute] Start.");
            try{

                // dismiss the dialog after the file was downloaded
                dismissDialog(progress_bar_type);

                showDownloadError(downloadAsyncTaskError);

                if(mainFileDownload == true){

                    btnDownload_Delete.setText("DELETE");
                    txtDownloadStausMessage.setText(Constants.fileDownloadedMessage);

                    if(Utils.classifyRequestedFileType(Constants.supportedAudioFiles,downloadURL.toString().toUpperCase())==true){

                        Intent audioPlayer = new Intent(DownloadActivity.this, AudioPlayerActivity.class);
                        audioPlayer.setAction(Intent.ACTION_VIEW);
                        audioPlayer.putExtra("authorProfileImage", Utils.getDocumentDirectory() + authorImageLocalPath);
                        audioPlayer.putExtra("authorName", txtAuthorNme.getText());
                        audioPlayer.putExtra("title", txtTitle.getText());
                        audioPlayer.putExtra("openMediaFile", absoluteLocalFilePath);
                        startActivity(audioPlayer);

                    }

                    else if(Utils.classifyRequestedFileType(Constants.supportedVideoFiles,downloadURL.toString().toUpperCase())==true){

//                        Intent intent = new Intent();
//                        intent.setAction(Intent.ACTION_VIEW);
//                        intent.setDataAndType(Uri.fromFile(new File(absoluteLocalFilePath)),"video/mp4");
//                        startActivity(intent);

                        try {
                            File openMediaFile=new File(absoluteLocalFilePath);
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            Uri uri = FileProvider.getUriForFile(DownloadActivity.this, DownloadActivity.this.getApplicationContext().getPackageName() + ".provider", openMediaFile);
                            intent.setDataAndType(uri, "video/mp4");
                            startActivity(intent);
                        }catch(ActivityNotFoundException ae){
                            String errorMsg="No video player App installed. Please install App that can play video." +
                                    absoluteLocalFilePath +
                                    "\n" + ae.getMessage();
                            Toast.makeText(DownloadActivity.this, errorMsg,Toast.LENGTH_LONG).show();
                            Log.e(TAG,errorMsg,ae);
                        }


                    } else if(Utils.classifyRequestedFileType(Constants.supportedEbookFiles,downloadURL.toString().toUpperCase())==true){



//                        try
//                        {
//                            Intent intent = new Intent();
//                            intent.setAction(Intent.ACTION_VIEW);
//                            intent.setDataAndType(Uri.parse("file://" + absoluteLocalFilePath),"application/pdf");
//                            startActivity(intent);
//
//                        }
//                        catch (ActivityNotFoundException e)
//                        {
//                            Toast.makeText(DownloadActivity.this, "No PDF Viewer Installed", Toast.LENGTH_LONG).show();
//                        }

                        Intent pdfViewer = new Intent(DownloadActivity.this, PdfViewerActivity.class);
                        pdfViewer.setAction(Intent.ACTION_VIEW);
                        pdfViewer.putExtra("openMediaFile", absoluteLocalFilePath);
                        startActivity(pdfViewer);

                    }



                }else{
                    openOnlinePdf();
                }

                if (downloadActivity != null){
                    downloadActivity = null;
                }

            } catch (Exception e) {
                Log.e(TAG, "Exception: "+ Log.getStackTraceString(e));
            }

            Log.d(TAG,"[onPostExecute] End.");
        }

        @Override
        protected void onCancelled(){

            super.onCancelled();
            downloadActivity.cancel(true);

        }




    }

    public void openOnlinePdf(){

        try{

            Log.d(TAG,"[openOnlinePdf] Start.");

            String tempPdfFile = Utils.getPDFTempFile();
            File tempFile = new File(tempPdfFile);
            File downloadedFile = new File(absoluteLocalFilePath);

            if (tempFile.exists()){

                tempFile.delete();
            }
            if (downloadedFile.exists()){

                downloadedFile.renameTo(tempFile);
//                Intent intent = new Intent();
//                intent.setAction(Intent.ACTION_VIEW);
//                intent.setDataAndType(Uri.parse("file://" + tempFile),"application/pdf");
//                startActivity(intent);

                Intent pdfViewer = new Intent(this, PdfViewerActivity.class);
                pdfViewer.setAction(Intent.ACTION_VIEW);
                pdfViewer.putExtra("openMediaFile", tempFile.getPath());
                startActivity(pdfViewer);

            }

            if(downloadedFile!=null){
                downloadedFile=null;
            }

        } catch (Exception e) {
            Log.e(TAG, "Exception: "+ Log.getStackTraceString(e));
        }
        Log.d(TAG,"[openOnlinePdf] End.");
    }

}
