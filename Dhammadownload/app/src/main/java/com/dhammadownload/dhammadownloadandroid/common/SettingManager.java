package com.dhammadownload.dhammadownloadandroid.common;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Chaw on 11/5/2017.
 */

public class SettingManager {
    public static final String PREFS_NAME = "DhammaDownloadPrefsFile";
    private final String STORAGE_LOCATION = "StorageLocation";

    private final String HOMEPAGE = "HomePage";
    private final String REMOTE_ROOT_FOLDER = "RemoteRootFolder";//Eg: dhammadownload.com or 192.168.1.142

    private final String PDF_OPENTYPE="PdfOpenType";


    /*Temporary Testing*/
    public Boolean IS_PSP=false;

    SharedPreferences settings = Utils.getContext().getSharedPreferences(PREFS_NAME, 0);



    public StorageLocation getStorageLocation() {
        int intStorageLocation=settings.getInt(STORAGE_LOCATION,StorageLocation.DEVICE.ordinal());
        storageLocation=StorageLocation.values()[intStorageLocation];
        return storageLocation;
    }

    public void setStorageLocation(StorageLocation storageLocation) {
        this.storageLocation = storageLocation;

        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(STORAGE_LOCATION,storageLocation.ordinal());
        editor.commit();
    }

    private StorageLocation storageLocation;
    private PDFOpenType  pdfOpenType;
    private Context context;

    public String getHomePage() {
        String homePage=settings.getString(HOMEPAGE,defaultHomePage);
        return homePage;
    }

    public String getRemoteRootFolder() {
        String remoteRootFolder=settings.getString(REMOTE_ROOT_FOLDER,"");
        return remoteRootFolder;
    }

    public void setHomePage(String homePage) throws MalformedURLException {
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(HOMEPAGE,homePage);

            URL url = new URL(homePage);
            int port=url.getPort();
            String strPort=(url.getPort()<0) ? "":(":"+port);

            String remoteRootFolder = url.getHost()+ strPort;
            editor.putString(REMOTE_ROOT_FOLDER, remoteRootFolder);

            editor.commit();

    }

    private String defaultHomePage=Constants.mainURL;

    private static final SettingManager ourInstance = new SettingManager();

    public static SettingManager getInstance() {
        return ourInstance;
    }

    private SettingManager() {
    }


}
