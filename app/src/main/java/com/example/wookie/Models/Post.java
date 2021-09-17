package com.example.wookie.Models;

public class Post {
    private String postId;
    private String groupId, userId, placeId;
    private String context, postDate, postImg;
    private boolean isReview;
    private boolean isGallery;
    private int score;

    public Post(){ }

    public Post(String postId, String groupId, String userId, String placeId, String context, String postDate, String postImg, boolean isReview, boolean isGallery, int score) {
        this.postId = postId;
        this.groupId = groupId;
        this.userId = userId;
        this.placeId = placeId;
        this.context = context;
        this.postDate = postDate;
        this.postImg = postImg;
        this.isReview = isReview;
        this.isGallery = isGallery;
        this.score = score;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getPostDate() {
        return postDate;
    }

    public void setPostDate(String postDate) {
        this.postDate = postDate;
    }

    public String getPostImg() {
        return postImg;
    }

    public void setPostImg(String postImg) {
        this.postImg = postImg;
    }

    public boolean isReview() {
        return isReview;
    }

    public void setReview(boolean review) {
        isReview = review;
    }

    public boolean isGallery() {
        return isGallery;
    }

    public void setGallery(boolean gallery) {
        isGallery = gallery;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
