package com.koushikdutta.clear;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import java.util.HashSet;

/**
 * Created by koush on 7/1/14.
 */
public class ClearService extends NotificationListenerService {
    public static final int CLEAR_NOTIFICATION_ID = 3421;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            active.clear();
            cancelAllNotifications();
            NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            nm.cancelAll();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter filter = new IntentFilter("com.koushikdutta.clear.CLEAR_NOTIFICATIONS");
        registerReceiver(receiver, filter, "com.koushikdutta.clear.CLEAR_NOTIFICATIONS", null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    void process() {
        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if (active.size() == 0) {
            nm.cancelAll();
            return;
        }

        PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent("com.koushikdutta.clear.CLEAR_NOTIFICATIONS"), 0);

        Notification notification = new Notification.Builder(this)
        .setOngoing(true)
        .setSmallIcon(R.drawable.ic_stat_clear)
        .setPriority(Notification.PRIORITY_HIGH)
        .setContentTitle(getString(R.string.clear_notifications))
        .setContentIntent(pi)
        .build();

        nm.notify(CLEAR_NOTIFICATION_ID, notification);
    }

    HashSet<String> active = new HashSet<String>();

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (shouldIgnoreNotification(sbn))
            return;
        active.add(sbn.getKey());
        process();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if (shouldIgnoreNotification(sbn))
            return;
        active.remove(sbn.getKey());
        process();
    }

    private boolean shouldIgnoreNotification(StatusBarNotification sbn) {
        return getPackageName().equals(sbn.getPackageName()) || !sbn.isClearable() || sbn.isOngoing();
    }
}
