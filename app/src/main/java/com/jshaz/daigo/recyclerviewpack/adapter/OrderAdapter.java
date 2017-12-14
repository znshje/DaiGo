package com.jshaz.daigo.recyclerviewpack.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jshaz.daigo.R;
import com.jshaz.daigo.client.ClientMainActivity;
import com.jshaz.daigo.client.OrderDetailActivity;
import com.jshaz.daigo.client.OrderFragment;
import com.jshaz.daigo.gson.OrderDAO;
import com.jshaz.daigo.interfaces.BaseClassImpl;
import com.jshaz.daigo.serverutil.ServerUtil;
import com.jshaz.daigo.util.Order;
import com.jshaz.daigo.util.Setting;
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
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by jshaz on 2017/11/21.
 */

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private List<OrderDAO> orderList = new ArrayList<>();

    private Context mContext;

    private OrderDAO order;

    private String userId;

    private ClientMainActivity parentActivity;

    private OrderFragment parentFragment;

    public OrderFragment getParentFragment() {
        return parentFragment;
    }

    public void setParentFragment(OrderFragment parentFragment) {
        this.parentFragment = parentFragment;
    }

    public ClientMainActivity getParentActivity() {
        return parentActivity;
    }

    public void setParentActivity(ClientMainActivity parentActivity) {
        this.parentActivity = parentActivity;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public OrderAdapter(List<OrderDAO> orderList) {

        this.orderList = orderList;

    }

    public OrderAdapter(List<OrderDAO> orderList, Context mContext) {

        this.orderList = orderList;
        this.mContext = mContext;
    }

    public void setOrderList(List<OrderDAO> orderList) {
        this.orderList = orderList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.info_item,
                parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        order = orderList.get(position);

        holder.infoTitle.setText(order.getTitle());
        holder.infoContent.setText(order.getPublicDetails());
        holder.infoTime.setText(Utility.getOrderReleaseTime(order.getOrderId()));
        holder.btnDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //parentActivity.setDetailActivityReturned(true);
                order = orderList.get(position);
                Intent intent = new Intent(mContext, OrderDetailActivity.class);
                intent.putExtra("order_id", order.getOrderId());
                intent.putExtra("user_id", userId);
                getParentActivity().startActivityForResult(intent, 0);
            }
        });

        holder.btnDetail.setLongClickable(true);
        holder.btnDetail.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return true;
            }
        });

        holder.senderHead.setImageBitmap(
                Utility.convertStringToBitmap(order.getOrderSender().getHeadIcon()));
        holder.senderNickName.setText(order.getOrderSender().getNickName());
        holder.reqeustTime.setText(order.getRequestTime() / 60000 + " 分钟内送达");
        holder.campusName.setText(Utility.getCampusName(order.getOrderId()));
        holder.itemBg.setImageResource(
                Setting.CAMPUS_BANNER_RID[Utility.getCampusCode(order.getOrderId())]);

        if (order.getOrderSender().getUserId().equals(userId)) {
            //自己发的单怎么能自己接。。。
            holder.btnAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(mContext, "不能接自己发布的订单哦:)", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            holder.btnAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    order = orderList.get(position);
                    parentActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            builder.setMessage("确定要接单吗？");
                            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    checkIsVerified(position);
                                }
                            });
                            builder.setNegativeButton("取消", null);
                            builder.show();
                        }
                    });
                }
            });
        }

        holder.btnAccept.setLongClickable(true);
        holder.btnAccept.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return true;
            }
        });
        
    }


    /**
     * 检查是否进行了身份认证
     */
    private void checkIsVerified(final int position) {
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
                        Message message = new Message();
                        message.what = 2;
                        message.obj = response;
                        message.arg1 = position;
                        handler.handleMessage(message);
                    } else {
                        Message message = new Message();
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

    /**
     * 接单
     */
    private void acceptOrder(final String orderId, final int position) {
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
                    Message msg = new Message();
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
                    /**
                     * 需要注意与后台的对接
                     */
                    params.add(new BasicNameValuePair("type", Order.RECEIVED + ""));
                    params.add(new BasicNameValuePair("orderid", order.getOrderId()));
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
                        message.arg1 = position;
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

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            String response = "";
            switch (msg.what) {
                case BaseClassImpl.NET_ERROR:

                    Toast.makeText(mContext, "网络错误", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    response = (String) msg.obj;
                    if (response.equals("true")) {
                        parentActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder info = new AlertDialog.Builder(mContext);
                                info.setMessage("您已接单。请在规定时间内配送完毕。");
                                info.setPositiveButton("确定", null);
                                info.show();
                                parentFragment.removeFromOrderList(msg.arg1);
                            }
                        });
                    } else {
                        Toast.makeText(mContext, "接单错误", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 2:
                    response = (String) msg.obj;
                    if (response.equals("true")) {
                        acceptOrder(order.getOrderId(), msg.arg1);
                    } else {
                            parentActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder = new
                                            AlertDialog.Builder(mContext);
                                    builder.setMessage("您需要先前往“我的-修改个人资料-身份认证”进行身份认证后才可以接单。");
                                    builder.setPositiveButton("确定", null);
                                    builder.show();
                                }
                            });

                    }
                    break;
            }
        }
    };

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private Button btnDetail; //详细信息

        private Button btnAccept; //快速接单

        private CircleImageView senderHead; //发送者头像

        private TextView senderNickName; //发送者昵称

        private TextView infoTitle; //订单标题

        private TextView infoContent; //订单内容

        private TextView infoTime; //订单时间

        private TextView reqeustTime; //要求送达时间

        private TextView campusName;

        private ImageView itemBg;


        public ViewHolder(View view) {
            super(view);
            btnDetail = (Button) view.findViewById(R.id.info_btn_details);
            btnAccept = (Button) view.findViewById(R.id.info_btn_accpet);
            senderHead = (CircleImageView) view.findViewById(R.id.info_ognz_head);
            senderNickName = (TextView) view.findViewById(R.id.info_ognz_nickname);
            infoTitle = (TextView) view.findViewById(R.id.info_content_title);
            infoContent = (TextView) view.findViewById(R.id.info_details);
            infoTime = (TextView) view.findViewById(R.id.info_time);
            reqeustTime = (TextView) view.findViewById(R.id.info_request_time);
            campusName = (TextView) view.findViewById(R.id.info_campus_name);
            itemBg = (ImageView) view.findViewById(R.id.info_itembg);
        }
    }
}
