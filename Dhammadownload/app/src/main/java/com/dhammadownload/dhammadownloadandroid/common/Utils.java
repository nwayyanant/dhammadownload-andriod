package com.dhammadownload.dhammadownloadandroid.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.webkit.WebView;

import com.dhammadownload.dhammadownloadandroid.activity.DownloadActivity;
import com.dhammadownload.dhammadownloadandroid.entity.MediaInfo;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Hashtable;
import android.content.Context;
import android.os.Environment;
import java.util.Locale;

/**
 * Created by zawlinaung on 9/17/16.
 */
public class Utils {

    private static final String TAG = "UTILS";

    private static Context context;
    public  static Context getContext(){
        return context;
    }

    //This function will classify whether requested file type is supported file type or not
    //Input parameter URL is completed web URL .. http://dhamm..../..../requestedfile.mp3
    //Retrun value is true if it is supported file type. Otherwise false
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static boolean classifyRequestedFileType(String[] supportedFileType,String checkString){

        boolean result = false;
        String extension = "";

        try{

            if(checkString != null && checkString.length()>0){

                int index = checkString.lastIndexOf('.');
                if (index > 0) {
                    extension = "";
                    extension = checkString.substring(index+1,checkString.length());
                }

                Log.i(TAG,extension.toString());

                for (int i=0;i<supportedFileType.length;i++){

                    if (supportedFileType[i].equalsIgnoreCase(extension)){

                        Log.i("Classify","supportedFileType is true.");
                        result = true;
                        break;
                    }
                }
            }

        } catch(Exception e){

            Log.e(TAG, "Exception: "+ Log.getStackTraceString(e));

        }
        return result;
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    //This function is used to get file name from any URL(local path or web URL)
    //Input is file path
    //Output is filename only
    public static String getFileNameFromAnyURL(String url){

        File file = new File(url);
        String strFileName = file.getName();
        return strFileName;

    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //This function is used to create folder according to given path
    //Input folderpath parameter should be begun with dhammadownload.com app main folder and without file name.e.g  dhammadownload.com/MP3Lib../../../Disc1
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void createFolder(String folderPath) {

        try{

            File docsFolder = new File(getDocumentDirectory() + "/Documents");
            if (!docsFolder.exists()) {
                docsFolder.mkdir();
            }



            Log.d(TAG , "Input Folder Path " + folderPath);
            // create a File object for the parent directory
            File downloadDirectory = new File(getDocumentDirectory() + folderPath);
            // have the object build the directory structure, if needed.
            downloadDirectory.mkdirs();

            Log.d(TAG , "Folders are created");

        } catch(Exception e){

            Log.e(TAG, "Exception: "+ Log.getStackTraceString(e));

        }

    }

    public static void createNoMediaFile() {

        try{
        //Create .nomedia File to hide App images in Gallery
        File nomedia=new File( getDocumentDirectory() + "/" + Constants.mainFolder  + "/.nomedia" );
        if (!nomedia.exists()) {
            nomedia.createNewFile();
        }
        } catch(Exception e){

            Log.e(TAG, "Exception: "+ Log.getStackTraceString(e));

        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //This function is used to get local folder path.
    //Input URL is completed web URL starting from http://..//..//./aaa.mp3
    //Output is starting from dhammadownload.com folder with no filename e.g  dhammadownload.com/MP3Lib../../../Disc1
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static String getLocalAppFolderPathFromWebURL(String url) {


        Log.d(TAG , "Download URL is " + url);
        String inputString = url;

        try{

                inputString = inputString.replaceAll(Constants.mainURLProcol,"");
                Log.d(TAG , "After removing protocol prefix " + inputString);

                String theFileName = getFileNameFromAnyURL(url);
                Log.d(TAG , "After getting file name: " + inputString);
                inputString = inputString.replaceAll(theFileName,"");

                URL webUrl=new URL(url);
                String path=webUrl.getPath();
                inputString=path.replaceAll(theFileName,"");

                inputString="/" + Constants.mainFolder + inputString;

                Log.d(TAG, "Result String-" + inputString);

        } catch(Exception e){

            Log.e(TAG, "Exception: "+ Log.getStackTraceString(e));

        }
        return inputString;

    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //This function is used to get complete path of local file path with filename from request web URL.
    //Input URL is completed web URL starting from http://..//..//./aaa.mp3
    //Output will be while system file path look like file:///User/../dhammadownload.com/../../aaa.mp3
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static String getLocalAbosoluteFilePathWithNameFromWebRUL(String url){

        String result= "";
        try{

            String documentsPath = getDocumentDirectory();
            result = documentsPath + getLocalAppFolderPathFromWebURL(url) + getFileNameFromAnyURL(url);

        } catch(Exception e){

            Log.e(TAG, "Exception: "+ Log.getStackTraceString(e));

        }
        return result;

    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //This function is used to check file which is already exist in local or not
    //Input parameter is complete web URL (http://dhamma.../.../../...mp4
    //Output parameter is true or false
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static boolean isFileAlreadyExist(String url)
    {
        boolean result = false;
        File file = new File( getDocumentDirectory() + getLocalAppFolderPathFromWebURL(url) + getFileNameFromAnyURL(url));

        if(file.exists()){
            result = true;
        }else{
            result = false;
        }

        return result;

    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //This function is used to get all all downloaded file(supported files only) from application folder dhammadownload.com
    //Output is list of all downloaded medias paths(DownloadMedia objects) -- path is look like dhammadownload.com/.../../..mp3
    public static void getListOfAllLocalDownloadedMedia(String[] filetypes,String folderpath,ArrayList<MediaInfo> listOfAllFiles) {

        try{

                File appMainFolder = new File(folderpath);
                File[] list = appMainFolder.listFiles();
                MediaInfo mediaInfo = null;

                if (list != null) {

                    for (File f : list) {
                        if (f.isDirectory()) {
                            getListOfAllLocalDownloadedMedia(filetypes,f.getAbsoluteFile().toString(),listOfAllFiles);
                        } else {

                            if ( classifyRequestedFileType(filetypes,f.getAbsolutePath()) == true ){

                                mediaInfo = new MediaInfo();
                                mediaInfo = searchSavedFileName(f.getAbsolutePath());
                                listOfAllFiles.add(mediaInfo);

                            }

                        }
                    }

                }

        } catch(Exception e){

            Log.e(TAG, "Exception: "+ Log.getStackTraceString(e));

        }

    }

    public static void getListOfAllExternalDownloadedMedia(String[] filetypes,ArrayList<MediaInfo> listOfAllFiles) {
        String sdCardPath=getSDCardPath();
        if(!("".equals(sdCardPath))) {
            String appHomeFolder = sdCardPath + "/" + Constants.mainFolder;
            getListOfAllDownloadedMediaByPath(filetypes, appHomeFolder, listOfAllFiles,StorageLocation.SDCARD);
        }
    }

    public static void getListOfAllDownloadedMediaByPath(String[] filetypes,String folderpath,ArrayList<MediaInfo> listOfAllFiles,StorageLocation storageLocation) {

        try{

            File appMainFolder = new File(folderpath);
            File[] list = appMainFolder.listFiles();
            MediaInfo mediaInfo = null;

            if (list != null) {

                for (File f : list) {
                    if (f.isDirectory()) {
                        getListOfAllLocalDownloadedMedia(filetypes,f.getAbsoluteFile().toString(),listOfAllFiles);
                    } else {

                        if ( classifyRequestedFileType(filetypes,f.getAbsolutePath()) == true ){

                            mediaInfo = new MediaInfo();
                            mediaInfo = searchSavedFileName(f.getAbsolutePath(),storageLocation);
                            listOfAllFiles.add(mediaInfo);

                        }

                    }
                }

            }

        } catch(Exception e){

            Log.e(TAG, "Exception: "+ Log.getStackTraceString(e));

        }

    }

    public static void getListOfAllDownloadedMedia(String[] filetypes, ArrayList<MediaInfo> listOfAllFiles) {
        Hashtable<String,ArrayList<MediaInfo>> combineListOfAllFiles=new Hashtable<>();

        String appHomeFolder = Utils.getDocumentDirectoryOnDevice() + "/" + Constants.mainFolder;
        ArrayList<MediaInfo> listOfAllFilesOnDevice=new ArrayList<MediaInfo>();
        getListOfAllDownloadedMediaByPath(filetypes,appHomeFolder,listOfAllFilesOnDevice,StorageLocation.DEVICE);

        ArrayList<MediaInfo> listOfAllFilesOnSDCard=new ArrayList<MediaInfo>();
        getListOfAllExternalDownloadedMedia(filetypes,listOfAllFilesOnSDCard);

        for(MediaInfo mediaInfo:listOfAllFilesOnDevice){
            String key=mediaInfo.getAuthorname();
            ArrayList<MediaInfo> listForKey=combineListOfAllFiles.get(key);
            if(listForKey==null){
                listForKey=new ArrayList<MediaInfo>();
            }
            mediaInfo.setStorageLocation(StorageLocation.DEVICE);
            listForKey.add(mediaInfo);
            combineListOfAllFiles.put(key,listForKey);
        }

        for(MediaInfo mediaInfo:listOfAllFilesOnSDCard){
            String key=mediaInfo.getAuthorname();
            ArrayList<MediaInfo> listForKey=combineListOfAllFiles.get(key);
            if(listForKey==null){
                listForKey=new ArrayList<MediaInfo>();
            }
            mediaInfo.setStorageLocation(StorageLocation.SDCARD);
            listForKey.add(mediaInfo);
            combineListOfAllFiles.put(key,listForKey);
        }

        listOfAllFiles.clear();

        for(ArrayList<MediaInfo> listForKey:combineListOfAllFiles.values()){
            listOfAllFiles.addAll(listForKey);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //This function is used to delete the file from local
    //Input parameter is local complete path is look like file:///Users/.../../../dhammadownload.com/.../../..mp3
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static boolean deleteFileFromLocal(String localFilePath)
    {
        Boolean result = false;

        File file = new File(localFilePath);

        if (file.exists()){
            result = file.delete();
        }

        return result;
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //This function is used to get local application path(dhammadownload.com/MP3.../... from local absolute path file://Users/../
    //including file name
    //Input parameter is local complete path is look like /file://Users/.././dhammadownload.com/.../../..mp3
    //Output is dhammadownload.com/MP3.../.../..mp3
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getLocalAppFilePathFromAbsoluteLocalPath(String completeFilePath)
    {

        String result = "";

        int index = completeFilePath.indexOf(Constants.mainFolder);
        result = completeFilePath.substring(index,completeFilePath.length());
        return result;

    }
    //=====================================================================================
    //=====================================================================================


    //This function is used to get author name(folder name) from absolute local path /file://Users/.././dhammadownload.com/.../../..mp3
    //Input parameter is local complete path is look like file://Users/.././dhammadownload.com/.../../..mp3
    //Output is Author Name(Folder Name)
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static String getAuthorNameFromLocalURL(String path)
    {

        Log.d(TAG , "Input Path" + path);
        String result = "";
        try{

            String tmpString = getLocalAppFilePathFromAbsoluteLocalPath(path);
            String[] arrFilePath = tmpString.split("/");
            result = arrFilePath[2];

        } catch(Exception e){

            Log.e(TAG, "Exception: "+ Log.getStackTraceString(e));

        }
        return result;

    }
    //=====================================================================================
    //=====================================================================================


    //This function is used to get author main folder path from local file path
    //Input parameter is local complete path is look like http://dhammadownload.com/.../../..mp3
    //Output is Author Main folder from remote location like http://dhammadownload.com/MP3Li.../Author Folder/
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static String getMainAuthorFolder(String url,String author){

        String result = "";

        Log.d(TAG, "[getMainAuthorFolder]Input URL is " + url);
        Log.d(TAG, "[getMainAuthorFolder]Author is " + author);
        try{

            int index=url.indexOf("/" + author + "/");
            Log.d(TAG, "[getMainAuthorFolder]INDEX is " + index);

            result = url.substring(0,index) + "/" + author ;
            Log.d(TAG, "[getMainAuthorFolder]RESULT is " + result);

        }catch (Exception e){

            Log.e(TAG, "Exception: "+ Log.getStackTraceString(e));

        }


        return result;

    }
    //=====================================================================================
    //=====================================================================================


    //This function is used to download the file without progress bar
    //Input parameter is complete remote web URL http://dhammadownload.com/.../../..mp3
    //Input parameter is local path like dhammadownload/MP3Lib.../..../..../...mp3
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static boolean downloadFile(String url, String destinationPath) {

            Boolean result = false;
        try {

            Log.d(TAG,"[downloadFile] Start.");

            Log.d(TAG,"[downloadFile] Input parameter URL - [" + url + "]");
            Log.d(TAG,"[downloadFile] Input parameter destinationPath - [" + destinationPath + "]");

            String localFilePath = getDocumentDirectory() + destinationPath;
            Log.d(TAG,"[downloadFile] Local absolute file path - [" + destinationPath + "]");

            File file = new File(localFilePath);
            if(file.exists()){
                file.delete();
                Log.d(TAG,"[downloadFile] File is already exist and deleted");
            }

            URL onlineURL = new URL(url);
            URLConnection conection = onlineURL.openConnection();
            conection.connect();
            Log.d(TAG,"[downloadFile] Open connection.");

            // download the file
            InputStream input = new BufferedInputStream(onlineURL.openStream(),8192);

            // Output stream
            OutputStream output = new FileOutputStream(localFilePath);

            byte data[] = new byte[1024];

            int count;
            while ((count = input.read(data)) != -1) {
                // writing data to file
                output.write(data, 0, count);
            }
            Log.d(TAG,"[downloadFile] Download completed.");
            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

            result = true;
            Log.d(TAG,"[downloadFile] End.");

        } catch (Exception e) {
            Log.e(TAG, "Exception: "+ Log.getStackTraceString(e));
        }

        return result;
    }

    //=====================================================================================
    //=====================================================================================

    //This function is used to search author name,profile image,title from configuration file
    //Input parameter is local complete path is look like /Users/.../../../dhammadownload.com/.../../..mp3
    //Output is string array which contain author name,profile image and title
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static MediaInfo searchSavedFileName(String mediaLocalAbsolutePath) {
        return searchSavedFileName(mediaLocalAbsolutePath,StorageLocation.DEVICE);//tmp method
    }

    public static MediaInfo searchSavedFileName(String mediaLocalAbsolutePath,StorageLocation storageLocation)
    {

        Log.d(TAG,"[searchSavedFileName] Start.");

        Log.d(TAG,"[searchSavedFileName] Input parameter mediaLocalAbsolutePath - [" + mediaLocalAbsolutePath + "]");

        MediaInfo result = new MediaInfo();
        try{

            String authorMainFolderPath = Utils.getLocalAppFilePathFromAbsoluteLocalPath(mediaLocalAbsolutePath);
            //=====================================================================================
            //download author image | main config | media info
            String authorName=Utils.getAuthorNameFromLocalURL(mediaLocalAbsolutePath);
            Log.d(TAG,"[searchSavedFileName] AuthorName - [" + authorName + "]");

            String authorLocalMainFolder=Utils.getMainAuthorFolder(authorMainFolderPath,authorName);
            Log.d(TAG,"[searchSavedFileName] authorLocalMainFolder - [" + authorLocalMainFolder + "]");
            //=====================================================================================
            String authorMainConfigLocalPath = "/" + authorLocalMainFolder + "/" + Constants.authorMainConfigFile;
            Log.d(TAG,"[searchSavedFileName] authorMainConfigLocalPath - [" + authorMainConfigLocalPath + "]");
            //String authorImageLocalPath = Utils.getDocumentDirectory() + "/" + authorLocalMainFolder + "/" + authorName + Constants.authorImageExt;
            String authorImageLocalPath = Utils.getDocumentDirectory(storageLocation) + "/" + authorLocalMainFolder + "/" + authorName + Constants.authorImageExt;
            Log.d(TAG,"[searchSavedFileName] authorImageLocalPath - [" + authorImageLocalPath + "]");
            String mediaconfigLocalPath = "/" + authorLocalMainFolder + "/" + Constants.authorMediaConfig;
            Log.d(TAG,"[searchSavedFileName] mediaconfigLocalPath - [" + mediaconfigLocalPath + "]");
            //=====================================================================================


            String appSavePath = getLocalAppFilePathFromAbsoluteLocalPath(mediaLocalAbsolutePath);
            Log.d(TAG,"[searchSavedFileName] appSavePath - [" + appSavePath + "]");

            //Set default
            result.setAuthorname(authorName);
            result.setProfileimage("");
            result.setFilename(getFileNameFromAnyURL(mediaLocalAbsolutePath));
            result.setPhysicallocation(mediaLocalAbsolutePath);

            String authorNameFromConfig = getProfileInfo(authorMainConfigLocalPath);
            Log.d(TAG,"[searchSavedFileName] authorNameFromConfig - [" + authorNameFromConfig + "]");
            if (authorNameFromConfig != "") {
                result.setAuthorname(authorNameFromConfig);
            }

            File authorImage = new File(authorImageLocalPath);
            Log.d(TAG,"[searchSavedFileName] authorImageLocalPath - [" + authorImageLocalPath + "]");
            if (authorImage.exists()){
                result.setProfileimage(authorImageLocalPath);
            }


            String mediaTitle = getMediaTitle(mediaconfigLocalPath,appSavePath);
            Log.d(TAG,"[searchSavedFileName] mediaTitle - [" + mediaTitle + "]");
            if(mediaTitle !=""){
                result.setFilename(mediaTitle);
            }

        }catch(Exception e){

            Log.e(TAG, "Exception: "+ Log.getStackTraceString(e));
        }

        Log.d(TAG,"[searchSavedFileName] End.");
        return result;
    }
    //=====================================================================================
    //=====================================================================================

    public static String getDocumentDirectoryOnDevice()
    {
        String path=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath();

        Log.i(TAG,"[getDocumentDirectory] Document Directory is " + path );
        return path;

    }

    public static String getDocumentDirectory()
    {
        StorageLocation storageLocation=SettingManager.getInstance().getStorageLocation();
        boolean external=(storageLocation== StorageLocation.SDCARD);

        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath();
        if(external){
            path=getSDCardPath();
        }

        Log.i(TAG,"[getDocumentDirectory] Document Directory for Download is " + path );
        return path;

    }

    public static String getDocumentDirectory(StorageLocation storageLocation)
    {
        String path="";
        if(storageLocation==StorageLocation.DEVICE){
            path=getDocumentDirectoryOnDevice();
        }else{
            path=getSDCardPath();
        }
        return path;
    }

    public static String getSDCardPath(){
        Context ctx=context;
        File[] files=ctx.getExternalFilesDirs("");

        String path="";

        for(int i=0;i<files.length;i++){
            File f=files[i];

            if(f==null){ continue;}

            boolean canRead=f.canRead();
            boolean canWrite=f.canWrite();
            boolean isExternalStorageEmulated=Environment.isExternalStorageEmulated();
            boolean isExternalStorageRemovable=isExternalStorageRemovable(f);

            if(canRead && canWrite && isExternalStorageRemovable) {
                path = f.getAbsolutePath();
            }
        }

        return path;
    }

    public static boolean isSDCardAvailable(){
        boolean result=false;
        String sdCardPath=getSDCardPath();
        result=(!("".equals(sdCardPath)));

        return result;
    }

    public static boolean isExternalStorageRemovable(File f){
        boolean result=false;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
           result= Environment.isExternalStorageRemovable(f);
        }else{
           String[] internalKeywords={"emulated",""};
            String path=f.getAbsolutePath();
            for(String keyword:internalKeywords){
                if(path.contains(keyword)){
                    return false;
                }
            }
            result=true;
        }
        return result;
    }



    public static void setContext(Context ctx){
        context=ctx;
    }



    //=====================================================================================
    //=====================================================================================

    public static String getProfileInfo(String authorMainConfigLocalPath){

        String result = "";


        try {

            Log.d(TAG,"[getProfileInfo] Start.");

            Log.d(TAG,"[getMediaTitle] Input parameter authorMainConfigLocalPath - [" + authorMainConfigLocalPath + "]");

            String localAbsoluteFilePath = getDocumentDirectory() + authorMainConfigLocalPath;
            File profileInfoData = new File(localAbsoluteFilePath);
            Log.d(TAG,"[getProfileInfo] Author main configuration file " + localAbsoluteFilePath);
            FileReader fileProfileReader = new FileReader(profileInfoData);
            BufferedReader bufferProfileReader = new BufferedReader(fileProfileReader);


            result = bufferProfileReader.readLine();
            Log.d(TAG,"[getProfileInfo] Result " + result);

            fileProfileReader.close();;
            bufferProfileReader.close();

            if(fileProfileReader !=null){
                fileProfileReader.close();
            }
            if(bufferProfileReader !=null){
                bufferProfileReader.close();
            }

        }catch(Exception e){
            Log.e(TAG, "Exception: "+ Log.getStackTraceString(e));
        }


        Log.d(TAG,"[getProfileInfo] End.");
        return result;

    }

    //=====================================================================================
    //=====================================================================================

    public static String getMediaTitle(String mediaInfoLocalPath,String url){

        String result = "";

        Log.d(TAG,"[getMediaTitle] Start.");

        Log.d(TAG,"[getMediaTitle] Input parameter mediaInfoLocalPath - [" + mediaInfoLocalPath + "]");
        Log.d(TAG,"[getMediaTitle] Input parameter url - [" + url + "]");

        try {


            String localAbsoluteFilePath = getDocumentDirectory() + mediaInfoLocalPath;
            File mediaInfoData = new File(localAbsoluteFilePath);
            FileReader fileMediaInfoReader = new FileReader(mediaInfoData);
            BufferedReader bufferMediaInfoReader = new BufferedReader(fileMediaInfoReader);

            Log.d(TAG,"[getProfileInfo] Author main configuration file " + localAbsoluteFilePath);

            String line = "";
            while ((line = bufferMediaInfoReader.readLine()) != null) {

                Log.d(TAG,"[getProfileInfo] Media info line " + line);

                if (line.contains(url) == true ){

                    Log.d(TAG,"[getProfileInfo] Found string " + line);
                    int checkStringIndex = line.indexOf("|");

                    if(checkStringIndex > 0){
                        result = line.substring(checkStringIndex + 1 , line.length());
                    }

                    break;
                }
            }

            if(fileMediaInfoReader !=null){
                fileMediaInfoReader.close();
            }
            if(bufferMediaInfoReader !=null){
                bufferMediaInfoReader.close();
            }

            Log.d(TAG,"[getProfileInfo] Result " + result);

        }catch(Exception e){
            Log.e(TAG, "Exception: "+ Log.getStackTraceString(e));
        }
        Log.d(TAG,"[getMediaTitle] End.");
        return result;

    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static int getDownloadSize(String url) {

        int result=0;

        try {

            Log.d(TAG,"[getDownloadSize] Start.");

            Log.d(TAG,"[getDownloadSize] Input parameter URL - [" + url + "]");


            URL onlineURL = new URL(url);
            URLConnection conection = onlineURL.openConnection();
            conection.connect();
            Log.d(TAG,"[getDownloadSize] Open connection.");

            result = conection.getContentLength();



            Log.d(TAG,"[getDownloadSize] End.");

        } catch (Exception e) {
            Log.e(TAG, "Exception: "+ Log.getStackTraceString(e));
        }

        return result/1024/1024;
    }

    public static String getPDFTempFile() {

        String result="";
        result = getDocumentDirectory() + "/" + Constants.mainFolder + "/" +  "onlinepdffile.tmp";
        return result;

    }

    public static void share(MediaInfo mediaInfo,Context ctx){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, Constants.ShareTitle);

        //String url=Constants.mainURLProcol+"/MP3Library/"+ mediaInfo.getAuthorname()+"/"+ mediaInfo.getFilename()+"/.mp3";
        String physicalLocation=mediaInfo.getPhysicallocation();
        String localDirectory="";

        localDirectory=Utils.getDocumentDirectory(mediaInfo.getStorageLocation());
        physicalLocation=physicalLocation.replace(localDirectory+"/","");

        String url=Constants.mainURLProcol + "/www." +physicalLocation;
        mediaInfo.getPhysicallocation();
        i.putExtra(Intent.EXTRA_TEXT,url  );
        ctx.startActivity(Intent.createChooser(i, Constants.ShareTitle));
    }

    public static MediaInfo convertToMeidaInfoFromUrl(String sourceUrl){
        MediaInfo result=new MediaInfo();

        try {
            URL url = new URL(sourceUrl);

            //Get Author Name
            String path=url.getPath();
            String[] pathSplit=path.split("/");
            String authorName="";
            if(pathSplit.length>2){
                authorName=pathSplit[2];
            }
            result.setAuthorname(authorName);

            String localStoragePath="/"+Constants.mainFolder+path;
            result.setLocalStoragePath(localStoragePath);

            String internetRemoteUrl=Constants.mainURL+path;
            result.setInternetRemoteUrl(internetRemoteUrl);

            String fileName=url.getFile();
            result.setFilename(fileName);

            MediaTypeEnum mediaType=MediaTypeEnum.MP3;
            if (Utils.classifyRequestedFileType(Constants.supportedAudioFiles, fileName)) {
                mediaType=MediaTypeEnum.MP3;
            } else if (Utils.classifyRequestedFileType(Constants.supportedVideoFiles, fileName)) {
                mediaType=MediaTypeEnum.MP4;
            } else {
                mediaType=MediaTypeEnum.PDF;
            }
            result.setMediaType(mediaType);

        }catch (MalformedURLException e){
            e.printStackTrace();
        }
        return result;
    }

    public static void gotoDownloadPage(Context context, String downloadURL, WebView webView) {

        Log.d(TAG,"[gotoDownloadPage] Start.");

        downloadURL=downloadURL.replace("http://","https://");//To support Android SDK 28

        try {

            Intent intentDownload = new Intent(context, DownloadActivity.class);
            intentDownload.putExtra("downloadURL", downloadURL.toString()); //Optional parameters
            context.startActivity(intentDownload);

            if(webView!=null) {
                webView.stopLoading();
            }

        }catch(Exception e){

            Log.e(TAG, "Exception: "+ Log.getStackTraceString(e));
        }

        Log.d(TAG,"[gotoDownloadPage] End.");

    }

    public static void getListFromAppPrivateDownloads(Context ctx, String[] supportedExts, ArrayList<MediaInfo> out) {
        out.clear();
        File root = ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (root == null || !root.exists()) return;
        walkAndCollectMedia(root, supportedExts, out);
    }

    private static void walkAndCollectMedia(File dir, String[] supportedExts, ArrayList<MediaInfo> out) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.isDirectory()) {
                walkAndCollectMedia(f, supportedExts, out);
                continue;
            }
            if (hasSupportedExt(f, supportedExts)) {
                MediaInfo mi = buildMediaInfoFromFile(f);
                out.add(mi);
            }
        }
    }

    private static boolean hasSupportedExt(File f, String[] supportedExts) {
        String name = f.getName().toLowerCase(Locale.ROOT);
        for (String ext : supportedExts) {
            // ext values in your Constants.supportedAudioFiles are like ".MP3" or "MP3"?
            // Handle both:
            String e = ext.toLowerCase(Locale.ROOT);
            if (!e.startsWith(".")) e = "." + e;
            if (name.endsWith(e)) return true;
        }
        return false;
    }

    private static MediaInfo buildMediaInfoFromFile(File f) {
        MediaInfo mi = new MediaInfo();
        // These 4 fields are used in LocalMP3Activity
        mi.setPhysicallocation(f.getAbsolutePath());
        mi.setFilename(f.getName());
        // We may not have author/profile image in the app-private listing; set safe defaults.
        mi.setAuthorname("");            // or derive from parent folder if you want
        mi.setProfileimage("");          // optional: path to a default author image
        // If MediaInfo has more attributes (size, date, etc.), set them here:
        // mi.setFilesize(f.length());
        // mi.setCreateddate(new Date(f.lastModified()));
        return mi;
    }

}
