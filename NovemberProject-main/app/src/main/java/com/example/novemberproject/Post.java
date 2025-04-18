package com.example.novemberproject;

public class Post {
    private String summary;
    private String postUrl;
    private String imageUrl;
    private long timestamp;
    private boolean liked;

    public Post(String summary, String postUrl, String imageUrl, long timestamp) {
        this.summary = summary;
        this.postUrl = postUrl;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
        this.liked = false;
    }

    public String getSummary() {
        return summary;
    }

    public String getPostUrl() {
        return postUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }
}
