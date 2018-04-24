package com.lab.dxy.bracelet.ble;

import android.text.TextUtils;
import android.util.Log;

import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.model.BleGattCharacter;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.model.BleGattService;
import com.lab.dxy.bracelet.Utils.HexUtil;
import com.lab.dxy.bracelet.Utils.L;
import com.lab.dxy.bracelet.Utils.Utils;
import com.lab.dxy.bracelet.ble.listener.BleDataListener;
import com.lab.dxy.bracelet.ble.listener.BleOnSleepListener;
import com.lab.dxy.bracelet.ble.listener.BleOnStepsListener;
import com.lab.dxy.bracelet.ble.listener.BleRssiListener;
import com.lab.dxy.bracelet.ble.listener.BleSettingFailListener;

import java.util.List;
import java.util.UUID;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;
import static com.inuker.bluetooth.library.Constants.REQUEST_FAILED;
import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;
import static com.lab.dxy.bracelet.Contents.ACTION_BLE_CONNECTED;
import static com.lab.dxy.bracelet.Contents.ACTION_BLE_DISCONNECT;
import static com.lab.dxy.bracelet.MyApplication.bluetoothClient;
import static com.lab.dxy.bracelet.service.BleService.isConnected;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：Jack
 * 创建时间：2017/10/19
 */
public class BleManagerKit {
    public static String MAC = "";
    private byte[] bytes;
    public byte[] data = null;
    public boolean isServiceDiscover = false;


    private final String UUID_RW = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private final String UUID_OTA = "00010203-0405-0607-0809-0a0b0c0d1911";


    private final String UUID_READ_CHAR = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private final String UUID_WRITE_CHAR = "0000ffe2-0000-1000-8000-00805f9b34fb";
    private final String UUID_OTA_CHAR = "00010203-0405-0607-0809-0a0b0c0d2b12";


    public BleManagerKit() {
    }


    public void disConnect() {
        bluetoothClient.disconnect(MAC);
        bluetoothClient.clearRequest(MAC, 0);
        bluetoothClient.refreshCache(MAC);
    }


    public void bleWriteAndRead(final byte[] bytes, final BleDataListener bleDataListener) {
        if (!isConnected) return;
        this.bleDataListener = bleDataListener;
        this.bytes = bytes;
        bluetoothClient.writeNoRsp(MAC, UUID.fromString(UUID_RW), UUID.fromString(UUID_WRITE_CHAR), bytes, code -> {
            if (code == REQUEST_SUCCESS) {
                Log.d(TAG, "写成功");

            } else if (code == REQUEST_FAILED) {
                L.d("写失败：" + code);

                Utils.broadUpdate(ACTION_BLE_DISCONNECT);
//                if (bluetoothClient.getConnectStatus(MAC) == Constants.STATUS_DEVICE_DISCONNECTED) {
//                    Utils.broadUpdate(ACTION_BLE_DISCONNECT);
//                } else {
//                    bleWriteAndRead(bytes, bleDataListener);
//                }
            }
        });
    }

    public void bleWriteNoRead(byte[] bytes, final BleSettingFailListener bleSettingFailListener) {
        if (!isConnected) return;
        bluetoothClient.writeNoRsp(MAC, UUID.fromString(UUID_RW), UUID.fromString(UUID_WRITE_CHAR), bytes, code -> {
            if (code == REQUEST_SUCCESS) {
                Log.d(TAG, "写成功");
                if (bleSettingFailListener != null)
                    bleSettingFailListener.isSuccess(true);
            } else if (code == REQUEST_FAILED) {
                Utils.broadUpdate(ACTION_BLE_DISCONNECT);
//                if (bluetoothClient.getConnectStatus(MAC) == Constants.STATUS_DEVICE_DISCONNECTED) {
//                } else {
//                    bleWriteNoRead(bytes, bleSettingFailListener);
//                }
                Log.d(TAG, "写失败：" + code);
            }
        });
    }

    public void bleRead() {
        bluetoothClient.read(MAC, UUID.fromString(UUID_RW), UUID.fromString(UUID_WRITE_CHAR), (code, data) -> {
            if (code == REQUEST_SUCCESS) {
                L.d("读成功" + HexUtil.encodeHexStr(data));
            }
        });
    }


    public void bleCloseNotify() {
        bluetoothClient.unnotify(MAC, UUID.fromString(UUID_RW), UUID.fromString(UUID_READ_CHAR), code -> {
            if (code == Constants.REQUEST_SUCCESS) {
                Log.d(TAG, "关闭Notify成功");
            } else {
                Log.d(TAG, "关闭Notify失败");
            }
        });
    }

    public void readRssi(final BleRssiListener bleRssiListener) {
        bluetoothClient.readRssi(MAC, (code, data) -> {
            if (code == REQUEST_SUCCESS) {
                bleRssiListener.rssiValue(data.intValue());
            } else if (code == REQUEST_FAILED) {
                bleRssiListener.fail();
            }
        });
    }

    private BleDataListener bleDataListener = null;

    private BleOnSleepListener bleonSleepListener = null;
    private BleOnStepsListener bleOnStepsListener = null;

    public void setBleDataListener(BleOnSleepListener bleonSleepListener) {
        this.bleonSleepListener = bleonSleepListener;
    }

    public void setBleOnStepsListener(BleOnStepsListener bleOnStepsListener) {
        this.bleOnStepsListener = bleOnStepsListener;
    }

    public void bleOpenNotify() {
        bluetoothClient.notify(MAC, UUID.fromString(UUID_RW), UUID.fromString(UUID_READ_CHAR), new BleNotifyResponse() {
            @Override
            public void onNotify(UUID service, UUID character, byte[] value) {
                Log.d(TAG, "notify成功:" + HexUtil.encodeHexStr(value));
                if (bleDataListener != null) {
                    if (bytes[1] == value[1])
                        bleDataListener.success(value);
                }
                if (bleonSleepListener != null)
                    bleonSleepListener.onReceiveListener(value);
                if (bleOnStepsListener != null)
                    bleOnStepsListener.onReceiveListener(value);
            }

            @Override
            public void onResponse(int code) {
                if (code == Constants.REQUEST_SUCCESS) {
                    isConnected = true;
                    Log.d(TAG, "打开Notify成功");
                    Utils.broadUpdate(ACTION_BLE_CONNECTED);
                } else {
//                    bluetoothClient.clearRequest(MAC, 0);
//                    bleOpenNotify();
                    Log.d(TAG, "Notify打开失败");
                    Utils.broadUpdate(ACTION_BLE_DISCONNECT);
                    disConnect();
                }
            }
        });
    }

    public synchronized void connectDevice() {
        if (isConnected) return;
        if (TextUtils.isEmpty(MAC)) return;
        L.d("开始连接：");
        bleCloseNotify();
        disConnect();
        BleConnectOptions build = new BleConnectOptions.Builder()
                .setConnectRetry(5)//连接次数
                .setConnectTimeout(1000)//连接超时时间
                .setServiceDiscoverRetry(3)//发现服务失败次数
                .setServiceDiscoverTimeout(20000).build();//发现服务超时时间

        bluetoothClient.connect(MAC, build, (code, data) -> {
            if (code == Constants.REQUEST_SUCCESS) {
                Log.d(TAG, "connect");
//                    checkService(data);
                bleOpenNotify();
            } else {
                Log.d(TAG, "fail");
            }
        });

    }


    public BleGattCharacter writeCharacteristic;
    public BleGattCharacter readCharacteristic;
    //    public BluetoothGattCharacteristic ANCSCharacteristic;
//    public BluetoothGattCharacteristic runningCharacteristic;
    public BleGattCharacter otaCharacteristic;
//    public BluetoothGattCharacteristic sleepCharacteristic;


    private void checkService(BleGattProfile data) {
        List<BleGattService> gattServices = data.getServices();
        for (BleGattService gattService : gattServices) {
            if (gattService.getUUID().toString().equals(UUID_RW)) {
                List<BleGattCharacter> gattRunnumChars = gattService.getCharacters();

                for (BleGattCharacter gattRunnumChar : gattRunnumChars) {
                    if (gattRunnumChar.getUuid().toString().equals(UUID_READ_CHAR)) {
                        readCharacteristic = gattRunnumChar;
                    } else if (gattRunnumChar.getUuid().toString().equals(UUID_WRITE_CHAR)) {
                        writeCharacteristic = gattRunnumChar;
                    }
                }
            } else if (gattService.getUUID().toString().equals(UUID_OTA)) {
                List<BleGattCharacter> gattRunnumChars = gattService.getCharacters();
                for (BleGattCharacter gattRunnumChar : gattRunnumChars) {
                    if (gattRunnumChar.getUuid().toString().equals(UUID_OTA_CHAR)) {
                        otaCharacteristic = gattRunnumChar;
                    }
                }
            }
        }
        if ((readCharacteristic != null) && (writeCharacteristic != null) && (otaCharacteristic != null)) {
            isServiceDiscover = true;
        }
    }
}
