package com.jshaz.daigo.util;

import cn.smssdk.SMSSDK;

/**
 * Created by jshaz on 2017/11/20.
 */

public class SMSUtil {

    public static final String APP_KEY = "228198a34c17d";

    public static final String APP_SECRET = "73b44581dcf3914180feb22fcdf3dbb9";

    public static void getVerCode(String disCode, String phoneNum) {
        SMSSDK.getVerificationCode(disCode, phoneNum);
    }

    public static void submitVerCode(String disCode, String phoneNum, String verCode) {
        SMSSDK.submitVerificationCode(disCode, phoneNum, verCode);
    }
}
