package com.syd.oden.odenble.listener;

import android.bluetooth.BluetoothDevice;

/**
 * 项目名称：MeshLed
 * 类描述：
 * 创建人：oden
 * 创建时间：2016/6/14 11:10
 */
public interface BleScanListener {
    void onScanTimeout();
    void onBleScanResult(BluetoothDevice device, int rssi, byte[] scanRecord);
}
