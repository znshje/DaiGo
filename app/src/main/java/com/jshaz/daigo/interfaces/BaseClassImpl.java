package com.jshaz.daigo.interfaces;


import android.os.Handler;

/**
 * Created by jshaz on 2017/11/21.
 */

public interface BaseClassImpl {

    int USER_RESPONSE = 8080;

    int USER_WRONG = 8082;

    int ORDER_RESPONSE = 8088;

    int NET_ERROR = 8081;

    void writeToLocalDatabase();

    void writeToLocalSharedPref();

    void readFromLocalDatabase();

    void readFromLocalSharedPref();

    void updateData();

    void cloneData(Handler handler);

    void convertJSON(String json);

}
