package com.jshaz.daigo.findpassword;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.jshaz.daigo.R;

/**
 * Created by jshaz on 2017/11/22.
 */

public class FragmentSuccess extends Fragment {

    private Button btnNext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_findsuccess, container, false);
        btnNext = view.findViewById(R.id.findpwd_next3);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
        return view;
    }
}
