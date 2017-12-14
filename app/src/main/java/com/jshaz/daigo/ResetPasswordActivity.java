package com.jshaz.daigo;

/**
 * 修改密码活动
 */

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jshaz.daigo.intents.UserIntent;
import com.jshaz.daigo.serverutil.ServerUtil;
import com.jshaz.daigo.ui.BaseActivity;
import com.jshaz.daigo.ui.ToolBarView;
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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.LoggingPermission;

public class ResetPasswordActivity extends BaseActivity {

    private ToolBarView toolBarView;

    private EditText ETOldPassword;
    private EditText ETNewPassword;
    private EditText ETReNewPassword;

    private ProgressDialog applyDialog;

    private Button submit;

    private Thread applyThread;

    private UserIntent userIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        userIntent = (UserIntent) getIntent().getSerializableExtra("user");


        initView();
    }

    private void initView() {
        toolBarView = (ToolBarView) findViewById(R.id.reset_pass_toolbar);
        ETOldPassword = (EditText) findViewById(R.id.reset_old_password);
        ETNewPassword = (EditText) findViewById(R.id.reset_new_password);
        ETReNewPassword = (EditText) findViewById(R.id.reset_reput_password);
        submit = (Button) findViewById(R.id.reset_pass_submit);

        toolBarView.setTitleCampusVisible(false);
        toolBarView.setTitleText("修改密码");
        toolBarView.setBackButtonImage(R.mipmap.icon_back);
        toolBarView.setBackButtonVisible(true);
        toolBarView.setBackButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        setSlideExit(true);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ETOldPassword.getText().equals("") ||
                        ETNewPassword.getText().equals("") || ETReNewPassword.getText().equals("")) {
                    Toast.makeText(ResetPasswordActivity.this, "有内容未填写", Toast.LENGTH_SHORT).show();
                } else if (!ETNewPassword.getText().toString().equals(ETReNewPassword.getText().toString())) {
                    Toast.makeText(ResetPasswordActivity.this, "两次输入密码不匹配", Toast.LENGTH_SHORT).show();
                } else if (checkPassword()){
                    verifyPassword();
                }
            }
        });

        ETNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 20) {
                    Toast.makeText(ResetPasswordActivity.this, "密码不能超过20位",
                            Toast.LENGTH_SHORT).show();
                    ETNewPassword.setText(charSequence.subSequence(0, 20));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        ETReNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 20) {
                    Toast.makeText(ResetPasswordActivity.this, "密码不能超过20位",
                            Toast.LENGTH_SHORT).show();
                    ETReNewPassword.setText(charSequence.subSequence(0, 20));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

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
     * 从服务器验证密码
     * 将旧密码和新密码一同提交到服务器
     * 由服务器判断密码是否正确
     * 如果正确就修改新密码
     * 如果不正确不修改
     */
    String response = "";
    private void verifyPassword() {
        openVerifyDialog();
        applyThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                try {
                    BasicHttpParams httpParams = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
                    HttpConnectionParams.setSoTimeout(httpParams, 5000);

                    HttpClient httpclient = new DefaultHttpClient(httpParams);

                    //服务器地址，指向Servlet
                    HttpPost httpPost = new HttpPost(ServerUtil.SLUpdateUserInfo);

                    List<NameValuePair> params = new ArrayList<NameValuePair>();//将数据装入list
                    params.add(new BasicNameValuePair("userid", userIntent.getUserId()));
                    params.add(new BasicNameValuePair("oldpassword", ETOldPassword.getText().toString()));
                    params.add(new BasicNameValuePair("newpassword", ETNewPassword.getText().toString()));
                    final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "utf-8");//以UTF-8格式发送
                    httpPost.setEntity(entity);
                    //对提交数据进行编码
                    HttpResponse httpResponse = httpclient.execute(httpPost);
                    if(httpResponse.getStatusLine().getStatusCode()==200)//在5000毫秒之内接收到返回值
                    {
                        HttpEntity entity1 = httpResponse.getEntity();
                        response = EntityUtils.toString(entity1, "utf-8");//以UTF-8格式解析
                        Message message = mHandler.obtainMessage();
                        message.what=0;
                        message.obj=response;
                        mHandler.handleMessage(message);
                    } else {
                        Message message = mHandler.obtainMessage();
                        message.what=2;
                        mHandler.handleMessage(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message message = mHandler.obtainMessage();
                    message.what=2;
                    mHandler.handleMessage(message);
                }
                Looper.loop();
            }
        });
        applyThread.start();
    }


    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    stopDialog();
                    String response = (String) msg.obj;
                    if (response.equals("true")) {
                        Toast.makeText(ResetPasswordActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                        passResult(ETNewPassword.getText().toString());
                    } else {
                        Toast.makeText(ResetPasswordActivity.this, "原密码错误", Toast.LENGTH_SHORT).show();
                    }

                    break;
                case 2:
                    stopDialog();
                    Toast.makeText(ResetPasswordActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                    break;

            }
        }
    };

    /**
     * 检查密码格式
     * @return
     */
    private boolean checkPassword() {
        String passwd = ETNewPassword.getText().toString();
        if (passwd.length() < 6) {
            Toast.makeText(this, "密码不能少于6位", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    private void openVerifyDialog() {
        if (applyDialog == null) {
            applyDialog = new ProgressDialog(this);
        }
        applyDialog.setMessage("正在验证...");
        applyDialog.setCancelable(false);
        applyDialog.setCanceledOnTouchOutside(false);
        applyDialog.show();
    }

    private void stopDialog() {
        if (applyDialog != null) {
            applyDialog.dismiss();
        }
    }
}
