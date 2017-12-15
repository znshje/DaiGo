package com.jshaz.daigo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.jshaz.daigo.serverutil.ServerUtil;
import com.jshaz.daigo.ui.ToolBarView;

public class EULAActivity extends AppCompatActivity {

    private ToolBarView toolBarView;

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eula);

        toolBarView = (ToolBarView) findViewById(R.id.eula_toolbar);
        webView = (WebView) findViewById(R.id.eula_webview);

        toolBarView.setTitleText("用户服务条款和隐私协议");
        toolBarView.setBackButtonImage(R.mipmap.icon_back);
        toolBarView.setBackButtonVisible(true);
        toolBarView.setBackButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(ServerUtil.WVAgreement);

    }
}
