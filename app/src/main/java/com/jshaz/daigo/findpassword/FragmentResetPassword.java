package com.jshaz.daigo.findpassword;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jshaz.daigo.R;
import com.jshaz.daigo.serverutil.ServerUtil;

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

/**
 * Created by jshaz on 2017/11/22.
 */

public class FragmentResetPassword extends Fragment implements View.OnClickListener {

    private EditText editPassword;

    private Button nextStep;

    private ProgressDialog applyDialog;

    private FragmentSuccess fragmentSuccess;

    private String findPhone = "";

    private final int SLIDE_FROM_LEFT_TO_RIGHT = 0;//从左向右滑动页面的动画代码
    private final int SLIDE_FROM_RIGHT_TO_LEFT = 1;//从右向左滑动页面的动画代码

    private Thread applyThread = new Thread(new Runnable() {
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

                params.add(new BasicNameValuePair("findphone", findPhone));
                params.add(new BasicNameValuePair("password", editPassword.getText().toString()));

                final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "utf-8");//以UTF-8格式发送
                httpPost.setEntity(entity);
                //对提交数据进行编码
                HttpResponse httpResponse = httpclient.execute(httpPost);
                if(httpResponse.getStatusLine().getStatusCode()==200)//在5000毫秒之内接收到返回值
                {
                    HttpEntity entity1 = httpResponse.getEntity();
//                    response = EntityUtils.toString(entity1, "utf-8");//以UTF-8格式解析
                    Message message=new Message();
                    message.what=0;
//                    message.obj=response;
                    handler.handleMessage(message);
                } else {
                    Message message=new Message();
                    message.what=1;
                    handler.handleMessage(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Message message=new Message();
                message.what=1;
                handler.handleMessage(message);
            }
            Looper.loop();
        }
    });

    View view = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_resetpassword, container, false);
        editPassword = view.findViewById(R.id.findpwd_password);
        nextStep = view.findViewById(R.id.findpwd_next2);

        nextStep.setOnClickListener(this);

        fragmentSuccess = new FragmentSuccess();

        editPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 20) {
                    Toast.makeText(getContext(), "密码不能超过20位", Toast.LENGTH_SHORT).show();
                    editPassword.setText(charSequence.subSequence(0, 20));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.findpwd_next2:
                String password = editPassword.getText().toString();
                if (checkPasswordFormat(password)) {
                    startApplyUIDialog();
                    applyNewPassword(password);

                }
                break;
        }
    }

    public void setFindPhone(String phone) {
        this.findPhone = phone;
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    replaceFragment(fragmentSuccess, SLIDE_FROM_RIGHT_TO_LEFT);
                    break;
                case 1:
                    Toast.makeText(getContext(), "网络错误", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private boolean checkPasswordFormat(String password) {
        if (password.length() < 6) {
            Toast.makeText(getContext(), "密码长度不能少于6位", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * 应用密码到服务器上
     * @param password
     */
    private void applyNewPassword(String password) {
        applyThread.start();
    }

    /**
     * 开启正在验证对话框
     */
    private void startApplyUIDialog() {
        applyDialog = new ProgressDialog(getContext());
        applyDialog.setMessage("正在应用...");
        applyDialog.setTitle("找回密码");
        applyDialog.setCancelable(false);
        applyDialog.setCanceledOnTouchOutside(false);
        applyDialog.show();
    }

    /**
     * 关闭正在验证对话框
     */
    private void stopApplyUIDialog() {
        if (applyDialog != null) {
            applyDialog.dismiss();
        }
    }

    /**
     * 替换碎片
     * 为了保证不出现碎片重叠现象，手动禁用控件并延迟启用
     * @param fragment
     * @param slideType
     */
    private void replaceFragment(Fragment fragment, int slideType) {

        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        switch (slideType){
            case SLIDE_FROM_LEFT_TO_RIGHT:
                transaction.setCustomAnimations(R.anim.slide_left_in,R.anim.slide_right_out);
                break;
            case SLIDE_FROM_RIGHT_TO_LEFT:
                transaction.setCustomAnimations(R.anim.slide_right_in,R.anim.slide_left_out);
                break;
        }

        transaction.replace(R.id.findpwd_main_frame, fragment);
        transaction.commit();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {}
        }, 200);
    }

}
