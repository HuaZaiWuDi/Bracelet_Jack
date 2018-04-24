package com.lab.dxy.bracelet.base;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class MvpFragment<P extends BasePresenter> extends Fragment implements BaseView<P> {
    public P mPresenter;
    private static final String STATE_SAVE_IS_HIDDEN = "STATE_SAVE_IS_HIDDEN";

    @Override
    public void setPresenter(BasePresenter presenter) {
        if (presenter != null)
            mPresenter = (P) presenter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            boolean isSupportHidden = savedInstanceState.getBoolean(STATE_SAVE_IS_HIDDEN);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if (isSupportHidden) {
                ft.hide(this);
            } else {
                ft.show(this);
            }
            ft.commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_SAVE_IS_HIDDEN, isHidden());
    }

}