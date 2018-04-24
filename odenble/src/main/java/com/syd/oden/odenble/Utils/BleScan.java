package com.syd.oden.odenble.Utils;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.syd.oden.odenble.BleCallback.PeriodScanCallback;
import com.syd.oden.odenble.LiteBle;
import com.syd.oden.odenble.listener.BleScanListener;

import java.util.ArrayList;

/**
 * 项目名称：MeshLed
 * 类描述：
 * 创建人：oden
 * 创建时间：2016/6/15 17:18
 */
public class BleScan {
    private BleLog myLog = new BleLog("[BleScan] ");
    private Context context;
    private LiteBle liteBle;
    private BleScanListener bleScanListener;
    private ArrayList<String> addrList = new ArrayList<>();
    private PeriodScanCallback periodScanCallback;
    private long period = 30000;

    public BleScan(Context context) {
        this.context = context;
        liteBle = new LiteBle(context);
    }

    public BleScan(Context context, long period) {
        this.context = context;
        this.period = period;
        liteBle = new LiteBle(context);

        periodScanCallback = new PeriodScanCallback(period) {

            @Override
            public void onScanTimeout() {
                myLog.d("periodScanCallback onScanTimeout");
                bleScanListener.onScanTimeout();
            }

            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                myLog.d("periodScanCallback onLeScan: device.getName(): " + device.getName() + ",device.getAddress(): " + device.getAddress());
//                if (!addrList.contains(device.getAddress())){
//                    addrList.add(device.getAddress());
                    bleScanListener.onBleScanResult(device, rssi, scanRecord);
//                }
            }
        };
    }

    public void setBleScanListener(BleScanListener l) {
        bleScanListener = l;
    }

    public void startScan() {
        myLog.d("startScan");
        if (!liteBle.isInScanning()){
            liteBle.startBleScan(periodScanCallback);
            addrList.clear();
        }
    }

    public void stopScan() {
        myLog.d("stopScan");
        if (liteBle.isInScanning())
            liteBle.stopBleScan(periodScanCallback);
    }

    public boolean isInScanning() {
        return liteBle.isInScanning();
    }


    public LiteBle getLiteBle() {
        return liteBle;
    }

}
