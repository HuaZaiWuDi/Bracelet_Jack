package com.lab.dxy.bracelet.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.lab.dxy.bracelet.BuildConfig;
import com.lab.dxy.bracelet.MyApplication;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：Jack
 * 创建时间：2017/11/13
 */
public class BaseFragment extends Fragment {

    private static final String STATE_SAVE_IS_HIDDEN = "STATE_SAVE_IS_HIDDEN";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {

            boolean aBoolean = savedInstanceState.getBoolean(STATE_SAVE_IS_HIDDEN);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            if (!aBoolean) {
                transaction.hide(this);
            } else {
                transaction.show(this);
            }
            transaction.commit();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (BuildConfig.USE_CANARY)
            MyApplication.getRefWatcher(getActivity()).watch(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_SAVE_IS_HIDDEN, isHidden());
    }
}
