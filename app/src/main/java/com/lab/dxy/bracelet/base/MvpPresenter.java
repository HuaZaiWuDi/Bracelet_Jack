package com.lab.dxy.bracelet.base;

import android.app.Activity;
import android.content.Context;

public class MvpPresenter <V extends BaseView> {
    public Context context;
    public V mView;
    public Activity mActivity;

    public void setView(Context context, V mView) {
        this.context = context;
        this.mView = mView;
        mView.setPresenter(this);
    }

    public void setView(Context context, Activity activity, V mView) {
        this.context = context;
        this.mView = mView;
        this.mActivity = activity;
        mView.setPresenter(this);
    }


}