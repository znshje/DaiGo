package com.jshaz.daigo.util;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.jshaz.daigo.R;

/**
 * Created by jshaz on 2017/12/20.
 */

public class NotificationUtil extends ContextWrapper {

    private NotificationManager manager;
    public static final String nId = "download_channel";
    public static final String nName = "channel_name_1";

    public NotificationUtil(Context base) {
        super(base);
    }

    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(nId, nName,
                    NotificationManager.IMPORTANCE_MIN);
            getManager().createNotificationChannel(channel);
        }
    }

    public NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        return manager;
    }

    public Notification.Builder getChannelNotification(String title, int progress) {
        if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder builder = new Notification.Builder(getApplicationContext(), nId);
            builder.setContentTitle(title);
            builder.setSmallIcon(R.mipmap.icon_house);
            if (progress > 0) {
                builder.setContentText(progress + "%");
                builder.setProgress(100, progress, false);
            }
            return builder;
        }
        return null;
    }

    public NotificationCompat.Builder getNotificationBuilder(String title, int progress) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext());
        builder.setContentTitle(title);
        builder.setSmallIcon(R.mipmap.icon_house);
        if (progress > 0) {
            builder.setContentText(progress + "%");
            builder.setProgress(100, progress, false);
        }
        return builder;
    }

    public void sendNotification(String title, int progress) {
        if (Build.VERSION.SDK_INT >= 26) {
            createNotificationChannel();
            Notification notification = getChannelNotification(title, progress)
                    .build();
            getManager().notify(1, notification);
        } else {
            Notification notification = getNotificationBuilder(title, progress).build();
            getManager().notify(1, notification);
        }
    }

}
