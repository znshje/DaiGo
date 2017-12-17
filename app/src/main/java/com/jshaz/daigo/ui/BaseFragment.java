package com.jshaz.daigo.ui;

import android.support.v4.app.Fragment;

/**
 * Created by jshaz on 2017/12/17.
 */

public class BaseFragment extends Fragment {

    private boolean isPaused = false;


    @Override
    public void onPause() {
        super.onPause();
        isPaused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        isPaused = false;
    }

    public boolean isPaused() {
        return isPaused;
    }
}
