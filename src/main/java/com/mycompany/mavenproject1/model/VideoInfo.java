package com.mycompany.mavenproject1.model;

public class VideoInfo {
    private String title;
    private String thumbnail;
    private String duration;
    private String url;

    // Constructor
    public VideoInfo(String title, String thumbnail, String duration, String url) {
        this.title = title;
        this.thumbnail = thumbnail;
        this.duration = duration;
        this.url = url;
    }

    // Getters và Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
