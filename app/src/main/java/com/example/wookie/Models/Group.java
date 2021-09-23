package com.example.wookie.Models;

public class Group {
    private String groupId;
    private String groupAdminId;
    private String groupName, groupPwd, groupImg, groupDate;
    private String inviteCode;

    public Group() { }

    public Group(String groupId, String groupAdminId, String groupName, String groupPwd, String groupImg, String groupDate, String inviteCode) {
        this.groupId = groupId;
        this.groupAdminId = groupAdminId;
        this.groupName = groupName;
        this.groupPwd = groupPwd;
        this.groupImg = groupImg;
        this.groupDate = groupDate;
        this.inviteCode = inviteCode;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupAdminId() {
        return groupAdminId;
    }

    public void setGroupAdminId(String groupAdminId) {
        this.groupAdminId = groupAdminId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupPwd() {
        return groupPwd;
    }

    public void setGroupPwd(String groupPwd) {
        this.groupPwd = groupPwd;
    }

    public String getGroupImg() {
        return groupImg;
    }

    public void setGroupImg(String groupImg) {
        this.groupImg = groupImg;
    }

    public String getGroupDate() {
        return groupDate;
    }

    public void setGroupDate(String groupDate) {
        this.groupDate = groupDate;
    }

    public String getInviteCode() { return inviteCode; }

    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
}
