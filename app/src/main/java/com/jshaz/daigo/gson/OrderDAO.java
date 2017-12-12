package com.jshaz.daigo.gson;

import com.google.gson.annotations.SerializedName;
import com.jshaz.daigo.client.OrderDetailActivity;
import com.jshaz.daigo.util.User;

/**
 * Created by jshaz on 2017/12/7.
 */

public class OrderDAO {
    //发送订单的User
    @SerializedName("sender")
    private UserDAO orderSender;
    //接单的User
    @SerializedName("receiver")
    private UserDAO orderReceiver;
    //订单标题
    private String title;
    //订单公开详细信息
    @SerializedName("publicContent")
    private String publicDetails;
    //订单私密详细信息
    @SerializedName("privateContent")
    private String privateDetails;
    //要求送达时间
    private long requestTime;
    //订单状态
    @SerializedName("state")
    private int orderState = NORMAL;
    //接单时间
    @SerializedName("acceptTime")
    private long acceptTime;
    //订单唯一编号
    private String orderId;
    //订单感谢金额
    private String price;
    //联系方式
    private String contact;
    //订单周期结束时间
    private String completeTime;

    public static int NORMAL = 0;
    public static int RECEIVED = -1;
    public static int INVALIDATE = 1;

    public OrderDAO() {}

    public OrderDAO(OrderDAO orderDAO) {
        this.orderSender = orderDAO.getOrderSender();
        this.orderReceiver = orderDAO.getOrderReceiver();
        this.title = orderDAO.getTitle();
        this.publicDetails = orderDAO.getPublicDetails();
        this.privateDetails = orderDAO.getPrivateDetails();
        this.requestTime = orderDAO.getRequestTime();
        this.orderState = orderDAO.getOrderState();
        this.acceptTime = orderDAO.getAcceptTime();
        this.orderId = orderDAO.getOrderId();
        this.price = orderDAO.getPrice();
        this.contact = orderDAO.getContact();
    }

    public String getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(String completeTime) {
        this.completeTime = completeTime;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
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

    public void setRequestTime(long requestTime) {
        this.requestTime = requestTime;
    }

    public int getOrderState() {
        return orderState;
    }

    public void setOrderState(int orderState) {
        this.orderState = orderState;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public long getAcceptTime() {
        return acceptTime;
    }

    public void setAcceptTime(long acceptTime) {
        this.acceptTime = acceptTime;
    }
}
