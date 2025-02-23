package com.mycompany.mavenproject1.model;

public class VideoMessage {
    private String videoUrl;
    private double currentTime;
    private String type;  // "VIDEO_UPDATE"

    // Getter và Setter cho các trường
    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public double getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(double currentTime) {
        this.currentTime = currentTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

