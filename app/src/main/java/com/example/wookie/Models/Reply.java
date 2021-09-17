package com.example.wookie.Models;

public class Reply {
    private String replyId;
    private String postId, groupId, userId;
    private String replyContext, replyDate;

    public Reply(){ }

    public Reply(String replyId, String postId, String groupId, String userId, String replyContext, String replyDate) {
        this.replyId = replyId;
        this.postId = postId;
        this.groupId = groupId;
        this.userId = userId;
        this.replyContext = replyContext;
        this.replyDate = replyDate;
    }

    public String getReplyId() {
        return replyId;
    }

    public void setReplyId(String replyId) {
        this.replyId = replyId;
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

    public String getReplyContext() {
        return replyContext;
    }

    public void setReplyContext(String replyContext) {
        this.replyContext = replyContext;
    }

    public String getReplyDate() {
        return replyDate;
    }

    public void setReplyDate(String replyDate) {
        this.replyDate = replyDate;
    }
}
