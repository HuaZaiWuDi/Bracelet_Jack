package com.lab.dxy.bracelet.ble.listener;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：oden
 * 创建时间：2017/5/31
 */
public interface BleOnSleepListener {
    void onReceiveListener(byte[] data);
}
