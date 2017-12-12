package com.jshaz.daigo.client;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.jshaz.daigo.R;
import com.jshaz.daigo.ui.ToolBarView;

public class ComplainActivity extends AppCompatActivity {

    private ToolBarView toolBarView;

    private String orderId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complain);

        initView();
    }

    private void initView() {
        toolBarView = (ToolBarView) findViewById(R.id.complain_toolbar);


        toolBarView.setBackButtonVisible(true);
        toolBarView.setBackButtonImage(R.mipmap.icon_back);
        toolBarView.setBackButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolBarView.setTitleText("投诉");
        toolBarView.setTitleCampusVisible(false);

    }
}
