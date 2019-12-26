package com.dragontelnet.helpzone.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.MutableLiveData;

import com.dragontelnet.helpzone.R;
import com.dragontelnet.helpzone.ui.activity.main.MainActivity;

public class MyBackgroundService extends Service {

    private static final int NOTIF_ID = 1;
    private static final String NOTIF_CHANNEL_ID = "Channel_Id";
    private static final String TAG = "MyBackgroundService";
    public static boolean mRUNNING;
    private String ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE";
    private static MutableLiveData<Boolean> isRunning = new MutableLiveData<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mRUNNING = false;
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: in");

        if (ACTION_STOP_SERVICE.equals(intent.getAction())) {
            Log.d(TAG, "called to cancel service");
            //stopSelf();
            //stopForeground(true);

            Intent intent1 = new Intent(getApplicationContext(), MyBackgroundService.class);
            stopService(intent1);
        }
        if (!mRUNNING) {
            isRunning.setValue(true);
            //running service only one time
            startForegroundSer();
            mRUNNING = true;
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startForegroundSer() {
        Log.d(TAG, "startForegroundSer: in");
        NotificationChannel channel;

        //change this intent
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(NOTIF_CHANNEL_ID, "MyNotification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        //PendingIntent pStopSelf = PendingIntent.getService(this, 0, stopSelf,PendingIntent.FLAG_CANCEL_CURRENT);

        Intent stopSelf = new Intent(this, MyBackgroundService.class);
        stopSelf.setAction(this.ACTION_STOP_SERVICE);
        PendingIntent pStopSelf = PendingIntent.getService(this, 0, stopSelf, PendingIntent.FLAG_CANCEL_CURRENT);

        startForeground(NOTIF_ID, new NotificationCompat.Builder(this,
                NOTIF_CHANNEL_ID) // don't forget create a notification channel first
                .setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Service is running background")
                .setContentIntent(pendingIntent)
                .addAction(0, "Stop Service", pStopSelf)
                .build());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: in");
        isRunning.setValue(false);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        //runs when user exit the app
        //start service again
        //check if user stop service then this is called or not
    }

    public static MutableLiveData<Boolean> getServiceStatus() {
        return isRunning;
    }
}