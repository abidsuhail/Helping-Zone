package com.dragontelnet.helpzone;

import android.app.Application;
import android.content.SharedPreferences;

public class MySharedPrefs extends Application {

    private static SharedPreferences startActivitysharedPreferences;
    private static SharedPreferences trustedNumbersSharedPrefs;

    public static SharedPreferences getStartActivitySharedPrefs() {
        return startActivitysharedPreferences;
    }

    public static SharedPreferences getTrustedNumbersSharedPrefs() {
        return trustedNumbersSharedPrefs;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startActivitysharedPreferences = getApplicationContext()
                .getSharedPreferences("MyPref", 0); // 0 - for private mode;

        trustedNumbersSharedPrefs = getApplicationContext()
                .getSharedPreferences("TrustedNoSharedRef", 0); // 0 - for private mode;
    }
}
