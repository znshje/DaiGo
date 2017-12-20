package com.jshaz.daigo;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.jshaz.daigo.intents.UserIntent;
import com.jshaz.daigo.serverutil.ServerUtil;
import com.jshaz.daigo.ui.BaseActivity;
import com.jshaz.daigo.ui.ToolBarView;
import com.jshaz.daigo.util.SMSUtil;
import com.jshaz.daigo.util.User;
import com.mob.MobSDK;

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
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.utils.SMSLog;

public class ResetPhoneActivity extends BaseActivity {

    private ToolBarView toolBarView;

    private LinearLayout llOld;
    private LinearLayout llNew;

    private EditText ETOldPhone;
    private EditText ETPassword;
    private EditText ETNewPhone;
    private EditText ETVercode;

    private Button getVercode;
    private Button verifyIdendtity;
    private Button submit;

    public static final String disCode = "86";

    private EventHandler eventHandler;
    private boolean checkVerCode = false;

    private ProgressDialog applyDialog;

    private Thread updateThread;
    private Thread verifyThread;

    private UserIntent userIntent;

    private SMSHandler mHandler = new SMSHandler(this);
    private MyHandler handler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_phone);

        //初始化短信SDK
        MobSDK.init(this, "228198a34c17d", "73b44581dcf3914180feb22fcdf3dbb9");

        userIntent = (UserIntent) getIntent().getSerializableExtra("user");

        initView();

        initSMSSDK();
    }

    /**
     * 初始化View
     */
    private void initView() {
        toolBarView = (ToolBarView) findViewById(R.id.reset_phone_toolbar);
        llOld = (LinearLayout) findViewById(R.id.reset_old_phone_layout);
        llNew = (LinearLayout) findViewById(R.id.reset_new_phone_layout);
        ETOldPhone = (EditText) findViewById(R.id.reset_old_phone);
        ETNewPhone = (EditText) findViewById(R.id.reset_new_phone);
        ETPassword = (EditText) findViewById(R.id.reset_phone_password);
        ETVercode = (EditText) findViewById(R.id.reset_phone_vercode);
        getVercode = (Button) findViewById(R.id.reset_phone_get_vercode);
        verifyIdendtity = (Button) findViewById(R.id.reset_phone_verify_identity);
        submit = (Button) findViewById(R.id.reset_phone_submit);

        llNew.setVisibility(View.GONE);

        toolBarView.setTitleCampusVisible(false);
        toolBarView.setTitleText("修改手机号");
        toolBarView.setBackButtonImage(R.mipmap.icon_back);
        toolBarView.setBackButtonVisible(true);
        toolBarView.setBackButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        verifyIdendtity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //验证身份
                verifyUserInfo(ETOldPhone.getText().toString(), ETPassword.getText().toString());
            }
        });

        getVercode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNum = nonspaceConvert(ETNewPhone.getText().toString()); //手机号码
                SMSUtil.getVerCode(disCode, phoneNum);
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //避免重复验证短信导致失败
                String verCode = ETVercode.getText().toString();
                if (!checkVerCode){
                    //弹出正在验证对话框
                    startRegUIDialog();
                    SMSUtil.submitVerCode(disCode, ETNewPhone.getText().toString(), verCode);
                }
            }
        });

    }

    /**
     * 去掉号码中的特殊字符
     * @param s
     * @return
     */
    private String nonspaceConvert(String s) {
        Scanner scanner = new Scanner(s);
        StringBuilder builder = new StringBuilder();
        while (scanner.hasNext()) {
            builder.append(scanner.next());
        }
        return builder.toString();
    }

    private void initSMSSDK() {
        // 创建EventHandler对象
        eventHandler = new EventHandler() {
            public void afterEvent(int event, int result, Object data) {
                Message msg = new Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                mHandler.handleMessage(msg);
            }
        };

        // 注册短信监听器
        SMSSDK.registerEventHandler(eventHandler);
    }

    private void verifyUserInfo(final String phoneNum, final String password) {
        verifyThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                String response = "";
                try {
                    BasicHttpParams httpParams = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
                    HttpConnectionParams.setSoTimeout(httpParams, 5000);

                    HttpClient httpclient = new DefaultHttpClient(httpParams);

                    //服务器地址，指向Servlet
                    HttpPost httpPost = new HttpPost(ServerUtil.SLLogin);

                    List<NameValuePair> params = new ArrayList<NameValuePair>();//将数据装入list
                    //params.add(new BasicNameValuePair("userid", userIntent.getUserId()));
                    params.add(new BasicNameValuePair("phonenum", phoneNum));
                    params.add(new BasicNameValuePair("password", password));
                    final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "utf-8");//以UTF-8格式发送
                    httpPost.setEntity(entity);
                    //对提交数据进行编码
                    HttpResponse httpResponse = httpclient.execute(httpPost);
                    if (httpResponse.getStatusLine().getStatusCode() == 200)//在5000毫秒之内接收到返回值
                    {
                        HttpEntity entity1 = httpResponse.getEntity();
                        response = EntityUtils.toString(entity1, "utf-8");//以UTF-8格式解析
                        Message message = new Message();
                        message.what = 1;
                        message.obj = response;
                        handler.handleMessage(message);
                    } else {
                        Message message = new Message();
                        message.what = User.NET_ERROR;
                        handler.handleMessage(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message message = new Message();
                    message.what = User.NET_ERROR;
                    handler.handleMessage(message);
                }
                Looper.loop();
            }
        });
        verifyThread.start();
    }

    /**
     * 传递数据
     */
    private void passResult(String data) {
        Intent intent = new Intent();
        intent.putExtra("data", data);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * 开启获取验证码的一分钟冷却时间
     */
    private void startVerCodeCountDown() {
        VerCodeCountDownTimer timer = new
                VerCodeCountDownTimer(60000, 1000);
        timer.start();
    }


    /**
     * 向服务器更新手机号
     * @param phoneNum
     */
    private void updateUserOnServer(final String phoneNum) {
        updateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                String response = "";
                try {
                    BasicHttpParams httpParams = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
                    HttpConnectionParams.setSoTimeout(httpParams, 5000);

                    HttpClient httpclient = new DefaultHttpClient(httpParams);

                    //服务器地址，指向Servlet
                    HttpPost httpPost = new HttpPost(ServerUtil.SLUpdateUserInfo);

                    List<NameValuePair> params = new ArrayList<NameValuePair>();//将数据装入list

                    params.add(new BasicNameValuePair("userid", userIntent.getUserId()));
                    params.add(new BasicNameValuePair("phonenum", phoneNum));

                    final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "utf-8");//以UTF-8格式发送
                    httpPost.setEntity(entity);
                    //对提交数据进行编码
                    HttpResponse httpResponse = httpclient.execute(httpPost);
                    if (httpResponse.getStatusLine().getStatusCode() == 200)//在5000毫秒之内接收到返回值
                    {
                        HttpEntity entity1 = httpResponse.getEntity();
                        response = EntityUtils.toString(entity1, "utf-8");//以UTF-8格式解析
                        Message message = handler.obtainMessage();
                        message.what = 0;
                        message.obj = response;
                        handler.handleMessage(message);
                    } else {
                        Message message = handler.obtainMessage();
                        message.what = User.NET_ERROR;
                        handler.handleMessage(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message message = handler.obtainMessage();
                    message.what = User.NET_ERROR;
                    handler.handleMessage(message);
                }
                Looper.loop();
            }
        });
        updateThread.start();
    }

    /**
     * 显示正在注册对话框
     */
    private void startRegUIDialog() {
        applyDialog = new ProgressDialog(this);
        applyDialog.setCancelable(false);
        applyDialog.setCanceledOnTouchOutside(false);
        applyDialog.setMessage("正在提交...");
        applyDialog.show();
    }

    /**
     * 关闭正在注册对话框
     */
    private void stopUIDialog() {
        if (applyDialog != null) {
            applyDialog.dismiss();
        }
    }


    private static class SMSHandler extends Handler {
        WeakReference<ResetPhoneActivity> activityWeakReference;
        public SMSHandler(ResetPhoneActivity activity) {
            this.activityWeakReference = new WeakReference<ResetPhoneActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final ResetPhoneActivity activity = activityWeakReference.get();
            int event = msg.arg1;
            int result = msg.arg2;
            Object data = msg.obj;

            if (result == SMSSDK.RESULT_COMPLETE) {
                //正确进行
                if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                    //获取验证码
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, "验证码已发送",
                                    Toast.LENGTH_SHORT).show();
                            activity.startVerCodeCountDown();
                        }
                    });

                } else if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                    //验证码正确
                    activity.checkVerCode = true;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            activity.stopUIDialog();
                        }
                    });
                    //执行修改逻辑
                    if (activity.checkVerCode) {

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //打开正在执行对话框
                                activity.startRegUIDialog();

                                String phoneNum = activity.nonspaceConvert(activity.ETNewPhone.getText().toString()); //手机号码

                                //开启网络请求
                                activity.updateUserOnServer(phoneNum);
                            }
                        });


                    }

                }

            } else {
                //出现异常
                int status = 0;
                try{
                    ((Throwable) data).printStackTrace();
                    Throwable throwable = (Throwable) data;

                    JSONObject object = new JSONObject(throwable.getMessage());
                    final String des = object.optString("detail");
                    status = object.optInt("status");
                    if (!TextUtils.isEmpty(des)) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                activity.stopUIDialog();
                                Toast.makeText(activity, des, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                } catch (Exception e) {
                    SMSLog.getInstance().w(e);
                }
            }
        }
    }

    private static class MyHandler extends Handler {
        WeakReference<ResetPhoneActivity> activityWeakReference;
        public MyHandler(ResetPhoneActivity activity) {
            this.activityWeakReference = new WeakReference<ResetPhoneActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final ResetPhoneActivity activity = activityWeakReference.get();
            switch (msg.what) {
                case 0:
                    activity.stopUIDialog();
                    Toast.makeText(activity, "修改成功", Toast.LENGTH_SHORT).show();
                    activity.passResult(activity.ETNewPhone.getText().toString());
                    break;
                case 1:
                    String response = (String) msg.obj;
                    if (!response.equals("false")) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                activity.llOld.setVisibility(View.GONE);
                                activity.llNew.setVisibility(View.VISIBLE);
                            }
                        });

                    } else {
                        Toast.makeText(activity, "手机号或密码错误", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case User.NET_ERROR:
                    activity.stopUIDialog();
                    Toast.makeText(activity, "网络错误", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }




    /**
     * 自定义计时器内部类
     */
    class VerCodeCountDownTimer extends CountDownTimer {
        public VerCodeCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            getVercode.setEnabled(false);
            getVercode.setText("获取验证码(" + l / 1000 + ")");
        }

        @Override
        public void onFinish() {
            getVercode.setEnabled(true);
            getVercode.setText("获取验证码");
        }
    }

}
