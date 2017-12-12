package com.jshaz.daigo.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.jshaz.daigo.R;
import com.jshaz.daigo.interfaces.BaseClassImpl;

import android.os.Handler;

/**
 * Created by jshaz on 2017/11/21.
 */

public class Setting implements BaseClassImpl {

    /**
     * 校区设置
     */
    public static final int CAMPUS_ZHONGXIN = 0;
    public static final int CARDVIEW_ZHONGXIN = R.id.select_campus_zhongxin;
    public static final int BACKGROUND_BIG_ZHONGXIN = R.drawable.zhongxin_big;
    public static final int NAV_MENU_ZHONGXIN = R.id.nav_menu_zhongxin;

    public static final int CAMPUS_RUANJIANYUAN = 1;
    public static final int CARDVIEW_RUANJIANYUAN = R.id.select_campus_ruanjianyuan;
    public static final int BACKGROUND_BIG_RUANJIANYUAN = R.drawable.ruanjianyuan_big;
    public static final int NAV_MENU_RUANJIANYUAN = R.id.nav_menu_ruanjianyuan;

    public static final int CAMPUS_HONGJIALOU = 2;
    public static final int CARDVIEW_HONGJIALOU = R.id.select_campus_hongjialou;
    public static final int BACKGROUND_BIG_HONGJIALOU = R.drawable.hongjialou_big;
    public static final int NAV_MENU_HONGJIALOU = R.id.nav_menu_hongjialou;

    public static final int CAMPUS_BAOTUQUAN = 3;
    public static final int CARDVIEW_BAOTUQUAN = R.id.select_campus_baotuquan;
    public static final int BACKGROUND_BIG_BAOTUQUAN = R.drawable.baotuquan_big;
    public static final int NAV_MENU_BAOTUQUAN = R.id.nav_menu_baotuquan;

    public static final int CAMPUS_XINGLONGSHAN = 4;
    public static final int CARDVIEW_XINGLONGSHAN = R.id.select_campus_xinglongshan;
    public static final int BACKGROUND_BIG_XINGLONGSHAN = R.drawable.xinglongshan_big;
    public static final int NAV_MENU_XINGLONGSHAN = R.id.nav_menu_xinglongshan;

    public static final int CAMPUS_QIANFOSHAN = 5;
    public static final int CARDVIEW_QIANFOSHAN = R.id.select_campus_qianfoshan;
    public static final int BACKGROUND_BIG_QIANFOSHAN = R.drawable.qianfoshan_big;
    public static final int NAV_MENU_QIANFOSHAN = R.id.nav_menu_qianfoshan;

    public static final int CAMPUS_QINGDAO = 6;
    public static final int CARDVIEW_QINGDAO = R.id.select_campus_qingdao;
    public static final int BACKGROUND_BIG_QINGDAO = R.drawable.qingdao_big;
    public static final int NAV_MENU_QINGDAO = R.id.nav_menu_qingdao;

    public static final int CAMPUS_WEIHAI = 7;
    public static final int CARDVIEW_WEIHAI = R.id.select_campus_weihai;
    public static final int BACKGROUND_BIG_WEIHAI = R.drawable.weihai_big;
    public static final int NAV_MENU_WEIHAI = R.id.nav_menu_weihai;

    public static final String[] CAMPUS_NAME = new String[] {"中心校区", "软件园校区",
        "洪家楼校区", "趵突泉校区", "兴隆山校区", "千佛山校区", "青岛校区", "威海校区"};

    public static final int[] CAMPUS_BANNER_RID = new int[] {R.drawable.zhongxin_banner,
        R.drawable.ruanjianyuan_banner, R.drawable.hongjialou_banner,
        R.drawable.baotuquan_banner, R.drawable.xinglongshan_banner,
        R.drawable.qianfoshan_big, R.drawable.qingdao_big, R.drawable.weihai_big};

    private int campusCode = -1;

    private Context mContext;


    public static String getCampusName(int campusCode) {
        return CAMPUS_NAME[campusCode];
    }

    public static int getCardViewId(int campusCode) {
        switch (campusCode) {
            case CAMPUS_ZHONGXIN:
                return CARDVIEW_ZHONGXIN;
            case CAMPUS_RUANJIANYUAN:
                return CARDVIEW_RUANJIANYUAN;
            case CAMPUS_HONGJIALOU:
                return CARDVIEW_HONGJIALOU;
            case CAMPUS_BAOTUQUAN:
                return CARDVIEW_BAOTUQUAN;
            case CAMPUS_XINGLONGSHAN:
                return CARDVIEW_XINGLONGSHAN;
            case CAMPUS_QIANFOSHAN:
                return CARDVIEW_QIANFOSHAN;
            case CAMPUS_QINGDAO:
                return CARDVIEW_QINGDAO;
            case CAMPUS_WEIHAI:
                return CARDVIEW_WEIHAI;
            default:
                return CARDVIEW_ZHONGXIN;
        }
    }

    public static int getBackgroundId(int campusCode) {
        switch (campusCode) {
            case CAMPUS_ZHONGXIN:
                return BACKGROUND_BIG_ZHONGXIN;
            case CAMPUS_RUANJIANYUAN:
                return BACKGROUND_BIG_RUANJIANYUAN;
            case CAMPUS_HONGJIALOU:
                return BACKGROUND_BIG_HONGJIALOU;
            case CAMPUS_BAOTUQUAN:
                return BACKGROUND_BIG_BAOTUQUAN;
            case CAMPUS_XINGLONGSHAN:
                return BACKGROUND_BIG_XINGLONGSHAN;
            case CAMPUS_QIANFOSHAN:
                return BACKGROUND_BIG_QIANFOSHAN;
            case CAMPUS_QINGDAO:
                return BACKGROUND_BIG_QINGDAO;
            case CAMPUS_WEIHAI:
                return BACKGROUND_BIG_WEIHAI;
            default:
                return BACKGROUND_BIG_ZHONGXIN;
        }
    }

    public static int getNavMenuCampusIcon(int campusCode) {
        switch (campusCode) {
            case CAMPUS_ZHONGXIN:
                return NAV_MENU_ZHONGXIN;
            case CAMPUS_RUANJIANYUAN:
                return NAV_MENU_RUANJIANYUAN;
            case CAMPUS_HONGJIALOU:
                return NAV_MENU_HONGJIALOU;
            case CAMPUS_BAOTUQUAN:
                return NAV_MENU_BAOTUQUAN;
            case CAMPUS_XINGLONGSHAN:
                return NAV_MENU_XINGLONGSHAN;
            case CAMPUS_QIANFOSHAN:
                return NAV_MENU_QIANFOSHAN;
            case CAMPUS_QINGDAO:
                return NAV_MENU_QINGDAO;
            case CAMPUS_WEIHAI:
                return NAV_MENU_WEIHAI;
            default:
                return NAV_MENU_ZHONGXIN;
        }
    }

    public static int getNavMenuCampusCode(int resId) {
        switch (resId) {
            case NAV_MENU_ZHONGXIN:
                return CAMPUS_ZHONGXIN;
            case NAV_MENU_RUANJIANYUAN:
                return CAMPUS_RUANJIANYUAN;
            case NAV_MENU_HONGJIALOU:
                return CAMPUS_HONGJIALOU;
            case NAV_MENU_BAOTUQUAN:
                return CAMPUS_BAOTUQUAN;
            case NAV_MENU_XINGLONGSHAN:
                return CAMPUS_XINGLONGSHAN;
            case NAV_MENU_QIANFOSHAN:
                return CAMPUS_QIANFOSHAN;
            case NAV_MENU_QINGDAO:
                return CAMPUS_QINGDAO;
            case NAV_MENU_WEIHAI:
                return CAMPUS_WEIHAI;
            default:
                return CAMPUS_ZHONGXIN;
        }
    }

    public int getCampusCode() {
        return campusCode;
    }

    public void setCampusCode(int campusCode) {
        this.campusCode = campusCode;
    }
    /*---------------------------------*/

    public Setting(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void writeToLocalDatabase() {

    }

    @Override
    public void convertJSON(String json) {

    }

    @Override
    public void writeToLocalSharedPref() {
        SharedPreferences.Editor editor = mContext
                .getSharedPreferences("settings", Context.MODE_PRIVATE).edit();
        editor.putInt("campus_code", campusCode);
        editor.apply();
    }

    @Override
    public void readFromLocalDatabase() {

    }

    @Override
    public void readFromLocalSharedPref() {
        SharedPreferences preferences = mContext
                .getSharedPreferences("settings", Context.MODE_PRIVATE);
        campusCode = preferences.getInt("campus_code", -1); //默认校区
    }

    @Override
    public void updateData() {

    }

    @Override
    public void cloneData(Handler handler) {

    }

    public User getCurUser() {
        return new User();
    }
}
