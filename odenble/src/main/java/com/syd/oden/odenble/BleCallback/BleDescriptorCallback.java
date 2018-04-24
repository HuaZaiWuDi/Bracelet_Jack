package com.syd.oden.odenble.BleCallback;

import android.bluetooth.BluetoothGattDescriptor;

/**
 * 项目名称：OdenDemo
 * 类描述：
 * 创建人：oden
 * 创建时间：2016/6/12 20:50
 */
public abstract class BleDescriptorCallback extends BleCallback {
    public abstract void onSuccess(BluetoothGattDescriptor descriptor);
}