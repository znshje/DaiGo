package com.jshaz.daigo.ui;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePalApplication;

/**
 * Created by jshaz on 2017/11/20.
 */

public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
}
