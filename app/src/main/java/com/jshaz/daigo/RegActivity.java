package com.jshaz.daigo;

/**
 * 手机号注册页面
 */

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.test.suitebuilder.annotation.Suppress;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jshaz.daigo.serverutil.ServerUtil;
import com.jshaz.daigo.ui.BaseActivity;
import com.jshaz.daigo.util.SMSUtil;
import com.jshaz.daigo.util.Utility;
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

public class RegActivity extends BaseActivity implements View.OnClickListener {

    private EditText editPhone;

    private EditText editVerCode;

    private EditText editDisCode;

    private EditText editPassword;

    private EventHandler eventHandler;

    private Button getVerCode;

    private Button reg;

    private boolean checkVerCode = false;

    private ProgressDialog regDialog, verDialog;

    private Thread regThread;

    private TextView eula;

    private SMSHandler mHandler = new SMSHandler(this);
    private MyHandler handler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);

        //初始化短信SDK
        MobSDK.init(this, "228198a34c17d", "73b44581dcf3914180feb22fcdf3dbb9");

        //绑定控件
        editPhone = (EditText) findViewById(R.id.reg_phonenum);
        editVerCode = (EditText) findViewById(R.id.reg_vercode);
        editDisCode = (EditText) findViewById(R.id.reg_discode);
        editPassword = (EditText) findViewById(R.id.reg_password);

        getVerCode = (Button) findViewById(R.id.reg_get_vercode);
        reg = (Button) findViewById(R.id.reg_regbtn);

        eula = (TextView) findViewById(R.id.reg_eula);


        eula.setOnClickListener(this);

        //设置按钮监听器
        getVerCode.setOnClickListener(this);
        reg.setOnClickListener(this);

        //手机号码文本框获得焦点
        editPhone.requestFocus();


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

        //监听密码文本框长度
        editPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 20) {
                    Toast.makeText(RegActivity.this, "密码不能超过20位",
                            Toast.LENGTH_SHORT).show();
                    editPassword.setText(charSequence.subSequence(0, 20));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }


    @Override
    public void onClick(View view) {
        String disCode = nonspaceConvert(editDisCode.getText().toString()); //区号
        String phoneNum = nonspaceConvert(editPhone.getText().toString()); //手机号码

        switch (view.getId()) {
            case R.id.reg_get_vercode:
                SMSUtil.getVerCode(disCode, phoneNum);
                break;
            case R.id.reg_regbtn:
                //避免重复验证短信导致失败
                String verCode = editVerCode.getText().toString();
                if (!checkVerCode){
                    SMSUtil.submitVerCode(disCode, phoneNum, verCode);
                    //弹出正在验证对话框
                    startVerifyUIDialog();
                } else {
                    addUserOnServer("86", phoneNum, editPassword.getText().toString());
                }

                break;
            case R.id.reg_eula:
                startActivity(new Intent(this, EULAActivity.class));
                break;
            default: break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //反注册短信监听器
        SMSSDK.unregisterEventHandler(eventHandler);
        //关闭所有对话框
        stopUIDialog();
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

    /**
     * 检查密码格式
     * @return
     */
    private boolean checkPassword() {
        String passwd = editPassword.getText().toString();
        if (passwd.length() < 6) {
            Toast.makeText(this, "密码不能少于6位", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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
     * 显示正在验证对话框
     */
    private void startVerifyUIDialog() {
        verDialog = new ProgressDialog(this);
        verDialog.setCancelable(false);
        verDialog.setCanceledOnTouchOutside(false);
        verDialog.setMessage("正在验证...");
        verDialog.setTitle("注册");
        verDialog.show();
    }

    /**
     * 显示正在注册对话框
     */
    private void startRegUIDialog() {
        regDialog = new ProgressDialog(this);
        regDialog.setCancelable(false);
        regDialog.setCanceledOnTouchOutside(false);
        regDialog.setMessage("正在注册...");
        regDialog.setTitle("注册");
        regDialog.show();
    }

    /**
     * 关闭正在注册对话框
     */
    private void stopUIDialog() {
        if (regDialog != null) {
            regDialog.dismiss();
        }
        if (verDialog != null) {
            verDialog.dismiss();
        }
    }

    /**
     * 向服务器端注册用户
     * @param disCode
     * @param phoneNum
     * @param password
     */
    private String response = "";
    private void addUserOnServer(final String disCode, final String phoneNum, final String password) {
    //耗时操作，需要在子线程中完成
            startRegUIDialog();
            regThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    try {
                        BasicHttpParams httpParams = new BasicHttpParams();
                        HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
                        HttpConnectionParams.setSoTimeout(httpParams, 5000);

                        HttpClient httpclient = new DefaultHttpClient(httpParams);

                        //服务器地址，指向Servlet
                        HttpPost httpPost = new HttpPost(ServerUtil.SLRegister);

                        List<NameValuePair> params = new ArrayList<NameValuePair>();//将数据装入list

                        params.add(new BasicNameValuePair("phonenum", phoneNum));
                        params.add(new BasicNameValuePair("password", password));
                        params.add(new BasicNameValuePair("headicon",
                                Utility.convertBitmapToString(BitmapFactory.decodeResource(
                                        getResources(), R.drawable.default_head_icon
                                ))));

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
                    } catch (Exception e) {
                        e.printStackTrace();
                        Message message = handler.obtainMessage();
                        message.what = 1;
                        handler.handleMessage(message);
                    }
                    Looper.loop();

                }

            });
            regThread.start();
    }

    private static class SMSHandler extends Handler {
        WeakReference<RegActivity> activityWeakReference;
        public SMSHandler(RegActivity activity) {
            this.activityWeakReference = new WeakReference<RegActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final RegActivity activity = activityWeakReference.get();
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
                    //执行注册逻辑
                    if (activity.checkPassword() && activity.checkVerCode) {

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //打开正在注册对话框
                                activity.startRegUIDialog();
                                String disCode = activity.nonspaceConvert(activity.editDisCode.getText().toString()); //区号
                                String phoneNum = activity.nonspaceConvert(activity.editPhone.getText().toString()); //手机号码
                                String password = activity.editPassword.getText().toString(); //密码

                                //开启网络请求
                                activity.addUserOnServer(disCode, phoneNum, password);
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
        WeakReference<RegActivity> activityWeakReference;
        public MyHandler(RegActivity activity) {
            this.activityWeakReference = new WeakReference<RegActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final RegActivity activity = activityWeakReference.get();
            switch (msg.what) {
                case 0:
                    String response = (String) msg.obj;
                    if (response.equals("")) {
                        Toast.makeText(activity,"连接超时",Toast.LENGTH_SHORT).show();
                        activity.stopUIDialog();
                    } else {
                        activity.stopUIDialog();
                        if (response.equals("false")) {
                            Toast.makeText(activity, "手机号已被注册", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(activity, "注册成功", Toast.LENGTH_SHORT).show();
                            activity.finish();
                        }

                    }
                    break;
                case 1:
                    Toast.makeText(activity,"连接超时",Toast.LENGTH_SHORT).show();
                    activity.stopUIDialog();
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
            getVerCode.setEnabled(false);
            getVerCode.setText("获取验证码(" + l / 1000 + ")");
        }

        @Override
        public void onFinish() {
            getVerCode.setEnabled(true);
            getVerCode.setText("获取验证码");
        }
    }

}
