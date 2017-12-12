package com.jshaz.daigo.util;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.jshaz.daigo.R;
import com.jshaz.daigo.gson.OrderDAO;
import com.jshaz.daigo.ui.MyApplication;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

/**
 * Created by jshaz on 2017/11/19.
 */

public class Utility {

    /**
     * 实现添加到List开头的逻辑
     * @param list 要进行添加的List
     * @param object 要添加的对象
     */
    public static void addToFirst(List list, Object object) {
        Object[] objects = list.toArray();
        list.clear();
        list.add(object);
        for (int i = 0; i < objects.length; i++) {
            list.add(objects[i]);
        }
    }

    /**
     * 将long类型的时间转为日期类型
     * @param date
     * @return
     */
    public static String convertMillToDate(long date) {
        return new SimpleDateFormat("y年M月d日 HH:mm").format(new Date(date));
    }
    public static String convertMillToDate(long date, int type) {
        switch (type) {
            case 0:
                return convertMillToDate(date);
            case 1:
                return new SimpleDateFormat("y年M月d日 HH:mm:ss").format(new Date(date));
            case 2:
                return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(date));
        }
        return "";
    }


    /**
     * 解析订单编号
     * @param orderId 订单编号
     * @return
     */
    public static OrderDAO decodeOrderNumber(String orderId) {
        OrderDAO order = new OrderDAO();
        String createDateYear = orderId.substring(0, 4) + "年";
        String createDateMonth = orderId.charAt(4) == '0' ?
                orderId.substring(5, 6) + "月" :
                orderId.substring(4, 6) + "月";
        String createDateDay = orderId.charAt(6) == '0' ?
                orderId.substring(7, 8) + "日 " :
                orderId.substring(6, 8) + "日 ";
        String createDateHour = orderId.charAt(8) == '0' ?
                orderId.substring(9, 10) + ":" :
                orderId.substring(8, 10) + ":";
        String createDateMinute = orderId.substring(10, 12) + ":";
        String createDateSecond = orderId.substring(12, 14);
        int campusCode = Integer.parseInt(orderId.substring(14, 15));
        String userId = orderId.substring(15);
        String dateTime = createDateYear + createDateMonth + createDateDay +
                createDateHour + createDateMinute + createDateSecond;
        return new OrderDAO();
    }

    public static String getOrderReleaseTime(String orderId) {
        String createDateYear = orderId.substring(0, 4) + "年";
        String createDateMonth = orderId.charAt(4) == '0' ?
                orderId.substring(5, 6) + "月" :
                orderId.substring(4, 6) + "月";
        String createDateDay = orderId.charAt(6) == '0' ?
                orderId.substring(7, 8) + "日 " :
                orderId.substring(6, 8) + "日 ";
        String createDateHour = orderId.charAt(8) == '0' ?
                orderId.substring(9, 10) + ":" :
                orderId.substring(8, 10) + ":";
        String createDateMinute = orderId.substring(10, 12);
        String dateTime = createDateYear + createDateMonth + createDateDay +
                createDateHour + createDateMinute;
        return dateTime;
    }

    public static String getCampusName(String orderId) {
        int campusCode = Integer.parseInt(orderId.substring(14, 15));
        return Setting.CAMPUS_NAME[campusCode];
    }

    public static int getCampusCode(String orderId) {
        return Integer.parseInt(orderId.substring(14, 15));
    }

    /**
     * 图片转成string
     *
     * @param bitmap
     * @return
     */
    public static String convertBitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();// outputstream
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();// 转为byte数组
        return Base64.encodeToString(b, Base64.DEFAULT);

    }

    /**
     * string转成bitmap
     *
     * @param st
     */
    public static Bitmap convertStringToBitmap(String st) {
        // OutputStream out;
        Bitmap bitmap = null;
        try
        {
            // out = new FileOutputStream("/sdcard/aa.jpg");
            byte[] bitmapArray;
            bitmapArray = Base64.decode(st, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
            // bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            return bitmap;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * 压缩图片
     * @param bitmap
     * @return
     */
    public static Bitmap compressBitmap(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.setScale(0.5f, 0.5f);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                matrix, true);
    }

    public static Bitmap compressBitmap(String bitmapString) {
        Bitmap t = convertStringToBitmap(bitmapString);
        return compressBitmap(t);
    }
}
