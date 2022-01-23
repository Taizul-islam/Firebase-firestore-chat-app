package com.rakib.soberpoint.items;

import java.util.Date;

public class CommentItem {
    String comment;
    String user_id;
    Date time;

    public CommentItem() {
    }

    public CommentItem(String comment, String user_id, Date time) {
        this.comment = comment;
        this.user_id = user_id;
        this.time = time;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
