package com.jshaz.daigo;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.jshaz.daigo.findpassword.FragmentVerifyPhone;
import com.jshaz.daigo.ui.BaseActivity;

public class FindPasswordActivity extends BaseActivity {

    private FragmentVerifyPhone fragmentVerifyPhone;

    private final int SLIDE_FROM_LEFT_TO_RIGHT = 0;//从左向右滑动页面的动画代码
    private final int SLIDE_FROM_RIGHT_TO_LEFT = 1;//从右向左滑动页面的动画代码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_password);

        fragmentVerifyPhone = new FragmentVerifyPhone();

        replaceFragment(fragmentVerifyPhone, SLIDE_FROM_RIGHT_TO_LEFT);
    }

    private void replaceFragment(Fragment fragment, int slideType) {

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

        transaction.replace(R.id.findpwd_main_frame, fragment);
        transaction.commit();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {}
        }, 200);
    }


}
