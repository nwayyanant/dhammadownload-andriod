package com.dhammadownload.dhammadownloadandroid.entity;

import com.dhammadownload.dhammadownloadandroid.common.MediaTypeEnum;
import com.dhammadownload.dhammadownloadandroid.common.StorageLocation;

/**
 * Created by zawlinaung on 9/18/16.
 */
public class MediaInfo {

    String authorname; //Author name
    String filename;  //file name alone
    String physicallocation; //absolute of local path of media e.g /Users/../../../dhammadownload.com/MP3../../....mp3
    String profileimage;//profile image
    String localStoragePath;
    private String internetRemoteUrl;

    private String title;

    private MediaTypeEnum mediaType;

    public StorageLocation getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(StorageLocation storageLocation) {
        this.storageLocation = storageLocation;
    }

    StorageLocation storageLocation;//StorageLocation


    public MediaInfo() {

    }

    public String getAuthorname() {
        return authorname;
    }

    public void setAuthorname(String authorname) {
        this.authorname = authorname;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getPhysicallocation() {
        return physicallocation;
    }

    public void setPhysicallocation(String physicallocation) {
        this.physicallocation = physicallocation;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getLocalStoragePath() {
        return localStoragePath;
    }

    public void setLocalStoragePath(String localStoragePath) {
        this.localStoragePath = localStoragePath;
    }

    public MediaTypeEnum getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaTypeEnum mediaType) {
        this.mediaType = mediaType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInternetRemoteUrl() {
        return internetRemoteUrl;
    }

    public void setInternetRemoteUrl(String internetRemoteUrl) {
        this.internetRemoteUrl = internetRemoteUrl;
    }
}
