package com.dragontelnet.helpzone;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class RadomKeyGenerator {

    public static String getRandomKey() {
        return getDate() + getTime();
    }

    public static String getDate() {
        Date calendar = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy ");
        return simpleDateFormat.format(calendar);
    }

    public static String getTime() {
        Date calendar = Calendar.getInstance().getTime();
        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("hh:mm:ss.SS a");
        return simpleTimeFormat.format(calendar);
    }

    public static Long getTimeStamp() {
        return System.currentTimeMillis() / 1000;
    }
}
