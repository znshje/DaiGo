package com.jshaz.daigo.client;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewParent;
import android.widget.TabHost;
import android.widget.TableLayout;

import com.jshaz.daigo.R;
import com.jshaz.daigo.intents.UserIntent;
import com.jshaz.daigo.ui.BaseActivity;
import com.jshaz.daigo.ui.ToolBarView;

import java.util.ArrayList;
import java.util.List;

public class MyOrderActivity extends BaseActivity {

    private ToolBarView toolBarView;

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private List<String> tabIndicators;
    private List<ShowMyOrdersFragment> tabFragments;
    private ContentPagerAdapter adapter;

    private ShowMyOrdersFragment[] showMyOrdersFragments =
            new ShowMyOrdersFragment[5];

    private static String[] fragmentName = new String[] {
            "未完成订单", "已完成订单", "我的接单", "已配送的单", "取消的订单"
    };

    UserIntent userIntent = new UserIntent();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_order);

        initView();
    }

    private void initView() {

        Intent intent = getIntent();
        userIntent = (UserIntent) intent.getSerializableExtra("user");

        toolBarView = (ToolBarView) findViewById(R.id.my_order_toolbar);
        tabLayout = (TabLayout) findViewById(R.id.my_order_tab);
        viewPager = (ViewPager) findViewById(R.id.my_order_viewpager);

        toolBarView.setTitleText("我的订单");
        toolBarView.setBackButtonVisible(true);
        toolBarView.setBackButtonImage(R.mipmap.icon_back);
        toolBarView.setBackButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        toolBarView.setRightButtonImage(R.mipmap.icon_refresh);
        toolBarView.setRightButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tabFragments.get(tabLayout.getSelectedTabPosition()).callOnRefresh();
            }
        });

        initTab();
        initContent();
    }

    private void initTab() {
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setTabTextColors(ContextCompat.getColor(this, R.color.colorWhitePrimary),
                ContextCompat.getColor(this, R.color.colorWhiteDarkPrimary));
        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.colorWhitePrimary));
        tabLayout.setupWithViewPager(viewPager);
    }

    private void initContent() {
        tabIndicators = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            tabIndicators.add(fragmentName[i]);
        }

        tabFragments = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            showMyOrdersFragments[i] = new ShowMyOrdersFragment();
            showMyOrdersFragments[i].setType(i);
            showMyOrdersFragments[i].setUserId(userIntent.getUserId());
            tabFragments.add(showMyOrdersFragments[i]);
        }

        adapter = new ContentPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    tabFragments.get(tabLayout.getSelectedTabPosition()).callOnRefresh();
                }
                break;
        }
    }

    class ContentPagerAdapter extends FragmentPagerAdapter {
        public ContentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabIndicators.get(position);
        }

        @Override
        public Fragment getItem(int position) {
            return tabFragments.get(position);
        }

        @Override
        public int getCount() {
            return tabIndicators.size();
        }


    }
}
