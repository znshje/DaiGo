package com.jshaz.daigo.client;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.jshaz.daigo.R;
import com.jshaz.daigo.serverutil.ServerUtil;
import com.jshaz.daigo.service.LocationService;
import com.jshaz.daigo.ui.BaseActivity;
import com.jshaz.daigo.ui.ToolBarView;
import com.jshaz.daigo.util.BMapUtil;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SeeLocationActivity extends BaseActivity {

    private String userId;
    private String headIcon;

    private MapView mapView;
    private BaiduMap baiduMap;

    private Thread thread;

    private BitmapDescriptor bitmapDescriptor;

    private LatLng centP;

    private MyHandler handler;

    private ToolBarView toolBarView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SDKInitializer.initialize(getApplicationContext());
        SDKInitializer.setCoordType(CoordType.GCJ02); //默认为BD09LL坐标

        setContentView(R.layout.activity_see_location);

        userId = getIntent().getStringExtra("userid");
        headIcon = getIntent().getStringExtra("headicon");

        initView();

        initHeadBitmap();

        getLocationFromServer();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        bitmapDescriptor.recycle();
    }

    private void initView() {
        mapView = (MapView) findViewById(R.id.see_location_mapview);
        toolBarView = (ToolBarView) findViewById(R.id.see_location_toolbar);

        toolBarView.setBackButtonImage(R.mipmap.icon_back);
        toolBarView.setBackButtonVisible(true);
        toolBarView.setBackButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolBarView.setTitleText("查看接单人位置");

        baiduMap = mapView.getMap();

        MapStatusUpdate update = MapStatusUpdateFactory.zoomTo(19f);
        baiduMap.setMapStatus(update);

        handler = new MyHandler(this);


    }

    private void initHeadBitmap() {
        bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(
                BMapUtil.createCircleImage(Utility.convertStringToBitmap(headIcon), 60, 10)
        );
    }

    private void getLocationFromServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    String response = "";
                    try {
                        BasicHttpParams httpParams = new BasicHttpParams();
                        HttpConnectionParams.setConnectionTimeout(httpParams, 500);
                        HttpConnectionParams.setSoTimeout(httpParams, 500);

                        HttpClient httpclient = new DefaultHttpClient(httpParams);

                        //服务器地址，指向Servlet
                        HttpPost httpPost = new HttpPost(ServerUtil.SLGetLocation);

                        List<NameValuePair> params = new ArrayList<NameValuePair>();//将数据装入list
                        params.add(new BasicNameValuePair("userid", "" + userId));

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
                    } finally {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }


                }
            }
        }).start();

    }


    private static class MyHandler extends Handler {
        WeakReference<SeeLocationActivity> activityWeakReference;
        public MyHandler(SeeLocationActivity activity) {
            this.activityWeakReference = new WeakReference<SeeLocationActivity>(activity);
        }

        /**
         * 修正百度坐标偏移方法
         * @param latitude
         * @param longitude
         * @return
         */
        private LatLng set(double latitude, double longitude) {
            double x = longitude;
            double y = latitude;
            double PI = 3.14159265358979323846264338327950;
            double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * PI);
            double temp = Math.atan2(y, x) + 0.000003 * Math.cos(x * PI);

            double bdLon = z * Math.cos(temp) + 0.0065;
            double bdLat = z * Math.sin(temp) + 0.006;
            LatLng newCenpt = new LatLng(bdLat, bdLon);
            return newCenpt;
        }

        @Override
        public void handleMessage(Message msg) {
            final SeeLocationActivity activity = activityWeakReference.get();
            switch (msg.what) {
                case 0:
                    final String response = (String) msg.obj;

                    Scanner scanner = new Scanner(response);
                    double latitude = Double.parseDouble(scanner.next());
                    double longitude = Double.parseDouble(scanner.next());
                    LatLng point = new LatLng(latitude, longitude);
                    if (activity.centP == null) {
                        activity.centP = point;
                        activity.baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(activity.centP));
                    }
                    OverlayOptions options = new MarkerOptions().position(point).icon(activity.bitmapDescriptor);
                    activity.baiduMap.clear();
                    activity.baiduMap.addOverlay(options);
                    break;
            }
        }
    }
}
