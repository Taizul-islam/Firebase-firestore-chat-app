package com.rakib.soberpoint.items;

import java.text.DateFormat;
import java.util.Date;

public class ItemGroupMessage {
    String sender,message,type,last;
    Date time;

    public ItemGroupMessage(String sender, String message, String type, Date time) {
        this.sender = sender;
        this.message = message;
        this.type = type;
        this.time = time;
    }

    public ItemGroupMessage() {
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }
}
