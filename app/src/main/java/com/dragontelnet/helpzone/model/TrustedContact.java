package com.dragontelnet.helpzone.model;

import java.util.HashMap;

public class TrustedContact {
    String phone1, phone2, phone3, phone4;

    public TrustedContact() {
    }

    public TrustedContact(String phone1, String phone2, String phone3, String phone4) {
        this.phone1 = phone1;
        this.phone2 = phone2;
        this.phone3 = phone3;
        this.phone4 = phone4;
    }

    public String getPhone1() {
        return phone1;
    }

    public void setPhone1(String phone1) {
        this.phone1 = phone1;
    }

    public String getPhone2() {
        return phone2;
    }

    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    public String getPhone3() {
        return phone3;
    }

    public void setPhone3(String phone3) {
        this.phone3 = phone3;
    }

    public String getPhone4() {
        return phone4;
    }

    public void setPhone4(String phone4) {
        this.phone4 = phone4;
    }

    public HashMap<String, Object> toMap() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("phone1", getPhone1());
        hashMap.put("phone2", getPhone2());
        hashMap.put("phone3", getPhone3());
        hashMap.put("phone4", getPhone4());
        return hashMap;

    }
}
