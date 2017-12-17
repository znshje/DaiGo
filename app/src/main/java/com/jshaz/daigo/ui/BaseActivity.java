package com.jshaz.daigo.ui;

import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

/**
 * Created by jshaz on 2017/11/20.
 */

public class BaseActivity extends AppCompatActivity {
    //是否处于Paused状态
    private boolean isPaused = false;

    private boolean isSlideExit = true;

    private boolean isDoubleBackExit = false;

    private long mExitTime;

    /**
     * 左滑退出模块
     */
    float x1 = 0;
    float x2 = 0;
    float y1 = 0;
    float y2 = 0;

    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPaused = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isSlideExit){
            //继承了Activity的onTouchEvent方法，直接监听点击事件
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                //当手指按下的时候
                x1 = event.getX();
                y1 = event.getY();
            }
            if(event.getAction() == MotionEvent.ACTION_UP) {
                //当手指离开的时候
                x2 = event.getX();
                y2 = event.getY();
                if(x2 - x1 > 50 && Math.abs(y1 - y2) < 160) {
                    //向右滑
                    finish();
                }
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        /*按下了返回键*/
        if (isDoubleBackExit) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if ((System.currentTimeMillis() - mExitTime) > 3000) {
                    Toast.makeText(this, "再按一次返回键退出程序", Toast.LENGTH_SHORT).show();
                    mExitTime = System.currentTimeMillis();
                } else {
                    this.finish();
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void setSlideExit(boolean isSlideExit) {
        this.isSlideExit = isSlideExit;
    }

    public void setDoubleBackExit(boolean b) {
        isDoubleBackExit = b;
    }

    public boolean isPaused() {
        return isPaused;
    }

}
