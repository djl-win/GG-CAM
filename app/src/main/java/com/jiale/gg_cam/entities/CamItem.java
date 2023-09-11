package com.jiale.gg_cam.entities;

import android.net.Uri;

/**
 * Media Item entity
 * <p>
 * Media Item entity
 * </p>
 *
 * @author Jiale Dong
 * @version 1.0
 * @since 2023-09-11
 */
public class CamItem {
    private Uri uri;
    private MediaType type;
    private String city;
    private String location;
    private String time;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public CamItem(Uri uri, MediaType type, String city, String location, String time) {
        this.uri = uri;
        this.type = type;
        this.city = city;
        this.location = location;
        this.time = time;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public Uri getUri() {
        return uri;
    }

    public MediaType getType() {
        return type;
    }

    public void setType(MediaType type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }


    public enum MediaType {
        IMAGE, VIDEO
    }

}
