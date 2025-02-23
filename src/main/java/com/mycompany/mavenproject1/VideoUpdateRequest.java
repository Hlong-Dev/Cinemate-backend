package com.mycompany.mavenproject1;

public class VideoUpdateRequest {
    private String currentVideoUrl;
    private String currentVideoTitle;

    // Getters và setters
    public String getCurrentVideoUrl() {
        return currentVideoUrl;
    }

    public void setCurrentVideoUrl(String currentVideoUrl) {
        this.currentVideoUrl = currentVideoUrl;
    }

    public String getCurrentVideoTitle() {
        return currentVideoTitle;
    }

    public void setCurrentVideoTitle(String currentVideoTitle) {
        this.currentVideoTitle = currentVideoTitle;
    }
}
