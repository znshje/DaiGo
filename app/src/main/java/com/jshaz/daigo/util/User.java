package com.jshaz.daigo.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Looper;
import android.os.Message;
import android.os.Handler;

import com.google.gson.Gson;
import com.jshaz.daigo.R;
import com.jshaz.daigo.gson.UserDAO;
import com.jshaz.daigo.interfaces.BaseClassImpl;
import com.jshaz.daigo.serverutil.ServerUtil;

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
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jshaz on 2017/11/21.
 */

public class User extends DataSupport implements BaseClassImpl, Serializable {
    //用户唯一ID标识号
    private String userId;
    //用户密码
    private String password;
    //电话区号
    private String disCode = "86";
    //电话号码
    private String phoneNum;
    //昵称
    private String nickName;
    //头像字符串
    private String headIcon;
    //校区代码
    private int campusCode = 0;
    //身份证号
    private String IDCode;
    //真实姓名
    private String realName;
    //学号
    private String campusIdCode;
    //默认收货地址
    private String defaultAddress;
    //是否通过实名认证
    private boolean isVerified = false;

    private UserDatabaseHelper dbHelper;

    private Context mContext;

    public User(Context context) {
        this.mContext = context;
        setNullValue();
        readFromLocalSharedPref();
    }

    public User() {

    }

    @Override
    public void writeToLocalDatabase() {
        final String CREATE_TABLE = "create table userinfo (" +
                "id integer primary key autoincrement," +
                "userid text," +
                "password text," +
                "discode text," +
                "phonenum text," +
                "nickname text," +
                "headicon text," +
                "campuscode integer," +
                "idcode text," +
                "realname text," +
                "campusidcode text," +
                "defaultaddress text)";
        if (userId == null) {
            return;
        }
        dbHelper = new UserDatabaseHelper(mContext, userId + ".db", null, 1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        db.execSQL("drop table if exists userinfo");
        db.execSQL(CREATE_TABLE);
        values.put("userid", userId);
        values.put("password", password);
        values.put("discode", disCode);
        values.put("phonenum", phoneNum);
        values.put("nickname", nickName);
        values.put("headicon", headIcon);
        values.put("campuscode", campusCode);
        values.put("idcode", IDCode);
        values.put("realname", realName);
        values.put("campusidcode", campusIdCode);
        values.put("defaultaddress", defaultAddress);
        db.insert("userinfo", null, values);
    }

    public void writeToLocalDatabase(String userId) {
        final String CREATE_TABLE = "create table userinfo (" +
                "id integer primary key autoincrement," +
                "userid text," +
                "password text," +
                "discode text," +
                "phonenum text," +
                "nickname text," +
                "headicon text," +
                "campuscode integer," +
                "idcode text," +
                "realname text," +
                "campusidcode text," +
                "defaultaddress text)";
        try{
            dbHelper = new UserDatabaseHelper(mContext, userId + ".db", null, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        try{
            db.execSQL(CREATE_TABLE);
            values.put("userid", userId);
            values.put("password", password);
            values.put("discode", disCode);
            values.put("phonenum", phoneNum);
            values.put("nickname", nickName);
            values.put("headicon", headIcon);
            values.put("campuscode", campusCode);
            values.put("idcode", IDCode);
            values.put("realname", realName);
            values.put("campusidcode", campusIdCode);
            values.put("defaultaddress", defaultAddress);
            db.insert("userinfo", null, values);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writeToLocalSharedPref() {
        SharedPreferences.Editor editor = mContext.
                getSharedPreferences("user_cache", Context.MODE_PRIVATE).edit();
        editor.putString("user_id", userId);
        editor.apply();
    }

    @Override
    public void readFromLocalDatabase() {
        if (userId == null) {
            return;
        }
        writeToLocalDatabase(userId);
        dbHelper = new UserDatabaseHelper(mContext, userId + ".db", null, 1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query("userinfo", null, null,
                null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                if (this.userId.equals(cursor.getString(cursor.getColumnIndex("userid")))) {
                    this.password = cursor.getString(cursor.getColumnIndex("password"));
                    this.disCode = cursor.getString(cursor.getColumnIndex("discode"));
                    String phonenumT = cursor.getString(cursor.getColumnIndex("phonenum"));
                    if (phonenumT != null) {
                        this.phoneNum = cursor.getString(cursor.getColumnIndex("phonenum"));
                    }
                    String nicknameT = cursor.getString(cursor.getColumnIndex("nickname"));

                    if (nicknameT != null) {
                        this.nickName = cursor.getString(cursor.getColumnIndex("nickname"));
                    }
                    String headiconT = cursor.getString(cursor.getColumnIndex("headicon"));
                    if (headiconT != null) {
                        this.headIcon = cursor.getString(cursor.getColumnIndex("headicon"));
                    }

                    this.campusCode = cursor.getInt(cursor.getColumnIndex("campuscode"));
                    this.IDCode = cursor.getString(cursor.getColumnIndex("idcode"));
                    this.realName = cursor.getString(cursor.getColumnIndex("realname"));
                    this.campusIdCode = cursor.getString(cursor.getColumnIndex("campusidcode"));
                    String addressT = cursor.getString(cursor.getColumnIndex("defaultaddress"));
                    if (addressT != null) {
                        this.defaultAddress = cursor.getString(cursor.getColumnIndex("defaultaddress"));
                    }
                }
            } while (cursor.moveToNext());
        }
    }

    @Override
    public void readFromLocalSharedPref() {
        SharedPreferences preferences = mContext.
                getSharedPreferences("user_cache", Context.MODE_PRIVATE);

        if (!preferences.getString("user_id","").equals("")) {
            this.userId = preferences.getString("user_id","");
        } else {
            setNullValue();
        }
    }

    @Override
    public void updateData() {

    }

    /**
     * 从服务器端获取用户数据
     * 用户名和密码必须相同
     * 需要在调用的时候开启子线程
     */
    @Override
    public void cloneData(final Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                String response = "";
                try {
                    BasicHttpParams httpParams = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
                    HttpConnectionParams.setSoTimeout(httpParams, 5000);

                    HttpClient httpclient = new DefaultHttpClient(httpParams);

                    //服务器地址，指向Servlet
                    HttpPost httpPost = new HttpPost(ServerUtil.SLCheckIDPassword);

                    List<NameValuePair> params = new ArrayList<NameValuePair>();//将数据装入list
                    params.add(new BasicNameValuePair("userid", userId));
                    params.add(new BasicNameValuePair("password", password));

                    final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "utf-8");//以UTF-8格式发送
                    httpPost.setEntity(entity);
                    //对提交数据进行编码
                    HttpResponse httpResponse = httpclient.execute(httpPost);
                    if (httpResponse.getStatusLine().getStatusCode() == 200)//在5000毫秒之内接收到返回值
                    {
                        HttpEntity entity1 = httpResponse.getEntity();
                        response = EntityUtils.toString(entity1, "utf-8");//以UTF-8格式解析
                        Message message = handler.obtainMessage();
                        if (response.equals("false")) {
                            message.what = USER_WRONG;
                            message.obj = response;
                            handler.handleMessage(message);
                        } else {
                            message.what = USER_RESPONSE;
                            //JSON data
                            message.obj = response;
                            handler.handleMessage(message);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = handler.obtainMessage();
                    message.what = NET_ERROR;
                    handler.handleMessage(message);
                }
                Looper.loop();
            }
        }).start();
    }

    @Override
    public void convertJSON(String json) {
        Gson gson = new Gson();
        UserDAO user = gson.fromJson(json, UserDAO.class);

            this.password = user.getPassword();
            this.phoneNum = user.getPhoneNum();
            this.nickName = user.getNickName();
            this.headIcon = user.getHeadIcon();
            this.IDCode = user.getIDCode();
            this.realName = user.getRealName();
            this.campusIdCode = user.getCampusIdCode();
            this.defaultAddress = user.getDefaultAddress();
            this.isVerified = user.isVerified();

    }

    public UserDAO toDAO() {
        UserDAO userDAO = new UserDAO();
        userDAO.setUserId(userId);
        userDAO.setDefaultAddress(defaultAddress);
        userDAO.setDisCode("86");
        userDAO.setPhoneNum(phoneNum);
        userDAO.setNickName(nickName);
        userDAO.setHeadIcon(headIcon);
        userDAO.setPassword(password);
        userDAO.setCampusIdCode(campusIdCode);
        userDAO.setIDCode(IDCode);
        userDAO.setRealName(realName);
        userDAO.setVerified(isVerified);
        return userDAO;
    }

    public boolean checkUserInfo() {
        return true;
    }


    /**
     * 设置用户默认值
     */
    public void setDefaultValue() {
        setCampusCode(0);
        setNickName("无名人士");
        setHeadIcon(Utility.convertBitmapToString(
                BitmapFactory.decodeResource(mContext.getResources(), R.drawable.default_head_icon)));
        setDefaultAddress("");
        setRealName("");
    }

    /**
     * 设置未登录默认值
     */
    public void setNullValue() {
        setUserId("");
        setCampusCode(0);
        setNickName("未登录");
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.default_head_icon);
        setHeadIcon(Utility.convertBitmapToString(bitmap));
        setDefaultAddress("");
        setRealName("");
        setPhoneNum("-");
    }


    //Setter and getter
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDisCode() {
        return disCode;
    }

    public void setDisCode(String disCode) {
        this.disCode = disCode;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getHeadIcon() {
        return headIcon;
    }

    public void setHeadIcon(String headIcon) {
        this.headIcon = headIcon;
    }

    public int getCampusCode() {
        return campusCode;
    }

    public void setCampusCode(int campusCode) {
        this.campusCode = campusCode;
    }

    public void setIDCode(String IDCode) {
        this.IDCode = IDCode;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getCampusIdCode() {
        return campusIdCode;
    }

    public void setCampusIdCode(String campusIdCode) {
        this.campusIdCode = campusIdCode;
    }

    public String getDefaultAddress() {
        return defaultAddress;
    }

    public void setDefaultAddress(String defaultAddress) {
        this.defaultAddress = defaultAddress;
    }

    public String getIDCode() {
        return IDCode;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isVerified() {
        return isVerified;
    }
}
