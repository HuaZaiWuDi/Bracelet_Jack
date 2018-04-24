package com.syd.oden.odenble.BleCallback;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 * 项目名称：OdenDemo
 * 类描述：
 * 创建人：oden
 * 创建时间：2016/6/12 16:43
 */
public abstract class BleCharactCallback extends BleCallback {
    public abstract void onSuccess(BluetoothGattCharacteristic characteristic);
}
