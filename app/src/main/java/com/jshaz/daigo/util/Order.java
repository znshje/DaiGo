package com.jshaz.daigo.util;

import android.content.Context;

import com.google.gson.Gson;
import com.jshaz.daigo.gson.OrderDAO;
import com.jshaz.daigo.gson.UserDAO;
import com.jshaz.daigo.interfaces.BaseClassImpl;
import com.jshaz.daigo.serverutil.ServerUtil;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

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
 * Created by jshaz on 2017/11/21.
 */

public class Order implements BaseClassImpl {
    //发送订单的User
    private UserDAO orderSender;
    //接单的User
    private UserDAO orderReceiver;
    //订单标题
    private String title;
    //校区代码
    private int campusCode;
    //订单公开详细信息
    private String publicDetails;
    //订单私密详细信息
    private String privateDetails;
    //订单发布时间
    private long releaseTime;
    //要求送达时间
    private long requestTime;
    //订单状态
    private int orderState = NORMAL;
    //订单唯一编号
    private String orderId;
    //订单接单时间
    private long acceptTime;
    //订单感谢金额
    private String price;
    //联系方式
    private String contact;
    //订单周期结束时间
    private String completeTime;

    private Context mContext;

    public String getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(String completeTime) {
        this.completeTime = completeTime;
    }

    public String getPrice() {
        return price;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setReleaseTime(long releaseTime) {
        this.releaseTime = releaseTime;
    }

    public void setRequestTime(long requestTime) {
        this.requestTime = requestTime;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    private boolean isContentSecret = false;

    public static final int NORMAL = 0;
    public static final int RECEIVED = 1;
    public static final int INVALIDATE = 2;
    public static final int COMPLETE = 3;

    public Order() {}

    public Order(Context mContext) {
        this.mContext = mContext;
    }

    public Order(String orderId) {
        this.orderId = orderId;
    }

    public Order(UserDAO orderSender, String title, String publicDetails, String privateDetails,
                 int campusCode) {
        this.orderSender = orderSender;
        this.title = title;
        this.publicDetails = publicDetails;
        this.privateDetails = privateDetails;
        this.campusCode = campusCode;
        this.orderReceiver = null;
    }

    public Order(UserDAO orderSender, String title, String publicDetails, String privateDetails,
                 int campusCode, long requestTime) {
        this.orderSender = orderSender;
        this.title = title;
        this.publicDetails = publicDetails;
        this.privateDetails = privateDetails;
        this.campusCode = campusCode;
        this.orderReceiver = null;
        this.requestTime = requestTime;
    }

    @Override
    public void writeToLocalDatabase() {

    }

    @Override
    public void writeToLocalSharedPref() {

    }

    @Override
    public void readFromLocalDatabase() {

    }

    @Override
    public void readFromLocalSharedPref() {

    }

    @Override
    public void updateData() {

    }

    /**
     * 从服务器端获取订单完整信息
     * @param handler
     */
    @Override
    public void cloneData(final Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                String response = "";
                try {
                    BasicHttpParams httpParams = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
                    HttpConnectionParams.setSoTimeout(httpParams, 5000);

                    HttpClient httpclient = new DefaultHttpClient(httpParams);

                    //服务器地址，指向Servlet
                    HttpPost httpPost = new HttpPost(ServerUtil.SLOrderDetail);

                    List<NameValuePair> params = new ArrayList<NameValuePair>();//将数据装入list
                    params.add(new BasicNameValuePair("orderid", orderId));
                    final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "utf-8");//以UTF-8格式发送
                    httpPost.setEntity(entity);
                    //对提交数据进行编码
                    HttpResponse httpResponse = httpclient.execute(httpPost);
                    if (httpResponse.getStatusLine().getStatusCode() == 200)//在5000毫秒之内接收到返回值
                    {
                        HttpEntity entity1 = httpResponse.getEntity();
                        response = EntityUtils.toString(entity1, "utf-8");//以UTF-8格式解析
                        Message message = handler.obtainMessage();
                        message.what = ORDER_RESPONSE;
                        message.obj = response;
                        handler.handleMessage(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message message = handler.obtainMessage();
                    message.what = NET_ERROR;
                    handler.handleMessage(message);
                }
                Looper.loop();
            }
        }).start();
    }

    @Override
    public void convertJSON(String json) {
        Gson gson = new Gson();
        OrderDAO order = gson.fromJson(json, OrderDAO.class);
        this.orderId = order.getOrderId();
        this.orderSender = order.getOrderSender();
        this.orderReceiver = order.getOrderReceiver();
        this.acceptTime = order.getAcceptTime();
        this.title = order.getTitle();
        this.publicDetails = order.getPublicDetails();
        this.privateDetails = order.getPrivateDetails();
        this.requestTime = order.getRequestTime();
        this.orderState = order.getOrderState();
        this.price = order.getPrice();
        this.contact = order.getContact();
        this.completeTime = order.getCompleteTime();
    }

    public UserDAO getOrderSender() {
        return orderSender;
    }

    public void setOrderSender(UserDAO orderSender) {
        this.orderSender = orderSender;
    }

    public UserDAO getOrderReceiver() {
        return orderReceiver;
    }

    public void setOrderReceiver(UserDAO orderReceiver) {
        this.orderReceiver = orderReceiver;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getCampusCode() {
        return campusCode;
    }

    public void setCampusCode(int campusCode) {
        this.campusCode = campusCode;
    }

    public String getPublicDetails() {
        return publicDetails;
    }

    public void setPublicDetails(String publicDetails) {
        this.publicDetails = publicDetails;
    }

    public String getPrivateDetails() {
        return privateDetails;
    }

    public void setPrivateDetails(String privateDetails) {
        this.privateDetails = privateDetails;
    }

    public long getRequestTime() {
        return requestTime;
    }

    public String getReleaseTime() {
        return Utility.convertMillToDate(releaseTime, 1);
    }


    public int getOrderState() {
        return orderState;
    }

    public void setOrderState(int orderState) {
        this.orderState = orderState;
    }

    public long getAcceptTime() {
        return acceptTime;
    }

    public void setAcceptTime(long acceptTime) {
        this.acceptTime = acceptTime;
    }
}
