package com.jshaz.daigo.util;

/**
 * Created by jshaz on 2018/1/4.
 */

public class SecurityJni {

    static {
        System.loadLibrary("GetSL");
    }

    public static native String getSL(String SLName);

}
