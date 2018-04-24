package com.syd.oden.odenble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;

import com.syd.oden.odenble.exception.BleException;

/**
 * 项目名称：OdenDemo
 * 类描述：
 * 创建人：oden
 * 创建时间：2016/6/8 18:21
 */
public abstract class LiteBleGattCallback extends BluetoothGattCallback {

    public abstract void onConnectSuccess(BluetoothGatt gatt, int status);

    public abstract void onConnectFailure(BleException exception);
}
