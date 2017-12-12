package com.jshaz.daigo.client;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jshaz.daigo.LoginActivity;
import com.jshaz.daigo.ModifyInfoActivity;
import com.jshaz.daigo.broadcasts.OrderUpdateReceiver;
import com.jshaz.daigo.intents.UserIntent;
import com.jshaz.daigo.serverutil.ServerUtil;
import com.jshaz.daigo.service.AutoUpdateService;
import com.jshaz.daigo.ui.NavigationView;
import com.jshaz.daigo.R;
import com.jshaz.daigo.ui.BaseActivity;
import com.jshaz.daigo.ui.ToolBarView;
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

    private int curFragment = 0;

    private CircleImageView drawerHead;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_main);

        initView();

        initReceiver();

        startAutoUpdateService();

        getUserInfo();

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
                    orderFragment.refreshOrder();
                }
            }
        });

        //调试修改个人资料模块，记得删除注释
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


        /*侧滑菜单的监听事件*/
        navigationMenuView.setNavigationItemSelectedListener(new android.support.design.widget.
                NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (!orderFragment.isRefreshing()) {
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
                return false;
            }
        });

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

    /**
     * 开启自动更新服务
     */
    private void startAutoUpdateService() {
        Intent intent = new Intent(this, AutoUpdateService.class);
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
        //从本地读取用户缓存数据
        curUser.readFromLocalDatabase();
        if (curUser.getUserId().equals("")) {
            isLogin = false;
            //setUnLogin();
        } else {

                isLogin = true;

                curUser.cloneData(handler); //从服务器端获取用户最新数据
//                fillUserInfo();

        }
    }

    public void setDetailActivityReturned(boolean b) {
        this.isDetailActivityReturned = b;
    }


    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case User.USER_RESPONSE:
                    isLogin = true;
                    String response = (String) msg.obj;
                    curUser.convertJSON(response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fillUserInfo();
                        }
                    });

                    break;
                case User.NET_ERROR:
                    Toast.makeText(ClientMainActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                    break;
                case User.USER_WRONG:
                    Toast.makeText(ClientMainActivity.this, "用户信息错误，请重新登录", Toast.LENGTH_SHORT).show();
                    isLogin = false;
                    break;
            }
        }
    };

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

        if (isLogin) {
            curUser.readFromLocalDatabase();
            drawerHead.setImageBitmap(Utility.convertStringToBitmap(curUser.getHeadIcon()));
            drawerNickName.setText(curUser.getNickName());
            //////
            String phoneT = curUser.getPhoneNum();
            drawerPhone.setText(phoneT.substring(0, 3) + "****" + phoneT.substring(7));
            drawerHead.setOnClickListener(null);
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

}
