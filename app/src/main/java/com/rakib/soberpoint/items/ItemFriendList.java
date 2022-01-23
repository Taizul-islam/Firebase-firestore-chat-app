package com.rakib.soberpoint.items;

import java.util.Date;

public class ItemFriendList {
    String friend_id;
    Date time;

    public ItemFriendList(String friend_id, Date time) {
        this.friend_id = friend_id;
        this.time = time;
    }

    public ItemFriendList() {
    }

    public String getFriend_id() {
        return friend_id;
    }

    public void setFriend_id(String friend_id) {
        this.friend_id = friend_id;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
