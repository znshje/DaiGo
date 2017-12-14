package com.jshaz.daigo.client;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.jshaz.daigo.LoginActivity;
import com.jshaz.daigo.ModifyInfoActivity;
import com.jshaz.daigo.R;
import com.jshaz.daigo.intents.UserIntent;
import com.jshaz.daigo.ui.ComplexButton;
import com.jshaz.daigo.ui.ToolBarView;
import com.jshaz.daigo.util.Setting;
import com.jshaz.daigo.util.User;

import java.util.Set;

/**
 * Created by jshaz on 2017/11/21.
 */

public class MeFragment extends Fragment {

    View view = null;

    private ComplexButton CBModifyInfo;
    private ComplexButton CBMyOrder;
    private ComplexButton CBSettings;
    private ComplexButton CBAboutUs;
    private ComplexButton CBReport;
    private ComplexButton CBOpenDrawer;

    private Button logout;

    private Setting setting;

    private User user;

    private Activity parentActivity;
    private ToolBarView toolBarView;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view != null) return view;
        view = inflater.inflate(R.layout.fragment_me, container, false);

        setting = new Setting(getContext());
        setting.readFromLocalSharedPref();

        user = new User(getContext());

        CBModifyInfo = (ComplexButton) view.findViewById(R.id.me_modify_info);
        CBMyOrder = (ComplexButton) view.findViewById(R.id.me_my_order);
        CBAboutUs = (ComplexButton) view.findViewById(R.id.me_about_us);
        CBReport = (ComplexButton) view.findViewById(R.id.me_report);
        CBSettings = (ComplexButton) view.findViewById(R.id.me_settings);
        CBOpenDrawer = (ComplexButton) view.findViewById(R.id.me_open_drawer);
        logout = (Button) view.findViewById(R.id.me_logout);


        CBOpenDrawer.selectType(ComplexButton.TYPE_TEXT_ONLY);
        CBOpenDrawer.setItemName("我的资料/选择校区");
        CBOpenDrawer.setButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toolBarView.callOnBackButtonClick();
            }
        });


        CBModifyInfo.setButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //修改个人资料
                user.readFromLocalSharedPref();
                if (user.getUserId() == null || user.getUserId().equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setNegativeButton("取消", null);
                    builder.setPositiveButton("登录", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivity(new Intent(getContext(), LoginActivity.class));
                            parentActivity.finish();
                        }
                    });
                    builder.setMessage("您还未登录");
                    builder.show();
                } else {
                    Intent intent = new Intent(getContext(), ModifyInfoActivity.class);
                    startActivity(intent);
                }

            }
        });
        CBModifyInfo.selectType(ComplexButton.TYPE_TEXT_ONLY);
        CBModifyInfo.setItemName("修改个人资料");

        CBMyOrder.setButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //我的订单
                user.readFromLocalSharedPref();
                if (user.getUserId() == null || user.getUserId().equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setNegativeButton("取消", null);
                    builder.setPositiveButton("登录", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivity(new Intent(getContext(), LoginActivity.class));
                            parentActivity.finish();
                        }
                    });
                    builder.setMessage("您还未登录");
                    builder.show();
                } else {
                    UserIntent userIntent = new UserIntent();
                    userIntent.setUserId(user.getUserId());
                    Intent intent = new Intent(getContext(), MyOrderActivity.class);
                    intent.putExtra("user", userIntent);
                    startActivity(intent);
                }
            }
        });
        CBMyOrder.selectType(ComplexButton.TYPE_TEXT_ONLY);
        CBMyOrder.setItemName("我的订单");

        CBAboutUs.selectType(ComplexButton.TYPE_TEXT_ONLY);
        CBAboutUs.setItemName("关于我们");
        CBAboutUs.setButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AboutUsActivity.class);
                startActivity(intent);
            }
        });

        CBReport.selectType(ComplexButton.TYPE_TEXT_ONLY);
        CBReport.setItemName("反馈");
        CBReport.setButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), ReportActivity.class));
            }
        });

        CBSettings.selectType(ComplexButton.TYPE_TEXT_ONLY);
        CBSettings.setItemName("设置");
        CBSettings.setButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), SettingActivity.class));
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //退出登录
                //清除本地SharedPref数据
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("确认退出登录吗？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences.Editor editor = getContext().
                                getSharedPreferences("user_cache", Context.MODE_PRIVATE).edit();
                        editor.putString("user_id", "");
                        editor.apply();
                        startActivity(new Intent(getContext(), ClientMainActivity.class));
                        parentActivity.finish();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.show();

            }
        });

        //获取用户信息
        getUserInfo();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getUserInfo();
    }

    public void setToolBarView(ToolBarView toolBarView) {
        this.toolBarView = toolBarView;
    }

    /**
     * 获取用户信息
     */
    private void getUserInfo() {
        user.readFromLocalSharedPref();
        user.readFromLocalDatabase();
        if (user.getUserId().equals("")) {
            setUnLogin();
        } else {
            if (user.checkUserInfo()) {
                user.cloneData(handler); //从服务器端获取用户最新数据
            } else {
                setUnLogin();
            }
        }
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case User.USER_RESPONSE:
                    setLogin();
                    break;
                case User.NET_ERROR:
                    break;
                case User.USER_WRONG:
                    setUnLogin();
                    break;
            }
        }
    };

    /**
     * 设置未登录的View
     */
    private void setUnLogin() {
        logout.setVisibility(View.GONE);
    }

    /**
     * 设置登录的View
     */
    private void setLogin() {
        logout.setVisibility(View.VISIBLE);
    }

    public void setActivity(Activity activity) {
        parentActivity = activity;
    }
}