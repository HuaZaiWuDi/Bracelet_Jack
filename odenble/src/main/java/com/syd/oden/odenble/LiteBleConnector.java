package com.syd.oden.odenble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.Handler;
import android.os.Message;

import com.syd.oden.odenble.BleCallback.BleCallback;
import com.syd.oden.odenble.BleCallback.BleCharactCallback;
import com.syd.oden.odenble.BleCallback.BleRssiCallback;
import com.syd.oden.odenble.Utils.BleLog;
import com.syd.oden.odenble.Utils.HexUtil;
import com.syd.oden.odenble.exception.BleException;
import com.syd.oden.odenble.exception.GattException;
import com.syd.oden.odenble.exception.InitiatedException;
import com.syd.oden.odenble.exception.OtherException;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 项目名称：OdenDemo
 * 类描述：
 * 创建人：oden
 * 创建时间：2016/6/12 16:44
 */
public class LiteBleConnector {

    private static final int MSG_WRIATE_CHA = 1;
    private static final int MSG_WRIATE_DES = 2;
    private static final int MSG_READ_CHA = 3;
    private static final int MSG_READ_DES = 4;
    private static final int MSG_READ_RSSI = 5;
    private static final int MSG_NOTIY_CHA = 6;
    private static final int MSG_NOTIY_DES = 7;

    private BluetoothGatt bluetoothGatt;
    private LiteBle liteBle;
    private int timeOutMillis = 5000;
    //    private Handler handler  = new MyHanlder();
    private BleLog bleLog = new BleLog("[LiteBleConnector] ");

    private class MyHanlder extends Handler {
        @Override
        public void handleMessage(Message msg) {
            bleLog.d("handleMessage TIMEOUT_EXCEPTION: " + msg.what);
            BleCallback call = (BleCallback) msg.obj;
            if (call != null) {
                liteBle.removeGattCallback(call.getBluetoothGattCallback());
                call.onFailure(BleException.TIMEOUT_EXCEPTION);
            }
            msg.obj = null;
        }
    }

    public LiteBleConnector(LiteBle liteBle) {
        this.liteBle = liteBle;
        this.bluetoothGatt = liteBle.getBluetoothGatt();
//        this.handler = new Handler(Looper.getMainLooper());
        bleLog.setBleTag(liteBle.getDeviceName() + ": " + liteBle.getDeviceAddress() + "--->");
    }

    // _____________________ main operation _____________________

    /**
     * write data to specified characteristic
     */
    public boolean writeCharacteristic(byte[] data, BluetoothGattCharacteristic charact,
                                       final BleCharactCallback bleCallback) {
        bleLog.d("characteristic write bytes: "
                + Arrays.toString(data) + " ,hex: " + HexUtil.encodeHexStr(data));
        handleCharacteristicWriteCallback(bleCallback);
        if (bleCallback != null) {
            charact.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        } else {
            charact.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        }
        charact.setValue(data);
        return handleAfterInitialed(getBluetoothGatt().writeCharacteristic(charact), bleCallback);
    }

    /**
     * read data from specified characteristic
     */
    public boolean readCharacteristic(BluetoothGattCharacteristic charact, BleCharactCallback bleCallback) {
        bleLog.d("readCharacteristic");
        if ((charact.getProperties() | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            // If there is an active notification on a characteristic, clear
            // it first so it doesn't update the data field on the user interface.
            setCharacteristicNotification(getBluetoothGatt(), charact, false);
            handleCharacteristicReadCallback(bleCallback);
            return handleAfterInitialed(getBluetoothGatt().readCharacteristic(charact), bleCallback);
        } else {
            if (bleCallback != null) {
                bleCallback.onFailure(new OtherException("Characteristic [is not] readable!"));
            }
            return false;
        }
    }

    /**
     * enable characteristic notification
     */
    public boolean enableCharacteristicNotification(BluetoothGattCharacteristic charact,
                                                    BleCharactCallback bleCallback) {
        bleLog.d("enableCharacteristicNotification");
        if ((charact.getProperties() | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            bleLog.d("enableCharacteristicNotification success");
            handleCharacteristicNotificationCallback(bleCallback);
            return setCharacteristicNotification(getBluetoothGatt(), charact, true);
        } else {
            if (bleCallback != null) {
                bleCallback.onFailure(new OtherException("Characteristic [not supports] readable!"));
            }
            return false;
        }
    }

    public void enableNotify(BluetoothGattCharacteristic charact) {
        setCharacteristicNotification(getBluetoothGatt(), charact, true);
    }

    public void setNotificationForCharacteristic(BluetoothGattCharacteristic paramBluetoothGattCharacteristic, boolean paramBoolean) {
        BluetoothGattDescriptor localBluetoothGattDescriptor;

        localBluetoothGattDescriptor = paramBluetoothGattCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
        if (paramBoolean) {
            localBluetoothGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            getBluetoothGatt().writeDescriptor(localBluetoothGattDescriptor);
        }
    }

    /**
     * read rssi of device
     */
    public boolean readRemoteRssi(BleRssiCallback bleCallback) {
//        bleLog.d("enableCharacteristicNotification");
        handleRSSIReadCallback(bleCallback);
        return handleAfterInitialed(getBluetoothGatt().readRemoteRssi(), bleCallback);
    }

    /**
     * Enable or disable notifications/indications for a given characteristic.
     * <p>
     * <p>Once notifications are enabled for a characteristic, a
     * {@link BluetoothGattCallback#onCharacteristicChanged} callback will be
     * triggered if the remote device indicates that the given characteristic
     * has changed.
     * <p>
     * <p>Requires {@link android.Manifest.permission#BLUETOOTH} permission.
     *
     * @param characteristic The characteristic for which to enable notifications
     * @param enable         Set to true to enable notifications/indications
     * @return true, if the requested notification status was set successfully
     */
    private boolean setCharacteristicNotification(BluetoothGatt gatt,
                                                  BluetoothGattCharacteristic characteristic,
                                                  boolean enable) {
        if (gatt != null && characteristic != null) {
            bleLog.d("Characteristic set notification value: " + enable);
            return gatt.setCharacteristicNotification(characteristic, enable);
        }
        return false;
    }

    /**
     * {@link BleCallback#onInitiatedSuccess} will be called,
     * if the read operation was initiated successfully.
     * Otherwize {@link BleCallback#onFailure} will be called.
     *
     * @return true, if the read operation was initiated successfully
     */
    private boolean handleAfterInitialed(boolean initiated, BleCallback bleCallback) {
        if (bleCallback != null) {
            if (initiated) {
                bleCallback.onInitiatedSuccess();
            } else {
                bleCallback.onFailure(new InitiatedException());
            }
        }
        return initiated;
    }

    // _____________________ handle call back _____________________

    private void handleCharacteristicWriteCallback(final BleCharactCallback bleCallback) {
        if (bleCallback != null) {
            listenAndTimer(bleCallback, MSG_WRIATE_CHA, new BluetoothGattCallback() {
                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt,
                                                  BluetoothGattCharacteristic characteristic, int status) {
                    bleLog.d("handleCharacteristicWriteCallback onCharacteristicWrite");
//                    handler.removeMessages(MSG_WRIATE_CHA, this);
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        bleCallback.onSuccess(characteristic);
                    } else {
                        bleCallback.onFailure(new GattException(status));
                    }
                    liteBle.removeGattCallback(this);
                }
            });
        }
    }

    private void handleCharacteristicReadCallback(final BleCharactCallback bleCallback) {
        if (bleCallback != null) {
            listenAndTimer(bleCallback, MSG_READ_CHA, new BluetoothGattCallback() {
                AtomicBoolean msgRemoved = new AtomicBoolean(false);

                @Override
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic, int status) {
                    if (!msgRemoved.getAndSet(true)) {
//                        handler.removeMessages(MSG_READ_CHA, this);
                    }
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        bleCallback.onSuccess(characteristic);
                    } else {
                        bleCallback.onFailure(new GattException(status));
                    }
                    liteBle.removeGattCallback(this);
                }
            });
        }
    }

    private void handleCharacteristicNotificationCallback(final BleCharactCallback bleCallback) {
        if (bleCallback != null) {
            listenAndTimer(bleCallback, MSG_NOTIY_CHA, new BluetoothGattCallback() {
                AtomicBoolean msgRemoved = new AtomicBoolean(false);

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    if (!msgRemoved.getAndSet(true)) {
//                        handler.removeMessages(MSG_NOTIY_CHA, this);
                    }
                    bleCallback.onSuccess(characteristic);
                }
            });
        }
    }

    private void handleRSSIReadCallback(final BleRssiCallback bleCallback) {
        if (bleCallback != null) {
            listenAndTimer(bleCallback, MSG_READ_RSSI, new BluetoothGattCallback() {
                @Override
                public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
//                    handler.removeMessages(MSG_READ_RSSI, this);
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        bleCallback.onSuccess(rssi);
                    } else {
                        bleCallback.onFailure(new GattException(status));
                    }
                }
            });
        }
    }

    /**
     * listen bluetooth gatt callback, and send a delayed message.
     */
    private void listenAndTimer(final BleCallback bleCallback, int what, BluetoothGattCallback callback) {

        bleCallback.setBluetoothGattCallback(callback);
        liteBle.addGattCallback(callback);
//        Message msg = handler.obtainMessage(what, bleCallback);
//        handler.sendMessageDelayed(msg, timeOutMillis);
    }

    public BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }

    public void setBluetoothGatt(BluetoothGatt bluetoothGatt) {
        this.bluetoothGatt = bluetoothGatt;
    }
}
