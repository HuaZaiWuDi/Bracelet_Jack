package com.lab.dxy.bracelet.ui.recyclerview;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Oden on 2016/6/18.
 */
public interface OnItemClicksListener<T> {
    void onItemClick(ViewGroup parent, View view, T t, int position);
    boolean onItemLongClick(ViewGroup parent, View view, T t, int position);
}
