package com.android.rcc.ble.controller;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import com.android.rcc.ble.Command;
import com.android.rcc.ble.Peripheral;
import com.android.rcc.ble.TelinkLog;
import com.android.rcc.util.Arrays;

import java.util.List;
import java.util.UUID;

public class Device extends Peripheral {

    public static final String TAG = Device.class.getSimpleName();


    public static UUID SERVICE_UUID = UUID.fromString("00010203-0405-0607-0809-0a0b0c0d1911");


    public static final UUID CHARACTERISTIC_UUID = UUID.fromString("00010203-0405-0607-0809-0a0b0c0d2b12");


    // 控件发送的单独指令的service_uuid
    public static final UUID SERVICE_UUID_SINGLE_COMMAND = UUID.fromString("0000180A-0000-1000-8000-00805F9B34FB");
    public static final UUID CHARACTERISTIC_UUID_SINGLE_COMMAND = UUID.fromString("00002A26-0000-1000-8000-00805F9B34FB");
    private static final int TAG_OTA_SINGLE_COMMAND = 21;

    // 海文遥控器
    public static final UUID CHARACTERISTIC_UUID_END_HAIWEN = UUID.fromString("00010203-0405-0607-0809-0a0b0c0d2b14");

    // 张久松
    public static final UUID CHARACTERISTIC_UUID_END_JIUSONG = UUID.fromString("00010203-0405-0607-0809-0a0b0c0d2b19");
//    {0x19,0x2B,0x0d,0x0c,0x0b,0x0a,0x09,0x08,0x07,0x06,0x05,0x04,0x03,0x02,0x01,0x00};

    public static final UUID PCM_NOTIFICATION = UUID.fromString("00010203-0405-0607-0809-0A0B0C0D2B18");

//    public static final UUID SERVICE_UUID = UUID.fromString("00002B11-0000-1000-8000-00805F9B34FB");
//    public static final UUID CHARACTERISTIC_UUID = UUID.fromString("00002B12-0000-1000-8000-00805F9B34FB");
//    public static final UUID PCM_NOTIFICATION = UUID.fromString("00010203-0405-0607-0809-0A0B0C0D2B18");

    public static final int OTA_START_REQ = 0xFF03;
    public static final int OTA_START_RSP = 0xFF04;
    public static final int OTA_START = 0xFF01;
    public static final int OTA_END = 0xFF02;

    public static final int STATE_SUCCESS = 1;
    public static final int STATE_FAILURE = 0;
    public static final int STATE_PROGRESS = 2;

    private static final int TAG_OTA_WRITE = 1;
    private static final int TAG_OTA_READ = 2;
    private static final int TAG_OTA_LAST = 3;
    private static final int TAG_OTA_LAST_READ = 10;
    private static final int TAG_OTA_PRE_READ = 4;
    private static final int TAG_OTA_START_REQ = 5;
    private static final int TAG_OTA_START_RSP = 6;
    private static final int TAG_OTA_START = 7;
    private static final int TAG_OTA_END = 8;
    private static final int TAG_OTA_ENABLE_NOTIFICATION = 9;

    private final OtaPacketParser mOtaParser = new OtaPacketParser();
    private final OtaCommandCallback mOtaCallback = new OtaCommandCallback();

    private Callback mCallback;

    private boolean isReadSupport = true;
    private long delay = 10;

    private boolean isSDK3D2 = false;

    public Device(BluetoothDevice device, byte[] scanRecord, int rssi) {
        super(device, scanRecord, rssi);
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    @Override
    protected void onConnect() {
        super.onConnect();
        if (mCallback != null) {
            mCallback.onConnected(this);
        }
    }

    public void setSDK3D2(boolean SDK3D2) {
        if (SDK3D2) {
            SERVICE_UUID = UUID.fromString("00010203-0405-0607-0809-0a0b0c0d1912");
        } else {
            SERVICE_UUID = UUID.fromString("00010203-0405-0607-0809-0a0b0c0d1911");
        }

    }

    @Override
    public void connect(Context context) {
        super.connect(context);
//        isReadSupport = SharedPreferencesHelper.getReadSupport(context);
//        delay = SharedPreferencesHelper.getPktDelay(context);
    }


    @Override
    protected void onDisconnect() {
        super.onDisconnect();
        resetOta();
        if (mCallback != null) {
            mCallback.onDisconnected(this);
        }
    }

    @Override
    protected void onServicesDiscovered(List<BluetoothGattService> services) {
        super.onServicesDiscovered(services);
        //this.enablePcmNotification();
        if (mCallback != null) {
            mCallback.onServicesDiscovered(this);
        }
    }

    @Override
    protected void onNotify(byte[] data, UUID serviceUUID, UUID characteristicUUID, Object tag) {
        super.onNotify(data, serviceUUID, characteristicUUID, tag);
        Log.d(TAG, " onNotify ==> " + Arrays.bytesToHexString(data, ":"));
    }


    protected void onOtaSuccess() {
        if (mCallback != null) {
            mCallback.onOtaStateChanged(this, STATE_SUCCESS);
        }
    }

    protected void onOtaFailure() {
        if (mCallback != null) {
            mCallback.onOtaStateChanged(this, STATE_FAILURE);
        }
    }

    protected void onOtaProgress() {
        if (mCallback != null) {
            mCallback.onOtaStateChanged(this, STATE_PROGRESS);
        }
    }

    /********************************************************************************
     * OTA API
     *******************************************************************************/

    public void startOta(byte[] fileName) {
        TelinkLog.d("Start OTA");
        this.resetOta();
        this.mOtaParser.set(fileName);
        //this.enableOtaNotification();
        this.sendOtaStartCommand();
    }


    public int getOtaProgress() {
        return this.mOtaParser.getProgress();
    }

    private void resetOta() {
        this.mDelayHandler.removeCallbacksAndMessages(null);
        this.mOtaParser.clear();
    }

    private void setOtaProgressChanged() {

        if (this.mOtaParser.invalidateProgress()) {
            onOtaProgress();
        }
    }

    private void sendOtaStartReqCommand() {
        Command reqCmd = Command.newInstance();
        reqCmd.serviceUUID = SERVICE_UUID;
        reqCmd.characteristicUUID = CHARACTERISTIC_UUID;
        reqCmd.type = Command.CommandType.WRITE_NO_RESPONSE;
        reqCmd.tag = TAG_OTA_START_REQ;
        reqCmd.data = new byte[]{OTA_START_REQ & 0xFF, (byte) (OTA_START_REQ >> 8 & 0xFF)};
        sendCommand(mOtaCallback, reqCmd);
    }

    // OTA 开始时发送的命令
    private void sendOtaStartCommand() {
        Command startCmd = Command.newInstance();
        startCmd.serviceUUID = SERVICE_UUID;
        startCmd.characteristicUUID = CHARACTERISTIC_UUID;
        startCmd.type = Command.CommandType.WRITE_NO_RESPONSE;
        startCmd.tag = TAG_OTA_START;
        startCmd.data = new byte[]{OTA_START & 0xFF, (byte) (OTA_START >> 8 & 0xFF)};
        sendCommand(mOtaCallback, startCmd);
    }

    private void sendOtaEndCommand() {
        Command endCmd = Command.newInstance();
        endCmd.serviceUUID = SERVICE_UUID;
        endCmd.characteristicUUID = CHARACTERISTIC_UUID;
        endCmd.type = Command.CommandType.WRITE_NO_RESPONSE;
        endCmd.tag = TAG_OTA_END;
        endCmd.data = new byte[]{OTA_END & 0xFF, (byte) (OTA_END >> 8 & 0xFF)};

        sendCommand(mOtaCallback, endCmd);
    }

    private void sendLastReadCommand() {
        Command cmd = Command.newInstance();
        cmd.serviceUUID = SERVICE_UUID;
        cmd.characteristicUUID = CHARACTERISTIC_UUID;
        cmd.type = Command.CommandType.READ;
        cmd.tag = TAG_OTA_LAST_READ;
        this.sendCommand(mOtaCallback, cmd);
    }

    /*private void sendOtaEndCommand() {
        Command endCmd = Command.newInstance();
        endCmd.serviceUUID = SERVICE_UUID;
        endCmd.characteristicUUID = Constant.curUser == Constant.User.Haiwen ? CHARACTERISTIC_UUID_END_HAIWEN : CHARACTERISTIC_UUID_END_JIUSONG;
        endCmd.type = Command.CommandType.WRITE_NO_RESPONSE;
        endCmd.tag = TAG_OTA_END;
        endCmd.data = new byte[]{0x03};
        endCmd.delay = 1000;
        sendCommand(mOtaCallback, endCmd);
    }*/

    private boolean sendNextOtaPacketCommand() {
        boolean result = false;

        if (this.mOtaParser.hasNextPacket()) {
            Command cmd = Command.newInstance();
            cmd.serviceUUID = SERVICE_UUID;
            cmd.characteristicUUID = CHARACTERISTIC_UUID;
            cmd.type = Command.CommandType.WRITE_NO_RESPONSE;
            cmd.data = this.mOtaParser.getNextPacket();
            cmd.tag = TAG_OTA_WRITE;
            if (this.mOtaParser.isLast()) {
                TelinkLog.d("ota last packet");
                result = true;
                //cmd.tag = TAG_OTA_LAST;
                Command end = Command.newInstance();
                end.serviceUUID = SERVICE_UUID;
                end.characteristicUUID = CHARACTERISTIC_UUID;
                end.type = Command.CommandType.WRITE_NO_RESPONSE;
                end.tag = TAG_OTA_LAST;
                end.delay = 0;
                byte[] endPacket = new byte[6];
                endPacket[0] = 0x02;
                endPacket[1] = (byte) 0xFF;
                endPacket[2] = cmd.data[0];
                endPacket[3] = cmd.data[1];
                endPacket[4] = (byte) (0xFF - cmd.data[0]);
                endPacket[5] = (byte) (0xFF - cmd.data[1]);
                end.data = endPacket;
                this.sendCommand(this.mOtaCallback, cmd);
                this.sendCommand(this.mOtaCallback, end);

            } else {
                this.sendCommand(this.mOtaCallback, cmd);
            }
        }

        return result;
    }


    ///////////////////////////////////////////////////////////////////////////
    // 新的逻辑
    ///////////////////////////////////////////////////////////////////////////

    private void sendNextOtaPacketCommand(int delay) {
        if (this.mOtaParser.hasNextPacket()) {
            Command cmd = Command.newInstance();
            cmd.serviceUUID = SERVICE_UUID;
            cmd.characteristicUUID = CHARACTERISTIC_UUID;
            cmd.type = Command.CommandType.WRITE_NO_RESPONSE;
            cmd.data = this.mOtaParser.getNextPacket();
            cmd.tag = TAG_OTA_WRITE;
            cmd.delay = delay;
            this.sendCommand(this.mOtaCallback, cmd);
        } else {
//            sendOTALastCommand();
            sendLastReadCommand();
        }
    }

    private void sendOTALastCommand() {
        Command end = Command.newInstance();
        end.serviceUUID = SERVICE_UUID;
        end.characteristicUUID = CHARACTERISTIC_UUID;
        end.type = Command.CommandType.WRITE_NO_RESPONSE;
        end.tag = TAG_OTA_LAST;
        end.delay = 0;
        byte[] endPacket = new byte[6];
        endPacket[0] = 0x02;
        endPacket[1] = (byte) 0xFF;
        int index = mOtaParser.getCurIndex();
        endPacket[2] = (byte) (index & 0xFF);
        endPacket[3] = (byte) (index >> 8 & 0xFF);
        endPacket[4] = (byte) (0xFF - index & 0xFF);
        endPacket[5] = (byte) (0xFF - (index >> 8 & 0xFF));
        end.data = endPacket;
        this.sendCommand(this.mOtaCallback, end);
    }


    private void enableOtaNotification() {
        Command endCmd = Command.newInstance();
        endCmd.serviceUUID = SERVICE_UUID;
        endCmd.characteristicUUID = CHARACTERISTIC_UUID;
        endCmd.type = Command.CommandType.ENABLE_NOTIFY;
        endCmd.tag = TAG_OTA_ENABLE_NOTIFICATION;
        sendCommand(mOtaCallback, endCmd);
    }

    private boolean validateOta_3D2() {
        /**
         * 发送read指令
         */
        int sectionSize = 16 * 4;
        int sendTotal = this.mOtaParser.getNextPacketIndex() * 16;
        TelinkLog.i("ota onCommandSampled byte length : " + sendTotal);
        if (sendTotal > 0 && sendTotal % sectionSize == 0) {

            if (!isReadSupport) {
                return true;
            }
            TelinkLog.i("onCommandSampled ota read packet " + mOtaParser.getNextPacketIndex());
            Command cmd = Command.newInstance();
            cmd.serviceUUID = SERVICE_UUID;
            cmd.characteristicUUID = CHARACTERISTIC_UUID;
            cmd.type = Command.CommandType.READ;
            cmd.tag = TAG_OTA_READ;
            this.sendCommand(mOtaCallback, cmd);
            return true;
        }
        return false;
    }


    ///////////////////////////////////////////////////////////////////////////
    // 新的逻辑
    ///////////////////////////////////////////////////////////////////////////
//
//    private void enableOtaNotification() {
//        Command endCmd = Command.newInstance();
//        endCmd.serviceUUID = SERVICE_UUID;
//        endCmd.characteristicUUID = CHARACTERISTIC_UUID;
//        endCmd.type = Command.CommandType.ENABLE_NOTIFY;
//        endCmd.tag = TAG_OTA_ENABLE_NOTIFICATION;
//        sendCommand(mOtaCallback, endCmd);
//    }

    // 发送单独的指令
    public void sendSingleCommand(Command.Callback callback) {
        Command cmd = Command.newInstance();
        cmd.serviceUUID = SERVICE_UUID_SINGLE_COMMAND;
        cmd.characteristicUUID = CHARACTERISTIC_UUID_SINGLE_COMMAND;
        cmd.type = Command.CommandType.READ;
        cmd.tag = TAG_OTA_SINGLE_COMMAND;
        this.sendCommand(callback, cmd);
    }


    private int readFlag = 0;

    private boolean validateOta() {
        int sectionSize = 256;
        int sendTotal = this.mOtaParser.getNextPacketIndex() * 16;
        TelinkLog.d("ota onCommandSampled byte length : " + sendTotal);
        if (sendTotal > 0 && sendTotal % sectionSize == 0) {
            TelinkLog.d("onCommandSampled ota read packet " + mOtaParser.getNextPacketIndex());

            Command cmd = Command.newInstance();
            cmd.serviceUUID = SERVICE_UUID;
            cmd.characteristicUUID = CHARACTERISTIC_UUID;
            cmd.type = Command.CommandType.READ;
            cmd.tag = TAG_OTA_READ;
            this.sendCommand(mOtaCallback, cmd);
            return true;
        }

        return false;
        /*int sendTotal = this.mOtaParser.getNextPacketIndex() * 16;
        if (sendTotal > 0 && readFlag >= 3){
            readFlag = 0;
            TelinkLog.d("onCommandSampled ota read packet " + mOtaParser.getNextPacketIndex());
            Command cmd = Command.newInstance();
            cmd.serviceUUID = SERVICE_UUID;
            cmd.characteristicUUID = CHARACTERISTIC_UUID;
            cmd.type = Command.CommandType.READ;
            cmd.tag = TAG_OTA_READ;
            this.sendCommand(mOtaCallback, cmd);
            return true;
        }
        readFlag ++;
        return false;*/

    }

    public interface Callback {
        void onConnected(Device device);

        void onDisconnected(Device device);

        void onServicesDiscovered(Device device);

        void onOtaStateChanged(Device device, int state);
    }

    private final class OtaCommandCallback implements Command.Callback {

        @Override
        public void success(Peripheral peripheral, Command command, Object obj) {
            if (command.tag.equals(TAG_OTA_PRE_READ)) {
                TelinkLog.d("read =========> " + Arrays.bytesToHexString((byte[]) obj, "-"));
            } else if (command.tag.equals(TAG_OTA_START)) {

//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//                    gatt.requestConnectionPriority(1);
                if (isSDK3D2) {
                    sendNextOtaPacketCommand(0);
                } else {
                    sendNextOtaPacketCommand();
                }

            } else if (command.tag.equals(TAG_OTA_END)) {

//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//                    gatt.requestConnectionPriority(0);

                // ota success
                resetOta();
                setOtaProgressChanged();
                onOtaSuccess();
            } else if (command.tag.equals(TAG_OTA_LAST)) {
//                sendLastReadCommand();
                sendOtaEndCommand();
                // OTA测试时无需发后面两个指令
                /*resetOta();
                setOtaProgressChanged();
                onOtaSuccess();*/
            } else if (command.tag.equals(TAG_OTA_WRITE)) {
                //int delay = 0;
                //if (delay <= 0) {

                if (isSDK3D2) {
                    if (!validateOta_3D2()) {
                        sendNextOtaPacketCommand(0);
                    } else {
                        if (!isReadSupport) {
                            sendNextOtaPacketCommand((int) delay);
                        }
                    }
                } else {
                    if (!validateOta())
                        sendNextOtaPacketCommand();
               /* } else {
                    mDelayHandler.postDelayed(mOtaTask, delay);
                }*/
                }
                setOtaProgressChanged();
            } else if (command.tag.equals(TAG_OTA_READ)) {
                if (isSDK3D2) {
                    sendNextOtaPacketCommand(0);
                } else
                    sendNextOtaPacketCommand();
            } else if (command.tag.equals(TAG_OTA_LAST_READ)) {
//                sendOtaEndCommand();
                if (isSDK3D2)
                    sendOTALastCommand();
            }
        }

        @Override
        public void error(Peripheral peripheral, Command command, String errorMsg) {
            TelinkLog.d("error packet : " + command.tag + " errorMsg : " + errorMsg);
            if (command.tag.equals(TAG_OTA_END)) {
                // ota success
                resetOta();
                setOtaProgressChanged();
                onOtaSuccess();
            } else {
                resetOta();
                onOtaFailure();
            }
        }

        @Override
        public boolean timeout(Peripheral peripheral, Command command) {
            TelinkLog.d("timeout : " + Arrays.bytesToHexString(command.data, ":"));
            if (command.tag.equals(TAG_OTA_END)) {
                // ota success
                resetOta();
                setOtaProgressChanged();
                onOtaSuccess();
            } else {
                resetOta();
                onOtaFailure();
            }
            return false;
        }
    }

}
