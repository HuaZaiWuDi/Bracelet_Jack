package com.lab.dxy.bracelet.ble.listener;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：oden
 * 创建时间：2017/10/19
 */
public interface BleDataListener {

    void success(byte[] data);

//    void fail(String e);
}
