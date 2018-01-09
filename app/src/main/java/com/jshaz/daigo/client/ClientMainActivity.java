package com.jshaz.daigo.client;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jshaz.daigo.LoginActivity;
import com.jshaz.daigo.ModifyInfoActivity;
import com.jshaz.daigo.broadcasts.OrderUpdateReceiver;
import com.jshaz.daigo.intents.UserIntent;
import com.jshaz.daigo.serverutil.ServerUtil;
import com.jshaz.daigo.service.AutoUpdateService;
import com.jshaz.daigo.service.DownloadService;
import com.jshaz.daigo.service.InstantOrderInfoService;
import com.jshaz.daigo.service.LocationService;
import com.jshaz.daigo.ui.MyApplication;
import com.jshaz.daigo.ui.NavigationView;
import com.jshaz.daigo.R;
import com.jshaz.daigo.ui.BaseActivity;
import com.jshaz.daigo.ui.ToolBarView;
import com.jshaz.daigo.util.AppInfo;
import com.jshaz.daigo.util.NetThread;
import com.jshaz.daigo.util.Setting;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ClientMainActivity extends BaseActivity implements View.OnClickListener{
    private long mExitTime;

    private ToolBarView toolBarView;

    private NavigationView navigationView;

    private OrderFragment orderFragment;

    private MeFragment meFragment;

    private android.support.design.widget.NavigationView navigationMenuView;

    private DrawerLayout drawerLayout;

    private RelativeLayout expressLayout;

    private int curFragment = 0;

    private CircleImageView drawerHead;
    private ImageView verifyLogo;

    private TextView drawerPhone;

    private TextView drawerNickName;

    private final int SLIDE_FROM_LEFT_TO_RIGHT = 0;//从左向右滑动页面的动画代码
    private final int SLIDE_FROM_RIGHT_TO_LEFT = 1;//从右向左滑动页面的动画代码

    private User curUser;

    private Setting campusSetting;

    private boolean isLogin = false;
    private boolean isDrawerLoaded = false; //判断滑动抽屉的时候是否加载了头部信息
    private boolean isDetailActivityReturned = false;

    private OrderUpdateReceiver orderUpdateReceiver;
    private LocalBroadcastManager localBroadcastManager;


    private DownloadService.DownloadBinder downloadBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (DownloadService.DownloadBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private MyHandler handler = new MyHandler(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_main);

        requestPermissions();

        getUserInfo();

        initView();

        initNavigationOnSelectedListener();

        fillUserInfo();

        initReceiver();

        startAutoUpdateService();

        startLocationService();

        startInstantOrderService();

        /**
         * need Debug
         */
        startExpress(getIntent().getBooleanExtra("first_start", false));

        prepareDownloadService();

        checkUpdate();

    }

    @Override
    public void onClick(View view) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                //从OrderDetailActivity返回
                if (resultCode == RESULT_OK) {
                    orderFragment.refreshOrder();
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserInfo();
        if (isDetailActivityReturned) {
            orderFragment.refreshOrder();
        }
        isDetailActivityReturned = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(orderUpdateReceiver);
        getApplicationContext().unbindService(connection);
        //关闭所有服务
        stopService(new Intent(ClientMainActivity.this, LocationService.class));
        stopService(new Intent(ClientMainActivity.this, DownloadService.class));
        stopService(new Intent(ClientMainActivity.this, AutoUpdateService.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "关键权限被拒绝", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    /**
     * 打开初次启动时的提示页面
     */
    private void startExpress(boolean b) {
        if (b) {
            //initNavigationOnSelectedListener();
            expressLayout.setVisibility(View.VISIBLE);
            final ImageView[] imageViews = new ImageView[2];
            imageViews[0] = (ImageView) findViewById(R.id.express_0);
            imageViews[1] = (ImageView) findViewById(R.id.express_1);

            for (int i = 0; i < 2; i++) {
                imageViews[i].setVisibility(View.GONE);
            }
            ////////////////////////////////////////
            imageViews[0].setVisibility(View.VISIBLE);

            expressLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (imageViews[0].getVisibility() == View.VISIBLE) {
                        imageViews[0].setVisibility(View.GONE);
                        imageViews[1].setVisibility(View.VISIBLE);
                    } else if (imageViews[1].getVisibility() == View.VISIBLE) {
                        expressLayout.setVisibility(View.GONE);
                        startActivity(new Intent(ClientMainActivity.this, ClientMainActivity.class));
                        finish();

                    }

                }
            });

        } else {
            expressLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 初始化View
     */
    private void initView() {

        toolBarView = (ToolBarView) findViewById(R.id.client_main_titlebar);
        navigationView = (NavigationView) findViewById(R.id.client_main_navbar);
        navigationMenuView = (android.support.design.widget.NavigationView)
                findViewById(R.id.nav_menu);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        expressLayout = (RelativeLayout) findViewById(R.id.express_layout);


        campusSetting = new Setting(this);

        //设置不可滑动退出
        setSlideExit(false);

        //设置双击返回键退出
        setDoubleBackExit(false);

        /*准备碎片*/
        orderFragment = new OrderFragment();
        orderFragment.setNavigationView(navigationView);
        orderFragment.setUser(curUser);
        meFragment = new MeFragment();
        meFragment.setActivity(this);
        meFragment.setToolBarView(toolBarView);

        /*设置标题栏左侧按钮功能*/
        toolBarView.setBackButtonVisible(true);
        toolBarView.setBackButtonImage(R.mipmap.icon_menu);
        toolBarView.setRightButtonImage(R.mipmap.icon_refresh);
        toolBarView.setBackButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        /*设置标题栏右侧按钮功能*/
        toolBarView.setRightButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                orderFragment.refreshOrder();
            }
        });

        /*开启中间按钮的可用性*/
        navigationView.setButtonMiddleEnabled(true);

        /*导航栏左按钮的监听事件*/
        navigationView.buttonLeftSetListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toolBarView.setTitleText("订单");
                navigationView.setBtnLeftDown();
                if (curFragment != 0) {
                    curFragment = 0;
                    replaceFragment(orderFragment, SLIDE_FROM_LEFT_TO_RIGHT);
                } else {
                    if (!orderFragment.isLoading() && !orderFragment.isRefreshing()) {
                        orderFragment.refreshOrder();
                    }
                }
            }
        });

        
        /*导航栏中按钮的监听事件*/
        navigationView.buttonMiddleSetListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (curUser.getUserId() == null || curUser.getUserId().equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ClientMainActivity.this);
                    builder.setNegativeButton("取消", null);
                    builder.setPositiveButton("登录", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivity(new Intent(ClientMainActivity.this, LoginActivity.class));
                            finish();
                        }
                    });
                    builder.setMessage("您还未登录");
                    builder.show();
                } else {
                    Intent intent = new Intent(ClientMainActivity.this, AddOrderActivity.class);
                    UserIntent userIntent = new UserIntent();
                    userIntent.setUserId(curUser.getUserId());
                    userIntent.setPhoneNum(curUser.getPhoneNum());
                    userIntent.setDefaultAddress(curUser.getDefaultAddress());
                    intent.putExtra("user",userIntent);
                    startActivity(intent);
                }
            }
        });

        /*导航栏右按钮的监听事件*/
        navigationView.buttonRightSetListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toolBarView.setTitleText("我的");
                navigationView.setBtnRightDown();
                if (curFragment != 1) {
                    curFragment = 1;
                    replaceFragment(meFragment, SLIDE_FROM_RIGHT_TO_LEFT);
                }

            }
        });


        /**
         * 初次运行时会出错
         */


        /**
         * 初始化抽屉内容
         * 防止空指针出现
         */
        drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if (!isDrawerLoaded) {
                    fillDrawerLayoutInfo();
                    isDrawerLoaded = true;
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                isDrawerLoaded = false;
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        /*设置标题*/
        toolBarView.setTitleText("订单");
        /*设置校区可见*/
        toolBarView.setTitleCampusVisible(true);
        /*切换到订单碎片*/
        replaceFragmentWithoutAnimation(orderFragment);
    }

    /**
     * 初始化广播监听器
     */
    private void initReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.jshaz.daigo.UPDATE_ORDER");
        orderUpdateReceiver = new OrderUpdateReceiver();
        orderUpdateReceiver.setNavigationView(navigationView);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(orderUpdateReceiver, intentFilter);
    }

    private void initNavigationOnSelectedListener() {
        /*侧滑菜单的监听事件*/
        navigationMenuView.setNavigationItemSelectedListener(new android.support.design.widget.
                NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getGroupId()) {
                    case R.id.nav_group_1:
                        try{
                            drawerLayout.closeDrawers();
                            UserIntent userIntent = new UserIntent();
                            userIntent.setUserId(curUser.getUserId());
                            startActivity(new Intent(ClientMainActivity.this, MyOrderActivity.class)
                                    .putExtra("user", userIntent));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        break;
                    case R.id.nav_group_2:
                        if (!orderFragment.isRefreshing() && !orderFragment.isLoading()) {
                            drawerLayout.closeDrawers();
                            curUser.setCampusCode(Setting.getNavMenuCampusCode(item.getItemId()));
                            campusSetting.setCampusCode(curUser.getCampusCode());
                            campusSetting.writeToLocalSharedPref();
                            curUser.writeToLocalDatabase();

                            /**
                             * 将更改的数据上传到服务器
                             */
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Looper.prepare();
                                    try{
                                        BasicHttpParams httpParams = new BasicHttpParams();
                                        HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
                                        HttpConnectionParams.setSoTimeout(httpParams, 5000);

                                        HttpClient httpclient = new DefaultHttpClient(httpParams);

                                        //服务器地址，指向Servlet
                                        HttpPost httpPost = new HttpPost(ServerUtil.SLUpdateCampusCode);

                                        List<NameValuePair> params = new ArrayList<NameValuePair>();//将数据装入list
                                        params.add(new BasicNameValuePair("userid", curUser.getUserId()));
                                        params.add(new BasicNameValuePair("campuscode",
                                                "" + campusSetting.getCampusCode()));

                                        final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "utf-8");//以UTF-8格式发送
                                        httpPost.setEntity(entity);
                                        //对提交数据进行编码
                                        httpclient.execute(httpPost);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    Looper.loop();
                                }
                            }).start();
                            if (curFragment != 0) {
                                navigationView.getBtnLeft().callOnClick();
                            }
                            orderFragment.refreshOrder();
                            fillUserInfo();
                            return true;
                        }
                        break;
                }


                return false;
            }
        });
    }

    /**
     * 开启自动更新服务
     */
    private void startAutoUpdateService() {
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    /**
     * 开启位置传输服务
     */
    private void startLocationService() {
        Intent intent = new Intent(this, LocationService.class);
        startService(intent);
    }

    private void startInstantOrderService() {
        Intent intent = new Intent(ClientMainActivity.this, InstantOrderInfoService.class);
        startService(intent);
    }


    /**
     * 从服务器端获取用户数据
     */
    private void getUserInfo() {
        //检查用户是否登录
        if (curUser == null) {
            curUser = new User(this);
        }
        //从本地读取登录的用户ID
        curUser.readFromLocalSharedPref();
        //从本地读取用户缓存数据（密码！密码！密码！）
        curUser.readFromLocalDatabase();
        if (curUser.getUserId().equals("")) {
            isLogin = false;
            //setUnLogin();
        } else {

                isLogin = true;

                curUser.cloneData(handler); //从服务器端获取用户最新数据

        }
    }

    public void setDetailActivityReturned(boolean b) {
        this.isDetailActivityReturned = b;
    }

    private void prepareDownloadService() {
        Intent intent = new Intent(this, DownloadService.class);
        startService(intent);
        getApplicationContext().bindService(intent, connection, BIND_AUTO_CREATE);
    }

    /**
     * 开启下载服务
     */
    public void startDownload(String url) {
        downloadBinder.startDownload(url);
    }


    /**
     * 填充用户信息
     */
    private void fillUserInfo() {
        //将最新数据写入本地缓存
        curUser.writeToLocalDatabase();
        campusSetting.setCampusCode(curUser.getCampusCode());
        campusSetting.writeToLocalSharedPref();
        toolBarView.setTitleCampus(curUser.getCampusCode());
        navigationMenuView.setCheckedItem(Setting.getNavMenuCampusIcon(curUser.getCampusCode()));
    }
    private void fillDrawerLayoutInfo() {
        //空指针异常
        drawerHead = (CircleImageView) findViewById(R.id.nav_head_image);
        drawerNickName = (TextView) findViewById(R.id.nav_head_nickname);
        drawerPhone = (TextView) findViewById(R.id.nav_head_phonenum);
        verifyLogo = (ImageView) findViewById(R.id.nav_head_verify_logo);

        if (isLogin) {
            curUser.readFromLocalDatabase();
            drawerHead.setImageBitmap(Utility.convertStringToBitmap(curUser.getHeadIcon()));
            drawerNickName.setText(curUser.getNickName());
            //////
            String phoneT = curUser.getPhoneNum();
            drawerPhone.setText(phoneT.substring(0, 3) + "****" + phoneT.substring(7));
            drawerHead.setOnClickListener(null);
            if (curUser.isVerified()) {
                verifyLogo.setImageResource(R.mipmap.icon_yes_verify);
            } else {
                verifyLogo.setImageResource(R.mipmap.icon_no_verify);
            }
        } else {
            curUser.setNullValue();
            drawerHead.setImageResource(R.drawable.login);
            drawerNickName.setText(curUser.getNickName());
            drawerPhone.setText(curUser.getPhoneNum());
            drawerHead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(ClientMainActivity.this, LoginActivity.class));
                    //finish();
                }
            });
            verifyLogo.setImageBitmap(null);
        }


    }

    /**
     * 按两次返回键退出
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        /*按下了返回键*/
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawers();
                } else {
                    if ((System.currentTimeMillis() - mExitTime) > 3000) {
                        Toast.makeText(this, "再按一次返回键退出程序", Toast.LENGTH_SHORT).show();
                        mExitTime = System.currentTimeMillis();
                    } else {
                        this.finish();
                        System.exit(0);
                    }
                }

                return true;
            }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * 替换碎片
     * 为了保证不出现碎片重叠现象，手动禁用控件并延迟启用
     * @param fragment
     * @param slideType
     */
    private void replaceFragment(Fragment fragment, int slideType) {

        navigationView.setAllButtonEnabled(false);

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        switch (slideType){
            case SLIDE_FROM_LEFT_TO_RIGHT:
                transaction.setCustomAnimations(R.anim.slide_left_in,R.anim.slide_right_out);
                break;
            case SLIDE_FROM_RIGHT_TO_LEFT:
                transaction.setCustomAnimations(R.anim.slide_right_in,R.anim.slide_left_out);
                break;
        }

        transaction.replace(R.id.main_frame, fragment);
        transaction.commit();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                navigationView.setAllButtonEnabled(true);
            }
        }, 200);
    }

    private void replaceFragmentWithoutAnimation(Fragment fragment) {

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.main_frame, fragment);
        transaction.commit();

    }

    /**
     * 检查是否有更新
     */
    private void checkUpdate() {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("type", "vercode"));
        ServerUtil.getThread(ServerUtil.SLUpdate, params, handler,
                0, 1, true).start();
    }

    private void requestPermissions() {
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(ClientMainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(ClientMainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(ClientMainActivity.this,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(ClientMainActivity.this, permissions, 1);
        }
    }

    private static class MyHandler extends Handler {

        WeakReference<ClientMainActivity> activityWeakReference;

        public MyHandler(ClientMainActivity activity) {
            this.activityWeakReference = new WeakReference<ClientMainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case User.USER_RESPONSE:
                    activityWeakReference.get().isLogin = true;
                    String response = (String) msg.obj;
                    activityWeakReference.get().curUser.convertJSON(response);

                    activityWeakReference.get().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            activityWeakReference.get().fillUserInfo();
                        }
                    });

                    break;
                case User.NET_ERROR:
                    Toast.makeText(activityWeakReference.get(), "网络错误", Toast.LENGTH_SHORT).show();
                    break;
                case User.USER_WRONG:
                    Toast.makeText(activityWeakReference.get(), "用户信息错误，请重新登录", Toast.LENGTH_SHORT).show();
                    activityWeakReference.get().isLogin = false;
                    break;
                case 0:
                    response = (String) msg.obj;
                    if (!response.equals("" + AppInfo.getVersionCode(
                            activityWeakReference.get()))) {
                        activityWeakReference.get().meFragment.setUpdate();
                    } else {
                        Intent intent = new Intent(activityWeakReference.get()
                                , DownloadService.class);
                        activityWeakReference.get().stopService(intent);
                    }
                    break;
            }
        }

    }

}
