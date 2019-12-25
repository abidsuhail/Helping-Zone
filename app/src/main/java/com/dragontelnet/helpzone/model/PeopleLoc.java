package com.dragontelnet.helpzone.model;

import java.util.HashMap;

public class PeopleLoc {
    private String uid, userName;
    private String distance;
    private double lat, lng;

    public PeopleLoc() {
    }

    public PeopleLoc(String uid, String distance, String userName) {
        this.uid = uid;
        this.distance = distance;
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public HashMap<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", getUid());
        result.put("distance", getDistance());
        result.put("trigger_generator_user_name", getUserName());
        result.put("lat", getLat());
        result.put("lng", getLng());
        result.put("userName", getUserName());
        return result;
    }
}
