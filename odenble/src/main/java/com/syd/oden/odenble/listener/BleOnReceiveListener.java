package com.syd.oden.odenble.listener;

/**
 * 项目名称：MeshLed
 * 类描述：
 * 创建人：oden
 * 创建时间：2016/6/23 10:16
 */
public interface BleOnReceiveListener {
    void onReceiveListener(byte[] data);
}
