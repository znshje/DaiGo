package com.jshaz.daigo.client;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jshaz.daigo.broadcasts.NetWorkStateReceiver;
import com.jshaz.daigo.broadcasts.OrderUpdateReceiver;
import com.jshaz.daigo.gson.OrderDAO;
import com.jshaz.daigo.recyclerviewpack.adapter.OrderAdapter;
import com.jshaz.daigo.serverutil.ServerUtil;
import com.jshaz.daigo.ui.NavigationView;
import com.jshaz.daigo.util.Order;
import com.jshaz.daigo.util.Setting;
import com.jshaz.daigo.util.Utility;
import com.jshaz.daigo.R;
import com.jshaz.daigo.recyclerviewpack.PullRefreshLayout;
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by jshaz on 2017/11/21.
 */

public class OrderFragment extends Fragment {

    List<OrderDAO> orderList = new ArrayList<>();

    OrderAdapter adapter;

    RecyclerView orderRecyclerView;

    public PullRefreshLayout refreshLayout;

    /*本地广播组件*/
    private OrderUpdateReceiver orderUpdateReceiver;

    private LocalBroadcastManager localBroadcastManager;

    private IntentFilter intentFilter;
    /*----------------------*/
    /*网络状态广播*/
    private NetWorkStateReceiver netWorkStateReceiver;
    /*----------------------*/

    private Setting setting;

    private User user;

    private NavigationView navigationView;

    private boolean isRefreshing = false;
    private boolean isLoadContent = false;
    private boolean isFirstStart = true;

    private Thread refreshThread;

    View view = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view != null) return view;
        view = inflater.inflate(R.layout.fragment_orders, container, false);

        //初始化设置对象
        setting = new Setting(getContext());
        user = new User(getContext());
        /*实现本地广播，接收刷新提示*/
        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());

        intentFilter = new IntentFilter();
        intentFilter.addAction("com.jshaz.daigo.UPDATE_ORDER");
        orderUpdateReceiver = new OrderUpdateReceiver();
        orderUpdateReceiver.setNavigationView(navigationView);
        localBroadcastManager.registerReceiver(orderUpdateReceiver, intentFilter);
        /*----------------------*/

        netWorkStateReceiver = new NetWorkStateReceiver(getContext());

        orderRecyclerView = (RecyclerView) view.findViewById(R.id.order_recycler_view);

        refreshLayout = (PullRefreshLayout) view.findViewById(R.id.swipe_order_list);

        initRecyclerView();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshOrder();
            }
        }, 200);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isRefreshing && !isFirstStart) {
            refreshThread.interrupt();
            refreshOrder();
        } else if (!isLoadContent && !isFirstStart) {
            refreshOrder();
        }
        isFirstStart = false;
    }

    /**
     * 初始化RecyclerView
     * 设置adapter和LayoutManager
     */
    private void initRecyclerView() {
        adapter = new OrderAdapter(orderList, getContext());
        adapter.setUserId(user.getUserId());
        adapter.setParentActivity((ClientMainActivity) getActivity());
        adapter.setParentFragment(this);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        orderRecyclerView.setLayoutManager(manager);
        orderRecyclerView.setAdapter(adapter);
        //orderRecyclerView.setItemAnimator(new DefaultItemAnimator());


            refreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshOrder();
                }
            });

            refreshLayout.setOnLoadListener(new PullRefreshLayout.OnLoadListener() {
                @Override
                public void onLoad() {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //initOrderList();
                            refreshLayout.setLoading(false);
                        }
                    }, 1000);
                }
            });


    }

    public void removeFromOrderList(int position) {
        orderList.remove(position);
        adapter.notifyDataSetChanged();
    }


    public void refreshOrder() {
        user.readFromLocalDatabase();
        final int campusCode = user.getCampusCode();
        refreshLayout.setRefreshing(true);
        isRefreshing = true;

        refreshThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String response;
                Looper.prepare();
                try {
                    BasicHttpParams httpParams = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
                    HttpConnectionParams.setSoTimeout(httpParams, 5000);

                    HttpClient httpclient = new DefaultHttpClient(httpParams);

                    //服务器地址，指向Servlet
                    HttpPost httpPost = new HttpPost(ServerUtil.SLOrderList);

                    List<NameValuePair> params = new ArrayList<NameValuePair>();//将数据装入list
                    params.add(new BasicNameValuePair("campusid", "" + campusCode));

                    final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "utf-8");//以UTF-8格式发送
                    httpPost.setEntity(entity);
                    //对提交数据进行编码
                    HttpResponse httpResponse = httpclient.execute(httpPost);
                    if(httpResponse.getStatusLine().getStatusCode()==200)//在5000毫秒之内接收到返回值
                    {
                        HttpEntity entity1 = httpResponse.getEntity();
                        response = EntityUtils.toString(entity1, "utf-8");//以UTF-8格式解析
                        Message message = handler.obtainMessage();
                        message.what = 0;
                        message.obj = response;
                        handler.handleMessage(message);
                    } else {
                        Message message = handler.obtainMessage();
                        message.what = 1;
                        handler.handleMessage(message);
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = handler.obtainMessage();
                    message.what = 1;
                    handler.handleMessage(message);
                }
                Looper.loop();

            }

        });
        refreshThread.start();
    }


    public void setNavigationView(NavigationView navigationView) {
        this.navigationView = navigationView;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * 是否正在刷新订单
     */
    public boolean isRefreshing() {
        return isRefreshing;
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            isRefreshing = false;
            switch (msg.what) {
                case 0:
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshLayout.setRefreshing(false);
                        }
                    });
                    String json = (String) msg.obj;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            navigationView.setRedDot(false);
                        }
                    });
                    orderList.clear();
                    isLoadContent = false;
                    String latestOrderId = "";
                    //解析JSON对象
                    if (json != null && !json.equals("null")) {
                        isLoadContent = true;
                        Gson gson = new Gson();
                        List<OrderDAO> orderDAOS = gson.fromJson(json, new TypeToken<List<OrderDAO>>()
                            {}.getType());
                        latestOrderId = orderDAOS.get(orderDAOS.size() - 1).getOrderId();
                        for (OrderDAO dao : orderDAOS) {
                            Utility.addToFirst(orderList, dao);
                        }
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.setOrderList(orderList);
                            adapter.notifyDataSetChanged();
                        }
                    });
                    SharedPreferences.Editor editor = getContext().getSharedPreferences("order_cache",
                            Context.MODE_PRIVATE).edit();
                    editor.putString("order_id", latestOrderId);
                    editor.apply();

                    break;
                case 1:
                    Toast.makeText(getContext(), "网络错误", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


}
