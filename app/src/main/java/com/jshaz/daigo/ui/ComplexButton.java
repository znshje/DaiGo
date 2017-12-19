package com.jshaz.daigo.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jshaz.daigo.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by jshaz on 2017/11/30.
 */

public class ComplexButton extends LinearLayout {

    private RelativeLayout button;
    private ImageView rightArrow;
    private CircleImageView circleImageView;
    private TextView itemName;
    private TextView detail;
    private ImageView redDot;

    public static final int TYPE_TEXT_ONLY = 0;
    public static final int TYPE_IMAGE_ROUND = 1;

    public ComplexButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.complex_button, this);

        button = (RelativeLayout) findViewById(R.id.complex_button_body);
        rightArrow = (ImageView) findViewById(R.id.complex_button_item_right_arrow);
        circleImageView = (CircleImageView) findViewById(R.id.complex_button_item_image);
        itemName = (TextView) findViewById(R.id.complex_button_item_name);
        detail = (TextView) findViewById(R.id.complex_button_item_detail);
        redDot = (ImageView) findViewById(R.id.complex_button_red_dot);

        redDot.setVisibility(GONE);

        setButtonClickable(true);
        button.setLongClickable(true);
        button.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return true;
            }
        });

        selectType(TYPE_IMAGE_ROUND);
    }

    /**
     * 设置按钮可点击性
     * @param clickable
     */
    public void setButtonClickable(boolean clickable) {
        button.setClickable(clickable);
    }

    /**
     * 设置按钮监听器
     * @param onClickListener
     */
    public void setButtonOnClickListener(OnClickListener onClickListener) {
        button.setOnClickListener(onClickListener);
    }

    /**
     * 选择按钮类型
     * @param type
     */
    public void selectType(int type) {
        switch (type) {
            case TYPE_TEXT_ONLY:
                rightArrow.setVisibility(VISIBLE);
                detail.setVisibility(VISIBLE);
                circleImageView.setVisibility(GONE);
                break;
            case TYPE_IMAGE_ROUND:
                rightArrow.setVisibility(INVISIBLE);
                detail.setVisibility(GONE);
                circleImageView.setVisibility(VISIBLE);
                break;
        }
    }

    public void setItemName(String itemName) {
        this.itemName.setText(itemName);
    }

    public void setDetail(String detail) {
        this.detail.setText(detail);
    }

    public void setImageBitmap(Bitmap bitmap) {
        circleImageView.setImageBitmap(bitmap);
    }

    public void setImageOnClickListener(OnClickListener onClickListener) {
        circleImageView.setOnClickListener(onClickListener);
    }

    public void setRedDot(boolean b) {
        if (b) {
            redDot.setVisibility(VISIBLE);
        } else {
            redDot.setVisibility(GONE);
        }
    }
}
