package com.jshaz.daigo.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.jshaz.daigo.client.MyOrderActivity;
import com.jshaz.daigo.intents.UserIntent;
import com.jshaz.daigo.serverutil.ServerUtil;
import com.jshaz.daigo.util.NotificationUtil;
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
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class InstantOrderInfoService extends Service {

    List<NameValuePair> params;

    MyHandler handler = new MyHandler(this);

    public InstantOrderInfoService() {
        super();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        prepareThread();
        startThread(params);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void prepareThread() {
        User user = new User(this);
        user.readFromLocalSharedPref();
        params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("userid", "" + user.getUserId()));
    }

    private void startThread(final List<NameValuePair> params) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String response = "";
                while (true) {
                    try {
                        BasicHttpParams httpParams = new BasicHttpParams();
                        HttpConnectionParams.setConnectionTimeout(httpParams, 500);
                        HttpConnectionParams.setSoTimeout(httpParams, 500);

                        HttpClient httpclient = new DefaultHttpClient(httpParams);

                        //服务器地址，指向Servlet
                        HttpPost httpPost = new HttpPost(ServerUtil.SLGetInstantOrderstate);

                        final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "utf-8");//以UTF-8格式发送
                        httpPost.setEntity(entity);
                        //对提交数据进行编码
                        HttpResponse httpResponse = httpclient.execute(httpPost);
                        if (httpResponse.getStatusLine().getStatusCode() == 200)//在500毫秒之内接收到返回值
                        {
                            HttpEntity entity1 = httpResponse.getEntity();
                            response = EntityUtils.toString(entity1, "utf-8");//以UTF-8格式解析

                            if (!response.equals("false")) {
                                Message msg = handler.obtainMessage();
                                msg.what = 0;
                                msg.obj = response;
                                handler.handleMessage(msg);
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();

                    } finally {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }).start();
    }

    private static class MyNotification extends NotificationUtil {
        public MyNotification(Context base) {
            super(base);
        }

        @Override
        public NotificationCompat.Builder getNotificationBuilder(String title, String content) {
            NotificationCompat.Builder builder = super.getNotificationBuilder(title, content);

            User user = new User(this);
            user.readFromLocalSharedPref();

            UserIntent userIntent = new UserIntent();
            userIntent.setUserId(user.getUserId());

            Intent intent = new Intent(this, MyOrderActivity.class);
            intent.putExtra("user", userIntent);

            PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);

            builder.setContentIntent(pi);
            return builder;
        }

        @Override
        public void sendNotification(String title, String content) {
            if (Build.VERSION.SDK_INT >= 26) {
                super.createNotificationChannel();
                Notification notification = getChannelNotification(title, content)
                        .build();
                getManager().notify(super.NotificationID++, notification);
            } else {
                Notification notification = getNotificationBuilder(title, content).build();
                getManager().notify(super.NotificationID++, notification);
            }
        }
    }

    private static class MyHandler extends Handler {

        WeakReference<InstantOrderInfoService> serviceWeakReference;

        public MyHandler(InstantOrderInfoService service) {
            super();
            serviceWeakReference = new WeakReference<InstantOrderInfoService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            final InstantOrderInfoService service = serviceWeakReference.get();
            switch (msg.what) {
                case 0:
                    String response = (String) msg.obj;
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        List<String> nameList = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            nameList.add(object.getString("title"));
                        }
                        MyNotification util = new MyNotification(service);
                        for (int i = 0; i < nameList.size(); i++) {
                            util.sendNotification("您的订单有新动态", "您的订单“" + nameList.get(i) + "”已被接单，请耐心等待送达。");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }
}
