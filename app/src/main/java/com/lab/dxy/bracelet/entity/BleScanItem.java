package com.lab.dxy.bracelet.entity;

import android.bluetooth.BluetoothDevice;

import java.util.Arrays;

/**
 * Created by 华 on 2017/5/4.
 */

public class BleScanItem {

    BluetoothDevice device;
    int rssi;
    byte[] scanRecord;


    public BleScanItem(BluetoothDevice device, int rssi, byte[] scanRecord) {
        this.device = device;
        this.rssi = rssi;
        this.scanRecord = scanRecord;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public byte[] getScanRecord() {
        return scanRecord;
    }

    public void setScanRecord(byte[] scanRecord) {
        this.scanRecord = scanRecord;
    }

    @Override
    public String toString() {
        return "BleScanItem{" +
                "device=" + device +
                ", rssi=" + rssi +
                ", scanRecord=" + Arrays.toString(scanRecord) +
                '}';
    }
}
