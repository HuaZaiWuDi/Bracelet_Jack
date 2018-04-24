package com.syd.oden.odenble.BleCallback;

import android.bluetooth.BluetoothGattCallback;

import com.syd.oden.odenble.exception.BleException;

/**
 * 项目名称：OdenDemo
 * 类描述：
 * 创建人：oden
 * 创建时间：2016/6/12 16:42
 */
public abstract class BleCallback {
    private BluetoothGattCallback bluetoothGattCallback;

    public BleCallback setBluetoothGattCallback(BluetoothGattCallback bluetoothGattCallback) {
        this.bluetoothGattCallback = bluetoothGattCallback;
        return this;
    }

    public BluetoothGattCallback getBluetoothGattCallback() {
        return bluetoothGattCallback;
    }

    public void onInitiatedSuccess() {
    }

    public abstract void onFailure(BleException exception);
}
