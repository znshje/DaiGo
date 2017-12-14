package com.jshaz.daigo.client;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jshaz.daigo.R;
import com.jshaz.daigo.gson.OrderDAO;
import com.jshaz.daigo.interfaces.BaseClassImpl;
import com.jshaz.daigo.recyclerviewpack.adapter.ShowOrderAdapter;
import com.jshaz.daigo.serverutil.ServerUtil;
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
import java.util.ArrayList;
import java.util.List;

/**
 * 用来显示“我的订单”页面的订单列表
 */
public class ShowMyOrdersFragment extends Fragment {

    private List<OrderDAO> orderDAOList = new ArrayList<>();

    private RecyclerView recyclerView;

    private ShowOrderAdapter adapter;

    private int type;
    private boolean isFirstLoad = false;
    private boolean isLoaded = false;

    private String userId;

    private RelativeLayout loadingLayout;

    private Thread thread;

    public ShowMyOrdersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_show_my_orders, container, false);

        initView(view);

        initRecyclerView();

        getOrderList();
        isFirstLoad = true;

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        thread.interrupt();
        setLoading(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isLoaded && !isFirstLoad) {
            callOnRefresh();
        }
    }

    public void callOnRefresh() {
        getOrderList();
    }

    private void initView(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.my_order_recycler_view);
        loadingLayout = (RelativeLayout) view.findViewById(R.id.my_order_loading_layout);

        adapter = new ShowOrderAdapter(orderDAOList);

        adapter.setParentActivity(getActivity());
        adapter.setUserId(userId);
        adapter.setmContext(getContext());
    }

    private void initRecyclerView() {
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
    }

    private void setOrderDAOList(List<OrderDAO> orderDAOList) {
        this.orderDAOList = orderDAOList;
    }

    public List<OrderDAO> getOrderDAOList() {
        return orderDAOList;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * 获取订单列表
     */
    private void getOrderList() {
        setLoading(true);
        isFirstLoad = true;
        Log.d("fragment", type + "getOrderList");
        thread = new Thread(new Runnable() {
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
                    HttpPost httpPost = new HttpPost(ServerUtil.SLMyOrder);
                    List<NameValuePair> params = new ArrayList<NameValuePair>();//将数据装入list
                    params.add(new BasicNameValuePair("type", "" + type));
                    params.add(new BasicNameValuePair("senderid", userId));
                    params.add(new BasicNameValuePair("receiverid", userId));

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
                        message.what = BaseClassImpl.NET_ERROR;
                        handler.handleMessage(message);
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = handler.obtainMessage();
                    message.what = BaseClassImpl.NET_ERROR;
                    handler.handleMessage(message);
                }
                Looper.loop();

            }

        });
        thread.start();
    }


    private void setLoading(boolean b) {
        if (b) {
            loadingLayout.setVisibility(View.VISIBLE);
        } else {
            loadingLayout.setVisibility(View.GONE);
        }
    }



    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setLoading(false);
                    }
                });
                switch (msg.what) {
                    case BaseClassImpl.NET_ERROR:
                        Toast.makeText(getContext(), "网络错误", Toast.LENGTH_SHORT).show();
                        break;
                    case 0:
                        isLoaded = true;
                        String json = (String) msg.obj;
                        orderDAOList.clear();
                        //解析JSON对象
                        if (json != null && !json.equals("null")) {
                            Gson gson = new Gson();
                            List<OrderDAO> orderDAOS = gson.fromJson(json, new TypeToken<List<OrderDAO>>()
                            {}.getType());
                            for (OrderDAO dao : orderDAOS) {
                                Utility.addToFirst(orderDAOList, dao);
                            }
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.setOrderDAOList(orderDAOList);
                                adapter.notifyDataSetChanged();
                            }
                        });
                        break;
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

        }
    };
}
