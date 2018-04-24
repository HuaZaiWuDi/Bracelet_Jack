package com.android.rcc;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.rcc.ble.BleUtils;
import com.android.rcc.ble.HidProfileService;
import com.android.rcc.ble.LeBluetooth;
import com.android.rcc.ble.controller.Device;
import com.android.rcc.util.Constant;

import java.util.Set;

/**
 * 随程序启动的service，主要是开启蓝牙扫描和监听蓝牙状态
 */
public class AutoConnectService extends Service {

    private static final String TAG = AutoConnectService.class.getSimpleName();

    private static final String ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";

    private final Binder mBinder = new LocalBinder();
    private final Handler mHandler = new Handler();
    private HidProfileService mHidService;
    private BluetoothDevice mHidDevice;
    private Device mDevice;
    private static AutoConnectService mThis;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            BluetoothDevice mDevice;
            int state;

            Log.d(TAG, "Action ---> " + intent.getAction());

            switch (intent.getAction()) {
                case BluetoothDevice.ACTION_FOUND:
                    mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Log.d(TAG, mDevice.getName() + "--" + mDevice.getAddress());
                    if (filter(mDevice)) {
                        if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                            BleUtils.pair(mDevice, new byte[]{0, 0, 0, 0});
                        } else {
                            hidConnect(mDevice);
                        }
                    }
                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    if (BluetoothAdapter.STATE_ON == state) {
//                        startScan();
                        startLeScan();
                    } else if (BluetoothAdapter.STATE_OFF == state) {
                        mHandler.removeCallbacksAndMessages(null);
                        LeBluetooth.getInstance().stopScan();
                    }
                    break;
                case BluetoothDevice.ACTION_PAIRING_REQUEST:
                    Log.d(TAG, "配对广播");
                    mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Log.d(TAG, "设备 ==> " + mDevice.getName() + "--" + mDevice.getAddress());
                    try {
                        if (filter(mDevice)) {
                            if (BleUtils.pair(mDevice, new byte[]{0, 0, 0, 0})) {
                                Log.d(TAG, "配对成功");
                            } else {
                                Log.d(TAG, "配对失败");
                            }
                        }
                    } catch (Exception e) {
                        Log.d(TAG, e.getMessage());
                    }
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (filter(mDevice)) {
                        int bondState = mDevice.getBondState();
                        if (bondState == BluetoothDevice.BOND_BONDED) {
                            Log.d(TAG, "已配对");
                            hidConnect(mDevice);
                        } else if (bondState == BluetoothDevice.BOND_BONDING) {
                            Log.d(TAG, "正在配对");
                        } else if (bondState == BluetoothDevice.BOND_NONE) {
                            Log.d(TAG, "还未配对");
//                            startScan();
                            startLeScan();
                        }
                    }
                    break;
                case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                    mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.ERROR);
                    switch (state) {
                        case BluetoothAdapter.STATE_CONNECTING:
                            Log.d(TAG, "正在连接：" + mDevice.getName());
                            break;
                        case BluetoothAdapter.STATE_CONNECTED:
                            Log.d(TAG, "已连接：" + mDevice.getName());
                            mHidDevice = mDevice;
                            if (mCallback != null) {
                                if (mDevice.getAddress().equals(mac))
                                    mCallback.onHidConnected(mDevice);
                            }
                            //gattConnect(mHidDevice);
                            break;
                        case BluetoothAdapter.STATE_DISCONNECTING:
                            Log.d(TAG, "正在断开连接：" + mDevice.getName());
                            mHidDevice = null;
                            if (mCallback != null) {
                                if (mDevice.getAddress().equals(mac))
                                    mCallback.onHidConnected(mDevice);
                            }
                            break;
                        case BluetoothAdapter.STATE_DISCONNECTED:
                            Log.d(TAG, "连接已断开：" + mDevice.getName());
                            break;
                    }
                    break;
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public static AutoConnectService Instance() {
        return mThis;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mThis = this;
        Log.d(TAG, "onCreate");
        mHidService = new HidProfileService(getApplicationContext());
        this.setupReceiver();
        if (LeBluetooth.getInstance().isSupport(getApplicationContext())) {
//            this.startScan();
            this.startLeScan();
        } else
            Log.d(TAG, "不支持蓝牙");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        this.unregisterReceiver(this.mReceiver);
        this.mHandler.removeCallbacksAndMessages(null);
        LeBluetooth.getInstance().stopScan();
    }

    /*synchronized private void startScan() {
        BluetoothAdapter mAdapter = LeBluetooth.getInstance().getAdapter(getApplicationContext());
        if (mAdapter == null)
            return;
        if (mAdapter.isDiscovering())
            mAdapter.cancelDiscovery();
        if (isExistsBounded())
            return;
        mAdapter.startDiscovery();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan();
            }
        }, 120 * 1000);
    }*/

    private boolean mIsScanning = false;

    private void startLeScan() {
        Toast.makeText(this, "startScan", Toast.LENGTH_SHORT).show();
        final BluetoothAdapter mAdapter = LeBluetooth.getInstance().getAdapter(getApplicationContext());
        if (mAdapter == null)
            return;
        mAdapter.startLeScan(scanCallback);
        mIsScanning = true;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mIsScanning = false;
                mAdapter.stopLeScan(scanCallback);
            }
        }, 150 * 1000);
    }

    BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.d(TAG, "onLeScan" + device.getAddress() + " " + device.getName());
        }
    };

    private void stopLeScan(){
        BluetoothAdapter mAdapter = LeBluetooth.getInstance().getAdapter(getApplicationContext());
        if (mAdapter == null)
            return;
        if (mIsScanning){
            mAdapter.stopLeScan(scanCallback);
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startLeScan();
            }
        }, 5 * 1000);
    }

    /*synchronized private void stopScan() {
        BluetoothAdapter mAdapter = LeBluetooth.getInstance().getAdapter(getApplicationContext());
        if (mAdapter == null)
            return;
        if (mAdapter.isDiscovering())
            mAdapter.cancelDiscovery();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startScan();
            }
        }, 5 * 1000);
    }*/

    private boolean isExistsBounded() {
        Log.d(TAG, "检查配对列表");
        BluetoothAdapter mAdapter = LeBluetooth.getInstance().getAdapter(getApplicationContext());
        if (mAdapter == null) {
            Log.d(TAG, "不支持蓝牙设备");
            return false;
        }

        Set<BluetoothDevice> bondedList = mAdapter.getBondedDevices();

        if (bondedList == null || bondedList.isEmpty())
            return false;

        for (BluetoothDevice device : bondedList) {
            if (filter(device))
                return true;
        }

        return false;
    }

    public void setMac(String mac) {
        this.mac = mac;
        //this.startScan();
    }

    private String mac;

    public boolean filter(BluetoothDevice device) {

        String name = device.getName();
        if (TextUtils.isEmpty(name))
            return false;
//        String prefix = "CANTVBV";
//        String prefix = "THID";
        String prefix = Constant.curUser == Constant.User.Haiwen ? Constant.Device_Name_Haiwen : Constant.Device_Name_JiuSong;
        return name.toUpperCase().indexOf(prefix) == 0;
        //return device.getAddress().equals(mac);
    }

    private void setupReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(ACTION_PAIRING_REQUEST);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        this.registerReceiver(this.mReceiver, filter);
    }

    private void hidConnect(final BluetoothDevice device) {
        if (filter(device)) {
            mHandler.removeCallbacksAndMessages(null);
            LeBluetooth.getInstance().stopScan();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!mHidService.connect(device)) {
                        Log.d(TAG, "Connect Fail");
                    } else {
//                        startScan();
                        startLeScan();
                    }
                }
            }, 0);
        }
    }

    private void hidDisconnect(final BluetoothDevice device) {
        if (filter(device)) {
            mHidService.disconnect(device);
        }
    }

    private void gattConnect(final BluetoothDevice device) {
        mDevice = new Device(device, null, 0);
        mDevice.connect(getApplicationContext());
    }

    private void gattDisconnect() {
        if (mDevice != null) {
            mDevice.disconnect();
        }
    }

    private Callback mCallback;

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    public interface Callback {
        void onHidConnected(BluetoothDevice device);

        void onHidDisconnected(BluetoothDevice device);
    }

    public class LocalBinder extends Binder {
        public AutoConnectService getService() {
            return AutoConnectService.this;
        }
    }
}
