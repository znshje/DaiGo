package com.jshaz.daigo.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.jshaz.daigo.R;


/**
 * 底部导航栏View
 * Created by jshaz on 2017/11/19.
 */

public class NavigationView extends LinearLayout {

    private ImageView btnLeft, btnMiddle, btnRight, redDot;

    public NavigationView(Context context, AttributeSet attr) {

        super(context, attr);

        LayoutInflater.from(context).inflate(R.layout.nav_bar, this);

        btnLeft = (ImageView) findViewById(R.id.nav_button_left);
        btnMiddle = (ImageView) findViewById(R.id.nav_button_middle);
        btnRight = (ImageView) findViewById(R.id.nav_button_right);
        redDot = (ImageView) findViewById(R.id.nav_red_dot);

        btnLeft.setImageResource(R.mipmap.icon_order_1);
        btnRight.setImageResource(R.mipmap.icon_me_0);

        setRedDot(false);

    }

    public void setBtnLeftDown() {
        btnLeft.setImageResource(R.mipmap.icon_order_1);
        btnRight.setImageResource(R.mipmap.icon_me_0);
    }

    public void setBtnRightDown() {
        btnLeft.setImageResource(R.mipmap.icon_order_0);
        btnRight.setImageResource(R.mipmap.icon_me_1);
    }

    public void buttonLeftSetListener(OnClickListener listener) {
        btnLeft.setOnClickListener(listener);
    }

    public void buttonRightSetListener(OnClickListener listener) {
        btnRight.setOnClickListener(listener);
    }

    public void buttonMiddleSetListener(OnClickListener listener) {
        btnMiddle.setOnClickListener(listener);
    }

    public void setButtonMiddleEnabled(boolean b) {
        int state = (b == true) ? VISIBLE : GONE;
        btnMiddle.setVisibility(state);
    }

    public void setAllButtonEnabled(boolean b) {
        btnLeft.setClickable(b);
        btnRight.setClickable(b);
        btnMiddle.setClickable(b);
    }

    public void setRedDot(boolean b) {
        if (b) {
            redDot.setVisibility(VISIBLE);
        } else {
            redDot.setVisibility(GONE);
        }
    }

    public ImageView getBtnLeft() {
        return btnLeft;
    }

    public ImageView getBtnMiddle() {
        return btnMiddle;
    }

    public ImageView getBtnRight() {
        return btnRight;
    }

    public ImageView getRedDot() {
        return redDot;
    }

}
