package com.syd.oden.odenble.listener;

import android.bluetooth.BluetoothGattService;

import java.util.List;

/**
 * 项目名称：OdenDemo
 * 类描述：
 * 创建人：oden
 * 创建时间：2016/6/12 19:09
 */
public interface BleServicesListener {
    void discoverServices(List<BluetoothGattService> gattServices);

}
