package com.syd.oden.odenble.BleCallback;

/**
 * 项目名称：OdenDemo
 * 类描述：
 * 创建人：oden
 * 创建时间：2016/6/12 20:49
 */
public abstract class BleRssiCallback extends BleCallback {
    public abstract void onSuccess(int rssi);
}
