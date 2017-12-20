package com.jshaz.daigo.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.jshaz.daigo.serverutil.ServerUtil;
import com.jshaz.daigo.util.User;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jshaz on 2017/12/19.
 */

public class LocationService extends Service {

    private User user;
    private String userId;

    public LocationClient mLocationClient;

    private Thread uploadThread;

    private double latitude, longitude;

    public LocationService() {
        super();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        user = new User(this);
        user.readFromLocalSharedPref();
        userId = user.getUserId();

        try {
            prepareLocationService();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//        int time = 10 * 1000; //每10秒自动更新一次
//        long triggerAtTime = SystemClock.elapsedRealtime() + time;
//        Intent sIntent = new Intent(this, LocationService.class);
//        PendingIntent pi = PendingIntent.getService(this, 0, sIntent, 0);
//        alarmManager.cancel(pi);
//        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 准备位置服务
     * @throws Exception 未取得权限抛出异常
     */
    public void prepareLocationService() throws Exception {
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        mLocationClient.start();
    }

    /**
     * 上传位置信息到服务器
     */
    private void updateLocationToServer(double latitude, double longitude) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("latitude", "" + latitude));
        params.add(new BasicNameValuePair("longitude", "" + longitude));
        params.add(new BasicNameValuePair("userid", userId));

        if (uploadThread == null) {
            uploadThread = ServerUtil.getThread(ServerUtil.SLLocation, params, null, 0, 1, false);
        }
        uploadThread.start();
    }

    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            latitude = bdLocation.getLatitude();
            longitude = bdLocation.getLongitude();

            updateLocationToServer(latitude, longitude);

        }
    }
}
