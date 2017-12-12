package com.jshaz.daigo.broadcasts;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.jshaz.daigo.ui.NavigationView;
import com.jshaz.daigo.util.Setting;

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

import javax.net.ssl.HandshakeCompletedListener;

public class OrderUpdateReceiver extends BroadcastReceiver {

    private NavigationView navigationView;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        navigationView.setRedDot(true);
    }

    public void setNavigationView(NavigationView navigationView) {
        this.navigationView = navigationView;
    }

}
