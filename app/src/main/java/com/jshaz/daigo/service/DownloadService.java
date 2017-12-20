package com.jshaz.daigo.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import com.jshaz.daigo.BuildConfig;
import com.jshaz.daigo.R;
import com.jshaz.daigo.interfaces.DownloadListener;
import com.jshaz.daigo.serverutil.DownloadTask;
import com.jshaz.daigo.util.NotificationUtil;

import java.io.File;

/**
 * Created by jshaz on 2017/12/17.
 * 下载更新文件服务
 */

public class DownloadService extends Service {

    public static final String nId = "download_channel";

    private DownloadTask downloadTask;

    private String downloadUrl;

    private NotificationUtil notificationUtil = new NotificationUtil(this);

    private DownloadListener listener = new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            notificationUtil.sendNotification("正在下载...", progress);
        }

        @Override
        public void onSuccess() {
            downloadTask = null;
            stopForeground(true);
            notificationUtil.sendNotification("下载成功", -1);
            openAPK();
        }

        @Override
        public void onFailed() {
            downloadTask = null;
            stopForeground(true);
            notificationUtil.sendNotification("下载失败", -1);
        }

        @Override
        public void onPaused() {
            downloadTask = null;
            Toast.makeText(DownloadService.this, "下载暂停", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCanceled() {
            downloadTask = null;
            stopForeground(true);
        }
    };

    private DownloadBinder mBinder = new DownloadBinder();

    public DownloadService() {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class DownloadBinder extends Binder {

        public void startDownload(String url) {
            if (downloadTask == null) {
                downloadUrl = url;
                downloadTask = new DownloadTask(listener);
                downloadTask.execute(downloadUrl);
                startForeground(1, notificationUtil.getNotificationBuilder("正在下载...", 0).build());
            }
        }

        public void pauseDownload() {
            if (downloadTask != null) {
                downloadTask.pauseDownload();
            }
        }

        public void cancelDownload() {
            if (downloadTask != null) {
                downloadTask.cancelDownload();
            } else {
                if (downloadUrl != null) {
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    String directory = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS
                    ).getPath();
                    File file = new File(directory + fileName);
                    if (file.exists()) {
                        file.delete();
                    }
                    new NotificationUtil(DownloadService.this).getManager().cancel(1);
//                    getNotificationManager().cancel(1);
                    notificationUtil.getManager().cancel(1);
                    stopForeground(true);
                }
            }
        }
    }

    /**
     * 打开下载好的APK文件
     */
    private void openAPK() {
        String fileName = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS).getPath() +
                downloadUrl.substring(downloadUrl.lastIndexOf("/"));
        Intent intent = new Intent();

        intent.setAction(Intent.ACTION_VIEW);

        Uri contentUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            contentUri = FileProvider.getUriForFile(this, "com.jshaz.daigo.fileprovider",
                    new File(fileName));
        } else {
            contentUri = Uri.fromFile(new File(fileName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        startActivity(intent);
    }
}
