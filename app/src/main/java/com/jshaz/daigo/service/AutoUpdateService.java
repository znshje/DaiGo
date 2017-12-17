package com.jshaz.daigo.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.jshaz.daigo.serverutil.ServerUtil;
import com.jshaz.daigo.ui.MyApplication;
import com.jshaz.daigo.util.Setting;

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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jshaz on 2017/11/29.
 */

public class AutoUpdateService extends Service {

    private int campusCode = 0;

    private String latestOrderId;

    private LocalBroadcastManager localBroadcastManager;

    public AutoUpdateService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setting = new Setting(this);
        mContext = this;
    }

    private Setting setting;

    private Context mContext;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getCampusCode(this);
        /////////////////////////////////////////Debug
        try {
            detectOrderUpdate();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int time = 2000; //每2秒自动更新一次
        long triggerAtTime = SystemClock.elapsedRealtime() + time;
        Intent sIntent = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, sIntent, 0);
        alarmManager.cancel(pi);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void getLatestOrderId() {
        SharedPreferences preferences = this.getSharedPreferences("order_cache",
                MODE_PRIVATE);
        latestOrderId = preferences.getString("order_id", "");
    }

    /**
     * 获取校区代码
     * @param context
     */
    private void getCampusCode(Context context) {
        if (setting == null) {
            setting = new Setting(context);
        }
        setting.readFromLocalSharedPref();
        campusCode = setting.getCampusCode();
    }

    /**
     * 检测订单是否更新
     */
    private void detectOrderUpdate() {
        getLatestOrderId();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                String response = "";
                try {
                    BasicHttpParams httpParams = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, 500);
                    HttpConnectionParams.setSoTimeout(httpParams, 500);

                    HttpClient httpclient = new DefaultHttpClient(httpParams);

                    //服务器地址，指向Servlet
                    HttpPost httpPost = new HttpPost(ServerUtil.SLOrderIDQuery);

                    List<NameValuePair> params = new ArrayList<NameValuePair>();//将数据装入list
                    params.add(new BasicNameValuePair("campusid", "" + campusCode));

                    final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "utf-8");//以UTF-8格式发送
                    httpPost.setEntity(entity);
                    //对提交数据进行编码
                    HttpResponse httpResponse = httpclient.execute(httpPost);
                    if (httpResponse.getStatusLine().getStatusCode() == 200)//在500毫秒之内接收到返回值
                    {
                        HttpEntity entity1 = httpResponse.getEntity();
                        response = EntityUtils.toString(entity1, "utf-8");//以UTF-8格式解析
                        Message message = handler.obtainMessage();

                            message.what = 0;
                            message.obj = response;
                            handler.handleMessage(message);

                    }
                } catch (Exception e) {
                    e.printStackTrace();

                }
                Looper.loop();
            }
        }).start();
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    //返回最新订单号
                    String response = (String) msg.obj;
                    if (!response.equals(latestOrderId) && !response.equals("null") &&
                            !response.equals("")) {
//                        SharedPreferences.Editor editor = mContext.
//                                getSharedPreferences("order_cache", MODE_PRIVATE).edit();
//                        editor.putString("order_id", response);
//                        editor.apply();
                        latestOrderId = response;

                        if (localBroadcastManager == null) {
                            localBroadcastManager = LocalBroadcastManager.
                                    getInstance(mContext);
                        }
                        Intent intent = new Intent("com.jshaz.daigo.UPDATE_ORDER");
                        localBroadcastManager.sendBroadcast(intent);
                    }
                    break;
            }
        }
    };


}
