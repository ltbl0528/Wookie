package com.example.wookie.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Pin {
    private String placeName, x, y, groupId, userId, pinId;

    public Pin() { }

    public Pin(String placeName, String x, String y, String groupId, String userId, String pinId){
        this.placeName = placeName;
        this.x = x;
        this.y = y;
        this.groupId = groupId;
        this.userId = userId;
        this.pinId = pinId;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
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

    public String getPinId() {
        return pinId;
    }

    public void setPinId(String pinId) {
        this.pinId = pinId;
    }
}
