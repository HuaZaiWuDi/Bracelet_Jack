package com.syd.oden.odenble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.syd.oden.odenble.BleCallback.PeriodScanCallback;
import com.syd.oden.odenble.BleCallback.ScanCallback;
import com.syd.oden.odenble.Utils.BleLog;
import com.syd.oden.odenble.Utils.BleToast;
import com.syd.oden.odenble.Utils.HexUtil;
import com.syd.oden.odenble.exception.BleException;
import com.syd.oden.odenble.exception.ConnectException;
import com.syd.oden.odenble.listener.BleConnectedReadRssi;
import com.syd.oden.odenble.listener.BleDisconnectListener;
import com.syd.oden.odenble.listener.BleServicesListener;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 项目名称：OdenDemo
 * 类描述：
 * 创建人：oden
 * 创建时间：2016/6/8 15:26
 */
public class LiteBle {

    private Context mContext;
    public static final String ACTION_BLE_DISCONNECT = "com.bracelet.ACTION_BLE_DISCONNECT";
    public static final String EXTRA_BLE_DISCONNECT = "com.bracelet.EXTRA_BLE_DISCONNECT";
    public static final String ACTION_SERVICES_DISCOVERED_FAIL = "com.BleService.ACTION_SERVICES_DISCOVERED_FAIL";
    public static final int DEFAULT_SCAN_TIME = 10000;
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_SCANNING = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_SERVICES_DISCOVERED = 4;
    private final int retryPeriod = 3000;
    private int retryDiscoverServiceCount = 0;

    private int connectionState = STATE_DISCONNECTED;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private Set<BluetoothGattCallback> callbackList = new LinkedHashSet<BluetoothGattCallback>();
    public BleServicesListener mBleServicesListener;
    public BleConnectedReadRssi mBleConnectedReadRssi;

    public String mDeviceAddress = "null";
    public String mDeviceName = "null";
    private BleLog bleLog = new BleLog("[LiteBle] ");
    private Handler handler;
    public BleDisconnectListener bleDisconnectListener;


    public LiteBle(Context context) {
        this.mContext = context;
        initialize();
        handler = new Handler();
    }

    public LiteBleConnector newBleConnector() {
        return new LiteBleConnector(this);
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            BleToast.showShort(mContext, "ble not supported");
            return false;
        }
        // For API level 18 and above, get a reference to BluetoothAdapter through BluetoothManager
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                bleLog.e("Unable to initialize BluetoothManager!");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            bleLog.e("Unable to obtain a BluetoothAdapte!");
            return false;
        }

        enableBluetoothIfDisabled();
        bleLog.d("Initialize Successful!");
        return true;
    }

    public void enableBluetoothIfDisabled() {
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
    }

    public void closeBluetooth() {
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
        }
    }


    //-------------SCAN FOR DEVICE--------------------------------------

    public void startBleScan(PeriodScanCallback callback) {
        callback.setLiteBle(this).notifyScanStarted();   //设置超时停止扫描
        boolean suc = mBluetoothAdapter.startLeScan(callback);
        connectionState = STATE_SCANNING;
        if (suc) {
            connectionState = STATE_SCANNING;
        } else {
            bleLog.e("StarScan Fail!");
        }
    }

    public void startBleScan(ScanCallback callback) {
        callback.setLiteBle(this).notifyScanStarted();   //设置超时停止扫描
        mBluetoothAdapter.startLeScan(callback);
    }

    public void stopBleScan(BluetoothAdapter.LeScanCallback callback) {
        if (callback instanceof PeriodScanCallback) {
            ((PeriodScanCallback) callback).removeHandlerMsg();
        } else if (callback instanceof ScanCallback) {
            ((ScanCallback) callback).removeHandlerMsg();
        }
        mBluetoothAdapter.stopLeScan(callback);
        if (connectionState == STATE_SCANNING) {
            connectionState = STATE_DISCONNECTED;
        }
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     * <p/>
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(String bleName, String bleAddr, BleServicesListener l) {
        mDeviceName = bleName;
        mDeviceAddress = bleAddr;
        if (mBluetoothAdapter == null || mDeviceAddress == null) {
            bleLog.w(mDeviceName + ": BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
        if (device == null) {
            bleLog.w(mDeviceName + ": Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        bleLog.d("Trying to create a new connection.");
        closeBluetoothGatt();
        mBluetoothGatt = device.connectGatt(mContext, false, coreGattCallback);
        bleLog.setBleTag(mDeviceName + ": " + mDeviceAddress + "--->");
        mBleServicesListener = l;


//        CONNECTION_PRIORITY_BALANCED：0//默认的值，ble建议的设备连接参数
//
//        CONNECTION_PRIORITY_HIGH：1//连接快的值，当需要跟设备进行大的数据传输时设置该值，当传输完成后需要设置为CONNECTION_PRIORITY_BALANCED（如固件升级时可设置为该值）
//
//        CONNECTION_PRIORITY_LOW_POWER：2.//低功耗值

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mBluetoothGatt.requestConnectionPriority(0);

        return true;
    }

    private LiteBleGattCallback coreGattCallback = new LiteBleGattCallback() {
        @Override
        public void onConnectSuccess(BluetoothGatt gatt, int status) {
            bleLog.d("onConnectSuccess.");
//            mBluetoothGatt = gatt;
//            retryDiscoverServiceCount = 0;
            if (mBluetoothGatt == null) return;

            mBluetoothGatt.discoverServices();
//            handler.postDelayed(retryDiscoverServices, retryPeriod);
            bleLog.d("Attempting to start service discovery");
            for (BluetoothGattCallback call : callbackList) {
                if (call == null) {
                    callbackList.remove(call);
                } else {
                    if (call instanceof LiteBleGattCallback) {
                        ((LiteBleGattCallback) call).onConnectSuccess(gatt, status);
                    }
                }
            }
        }

        @Override
        public void onConnectFailure(BleException exception) {
            bleLog.e("onConnectFailure.");
//            mBluetoothGatt = null;

//            braodUpdate(mContext, ACTION_BLE_DISCONNECT);

            if (bleDisconnectListener != null) {
                bleDisconnectListener.onBleDisconnect(false);
            }
            for (BluetoothGattCallback call : callbackList) {
                if (call == null) {
                    callbackList.remove(call);
                } else {
                    if (call instanceof LiteBleGattCallback) {
                        ((LiteBleGattCallback) call).onConnectFailure(exception);
                    }
                }
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            bleLog.d("onConnectionStateChange  status: " + status
                    + " ,newState: " + newState + "  ,thread: " + Thread.currentThread().getId());
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                connectionState = STATE_CONNECTED;
                onConnectSuccess(gatt, status);
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                connectionState = STATE_DISCONNECTED;
                onConnectFailure(new ConnectException(gatt, status));
            } else if (newState == BluetoothGatt.STATE_CONNECTING) {
                connectionState = STATE_CONNECTING;
            }
            for (BluetoothGattCallback call : callbackList) {
                if (call == null) {
                    callbackList.remove(call);
                } else {
                    call.onConnectionStateChange(gatt, status, newState);
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            connectionState = STATE_SERVICES_DISCOVERED;
            if (status == BluetoothGatt.GATT_SUCCESS) {
                bleLog.d("onServicesDiscovered success.");
                mBleServicesListener.discoverServices(gatt.getServices());
                for (BluetoothGattCallback call : callbackList) {
                    if (call == null) {
                        callbackList.remove(call);
                    } else {
                        call.onServicesDiscovered(gatt, status);
                    }
                }
            } else {
                bleLog.e("onServicesDiscovered fail." + "gatt:" + gatt + "---status:" + status);
                braodUpdate(mContext, ACTION_SERVICES_DISCOVERED_FAIL);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            bleLog.d("onCharacteristicRead.");
            for (BluetoothGattCallback call : callbackList) {
                if (call == null) {
                    callbackList.remove(call);
                } else {
                    call.onCharacteristicRead(gatt, characteristic, status);
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            bleLog.d("onCharacteristicWrite,callbackList.size(): " + callbackList.size());
            for (BluetoothGattCallback call : callbackList) {
                if (call == null) {
                    callbackList.remove(call);
                } else {
                    call.onCharacteristicWrite(gatt, characteristic, status);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            bleLog.d("onCharacteristicChanged: " + HexUtil.encodeHexStr(characteristic.getValue()));
            try {
                for (BluetoothGattCallback call : callbackList) {
                    if (call == null) {
                        callbackList.remove(call);
                    } else {
                        call.onCharacteristicChanged(gatt, characteristic);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            bleLog.d("onReliableWriteCompleted.");
            for (BluetoothGattCallback call : callbackList) {
                if (call == null) {
                    callbackList.remove(call);
                } else {
                    call.onReliableWriteCompleted(gatt, status);
                }
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
//            bleLog.d("onReadRemoteRssi.");
            for (BluetoothGattCallback call : callbackList) {
                if (call == null) {
                    callbackList.remove(call);
                } else {
                    call.onReadRemoteRssi(gatt, rssi, status);
                }
            }
        }
    };

    Runnable retryDiscoverServices = new Runnable() {
        @Override
        public void run() {
            if (!isConnectingOrConnected() && retryDiscoverServiceCount < 3) {
                bleLog.d("retryDiscoverServices: " + retryDiscoverServiceCount);
                retryDiscoverServiceCount++;
                handler.postDelayed(retryDiscoverServices, retryPeriod);
            }
        }
    };

    /**
     * Clears the device cache. After uploading new hello4 the DFU target will have other services than before.
     */
//    public boolean refreshDeviceCache() {
//        /*
//         * There is a refresh() method in BluetoothGatt class but for now it's hidden. We will call it using reflections.
//		 */
//        try {
//            final Method refresh = BluetoothGatt.class.getMethod("refresh");
//            if (refresh != null) {
//                final boolean success = (Boolean) refresh.invoke(getBluetoothGatt());
//                bleLog.d("Refreshing result: " + success);
//                return success;
//            }
//        } catch (Exception e) {
//            bleLog.e("An exception occured while refreshing device: " + e);
//        }
//        return false;
//    }
    public boolean refreshGattCache(BluetoothGatt gatt) {
        boolean result = false;
        try {
            if (gatt != null) {
                Method refresh = BluetoothGatt.class.getMethod("refresh");
                if (refresh != null) {
                    refresh.setAccessible(true);
                    result = (boolean) refresh.invoke(gatt, new Object[0]);
                }
            }
        } catch (Exception e) {
            bleLog.e(e.fillInStackTrace().toString());
        }

        bleLog.d(String.format("refreshDeviceCache return %b", result));

        return result;
    }


    public boolean refreshDeviceCache() {
        if (mBluetoothGatt != null) {
            try {
                BluetoothGatt localBluetoothGatt = mBluetoothGatt;
                Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
                if (localMethod != null) {
                    boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                    return bool;
                }
            } catch (Exception localException) {
                bleLog.d("An exception occured while refreshing device");
            }
        }
        return false;
    }


    /**
     * disconnect, refresh and close bluetooth gatt.
     */
    public void closeBluetoothGatt() {
        Log.d("", "mBluetoothGatt:" + mBluetoothGatt);
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            refreshDeviceCache();
            connectionState = STATE_DISCONNECTED;
//            handler.removeCallbacks(retryDiscoverServices);
            mBluetoothGatt.close();
            mBluetoothGatt = null;
            bleLog.d("closed BluetoothGatt");
        }
    }


    public void closeGatt() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
        }
    }

    public boolean isInScanning() {
        return connectionState == STATE_SCANNING;
    }

    public boolean isConnectingOrConnected() {
        return connectionState >= STATE_CONNECTING;
    }

    public boolean isConnected() {
        return connectionState >= STATE_CONNECTED;
    }

    public boolean isServiceDiscoered() {
        return connectionState == STATE_SERVICES_DISCOVERED;
    }

    public boolean addGattCallback(BluetoothGattCallback callback) {
        return callbackList.add(callback);
    }

    public boolean addGattCallback(LiteBleGattCallback callback) {
        return callbackList.add(callback);
    }

    public boolean removeGattCallback(BluetoothGattCallback callback) {
        return callbackList.remove(callback);
    }

    public void braodUpdate(Context mContext, final String action) {
        final Intent intent = new Intent(action);
        mContext.sendBroadcast(intent);
    }

    public void braodUpdate(Context mContext, final String action, String extra, int value) {
        final Intent intent = new Intent(action);
        intent.putExtra(extra, value);
        mContext.sendBroadcast(intent);
    }

    public void setBleDisconnectListener(BleDisconnectListener bleDisconnectListener) {
        this.bleDisconnectListener = bleDisconnectListener;
    }

    /**
     * return
     * {@link #STATE_DISCONNECTED}
     * {@link #STATE_SCANNING}
     * {@link #STATE_CONNECTING}
     * {@link #STATE_CONNECTED}
     * {@link #STATE_SERVICES_DISCOVERED}
     */
    public int getConnectionState() {
        return connectionState;
    }

    public BluetoothManager getBluetoothManager() {
        return mBluetoothManager;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public BluetoothGatt getBluetoothGatt() {
        return mBluetoothGatt;
    }

    public String getDeviceAddress() {
        return mDeviceAddress;
    }

    public String getDeviceName() {
        return mDeviceName;
    }
}
