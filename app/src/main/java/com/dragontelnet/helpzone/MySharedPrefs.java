package com.dragontelnet.helpzone;

import android.app.Application;
import android.content.SharedPreferences;

public class MySharedPrefs extends Application {

    private static SharedPreferences startActivitySharedPreferences;
    private static SharedPreferences trustedNumbersSharedPrefs;

    private static final String TAG = "MySharedPrefs";
    public static SharedPreferences getStartActivitySharedPrefs() {
        return startActivitySharedPreferences;
    }

    public static SharedPreferences getTrustedNumbersSharedPrefs() {
        return trustedNumbersSharedPrefs;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startActivitySharedPreferences = getApplicationContext()
                .getSharedPreferences("MyPref", 0); // 0 - for private mode;

        trustedNumbersSharedPrefs = getApplicationContext()
                .getSharedPreferences("TrustedNoSharedRef", 0); // 0 - for private mode;

    }
}
