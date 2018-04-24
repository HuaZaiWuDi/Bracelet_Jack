package com.lab.dxy.bracelet.ui.recyclerview;

/**
 * Created by Oden on 2016/6/19.
 */
public interface MultiItemTypeSupport<T>
{
    int getLayoutId(int itemType);

    int getItemViewType(int position, T t);
}
