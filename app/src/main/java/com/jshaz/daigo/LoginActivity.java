package com.jshaz.daigo;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.GravityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jshaz.daigo.broadcasts.NetWorkStateReceiver;
import com.jshaz.daigo.client.ClientMainActivity;
import com.jshaz.daigo.serverutil.ServerUtil;
import com.jshaz.daigo.ui.BaseActivity;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends BaseActivity {

    private Button btnLogin;

    private Button btnReg;

    private Button btnForgetPassword;

    private EditText disCode;

    private EditText phoneNum;

    private EditText password;

    private ProgressDialog loginDialog;

    private User user;

    /**
     * 网络状态广播接收器
     */
    private IntentFilter intentFilter;

    private NetWorkStateReceiver netWorkStateReceiver = new NetWorkStateReceiver(this);

    private Thread checkThread;

    private MyHandler handler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

         user = new User(this);

        btnLogin = (Button) findViewById(R.id.login_submit);
        btnReg = (Button) findViewById(R.id.login_to_reg);
        btnForgetPassword = (Button) findViewById(R.id.login_find_password);
        disCode = (EditText) findViewById(R.id.login_discode);
        phoneNum = (EditText) findViewById(R.id.login_phonenum);
        password = (EditText) findViewById(R.id.login_password);

        disCode.clearFocus();
        phoneNum.requestFocus();
        setSlideExit(false);

        /*初始化网络广播器*/
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(netWorkStateReceiver, intentFilter);
        /*-------------*/

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkFullment()) {
                    checkUserInfoFromDatabase("86",
                            phoneNum.getText().toString(), password.getText().toString());
                }

            }
        });

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegActivity.class);
                startActivity(intent);
            }
        });

        btnForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, FindPasswordActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        /*按下了返回键*/
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (loginDialog != null && loginDialog.isShowing()) {
            /*正在登录*/
                checkThread.interrupt();
                stopLoginDialog();
            } else {
            /*未在登录*/
                finish();
            }

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }


    /**
     * 数据库模块
     */
    //数据库模块
    public String response = "";

    public void checkUserInfoFromDatabase(final String disCode,final String phoneNum, final String passwd) {
        startLoginDialog();
        user.setPhoneNum(phoneNum);
        user.setPassword(passwd);
        checkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                try {
                    BasicHttpParams httpParams = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
                    HttpConnectionParams.setSoTimeout(httpParams, 5000);

                    HttpClient httpclient = new DefaultHttpClient(httpParams);

                    //服务器地址，指向Servlet
                    HttpPost httpPost = new HttpPost(ServerUtil.SLLogin);

                    List<NameValuePair> params = new ArrayList<NameValuePair>();//将数据装入list
                    params.add(new BasicNameValuePair("discode", disCode));
                    params.add(new BasicNameValuePair("phonenum", phoneNum));
                    params.add(new BasicNameValuePair("password", passwd));
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
        checkThread.start();
    }



    private void LoginMoudle(String response) {
        Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(LoginActivity.this, ClientMainActivity.class));
        SharedPreferences.Editor editor = getSharedPreferences("user_cache", MODE_PRIVATE).edit();
        editor.putString("user_id",response);
        user.setUserId(response);
        user.writeToLocalDatabase();
        editor.apply();
        finish();
    }

    private boolean checkFullment() {
        if (phoneNum.getText().toString().equals("")) {
            Toast.makeText(this, "请填写手机号", Toast.LENGTH_SHORT).show();
            return false;
        } else if (password.getText().toString().equals("")) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * 开启登录对话框
     */
    public void startLoginDialog() {
        loginDialog = new ProgressDialog(this);
        loginDialog.setTitle("登录");
        loginDialog.setMessage("正在登录...");
        loginDialog.setCancelable(false);
        loginDialog.setCanceledOnTouchOutside(false);
        loginDialog.show();
    }

    /**
     * 关闭登录对话框
     */
    public void stopLoginDialog() {
        if (loginDialog != null) {
            loginDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(netWorkStateReceiver);
    }

    private static class MyHandler extends Handler {
        WeakReference<LoginActivity> activityWeakReference;

        public MyHandler(LoginActivity activity) {
            this.activityWeakReference = new WeakReference<LoginActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            LoginActivity activity = activityWeakReference.get();
            switch (msg.what){
                case 0:
                    String response = (String) msg.obj;
                    if (response.equals("")) {
                        Toast.makeText(activity,"连接超时",Toast.LENGTH_SHORT).show();
                        activity.stopLoginDialog();
                    } else {
                        activity.stopLoginDialog();
                        if (response.equals("false")){
                            Toast.makeText(activity, "手机号或密码错误", Toast.LENGTH_SHORT).show();
                        } else {
                            activity.LoginMoudle(response);
                        }

                    }
                    break;
                case 1:
                    Toast.makeText(activity,"连接超时",Toast.LENGTH_SHORT).show();
                    activity.stopLoginDialog();
                    break;
                default:
                    break;
            }
        }
    }
}
