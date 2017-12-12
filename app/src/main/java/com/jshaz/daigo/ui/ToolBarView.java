package com.jshaz.daigo.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jshaz.daigo.R;
import com.jshaz.daigo.util.Setting;

/**
 * Created by jshaz on 2017/11/19.
 * 标题栏的自定义控件
 */

public class ToolBarView extends RelativeLayout{

    private Button backButton; //返回按钮

    private TextView titleText; //标题文字

    private TextView titleCampus;

    private ImageView rightButton; //右侧按钮

    public ToolBarView(Context context, AttributeSet attr) {

        super(context, attr);

        /**
         * 加载ToolBar布局文件
         */
        LayoutInflater.from(context).inflate(R.layout.tool_bar, this);

        titleText = (TextView) findViewById(R.id.title_bar_text);

        backButton = (Button) findViewById(R.id.title_bar_back);

        rightButton = (ImageView) findViewById(R.id.title_bar_right_button);

        titleCampus = (TextView) findViewById(R.id.title_bar_campus);

        setBackButtonVisible(false);

        setTitleCampusVisible(false);

        rightButton.setEnabled(false);

    }

    /**
     * 设置标题文字
     * @param titleText
     */
    public void setTitleText(String titleText) {

        this.titleText.setText(titleText);

    }

    /**
     * 设置返回按钮的监听事件
     * @param listener
     */
    public void setBackButtonOnClickListener(OnClickListener listener) {

        backButton.setOnClickListener(listener);

    }

    /**
     * 设置左侧按钮背景图像
     * @param resourceId
     */
    public void setBackButtonImage(int resourceId) {

        backButton.setBackgroundResource(resourceId);

    }

    /**
     * 设置右侧按钮背景图像
     * @param resourceId
     */
    public void setRightButtonImage(int resourceId) {

        rightButton.setBackgroundResource(resourceId);

    }

    /**
     * 设置右侧按钮监听事件
     * @param listener
     */
    public void setRightButtonOnClickListener(OnClickListener listener) {

        rightButton.setEnabled(true);

        rightButton.setOnClickListener(listener);


    }

    public void setBackButtonVisible(boolean b) {
        int state;
        if (b) {
            state = VISIBLE;
        } else {
            state = GONE;
        }
        backButton.setVisibility(state);
    }

    public void setTitleCampusVisible(boolean b) {
        int state;
        if (b) {
            state = VISIBLE;
        } else {
            state = GONE;
        }
        titleCampus.setVisibility(state);
    }

    /**
     * 设置标题栏校区名称
     * @param campusCode
     */
    public void setTitleCampus(int campusCode) {
        titleCampus.setText(Setting.getCampusName(campusCode));
    }
}
