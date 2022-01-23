package com.rakib.soberpoint.Notification;

public class Data {

    String body, title, sent,type;

    public Data(String body, String title, String sent, String type) {
        this.body = body;
        this.title = title;
        this.sent = sent;
        this.type = type;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSent() {
        return sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
