package com.jshaz.daigo.client;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.jshaz.daigo.EULAActivity;
import com.jshaz.daigo.R;
import com.jshaz.daigo.ui.BaseActivity;
import com.jshaz.daigo.ui.ToolBarView;

public class AboutUsActivity extends BaseActivity {

    private ToolBarView toolBarView;

    private TextView eula;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        initView();
    }

    private void initView() {

        setSlideExit(true);

        toolBarView = (ToolBarView) findViewById(R.id.about_us_toolbar);
        eula = (TextView) findViewById(R.id.about_us_eula);

        toolBarView.setTitleText("关于我们");
        toolBarView.setBackButtonVisible(true);
        toolBarView.setBackButtonImage(R.mipmap.icon_back);
        toolBarView.setBackButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        eula.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AboutUsActivity.this, EULAActivity.class));
            }
        });
    }
}
