package com.lab.dxy.bracelet.ui.recyclerview;

/**
 * Created by Oden on 2016/6/18.
 */
public interface OnItemClickListener<T>
{
    void onItemClick(ViewHolder viewHolder, T t, int position);
}