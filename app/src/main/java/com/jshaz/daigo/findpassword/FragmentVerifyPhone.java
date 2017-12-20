package com.jshaz.daigo.findpassword;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jshaz.daigo.util.SMSUtil;
import com.jshaz.daigo.R;
import com.mob.MobSDK;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Scanner;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.utils.SMSLog;

/**
 * Created by jshaz on 2017/11/22.
 */

public class FragmentVerifyPhone extends Fragment implements View.OnClickListener {

    private EditText editDisCode;

    private EditText editPhone;

    private EditText editVerCode;

    private Button getVerCode;

    private Button nextStep;

    private boolean checkVerCode = false;

    private ProgressDialog verDialog;

    private FragmentResetPassword fragmentResetPassword;

    private EventHandler eventHandler;

    private final int SLIDE_FROM_LEFT_TO_RIGHT = 0;//从左向右滑动页面的动画代码
    private final int SLIDE_FROM_RIGHT_TO_LEFT = 1;//从右向左滑动页面的动画代码

    private MyHandler mHandler = new MyHandler(this);

    View view = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_verifyphone, container, false);
        editDisCode = view.findViewById(R.id.findpwd_discode);
        editPhone = view.findViewById(R.id.findpwd_phonenum);
        editVerCode = view.findViewById(R.id.findpwd_vercode);
        getVerCode = view.findViewById(R.id.findpwd_get_vercode);
        nextStep = view.findViewById(R.id.findpwd_next);

        getVerCode.setOnClickListener(this);
        nextStep.setOnClickListener(this);

        editDisCode.clearFocus();
        editPhone.requestFocus();

        //初始化短信SDK
        MobSDK.init(getContext(), "228198a34c17d", "73b44581dcf3914180feb22fcdf3dbb9");

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

        fragmentResetPassword = new FragmentResetPassword();

        return view;
    }

    @Override
    public void onClick(View view) {
        String disCode = nonspaceConvert(editDisCode.getText().toString()); //区号
        String phoneNum = nonspaceConvert(editPhone.getText().toString()); //手机号码
        switch (view.getId()) {
            case R.id.findpwd_get_vercode:
                SMSUtil.getVerCode(disCode, phoneNum);
                break;
            case R.id.findpwd_next:
                String verCode = editVerCode.getText().toString();
                if (!checkVerCode){
                    SMSUtil.submitVerCode(disCode, phoneNum, verCode);
                    //弹出正在验证对话框
                    startVerifyUIDialog();
                }
                break;
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        SMSSDK.unregisterEventHandler(eventHandler);
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
     * 开启获取验证码的一分钟冷却时间
     */
    private void startVerCodeCountDown() {
        VerCodeCountDownTimer timer = new
                VerCodeCountDownTimer(60000, 1000);
        timer.start();
    }

    /**
     * 开启正在验证对话框
     */
    private void startVerifyUIDialog() {
        verDialog = new ProgressDialog(getContext());
        verDialog.setMessage("正在验证...");
        verDialog.setTitle("找回密码");
        verDialog.setCancelable(false);
        verDialog.setCanceledOnTouchOutside(false);
        verDialog.show();
    }

    /**
     * 关闭正在验证对话框
     */
    private void stopVerifyUIDialog() {
        if (verDialog != null) {
            verDialog.dismiss();
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


    private static class MyHandler extends Handler {
        WeakReference<FragmentVerifyPhone> fragmentVerifyPhoneWeakReference;

        public MyHandler(FragmentVerifyPhone fragmentVerifyPhone) {
            this.fragmentVerifyPhoneWeakReference = new
                    WeakReference<FragmentVerifyPhone>(fragmentVerifyPhone);
        }

        @Override
        public void handleMessage(Message msg) {
            final FragmentVerifyPhone fragment = fragmentVerifyPhoneWeakReference.get();
            int event = msg.arg1;
            int result = msg.arg2;
            Object data = msg.obj;

            if (result == SMSSDK.RESULT_COMPLETE) {
                //正确进行
                if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                    //获取验证码
                    Message uiMsg = new Message();
                    uiMsg.what = 0;
                    fragment.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(fragment.getContext(), "验证码已发送",
                                    Toast.LENGTH_SHORT).show();
                            fragment.startVerCodeCountDown();
                        }
                    });

                } else if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                    //验证码正确
                    fragment.checkVerCode = true;
                    fragment.stopVerifyUIDialog();
                    fragment.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fragment.fragmentResetPassword.setFindPhone(fragment.editPhone.getText().toString());
                            fragment.replaceFragment(fragment.fragmentResetPassword, fragment.SLIDE_FROM_RIGHT_TO_LEFT);
                        }
                    });
                }

            } else {
                //出现异常
                int status = 0;
                fragment.stopVerifyUIDialog();
                try{
                    ((Throwable) data).printStackTrace();
                    Throwable throwable = (Throwable) data;

                    JSONObject object = new JSONObject(throwable.getMessage());
                    final String des = object.optString("detail");
                    status = object.optInt("status");
                    if (!TextUtils.isEmpty(des)) {
                        fragment.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(fragment.getContext(), des, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                } catch (Exception e) {
                    SMSLog.getInstance().w(e);
                }
            }
        }
    }
}
