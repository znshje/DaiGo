package com.jshaz.daigo.ui;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.jshaz.daigo.R;

/**
 * Created by jshaz on 2017/11/28.
 */

public class PopUtil extends PopupWindow {

    private LinearLayout linearLayout;

    private long mLastTime = -1;

    private boolean isAnimation;
    private Activity activity;
    private int layout;
    private View bgview;
    private View view;

    public PopUtil(Activity activity, int layout, boolean isAnimation) {
        this.activity = activity;
        this.layout = layout;
        this.isAnimation = isAnimation;
        initPop();
    }

    public LinearLayout getPopup() {
        return linearLayout;
    }

    public View getview() {
        return view;
    }

    public void initPop() {
        view = activity.getLayoutInflater().inflate(layout, null);
        //pop = new PopupWindow();
        linearLayout = (LinearLayout) view.findViewById(R.id.clip_pop_layout);

        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setBackgroundDrawable(new BitmapDrawable());
        setFocusable(true);
        setOutsideTouchable(true);
        setContentView(view);
//      setBackgroundDrawable(new ColorDrawable(activity.getResources().getColor(R.color.black)));

        bgview = linearLayout.findViewById(R.id.clip_view);
        bgview.setAlpha(0.5f);
//      消失的时候设置窗体背景变亮
//      setOnDismissListener(new PopupWindow.OnDismissListener() {
//      @Override
//      public void onDismiss() {
//      }
//      });

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (isAnimation){
                    endAnimationPopup(getAnimation());
                }
            }
        });
    }

    public Animation getAnimation() {
        final Animation endAnimation = AnimationUtils.loadAnimation(activity, R.anim.translate_bottom_out);
        endAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                dismiss();

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        return endAnimation;
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
//        backgroundAlpha(0.5f);
//        bgview.setVisibility(View.VISIBLE);

    }

    /**
     * 防止重复点击关闭pop动画
     *
     * @param endAnimation
     */
    public void endAnimationPopup(Animation endAnimation) {
        if (mLastTime == -1) {
            linearLayout.startAnimation(endAnimation);
        } else {
            long currentTimeMillis = System.currentTimeMillis();
            long interval = currentTimeMillis - mLastTime;
            if (interval < 300) {
                return;
            } else {
                linearLayout.startAnimation(endAnimation);
            }
        }
        mLastTime = System.currentTimeMillis();
    }

    @Override
    public void dismiss() {
        super.dismiss();
//        backgroundAlpha(1f);
    }

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha
     */
    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        activity.getWindow().setAttributes(lp);
    }
}


