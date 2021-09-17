package com.example.wookie.Models;

public class Users {

    String userId, userName, userProfile;

    public Users(){

    }

    public Users(String uid, String name, String profile){
        this.userId = uid;
        this.userName = name;
        this.userProfile = profile;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(String userProfile) {
        this.userProfile = userProfile;
    }
}
