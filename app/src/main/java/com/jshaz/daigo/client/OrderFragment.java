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
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import com.jshaz.daigo.ui.BaseFragment;
import com.jshaz.daigo.ui.NavigationView;
import com.jshaz.daigo.ui.ScrollLinearLayoutManager;
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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by jshaz on 2017/11/21.
 */

public class OrderFragment extends BaseFragment {

    private static final String TAG = "OrderFragment";

    List<OrderDAO> orderList = new ArrayList<>();

    OrderAdapter adapter;

    RecyclerView orderRecyclerView;

    ScrollLinearLayoutManager linearLayoutManager;

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
    private boolean isLoading = false;

    private Thread refreshThread, loadThread;

    //记录待加载的订单ID
    private List<String> orderIdList = new ArrayList<>();

    private MyHandler handler = new MyHandler(this);

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
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
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
        linearLayoutManager = new ScrollLinearLayoutManager(getContext());
        orderRecyclerView.setLayoutManager(linearLayoutManager);
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
                    loadOrder();
                }
            });


    }

    public void removeFromOrderList(int position) {
        orderList.remove(position);
        adapter.notifyDataSetChanged();
    }


    public void refreshOrder() {
        orderRecyclerView.scrollToPosition(0);
//        orderRecyclerView.setNestedScrollingEnabled(false);

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
                    //HttpPost httpPost = new HttpPost(ServerUtil.SLOrderList);
                    HttpPost httpPost = new HttpPost(ServerUtil.SLOrderIDList);

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

                        orderIdList.clear();

                        if (!response.equals("null")) {
                            Scanner sc = new Scanner(response);
                            while (sc.hasNext()) {
                                Utility.addToFirst(orderIdList, sc.next());
                            }
                        }

                        Message message = handler.obtainMessage();
                        message.what = 0;
                        message.obj = orderIdList;
                        while (true) {
                            if (!isPaused()) {
                                handler.handleMessage(message);
                                break;
                            }
                        }
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

    private void loadOrder() {
        if (orderIdList.size() == 0) {
            refreshLayout.setLoading(false);
            return;
        }

        isLoading = true;
        if (orderIdList.size() > 5) {
            loadOrder(orderIdList.get(0), 0, 4);
        } else {
            loadOrder(orderIdList.get(0), 0, orderIdList.size() - 1);
        }

    }

    private void loadOrder(final String orderId, final int cur, final int des) {
        if (cur > des) {
            for (int i = 0; i <= des; i++) {
                orderIdList.remove(0);
            }
            Message message = new Message();
            message.what = 2;
            message.arg1 = cur;
            message.arg2 = des;
            handler.handleMessage(message);
            return;
        }
        loadThread = new Thread(new Runnable() {
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
//                    HttpPost httpPost = new HttpPost(ServerUtil.SLOrderIDList);

                    List<NameValuePair> params = new ArrayList<NameValuePair>();//将数据装入list
                    params.add(new BasicNameValuePair("orderid", orderId));

                    final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "utf-8");//以UTF-8格式发送
                    httpPost.setEntity(entity);
                    //对提交数据进行编码
                    HttpResponse httpResponse = httpclient.execute(httpPost);
                    if(httpResponse.getStatusLine().getStatusCode()==200)//在5000毫秒之内接收到返回值
                    {
                        HttpEntity entity1 = httpResponse.getEntity();
                        response = EntityUtils.toString(entity1, "utf-8");//以UTF-8格式解析
                        Message message = handler.obtainMessage();
                        message.what = 2;
                        message.obj = response;
                        message.arg1 = cur;
                        message.arg2 = des;

                        while (true) {
                            if (!isPaused()) {
                                handler.handleMessage(message);
                                break;
                            }
                        }
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
        loadThread.start();
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

    /**
     * 是否正在加载订单
     */
    public boolean isLoading() {
        return isLoading;
    }

    /**
     * 用于异步填充订单列表
     */
    private void fillRefreshOrderInfo(List<String> list) {

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    navigationView.setRedDot(false);
                    int preSize = orderList.size();
                    orderList.clear();
                    //此处被优化，修复了刷新时移动列表造成的崩溃
                    adapter.notifyItemRangeRemoved(0, preSize);
                }
            });

            orderIdList = removeSameByOrderId(list);

            /**
             * 更新最新订单信息
             */
            String latestOrderId = "";
            if (orderIdList.size() > 0) {
                latestOrderId = orderIdList.get(0);
                SharedPreferences.Editor editor = getContext().getSharedPreferences("order_cache",
                        Context.MODE_PRIVATE).edit();
                editor.putString("order_id", latestOrderId);
                editor.apply();

                loadOrder();
            } else {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isRefreshing) {
                            refreshLayout.setRefreshing(false);
                            isRefreshing = false;
                        }
                        adapter.notifyItemRangeRemoved(0, 0);
                    }
                });
            }

    }
    private void fillLoadOrderInfo(final Message msg) {
                if (msg.arg1 > msg.arg2) {
                    try {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isRefreshing) {
                                    refreshLayout.setRefreshing(false);
                                    isRefreshing = false;
                                }
                                adapter.setOrderList(orderList);
                                int startPos = adapter.getItemCount();
                                adapter.notifyItemRangeInserted(startPos, orderList.size());
                                navigationView.setRedDot(false);

                                //滑动到下一个
                                orderRecyclerView.scrollToPosition(orderList.size() - msg.arg2 - 1);

                                if (isLoading) {
                                    refreshLayout.setLoading(false);
                                }
                                isLoading = false;
                            }
                        });
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }


                } else {
                    String json = (String) msg.obj;

                    //解析JSON对象
                    if (json != null && !json.equals("null")) {
                        Gson gson = new Gson();
                        List<OrderDAO> orderDAOS = gson.fromJson(json, new TypeToken<List<OrderDAO>>()
                        {}.getType());

                        if (orderDAOS.size() > 0) {
                            for (OrderDAO dao : orderDAOS) {
                                orderList.add(dao);
                            }
                        } else {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    refreshLayout.setLoading(false);
                                    isLoading = false;
                                }
                            });
                        }



                    }

                    if (msg.arg1 + 1 >= orderIdList.size()) {
                        loadOrder(orderIdList.get(msg.arg1), msg.arg1 + 1, msg.arg2);
                    } else {
                        loadOrder(orderIdList.get(msg.arg1 + 1), msg.arg1 + 1, msg.arg2);
                    }

                }
    }

    private List<String> removeSameByOrderId(List<String> list) {
        List<String> stringList = list;
        int i = 0;
        while (i < stringList.size() - 1) {
            int j = i + 1;
            while (j < stringList.size()) {
                if (stringList.get(i).equals(stringList.get(j))) {
                    stringList.remove(j);
                } else {
                    j++;
                }
            }
            i++;
        }
        return stringList;
    }

    private static void forceStopRecyclerViewScroll(RecyclerView mRecyclerView) {
        mRecyclerView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, 0, 0, 0));
    }

    private static class MyHandler extends Handler {

        WeakReference<OrderFragment> fragmentWeakReference;

        public MyHandler(OrderFragment fragment) {
            this.fragmentWeakReference = new WeakReference<OrderFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    try {
                        fragmentWeakReference.get().fillRefreshOrderInfo((List<String>) msg.obj);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;
                case 1:
                    Toast.makeText(fragmentWeakReference.get().getContext(), "网络错误", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    fragmentWeakReference.get().fillLoadOrderInfo(msg);
                    break;
            }
        }
    }

    /*
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    try {
                        fillRefreshOrderInfo((List<String>) msg.obj);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;
                case 1:
                    Toast.makeText(getContext(), "网络错误", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                        fillLoadOrderInfo(msg);
                    break;
            }
        }
    };
*/

}
