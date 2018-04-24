package com.syd.oden.odenble.BleCallback;

import android.bluetooth.BluetoothAdapter;
import android.os.Handler;
import android.os.Looper;

import com.syd.oden.odenble.LiteBle;

/**
 * 项目名称：OdenDemo
 * 类描述：
 * 创建人：oden
 * 创建时间：2016/6/8 16:11
 */
public abstract class PeriodScanCallback implements BluetoothAdapter.LeScanCallback{
    protected Handler handler = new Handler(Looper.getMainLooper());
    protected long timeoutMillis;
    protected LiteBle liteBle;

    public abstract void onScanTimeout();

    public PeriodScanCallback(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public void notifyScanStarted() {
        if (timeoutMillis > 0) {
            removeHandlerMsg();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    liteBle.stopBleScan(PeriodScanCallback.this);
                    onScanTimeout();
                }
            }, timeoutMillis);
        }
    }

    public void removeHandlerMsg() {
        handler.removeCallbacksAndMessages(null);
    }

    public PeriodScanCallback setLiteBle(LiteBle liteBle) {
        this.liteBle = liteBle;
        return this;
    }
}
