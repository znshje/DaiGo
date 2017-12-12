package com.jshaz.daigo.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.jshaz.daigo.ui.MyApplication;

import java.util.concurrent.CopyOnWriteArrayList;


public class NetWorkStateReceiver extends BroadcastReceiver {

    public boolean isNetWorkConnected;

    private Context mContext;

    public NetWorkStateReceiver() {
        super();
    }

    public NetWorkStateReceiver(Context context) {
        mContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            isNetWorkConnected = true;
        } else {
            isNetWorkConnected = false;
            Toast.makeText(context, "网络无连接", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 判断网络可用性
     * @return
     */
    public boolean isNetWorkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            return networkInfo.isConnected();
        }
        return false;
    }
}
