package com.syd.oden.odenble.listener;

/**
 * 项目名称：MeshLed
 * 类描述：
 * 创建人：oden
 * 创建时间：2016/6/15 15:31
 */
public interface BleReadCallback {
    void onReadSuccess(byte[] value);
}
