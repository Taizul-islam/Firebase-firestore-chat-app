package com.rakib.soberpoint.items;

import java.util.Date;

public class FriendRequest extends PostId {
    String current_id;
    String user_id;
    Date time;
    String type;
    String group_id;

    public FriendRequest(String current_id, String user_id, Date time, String type, String group_id) {
        this.current_id = current_id;
        this.user_id = user_id;
        this.time = time;
        this.type = type;
        this.group_id = group_id;
    }

    public FriendRequest() {
    }

    public String getCurrent_id() {
        return current_id;
    }

    public void setCurrent_id(String current_id) {
        this.current_id = current_id;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }
}
