package com.dragontelnet.helpzone.model;

import java.util.HashMap;

public class Trigger {
    String title, body, byUid, audioLink, date, time;
    long timeStamp;

    public Trigger() {
    }

    public Trigger(String title, String body, String byUid, String audioLink, String date, String time, long timeStamp) {
        this.title = title;
        this.body = body;
        this.byUid = byUid;
        this.audioLink = audioLink;
        this.date = date;
        this.time = time;
        this.timeStamp = timeStamp;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAudioLink() {
        return audioLink;
    }

    public void setAudioLink(String audioLink) {
        this.audioLink = audioLink;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getByUid() {
        return byUid;
    }

    public void setByUid(String byUid) {
        this.byUid = byUid;
    }

    public HashMap<String, Object> toMap() {
        HashMap<String, Object> triggerMap = new HashMap<>();
        triggerMap.put("title", getTitle());
        triggerMap.put("body", getBody());
        triggerMap.put("byUid", getByUid());
        triggerMap.put("audioLink", getAudioLink());
        triggerMap.put("date", getDate());
        triggerMap.put("time", getTime());
        triggerMap.put("timeStamp", getTimeStamp());
        return triggerMap;
    }
}
