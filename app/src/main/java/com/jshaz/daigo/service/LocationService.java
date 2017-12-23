package com.jshaz.daigo.service;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.jshaz.daigo.serverutil.ServerUtil;
import com.jshaz.daigo.util.User;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by jshaz on 2017/12/19.
 */

public class LocationService extends Service {

    private User user;
    private String userId;

    public LocationClient mLocationClient;
    public MyLocationListener listener;

    private Thread uploadThread;
    private ThreadPoolExecutor executor;

    private double latitude, longitude;

    public LocationService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        /*
        SDKInitializer.initialize(getApplicationContext());
        SDKInitializer.setCoordType(CoordType.GCJ02);
        */
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        user = new User(this);
        user.readFromLocalSharedPref();
        try {
            userId = user.getUserId();
        } catch (Exception e) {
            //未登录
            stopSelf();
        }


        try {
            prepareLocationService();
        } catch (Exception e) {
            e.printStackTrace();
        }

        prepareThreadPool();

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        executor.shutdownNow();
        stopSelf();
    }

    /**
     * 准备百度地图位置服务
     * @throws Exception 未取得权限抛出异常
     */
    public void prepareLocationService() throws Exception {
        mLocationClient = new LocationClient(getApplicationContext());
        listener = new MyLocationListener();
        mLocationClient.registerLocationListener(listener);
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(2000);
        option.setEnableSimulateGps(false);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setOpenGps(true);
        option.disableCache(true);
        mLocationClient.setLocOption(option);
        mLocationClient.start();
    }

    private void prepareThreadPool() {
        executor = new ThreadPoolExecutor(4, 8, 1000, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(4));
    }


    /**
     * 上传位置信息到服务器
     */
    private void updateLocationToServer(final double latitude, final double longitude) {
        UpdateRunnable runnable = new UpdateRunnable(latitude, longitude, userId);
        executor.execute(runnable);
    }


    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            latitude = bdLocation.getLatitude();
            longitude = bdLocation.getLongitude();
            updateLocationToServer(latitude, longitude);
        }
    }

    private class UpdateRunnable implements Runnable {
        private final double latitude, longitude;
        private final String userId;

        public UpdateRunnable(double latitude, double longitude, String userId) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.userId = userId;
        }

        @Override
        public void run() {
            try {
                BasicHttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, 500);
                HttpConnectionParams.setSoTimeout(httpParams, 500);

                HttpClient httpclient = new DefaultHttpClient(httpParams);

                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("latitude", "" + latitude));
                params.add(new BasicNameValuePair("longitude", "" + longitude));
                params.add(new BasicNameValuePair("userid", userId));

                //服务器地址，指向Servlet
                HttpPost httpPost = new HttpPost(ServerUtil.SLLocation);

                final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "utf-8");//以UTF-8格式发送
                httpPost.setEntity(entity);
                //对提交数据进行编码
                httpclient.execute(httpPost);
            } catch (Exception e) {

            }
        }
    }
}
