package com.jshaz.daigo.intents;

import java.io.Serializable;

/**
 * Created by jshaz on 2017/12/6.
 */

public class UserIntent implements Serializable {

    private String userId;

    private String password;

    private String phoneNum;

    private String defaultAddress;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getDefaultAddress() {
        return defaultAddress;
    }

    public void setDefaultAddress(String defaultAddress) {
        this.defaultAddress = defaultAddress;
    }
}
