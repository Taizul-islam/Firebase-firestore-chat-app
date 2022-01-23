package com.rakib.soberpoint.items;

import java.util.Date;

public class ItemPost extends PostId {
    String image,message,user_id,feeling;
    Date time;

    public ItemPost() {
    }

    public ItemPost(String image, String message, String user_id, String feeling, Date time) {
        this.image = image;
        this.message = message;
        this.user_id = user_id;
        this.feeling = feeling;
        this.time = time;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getFeeling() {
        return feeling;
    }

    public void setFeeling(String feeling) {
        this.feeling = feeling;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
