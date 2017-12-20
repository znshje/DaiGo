package com.jshaz.daigo.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;

import com.jshaz.daigo.ui.BaseActivity;
import com.jshaz.daigo.ui.BaseFragment;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;

import java.util.List;

/**
 * Created by jshaz on 2017/12/17.
 */

public class NetThread extends Thread {

    private String ipAddress;
    private List<NameValuePair> params;
    private Handler handler;
    private int successCode;
    private int failCode;
    private BaseFragment fragment;
    private BaseActivity activity;

    private boolean isLoop = true;

    public NetThread() {
        super();
    }

    public NetThread(String ipAddress, List<NameValuePair> params, @Nullable Handler handler,
                     int successCode, int failCode, boolean isLoop) {
        this.ipAddress = ipAddress;
        this.params = params;
        this.handler = handler;
        this.successCode = successCode;
        this.failCode = failCode;
        this.isLoop = isLoop;
    }

    public NetThread(String ipAddress, List<NameValuePair> params, @Nullable Handler handler,
                     int successCode, int failCode, BaseFragment fragment, boolean isLoop) {
        this.ipAddress = ipAddress;
        this.params = params;
        this.handler = handler;
        this.successCode = successCode;
        this.failCode = failCode;
        this.fragment = fragment;
        this.isLoop = isLoop;
    }

    public NetThread(String ipAddress, List<NameValuePair> params, @Nullable Handler handler,
                     int successCode, int failCode, BaseActivity activity, boolean isLoop) {
        this.ipAddress = ipAddress;
        this.params = params;
        this.handler = handler;
        this.successCode = successCode;
        this.failCode = failCode;
        this.activity = activity;
        this.isLoop = isLoop;
    }

    @Override
    public void run() {
        if (isLoop) {
            Looper.prepare();
        }
        try {
            BasicHttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
            HttpConnectionParams.setSoTimeout(httpParams, 5000);

            HttpClient httpclient = new DefaultHttpClient(httpParams);

            //服务器地址，指向Servlet
            HttpPost httpPost = new HttpPost(ipAddress);

            final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "utf-8");//以UTF-8格式发送
            httpPost.setEntity(entity);
            //对提交数据进行编码
            HttpResponse httpResponse = httpclient.execute(httpPost);

            if (handler == null) return;

            if(httpResponse.getStatusLine().getStatusCode()==200)//在5000毫秒之内接收到返回值
            {
                String response = "";
                HttpEntity entity1 = httpResponse.getEntity();
                response = EntityUtils.toString(entity1, "utf-8");//以UTF-8格式解析
                Message message = handler.obtainMessage();
                message.what = successCode;
                message.obj = response;
                if (fragment != null) {
                    while (true) {
                        if (!fragment.isPaused()) {
                            handler.handleMessage(message);
                            break;
                        }
                    }
                } else if (activity != null) {
                    while (true) {
                        if (!activity.isPaused()) {
                            handler.handleMessage(message);
                            break;
                        }
                    }
                } else {
                    handler.handleMessage(message);
                }
            } else {
                Message message = handler.obtainMessage();
                message.what = failCode;
                handler.handleMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (handler == null) return;
            Message message = handler.obtainMessage();
            message.what = failCode;
            handler.handleMessage(message);
        }
        if (isLoop) {
            Looper.loop();
        }
    }
}
