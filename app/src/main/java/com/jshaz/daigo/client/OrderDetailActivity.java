package com.jshaz.daigo.client;

/**
 * 展示订单详细信息的活动
 */

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.jshaz.daigo.R;
import com.jshaz.daigo.interfaces.BaseClassImpl;
import com.jshaz.daigo.serverutil.ServerUtil;
import com.jshaz.daigo.ui.BaseActivity;
import com.jshaz.daigo.ui.ToolBarView;
import com.jshaz.daigo.util.Order;
import com.jshaz.daigo.util.User;
import com.jshaz.daigo.util.Utility;

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
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.GONE;


public class OrderDetailActivity extends BaseActivity implements View.OnClickListener {

    private CircleImageView senderHead;
    private CircleImageView receiverHead;

    private TextView title;
    private TextView senderNickName;
    private TextView receiverNickName;
    private TextView timeInfo;
    private TextView requestTime;
    private TextView price;
    private TextView contact;
    private TextView publicContent;
    private TextView privateContent;
    private TextView receiverPhone;
    private TextView releaseTime;
    private TextView orderID;
    private TextView orderState;
    private TextView reportReceiver;

    private LinearLayout stateBar;

    private CardView receiverCardView;

    private Button cancelOrder;
    private Button acceptOrder;
    private Button completeOrder;

    private Intent intent;

    private Order order;

    private ToolBarView toolBarView;

    private String orderId;

    private String userId;

    private ProgressDialog getDataDialog;
    private ProgressDialog progressDialog;

    private Thread getInfoThread;

    private ScrollView scrollView;

    private MyHandler handler = new MyHandler(this);

    /**
     * 打开方式
     * 0：从订单大厅打开
     * 1：从我的订单打开
     */
    private int openMethod;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        scrollView = (ScrollView) findViewById(R.id.order_detail_scroll_view);
        toolBarView = (ToolBarView) findViewById(R.id.order_detail_toolbar);
        senderHead = (CircleImageView) findViewById(R.id.order_detail_sender_head);
        receiverHead = (CircleImageView) findViewById(R.id.order_detail_receiver_head);
        title = (TextView) findViewById(R.id.order_detail_title);
        senderNickName = (TextView) findViewById(R.id.order_detail_sender_nickname);
        receiverNickName = (TextView) findViewById(R.id.order_detail_receiver_nickname);
        timeInfo = (TextView) findViewById(R.id.order_detail_time_info);
        requestTime = (TextView) findViewById(R.id.order_detail_request_time);
        price = (TextView) findViewById(R.id.order_detail_price);
        contact = (TextView) findViewById(R.id.order_detail_contact);
        publicContent = (TextView) findViewById(R.id.order_detail_public_content);
        privateContent = (TextView) findViewById(R.id.order_detail_private_content);
        receiverCardView = (CardView) findViewById(R.id.receiver_cardview);
        cancelOrder = (Button) findViewById(R.id.order_detail_cancel_order);
        acceptOrder = (Button) findViewById(R.id.order_detail_accept_order);
        completeOrder = (Button) findViewById(R.id.order_detail_complete_order);
        receiverPhone = (TextView) findViewById(R.id.order_detail_receiver_phonenum);
        releaseTime = (TextView) findViewById(R.id.order_detail_release_time);
        orderID = (TextView) findViewById(R.id.order_detail_orderid);
        orderState = (TextView) findViewById(R.id.order_detail_order_state);
        stateBar = (LinearLayout) findViewById(R.id.order_detail_state_bar);
        reportReceiver = (TextView) findViewById(R.id.order_detail_report_receiver);

        reportReceiver.setOnClickListener(this);
        receiverCardView.setOnClickListener(this);


        setSlideExit(false);


        cancelOrder.setOnClickListener(this);
        acceptOrder.setOnClickListener(this);
        completeOrder.setOnClickListener(this);

        /*设置标题栏相关信息*/
        toolBarView.setTitleCampusVisible(false);
        toolBarView.setTitleText("详细信息");

        /*设置标题栏返回按钮*/
        toolBarView.setBackButtonVisible(true);
        toolBarView.setBackButtonImage(R.mipmap.icon_back);
        toolBarView.setBackButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //获取OrderID和UserID
        intent = getIntent();
        orderId = intent.getStringExtra("order_id");
        userId = intent.getStringExtra("user_id");
        openMethod = intent.getIntExtra("openMethod", 0);
        order = new Order(orderId);

        orderID.setText("订单号：" + orderId);

        //同步订单信息
        getDataFromServer();
        //隐藏按钮
        cancelOrder.setVisibility(GONE);
        acceptOrder.setVisibility(GONE);
    }

    @Override
    public void onClick(View view) {
        AlertDialog.Builder builder;
        switch (view.getId()) {
            case R.id.order_detail_cancel_order:
                builder = new AlertDialog.Builder(this);
                builder.setMessage("确定要取消订单吗？");
                builder.setPositiveButton("确定取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        cancelOrder();
                    }
                });
                builder.setNegativeButton("不了", null);
                builder.show();
                break;
            case R.id.order_detail_accept_order:
                builder = new AlertDialog.Builder(this);
                builder.setMessage("确定要接单吗？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        checkIsVerified();
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.show();
                break;
            case R.id.order_detail_complete_order:
                builder = new AlertDialog.Builder(this);
                builder.setMessage("请确认配送完毕，并请发单人当面查验货物。");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    completeOrder();
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.show();
                break;
            case R.id.order_detail_report_receiver:
                Intent intent = new Intent(this, ComplainActivity.class);
                intent.putExtra("order_id", orderId);
                startActivity(intent);
                break;
            case R.id.receiver_cardview:
                if (order.getOrderState() != Order.RECEIVED) {
                    Toast.makeText(this, "订单已完成，无法查看", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent1 = new Intent(OrderDetailActivity.this, SeeLocationActivity.class);
//                    Intent intent1 = new Intent(OrderDetailActivity.this, AMapLocationActivity.class);
                    intent1.putExtra("userid", order.getOrderReceiver().getUserId());
                    intent1.putExtra("headicon", order.getOrderReceiver().getHeadIcon());
                    startActivity(intent1);
                }
                break;
        }
    }

    private void getDataFromServer() {
        startGetDataDialog();
        order.cloneData(handler);
    }

    private void startGetDataDialog() {
        getDataDialog = new ProgressDialog(this);
        getDataDialog.setTitle("");
        getDataDialog.setMessage("正在获取订单信息...");
        getDataDialog.setCancelable(false);
        getDataDialog.setCanceledOnTouchOutside(false);
        getDataDialog.show();
    }

    private void stopGetDataDialog() {
        if (getDataDialog != null) {
            getDataDialog.dismiss();
        }
    }

    /**
     * 设置订单状态
     */
    private void setOrderState() {
        switch (order.getOrderState()) {
            case Order.NORMAL:
                orderState.setText("未接单");
                stateBar.setBackgroundResource(R.color.colorPrimary);
                break;
            case Order.RECEIVED:
                if (order.getOrderSender().getUserId().equals(userId)) {
                    orderState.setText("正在配送");
                    stateBar.setBackgroundColor(Color.rgb(255,66,66));
                } else {
                    orderState.setText("已接单，请尽快送达");
                    stateBar.setBackgroundColor(Color.rgb(255,128,64));
                }
                break;
            case Order.COMPLETE:
                if (order.getOrderSender().getUserId().equals(userId)) {
                    orderState.setText("订单于" + Utility.convertMillToDate(
                            Long.parseLong(order.getCompleteTime())
                    ) + "完成");
                    stateBar.setBackgroundColor(Color.rgb(34,181,78));
                } else {
                    orderState.setText("配送完成");
                    stateBar.setBackgroundColor(Color.rgb(236,214,11));
                }

                break;
            case Order.INVALIDATE:
                orderState.setText("订单于" + Utility.convertMillToDate(
                        Long.parseLong(order.getCompleteTime())
                ) + "取消");
                stateBar.setBackgroundColor(Color.rgb(125,125,125));
                break;
        }
    }

    /**
     * 返回到Activity
     */
    private void returnResult() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
    }

    /**
     * 填充订单信息
     */
    private void fillOrderInfo() {

        setOrderState();

        receiverCardView.setVisibility(GONE);
        title.setText(order.getTitle());
        senderHead.setImageBitmap(Utility.convertStringToBitmap(order.getOrderSender().getHeadIcon()));
        senderNickName.setText(order.getOrderSender().getNickName());
        requestTime.setText("接单后 " + order.getRequestTime() / 60000 + " 分钟内送达");
        price.setText(new java.text.DecimalFormat("#.00").format(
                Double.parseDouble(order.getPrice())) + "元");
        contact.setText(order.getContact());
        publicContent.setText(order.getPublicDetails());
        releaseTime.setText(Utility.getOrderReleaseTime(orderId.substring(0, 14)));

        if (order.getOrderReceiver().getUserId() != null &&
                !order.getOrderReceiver().getUserId().equals("null") &&
                !order.getOrderReceiver().getUserId().equals("")) {
            receiverCardView.setVisibility(View.VISIBLE);
            receiverHead.setImageBitmap(Utility.convertStringToBitmap(
                    order.getOrderReceiver().getHeadIcon()));
            receiverNickName.setText(order.getOrderReceiver().getNickName());
            timeInfo.setText(Utility.convertMillToDate(order.getAcceptTime()) +
                    "接单，预计" + Utility.convertMillToDate(order.getAcceptTime() + order.getRequestTime())
                    + "送达");

            receiverPhone.setText("接单人电话： " + order.getOrderReceiver().getPhoneNum());

            cancelOrder.setText("已经被接单，无法取消");
            cancelOrder.setEnabled(false);
        }
        /*特殊视角的判断*/
        if (userId.equals(order.getOrderSender().getUserId())) {
            //发送者（自己）
            setSenderView();
        } else if (order.getOrderReceiver().getUserId() != null &&
                !order.getOrderReceiver().getUserId().equals("null") &&
                !order.getOrderReceiver().getUserId().equals("") &&
                order.getOrderReceiver().getUserId().equals(userId)){
            //接单人
            setReceiverView();
        } else {
            //第三方
            setOtherView();
        }

        if (order.getOrderState() == Order.INVALIDATE) {
            //如果取消了订单就隐藏所有按钮
            cancelOrder.setVisibility(GONE);
            completeOrder.setVisibility(GONE);
            acceptOrder.setVisibility(GONE);
        }
        if (order.getOrderState() == Order.COMPLETE) {
            //如果完成了订单就隐藏所有按钮
            cancelOrder.setVisibility(GONE);
            completeOrder.setVisibility(GONE);
            acceptOrder.setVisibility(GONE);
        }
    }

    /**
     * 设置为接单人视角
     */
    private void setReceiverView() {
        privateContent.setText(order.getPrivateDetails());
        cancelOrder.setVisibility(GONE);
        acceptOrder.setVisibility(GONE);
        completeOrder.setVisibility(View.VISIBLE);
//        acceptOrder.setText("您已接单。请尽快完成配送。");
//        acceptOrder.setEnabled(false);
        reportReceiver.setVisibility(View.INVISIBLE);
        contact.setText(order.getContact());
    }

    /**
     * 设置为发单人视角
     */
    private void setSenderView() {
        cancelOrder.setVisibility(View.VISIBLE);
        if (order.getOrderState() == Order.RECEIVED) {
            cancelOrder.setEnabled(false);
            cancelOrder.setText("正在配送，无法取消订单");
        }

        acceptOrder.setVisibility(GONE);
        completeOrder.setVisibility(GONE);
        privateContent.setText(order.getPrivateDetails());
        contact.setText(order.getContact());

    }

    /**
     * 设置为他人视角
     */
    private void setOtherView() {
        receiverCardView.setVisibility(View.GONE);
        privateContent.setText("只有接单后才能查看隐私内容");
        cancelOrder.setVisibility(GONE);
        acceptOrder.setVisibility(View.VISIBLE);
        completeOrder.setVisibility(GONE);
        reportReceiver.setVisibility(View.INVISIBLE);
        contact.setText("（接单后可查看）");
    }

    /**
     * 取消订单
     */
    private void cancelOrder() {
        startProgressDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String response = "";
                Looper.prepare();
                long date = 0;
                /*获取网络时间*/
                try {
                    URL url = new URL("https://www.baidu.com");
                    URLConnection uc = url.openConnection();
                    uc.connect();
                    date = uc.getDate();
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = handler.obtainMessage();
                    msg.what = BaseClassImpl.NET_ERROR;
                    handler.handleMessage(msg);
                }
                try {
                    BasicHttpParams httpParams = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
                    HttpConnectionParams.setSoTimeout(httpParams, 5000);

                    HttpClient httpclient = new DefaultHttpClient(httpParams);

                    //服务器地址，指向Servlet
                    HttpPost httpPost = new HttpPost(ServerUtil.SLOrderStateModify);

                    List<NameValuePair> params = new ArrayList<NameValuePair>();//将数据装入list
                    params.add(new BasicNameValuePair("type", "2"));
                    params.add(new BasicNameValuePair("orderid", orderId));
                    params.add(new BasicNameValuePair("completetime", "" + date));

                    final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "utf-8");//以UTF-8格式发送
                    httpPost.setEntity(entity);
                    //对提交数据进行编码
                    HttpResponse httpResponse = httpclient.execute(httpPost);
                    if (httpResponse.getStatusLine().getStatusCode() == 200)//在5000毫秒之内接收到返回值
                    {
                        Message message = handler.obtainMessage();
                        message.what = 0;
                        handler.handleMessage(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = handler.obtainMessage();
                    message.what = BaseClassImpl.NET_ERROR;
                    handler.handleMessage(message);
                }
                Looper.loop();
            }
        }).start();
    }

    /**
     * 检查是否进行了身份认证
     */
    private void checkIsVerified() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String response = "";
                Looper.prepare();
                try {
                    BasicHttpParams httpParams = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
                    HttpConnectionParams.setSoTimeout(httpParams, 5000);

                    HttpClient httpclient = new DefaultHttpClient(httpParams);

                    //服务器地址，指向Servlet
                    HttpPost httpPost = new HttpPost(ServerUtil.SLCheckVerification);

                    List<NameValuePair> params = new ArrayList<NameValuePair>();//将数据装入list
                    params.add(new BasicNameValuePair("userid", userId));


                    final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "utf-8");//以UTF-8格式发送
                    httpPost.setEntity(entity);
                    //对提交数据进行编码
                    HttpResponse httpResponse = httpclient.execute(httpPost);
                    if (httpResponse.getStatusLine().getStatusCode() == 200)//在5000毫秒之内接收到返回值
                    {
                        HttpEntity entity1 = httpResponse.getEntity();
                        response = EntityUtils.toString(entity1, "utf-8");//以UTF-8格式解析
                        Message message = handler.obtainMessage();
                        message.what = 2;
                        message.obj = response;
                        handler.handleMessage(message);
                    } else {
                        Message message = handler.obtainMessage();
                        message.what = BaseClassImpl.NET_ERROR;
                        handler.handleMessage(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = handler.obtainMessage();
                    message.what = BaseClassImpl.NET_ERROR;
                    handler.handleMessage(message);
                }
                Looper.loop();
            }
        }).start();
    }

    /**
     * 接单
     */
    private void acceptOrder() {
        startProgressDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String response = "";
                Looper.prepare();
                long date = 0;
                /*获取网络时间*/
                try {
                    URL url = new URL("https://www.baidu.com");
                    URLConnection uc = url.openConnection();
                    uc.connect();
                    date = uc.getDate();
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = handler.obtainMessage();
                    msg.what = BaseClassImpl.NET_ERROR;
                    handler.handleMessage(msg);
                }
                /*提交修改数据*/
                try {
                    BasicHttpParams httpParams = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
                    HttpConnectionParams.setSoTimeout(httpParams, 5000);

                    HttpClient httpclient = new DefaultHttpClient(httpParams);

                    //服务器地址，指向Servlet
                    HttpPost httpPost = new HttpPost(ServerUtil.SLOrderStateModify);

                    List<NameValuePair> params = new ArrayList<NameValuePair>();//将数据装入list
                    params.add(new BasicNameValuePair("type", "1"));
                    params.add(new BasicNameValuePair("orderid", orderId));
                    params.add(new BasicNameValuePair("receiverid", userId));
                    params.add(new BasicNameValuePair("accepttime", "" + date));


                    final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "utf-8");//以UTF-8格式发送
                    httpPost.setEntity(entity);
                    //对提交数据进行编码
                    HttpResponse httpResponse = httpclient.execute(httpPost);
                    if (httpResponse.getStatusLine().getStatusCode() == 200)//在5000毫秒之内接收到返回值
                    {
                        HttpEntity entity1 = httpResponse.getEntity();
                        response = EntityUtils.toString(entity1, "utf-8");//以UTF-8格式解析
                        Message message = handler.obtainMessage();
                        message.what = 1;
                        message.obj = response;
                        handler.handleMessage(message);
                    } else {
                        Message message = handler.obtainMessage();
                        message.what = BaseClassImpl.NET_ERROR;
                        handler.handleMessage(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = handler.obtainMessage();
                    message.what = BaseClassImpl.NET_ERROR;
                    handler.handleMessage(message);
                }
                Looper.loop();
            }
        }).start();
    }

    /**
     * 完成订单
     */
    private void completeOrder() {
        startProgressDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String response = "";
                Looper.prepare();
                long date = 0;
                /*获取网络时间*/
                try {
                    URL url = new URL("https://www.baidu.com");
                    URLConnection uc = url.openConnection();
                    uc.connect();
                    date = uc.getDate();
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = handler.obtainMessage();
                    msg.what = BaseClassImpl.NET_ERROR;
                    handler.handleMessage(msg);
                }
                /*提交修改数据*/
                try {
                    BasicHttpParams httpParams = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
                    HttpConnectionParams.setSoTimeout(httpParams, 5000);

                    HttpClient httpclient = new DefaultHttpClient(httpParams);

                    //服务器地址，指向Servlet
                    HttpPost httpPost = new HttpPost(ServerUtil.SLOrderStateModify);

                    List<NameValuePair> params = new ArrayList<NameValuePair>();//将数据装入list
                    params.add(new BasicNameValuePair("type", "3"));
                    params.add(new BasicNameValuePair("orderid", orderId));
                    params.add(new BasicNameValuePair("completetime", "" + date));


                    final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "utf-8");//以UTF-8格式发送
                    httpPost.setEntity(entity);
                    //对提交数据进行编码
                    HttpResponse httpResponse = httpclient.execute(httpPost);
                    if (httpResponse.getStatusLine().getStatusCode() == 200)//在5000毫秒之内接收到返回值
                    {
                        HttpEntity entity1 = httpResponse.getEntity();
                        response = EntityUtils.toString(entity1, "utf-8");//以UTF-8格式解析
                        Message message = handler.obtainMessage();
                        message.what = 3;
                        message.obj = response;
                        handler.handleMessage(message);
                    } else {
                        Message message = handler.obtainMessage();
                        message.what = BaseClassImpl.NET_ERROR;
                        handler.handleMessage(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = new Message();
                    message.what = BaseClassImpl.NET_ERROR;
                    handler.handleMessage(message);
                }
                Looper.loop();
            }
        }).start();
    }

    private void startProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setMessage("正在处理...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    private void stopProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private static class MyHandler extends Handler {
        WeakReference<OrderDetailActivity> activityWeakReference;
        public MyHandler(OrderDetailActivity activity) {
            this.activityWeakReference = new WeakReference<OrderDetailActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final OrderDetailActivity activity = activityWeakReference.get();
            String response = (String) msg.obj;
            activity.stopGetDataDialog();
            activity.stopProgressDialog();
            switch (msg.what) {
                case Order.ORDER_RESPONSE:

                    activity.order.convertJSON(response);
                    if (activity.order.getOrderState() == Order.NORMAL) {
                        //填充订单信息
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                activity.fillOrderInfo();
                            }
                        });
                    } else if (activity.order.getOrderState() == Order.INVALIDATE){
                        //从接单大厅打开
                        if (activity.openMethod == 0) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(activity, "此订单无法查看", Toast.LENGTH_SHORT).show();
                                    activity.finish();
                                }
                            });
                        } else {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    activity.fillOrderInfo();
                                }
                            });
                        }

                    } else if (activity.order.getOrderState() == Order.COMPLETE) {
                        //从接单大厅打开
                        if (activity.openMethod == 0) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(activity, "此订单无法查看", Toast.LENGTH_SHORT).show();
                                    activity.finish();
                                }
                            });
                        } else {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    activity.fillOrderInfo();
                                }
                            });
                        }
                    } else if (activity.order.getOrderState() == Order.RECEIVED) {
                        //从接单大厅打开
                        if (activity.openMethod == 0) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(activity, "此订单无法查看", Toast.LENGTH_SHORT).show();
                                    activity.finish();
                                }
                            });
                        } else {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    activity.fillOrderInfo();
                                }
                            });
                        }
                    }

                    break;
                case User.NET_ERROR:
                    Toast.makeText(activity, "网络错误", Toast.LENGTH_SHORT).show();
                    break;
                case 0:
                    Toast.makeText(activity, "取消成功", Toast.LENGTH_SHORT).show();
                    activity.returnResult();
                    activity.finish();
                    break;
                case 1:
                    response = (String) msg.obj;
                    if (response.equals("true")) {
                        AlertDialog.Builder info = new AlertDialog.Builder(activity);
                        info.setMessage("您已接单。请在规定时间内配送完毕。");
                        info.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                activity.returnResult();
                                activity.finish();
                            }
                        });
                        info.show();
                    } else {
                        Toast.makeText(activity, "接单错误", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 2:
                    response = (String) msg.obj;
                    if (response.equals("true")) {
                        activity.acceptOrder();
                    } else {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new
                                        AlertDialog.Builder(activity);
                                builder.setMessage("您需要先前往“我的-修改个人资料-身份认证”进行身份认证后才可以接单。");
                                builder.setPositiveButton("确定", null);
                                builder.show();
                            }
                        });
                    }
                    break;
                case 3:
                    response = (String) msg.obj;
                    if (response.equals("true")) {
                        Toast.makeText(activity, "订单已完成", Toast.LENGTH_SHORT).show();
                        activity.returnResult();
                        activity.finish();
                    } else {
                        Toast.makeText(activity, "未知错误。无法完成订单", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }

    /*
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String response = (String) msg.obj;
            stopGetDataDialog();
            stopProgressDialog();
            switch (msg.what) {
                case Order.ORDER_RESPONSE:

                    order.convertJSON(response);
                    if (order.getOrderState() == Order.NORMAL) {
                        //填充订单信息
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fillOrderInfo();
                            }
                        });
                    } else if (order.getOrderState() == Order.INVALIDATE){
                        //从接单大厅打开
                        if (openMethod == 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(OrderDetailActivity.this, "此订单无法查看", Toast.LENGTH_SHORT).show();
                                    OrderDetailActivity.this.finish();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    fillOrderInfo();
                                }
                            });
                        }

                    } else if (order.getOrderState() == Order.COMPLETE) {
                        //从接单大厅打开
                        if (openMethod == 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(OrderDetailActivity.this, "此订单无法查看", Toast.LENGTH_SHORT).show();
                                    OrderDetailActivity.this.finish();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    fillOrderInfo();
                                }
                            });
                        }
                    } else if (order.getOrderState() == Order.RECEIVED) {
                        //从接单大厅打开
                        if (openMethod == 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(OrderDetailActivity.this, "此订单无法查看", Toast.LENGTH_SHORT).show();
                                    OrderDetailActivity.this.finish();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    fillOrderInfo();
                                }
                            });
                        }
                    }

                    break;
                case User.NET_ERROR:
                    Toast.makeText(OrderDetailActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                    break;
                case 0:
                    Toast.makeText(OrderDetailActivity.this, "取消成功", Toast.LENGTH_SHORT).show();
                    returnResult();
                    finish();
                    break;
                case 1:
                    response = (String) msg.obj;
                    if (response.equals("true")) {
                        AlertDialog.Builder info = new AlertDialog.Builder(OrderDetailActivity.this);
                        info.setMessage("您已接单。请在规定时间内配送完毕。");
                        info.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                returnResult();
                                OrderDetailActivity.this.finish();
                            }
                        });
                        info.show();
                    } else {
                        Toast.makeText(OrderDetailActivity.this, "接单错误", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 2:
                    response = (String) msg.obj;
                    if (response.equals("true")) {
                        acceptOrder();
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new
                                        AlertDialog.Builder(OrderDetailActivity.this);
                                builder.setMessage("您需要先前往“我的-修改个人资料-身份认证”进行身份认证后才可以接单。");
                                builder.setPositiveButton("确定", null);
                                builder.show();
                            }
                        });
                    }
                    break;
                case 3:
                    response = (String) msg.obj;
                    if (response.equals("true")) {
                        Toast.makeText(OrderDetailActivity.this, "订单已完成", Toast.LENGTH_SHORT).show();
                        returnResult();
                        finish();
                    } else {
                        Toast.makeText(OrderDetailActivity.this, "未知错误。无法完成订单", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };
    */

}
