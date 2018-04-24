package com.lab.dxy.bracelet.ui.recyclerview.cardstack;

public interface RxScrollDelegate {

    void scrollViewTo(int x, int y);
    void setViewScrollY(int y);
    void setViewScrollX(int x);
    int getViewScrollY();
    int getViewScrollX();

}
