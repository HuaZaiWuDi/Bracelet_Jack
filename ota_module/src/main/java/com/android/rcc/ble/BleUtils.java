package com.android.rcc.ble;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.lang.reflect.Method;

public final class BleUtils {

    private static final String TAG = BleUtils.class.getSimpleName();

    private BleUtils() {
    }

    /**
     * 与设备配对 参考源码：platform/packages/apps/Settings.git
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
     */
    static public boolean createBond(Class<? extends BluetoothDevice> btClass, BluetoothDevice btDevice)
            throws Exception {
        Method createBondMethod = btClass.getMethod("createBond");
        return (Boolean) createBondMethod.invoke(btDevice);
    }

    /**
     * 与设备解除配对 参考源码：platform/packages/apps/Settings.git
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
     */
    static public boolean removeBond(Class<? extends BluetoothDevice> btClass, BluetoothDevice btDevice)
            throws Exception {
        Method removeBondMethod = btClass.getMethod("removeBond");
        return (Boolean) removeBondMethod.invoke(btDevice);
    }

    static public boolean setPin(Class<? extends BluetoothDevice> btClass, BluetoothDevice btDevice, byte[] pin) throws Exception {
        Method setPingMethod = btClass.getDeclaredMethod("setPin", byte[].class);
        return (Boolean) setPingMethod.invoke(btDevice, new Object[]{pin});
    }

    // 取消用户输入
    static public boolean cancelPairingUserInput(Class<? extends BluetoothDevice> btClass, BluetoothDevice device) throws Exception {
        Method createBondMethod = btClass.getMethod("cancelPairingUserInput");
        return (Boolean) createBondMethod.invoke(device);
    }

    // 取消配对
    static public boolean cancelBondProcess(Class<? extends BluetoothDevice> btClass, BluetoothDevice device) throws Exception {
        Method createBondMethod = btClass.getMethod("cancelBondProcess");
        return (boolean) createBondMethod.invoke(device);
    }

    public static boolean setPairingConfirmation(Class<? extends BluetoothDevice> btClass, BluetoothDevice device, boolean confirm) throws Exception {
        Method confirmMethod = btClass.getDeclaredMethod("setPairingConfirmation", boolean.class);
        return (Boolean) confirmMethod.invoke(device, confirm);
    }

    public static boolean pair(BluetoothDevice device, byte[] pin) {
        boolean result = false;

        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
            try {
                Log.d(TAG, "NOT BOND_BONDED");
                BleUtils.setPin(device.getClass(), device, pin);
                BleUtils.setPairingConfirmation(device.getClass(), device, true);
                BleUtils.createBond(device.getClass(), device);
                result = true;
            } catch (Exception e) {
                Log.d(TAG, "setPiN failed!");
                e.printStackTrace();
            }

        } else {
            Log.d(TAG, "HAS BOND_BONDED");
            try {
                BleUtils.removeBond(device.getClass(), device);
                BleUtils.setPin(device.getClass(), device, pin);
                BleUtils.setPairingConfirmation(device.getClass(), device, true);
                BleUtils.createBond(device.getClass(), device);
                BleUtils.cancelPairingUserInput(device.getClass(), device);
                result = true;
            } catch (Exception e) {
                Log.d(TAG, "setPiN failed!");
                e.printStackTrace();
            }
        }

        return result;
    }
}
