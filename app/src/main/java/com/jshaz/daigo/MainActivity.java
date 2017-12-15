package com.jshaz.daigo;

import android.content.Intent;
import android.os.Bundle;
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
            Intent intent = new Intent(MainActivity.this, ClientMainActivity.class);
            startActivity(intent);
            this.finish();
        } else {
            campusSetting.setCampusCode(0);
            campusSetting.writeToLocalSharedPref();
            Intent intent = new Intent(MainActivity.this, ClientMainActivity.class);
            intent.putExtra("first_start", true);
            startActivity(intent);
            this.finish();
        }

        /*
        for (int i = 0; i < 8; i++) {
            cardViews[i] = (ExtendedCardView) findViewById(Setting.getCardViewId(i));
            cardViews[i].setCampusBg(i);
            cardViews[i].setCampusName(i);
            cardViews[i].setOnClickListener(this);
        }
*/

    }

    /*
    @Override
    public void onClick(View view) {
        Intent intent = new Intent(MainActivity.this, ClientMainActivity.class);
        switch (view.getId()) {
            case Setting.CARDVIEW_ZHONGXIN:
                campusSetting.setCampusCode(Setting.CAMPUS_ZHONGXIN);
                campusSetting.writeToLocalSharedPref();
                startActivity(intent);
                this.finish();
                break;
            case Setting.CARDVIEW_RUANJIANYUAN:
                campusSetting.setCampusCode(Setting.CAMPUS_RUANJIANYUAN);
                campusSetting.writeToLocalSharedPref();
                startActivity(intent);
                this.finish();
                break;
            case Setting.CARDVIEW_HONGJIALOU:
                campusSetting.setCampusCode(Setting.CAMPUS_HONGJIALOU);
                campusSetting.writeToLocalSharedPref();
                startActivity(intent);
                this.finish();
                break;
            case Setting.CARDVIEW_BAOTUQUAN:
                campusSetting.setCampusCode(Setting.CAMPUS_BAOTUQUAN);
                campusSetting.writeToLocalSharedPref();
                startActivity(intent);
                this.finish();
                break;
            case Setting.CARDVIEW_XINGLONGSHAN:
                campusSetting.setCampusCode(Setting.CAMPUS_XINGLONGSHAN);
                campusSetting.writeToLocalSharedPref();
                startActivity(intent);
                this.finish();
                break;
            case Setting.CARDVIEW_QIANFOSHAN:
                campusSetting.setCampusCode(Setting.CAMPUS_QIANFOSHAN);
                campusSetting.writeToLocalSharedPref();
                startActivity(intent);
                this.finish();
                break;
            case Setting.CARDVIEW_QINGDAO:
                campusSetting.setCampusCode(Setting.CAMPUS_QINGDAO);
                campusSetting.writeToLocalSharedPref();
                startActivity(intent);
                this.finish();
                break;
            case Setting.CARDVIEW_WEIHAI:
                campusSetting.setCampusCode(Setting.CAMPUS_WEIHAI);
                campusSetting.writeToLocalSharedPref();
                startActivity(intent);
                this.finish();
                break;
        }

    }
*/

    @Override
    public void onClick(View v) {

    }
}
