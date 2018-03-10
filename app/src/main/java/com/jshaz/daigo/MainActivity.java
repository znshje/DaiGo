package com.jshaz.daigo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Toast;

import com.jshaz.daigo.client.ClientMainActivity;
import com.jshaz.daigo.ui.BaseActivity;
import com.jshaz.daigo.ui.ExtendedCardView;
import com.jshaz.daigo.util.Setting;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private ExtendedCardView[] cardViews = new ExtendedCardView[8];

    private Setting campusSetting = new Setting(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*设置双击返回键退出*/
        setDoubleBackExit(true);

        /*从本地读取设置信息
        * 如果之前选择过校区，则跳转到校区页面*/
        campusSetting.readFromLocalSharedPref();

        if (campusSetting.getCampusCode() != -1) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(MainActivity.this, ClientMainActivity.class);
                    startActivity(intent);
                    MainActivity.this.finish();
                }
            }, 1000);
        } else {
            campusSetting.setCampusCode(0);
            campusSetting.writeToLocalSharedPref();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(MainActivity.this, ClientMainActivity.class);
                    intent.putExtra("first_start", true);
                    startActivity(intent);
                    MainActivity.this.finish();
                }
            }, 1000);
        }


    }


    @Override
    public void onClick(View v) {

    }
}
