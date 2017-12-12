package com.jshaz.daigo.ui;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.jshaz.daigo.R;

/**
 * 自定义验证码文本框
 * Created by jshaz on 2017/11/21.
 */

public class VerCodeTextView extends LinearLayout implements TextWatcher, View.OnKeyListener{

    private EditText[] et = new EditText[4];

    private int curFocus = 0; //当前聚焦的文本框

    private int curTotal = 0; //当前总字数


    public VerCodeTextView(Context context) {
        super(context);
    }

    public VerCodeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.vercode_textview,
                this, false);
        et[0] = (EditText) view.findViewById(R.id.vercode_textview_1);
        et[1] = (EditText) view.findViewById(R.id.vercode_textview_2);
        et[2] = (EditText) view.findViewById(R.id.vercode_textview_3);
        et[3] = (EditText) view.findViewById(R.id.vercode_textview_4);

        for(int i = 0; i < 4; i++) {
            et[i].setOnKeyListener(this);
        }

        et[0].requestFocus();

    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//        if (curFocus < 3) {
//            curFocus++;
//        } else {
//            et[curFocus].setText(charSequence.subSequence(0, 1));
//        }
//        et[curFocus].requestFocus();
        setText(curFocus, charSequence.toString());
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {

        if (i == keyEvent.KEYCODE_DEL) {
            int action = keyEvent.getAction();
            if (curTotal != 0 && action == keyEvent.ACTION_DOWN) {
                curTotal--;
                et[curFocus].setText("");
                curFocus = (curFocus - 1 < 0) ? 0 : curFocus - 1;
            }
        }
        return false;
    }

    /**
     * 获取文本框内容
     * @return
     */
    public String getTextString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            builder.append(et[i].getText().toString());
        }
        return builder.toString();
    }

    /**
     * setText
     * @param i 填充的文本框序号
     * @param s 填充的内容
     */
    public void setText(int i, String s) {
        if (i > 3) return;

        et[i].setText(s.charAt(0));

        curFocus = (i + 1) > 3 ? 3 : (i + 1);
        et[curFocus].requestFocus();

        curTotal++;

        setText(i + 1, s.substring(1));
    }
}
