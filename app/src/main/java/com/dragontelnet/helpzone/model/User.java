package com.dragontelnet.helpzone.model;

import java.util.HashMap;

public class User {
    private String userName, uid, phone, imageUrl;

    public User() {
    }

    public User(String userName, String uid, String phone, String imageUrl) {
        this.userName = userName;
        this.uid = uid;
        this.phone = phone;
        this.imageUrl = imageUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public HashMap<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userName", getUserName());
        result.put("phone", getPhone());
        result.put("uid", getUid());
        if (getImageUrl() != null) {
            result.put("imageUrl", getImageUrl());
        }
        return result;
    }
}
