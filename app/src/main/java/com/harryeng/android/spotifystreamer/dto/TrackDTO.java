package com.harryeng.android.spotifystreamer.dto;

import android.os.Parcel;
import android.os.Parcelable;

public class TrackDTO implements Parcelable {
    private String id;
    private String trackName;
    private String albumName;
    private String smallAlbumImageURL;
    private String largeAlbumImageURL;
    private String previewURL;
    private String extra;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(trackName);
        dest.writeString(albumName);
        dest.writeString(smallAlbumImageURL);
        dest.writeString(largeAlbumImageURL);
        dest.writeString(previewURL);
    }

    public TrackDTO() {

    }

    public TrackDTO(String extra) {
        this.extra = extra;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getSmallAlbumImageURL() {
        return smallAlbumImageURL;
    }

    public void setSmallAlbumImageURL(String smallAlbumImageURL) {
        this.smallAlbumImageURL = smallAlbumImageURL;
    }

    public String getLargeAlbumImageURL() {
        return largeAlbumImageURL;
    }

    public void setLargeAlbumImageURL(String largeAlbumImageURL) {
        this.largeAlbumImageURL = largeAlbumImageURL;
    }

    public String getPreviewURL() {
        return previewURL;
    }

    public void setPreviewURL(String previewURL) {
        this.previewURL = previewURL;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}
