package com.android.rcc.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;

public class HidProfileService implements BluetoothProfile.ServiceListener {

    public static final int INPUT_DEVICE = 4;

    private static final String TAG = HidProfileService.class.getSimpleName();

    private BluetoothAdapter mAdapter;
    private BluetoothProfile mProfile;

    public HidProfileService(Context context) {
        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mAdapter = manager.getAdapter();
        mAdapter.getProfileProxy(context, this, INPUT_DEVICE);
    }

    @Override
    public void onServiceConnected(int profile, BluetoothProfile proxy) {
        this.mProfile = proxy;
        Log.d(TAG, "onServiceConnected");
    }

    @Override
    public void onServiceDisconnected(int profile) {
        Log.d(TAG, "onServiceDisconnected");
    }

    public boolean connect(BluetoothDevice device) {
        try {
            Log.d(TAG, "connect");
            Class<? extends BluetoothProfile> mProfileClass = mProfile.getClass();
            Method connectMethod = mProfileClass.getDeclaredMethod("connect", BluetoothDevice.class);
            return (boolean) connectMethod.invoke(mProfile, device);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage(), e);
        }

        return false;
    }

    public boolean disconnect(BluetoothDevice device) {
        try {
            Log.d(TAG, "disconnect");
            Class<? extends BluetoothProfile> mProfileClass = mProfile.getClass();
            Method connectMethod = mProfileClass.getDeclaredMethod("disconnect", BluetoothDevice.class);
            return (boolean) connectMethod.invoke(mProfile, device);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage(), e);
        }
        return false;
    }

    public static final int PROTOCOL_REPORT_MODE = 0;
    public static final int PROTOCOL_BOOT_MODE = 1;

    public boolean setProtocolMode(BluetoothDevice device, int protocolMode) {
        try {
            Log.d(TAG, "setProtocolMode");
            Class<? extends BluetoothProfile> mProfileClass = mProfile.getClass();
            Method connectMethod = mProfileClass.getDeclaredMethod("setProtocolMode", BluetoothDevice.class, int.class);
            return (boolean) connectMethod.invoke(mProfile, device, protocolMode);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage(), e);
        }
        return false;
    }
    public boolean getProtocolMode(BluetoothDevice device) {
        try {
            Log.d(TAG, "setProtocolMode");
            Class<? extends BluetoothProfile> mProfileClass = mProfile.getClass();
            Method connectMethod = mProfileClass.getDeclaredMethod("getProtocolMode", BluetoothDevice.class);
            return (boolean) connectMethod.invoke(mProfile, device);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage(), e);
        }
        return false;
    }
}
