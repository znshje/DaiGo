package com.jshaz.daigo.ui;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.jshaz.daigo.R;
import com.jshaz.daigo.util.Setting;

/**
 * 自定义CardView
 * Created by jshaz on 2017/11/25.
 */

public class ExtendedCardView extends CardView {

    private ImageView campusBg;

    private TextView campusName;

    public ExtendedCardView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(getContext()).inflate(R.layout.extended_cardview, this);

        campusBg = (ImageView) findViewById(R.id.extended_card_view_campus_bg);
        campusName = (TextView) findViewById(R.id.extended_card_view_campus_name);

        this.setRadius(8.0f);
        this.setCardElevation(20.0f);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.setCardElevation(5.0f);
                break;
            case MotionEvent.ACTION_UP:
                this.setCardElevation(20.0f);
                break;
            case MotionEvent.ACTION_CANCEL:
                this.setCardElevation(20.0f);
                break;
        }
        return super.onTouchEvent(event);
    }

    public void setCampusBg(int campusCode) {
//        Glide.with(getContext()).load(Setting.getBackgroundId(campusCode)).into(campusBg);
        campusBg.setImageResource(Setting.getBackgroundId(campusCode));
    }

    public void setCampusName(int campusCode) {
        campusName.setText(Setting.getCampusName(campusCode));
    }
}
