package com.syd.oden.odenble.BleCallback;

import android.bluetooth.BluetoothAdapter;
import android.os.Handler;
import android.os.Looper;

import com.syd.oden.odenble.LiteBle;

/**
 * 项目名称：OdenDemo
 * 类描述：
 * 创建人：oden
 * 创建时间：2016/6/8 16:56
 */
public abstract class ScanCallback implements BluetoothAdapter.LeScanCallback {
    protected Handler handler = new Handler(Looper.getMainLooper());
    protected LiteBle liteBle;

    public ScanCallback() {
    }

    public void notifyScanStarted() {
        removeHandlerMsg();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                liteBle.stopBleScan(ScanCallback.this);
            }
        }, LiteBle.DEFAULT_SCAN_TIME);
    }

    public void removeHandlerMsg() {
        handler.removeCallbacksAndMessages(null);
    }

    public ScanCallback setLiteBle(LiteBle liteBle) {
        this.liteBle = liteBle;
        return this;
    }
}
