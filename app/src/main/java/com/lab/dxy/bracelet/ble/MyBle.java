package com.lab.dxy.bracelet.ble;

import android.os.Handler;
import android.text.TextUtils;

import com.lab.dxy.bracelet.Contents;
import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.Utils.ByteUtil;
import com.lab.dxy.bracelet.Utils.L;
import com.lab.dxy.bracelet.ble.listener.BleBindListener;
import com.lab.dxy.bracelet.ble.listener.BleCommandListener;
import com.lab.dxy.bracelet.ble.listener.BleDataListener;
import com.lab.dxy.bracelet.ble.listener.BleOnSleepListener;
import com.lab.dxy.bracelet.ble.listener.BleSettingFailListener;
import com.lab.dxy.bracelet.core.BleLongPackageThread;
import com.lab.dxy.bracelet.core.SynDataThread;
import com.lab.dxy.bracelet.entity.spl.UserAlarmTab;
import com.lab.dxy.bracelet.ui.RxToast;
import com.syd.oden.odenble.Utils.BleLog;
import com.syd.oden.odenble.Utils.HexUtil;
import com.syd.oden.odenble.listener.BleOnReceiveListener;

import static com.lab.dxy.bracelet.Contents.BRA_DEVICE_DXY;
import static com.lab.dxy.bracelet.Contents.BRA_DEVICE_JQ;
import static com.lab.dxy.bracelet.Contents.BRA_DEVICE_MST;
import static com.lab.dxy.bracelet.Contents.BRA_DEVICE_YL;
import static com.lab.dxy.bracelet.Contents.deviceNumber;
import static com.lab.dxy.bracelet.MyApplication.mContext;


/**
 * 项目名称：BleCar
 * 类描述：
 * 创建人：Jack
 * 创建时间：2016/8/25 14:37
 */
public class MyBle {
    private BleLog myLog = new BleLog("[MyBle] ");
    private BleManagerKit bleManagerKit;
    private Ble2Class ble2Class;
    private Handler handler = new Handler();

    public MyBle() {

        bleManagerKit = new BleManagerKit();
        ble2Class = new Ble2Class();
    }

    private static MyBle mInstance;

    public static MyBle getInstance() {
        if (mInstance == null) {
            synchronized (MyBle.class) {
                mInstance = new MyBle();
            }
        }
        return mInstance;
    }


    //5b0100 0000322000000000  0000 0001 573036344c
    public void synPrams(int stepsAim, int location, int mode, boolean isFirst, boolean isMan, int age, int weight, int height,
                         boolean[] Function, final BleCommandListener bleCommandListener) {

        bleManagerKit.bleWriteAndRead(ble2Class.setSynPrams(stepsAim, location, mode, isFirst, isMan, age, weight, height, Function), data -> A(data, bleCommandListener));
    }

    private void A(byte[] data, BleCommandListener bleCommandListener) {
        L.d("回调的数据：" + HexUtil.encodeHexStr(data));
        if (bleCommandListener != null) {
            L.d("接受同步参数返回:" + HexUtil.encodeHexStr(data));
            if (data[1] == 0x01) {
                bleCommandListener.success();
                byte[] agreementVersion = new byte[2];
                byte[] deviceType = new byte[5];

                System.arraycopy(data, 3, deviceNumber, 0, deviceNumber.length);
                System.arraycopy(data, 11, agreementVersion, 0, agreementVersion.length);
                System.arraycopy(data, 15, deviceType, 0, deviceType.length);

                Contents.deviceVersion = data[13];
                switch (data[13]) {
                    case BRA_DEVICE_DXY:
                        Contents.postInfo = "OhuqY";
                        break;
                    case BRA_DEVICE_MST:
                        break;
                    case BRA_DEVICE_YL:
                        break;
                    case BRA_DEVICE_JQ:
                        Contents.postInfo = "Legend";
                        break;
                    default:
                        Contents.postInfo = "OhuqY";
                        break;
                }

                Contents.otaVersion = data[14];

                L.d("设备编号：" + HexUtil.encodeHexStr(deviceNumber));
                L.d("协议版本号：" + HexUtil.encodeHexStr(agreementVersion));
                L.d("设备类型：" + HexUtil.encodeHexStr(deviceType));
                L.d("固件版本：" + Contents.otaVersion);
                L.d("设备厂商：" + Contents.deviceVersion);
            } else {
                bleCommandListener.fail();
            }
        }
    }


    /**
     *  内容：需要恢复的内容，
     * 00：恢复全部参数为出厂设置，并重启
     * 01：仅重启设备
     * 02：查询已存储的上一条指令的执行结果（“回传”字段固定为0）
     * 03：清除计步数据
     * 04：清除睡眠数据
     */
    public void reStart(final int content) {

        bleManagerKit.bleWriteAndRead(ble2Class.restart(content, true), new BleDataListener() {
            @Override
            public void success(byte[] data) {
                L.d("接收重启返回：" + HexUtil.encodeHexStr(data));
                if (data[1] == 0x0f) {
                    switch (data[3]) {
                        case 0x00:
                            L.d("蓝牙收到指令");
                            break;
                        case 0x01:
                            L.d("重启完成");
                            break;
                        case 0x02:
                            L.d("操作完成");
                            break;
                        case (byte) 0xff:
                            L.d("操作失败");
                            break;
                    }
                }
            }
        });
    }


    int bindState = 0;
    private BleBindListener bleBindListener;

    public void queryBind(final BleBindListener bleBindListener) {
        this.bleBindListener = bleBindListener;
        bleManagerKit.bleWriteAndRead(ble2Class.queryBind(), data -> {
            L.d("接收绑定状态返回：" + HexUtil.encodeHexStr(data));
            if (data[1] == 0x0b) {
                switch (data[4]) {
                    case 0x00:
                        bindState = 0;
                        handler.removeCallbacks(requestBind);
                        handler.postDelayed(requestBind, 100);
                        L.d("0x00表示设备未与任何APP绑定，允许建立新的绑定");
                        break;
                    case 0x01:
                        bindState = 1;
                        handler.removeCallbacks(requestBind);
                        handler.postDelayed(requestBind, 100);
                        L.d("0x01表示已经与APP绑定，并且允许再绑定，但再绑定之后将清除与原APP的绑定关系");
                        break;
                    case 0x02:
                        L.d("0x02表示已经与APP绑定，并且不允许再绑定");
                        bleBindListener.fail();
                        break;
                }
            }
        });
    }


    public void requestBind() {
        bleManagerKit.bleWriteAndRead(ble2Class.requestBind(), new BleDataListener() {
            @Override
            public void success(byte[] data) {
                L.d("接收请求绑定状态返回：" + HexUtil.encodeHexStr(data));
                if (data[1] == 0x0b) {
                    switch (data[4]) {
                        case 0x00:
                            L.d("0x00表示设备允许APP绑定，需要等待");
                            int time = ByteUtil.bytesToIntG2(new byte[]{data[5], data[6]});
                            handler.removeCallbacks(resultBind);
                            handler.postDelayed(resultBind, 100);
                            handler.removeCallbacks(bindOutTime);
                            handler.postDelayed(bindOutTime, time * 1000);

                            L.d("等待时间：" + time);
                            break;
                        case 0x01:
                            L.d("0x01表示绑定失败，原因是：设备不允许与APP绑定");
                            bleBindListener.fail();
                            break;
                        case 0x02:
                            L.d("0x02表示绑定失败，原因是：设备已经与其它APP绑定，不允许与本APP绑定");
                            bleBindListener.fail();
                            break;
                    }
                }
            }
        });
    }


    public void veriftBind(byte Result) {
        bleManagerKit.bleWriteAndRead(ble2Class.veriftBind(Result), new BleDataListener() {
            @Override
            public void success(byte[] data) {
                L.d("接收验证绑定状态返回：" + HexUtil.encodeHexStr(data));
                if (data[1] == 0x0b) {
                    if (data[4] == 0x00) {
                        bindState = -1;
                        L.d("0x00表示确认绑定成功");
                        bleBindListener.success();
                    } else {
                        bleBindListener.fail();
                        L.d("其他表示确认绑定失败");
                    }
                }
            }
        });
    }


    /**
     * 附加操作：解除绑定之后设备端执行的附加动作，取值为：
     * 0x00：无指定的附加操作，由设备自行决定；
     * 0x01：重启设备；
     * 0x02：清除所有数据；
     * 0x03：清除所有数据并重启设备；
     */
    public void removeBind(int action) {
        bleManagerKit.bleWriteAndRead(ble2Class.removeBind(action), new BleDataListener() {
            @Override
            public void success(byte[] data) {
                L.d("接收解除绑定状态返回：" + HexUtil.encodeHexStr(data));
                if (data[1] == 0x0b) {
                    if (data[4] == 0x00) {
                        L.d("0x00表示解除绑定成功");
                    } else {
                        L.d("其他表示解除绑定失败");
                    }
                }
            }
        });
    }


    private Runnable requestBind = new Runnable() {
        @Override
        public void run() {
            requestBind();
        }
    };

    private byte result = 0x01;
    private Runnable resultBind = new Runnable() {
        @Override
        public void run() {
            bleManagerKit.setBleDataListener(new BleOnSleepListener() {
                @Override
                public void onReceiveListener(byte[] data) {
                    L.d("确认绑定返回：" + HexUtil.encodeHexStr(data));
                    if (data[1] == 0x0b && data[3] == 0x02) {
                        result = data[5];
                        if (result == 0x00) {
                            handler.postDelayed(veriftBind, 100);
                        }
                    }
                }
            });
            handler.postDelayed(this, 1000);
        }
    };

    private Runnable veriftBind = new Runnable() {
        @Override
        public void run() {
            veriftBind(result);
            if (bindState == 1) {
                handler.postDelayed(removeBind, 100);
            }
        }
    };

    private Runnable removeBind = new Runnable() {
        @Override
        public void run() {
            removeBind(0);
        }
    };

    private Runnable bindOutTime = new Runnable() {
        @Override
        public void run() {
            if (bindState != -1) {
                bleBindListener.fail();
            }
        }
    };


    public void last(boolean isOpen, int time, BleSettingFailListener bleSettingFailListener) {

        bleManagerKit.bleWriteNoRead(ble2Class.last(isOpen, time), bleSettingFailListener);
    }


    public void find() {
        bleManagerKit.bleWriteNoRead(ble2Class.find(), null);
    }


    public void sendPush(int type, String message) {
        bleManagerKit.bleWriteAndRead(ble2Class.sendPush(type, message.getBytes()), new BleDataListener() {
            @Override
            public void success(byte[] data) {
                L.d("接收消息提醒返回：" + HexUtil.encodeHexStr(data));
                if (data[1] == 0x15) {
                    if (data[4] == 0x00) {
                        L.d("0x00表示消息提醒成功");
                    } else {
                        L.d("0x00表示消息提醒失败");
                        RxToast.error(mContext.getString(R.string.settingFail));
                    }
                }
            }

        });
    }


    public void pushPhone(final String phone, final String name) {
        if (!name.equals(phone) || !TextUtils.isEmpty(name)) {
            pushPhoneName(name);
        } else {
            pushPhoneNumber(phone);
        }
    }


    private void pushPhoneName(String name) {
        bleManagerKit.bleWriteNoRead(ble2Class.pushPhoneName_DXY(name), null);
    }

    private void pushPhoneNumber(String phone) {

        bleManagerKit.bleWriteAndRead(ble2Class.pushPhone(phone), new BleDataListener() {
            @Override
            public void success(byte[] data) {
                L.d("接收电话提醒返回：" + HexUtil.encodeHexStr(data));
                if (data[1] == 0x15 && data[3] == 0x01) {
                    if (data[4] == 0x00) {
                        L.d("0x00表示电话提醒成功");
                    } else {
                        L.d("0x00表示电话提醒失敗");
                        RxToast.error(mContext.getString(R.string.settingFail));
                    }
                }
            }

        });
    }


    public void queryPower(final BleOnReceiveListener bleOnReceiveListener) {
        bleManagerKit.bleWriteAndRead(ble2Class.readPrower(), data -> bleOnReceiveListener.onReceiveListener(data));
    }

    public void queryOneDaySteps(int year, int month, int day, final BleOnReceiveListener bleOnReceiveListener) {

        bleManagerKit.bleWriteAndRead(ble2Class.queryOneDaySteps(year, month, day), data -> bleOnReceiveListener.onReceiveListener(data));
    }


    public void synStepsData(byte[] time, final BleCommandListener bleCommandListener) {
        bleManagerKit.bleWriteAndRead(ble2Class.synStepsData(time), data -> {
            L.d("同步步数返回：" + HexUtil.encodeHexStr(data));
            if (data[1] == 0x03 && data[4] != 0x00) {
                L.d("开始线程");
                SynDataThread thread = new SynDataThread(SynDataThread.DATA_STEPS, data);
                thread.setOnSynDataListener(() -> bleCommandListener.success());
                thread.start();
            } else
                bleCommandListener.fail();
        });
    }


    public void synSleepData(byte[] time, final BleCommandListener bleCommandListener) {
        bleManagerKit.bleWriteAndRead(ble2Class.synSleepData(time), data -> F(data, bleCommandListener));
    }

    private void F(byte[] data, final BleCommandListener bleCommandListener) {
        L.d("同步睡眠返回：" + HexUtil.encodeHexStr(data));
        if (data[1] == 0x07 && data[4] != 0x00) {
            L.d("开始线程");
            SynDataThread thread = new SynDataThread(SynDataThread.DATA_SLEEP, data);
            thread.setOnSynDataListener(() -> bleCommandListener.success());
            thread.start();
        } else {
            bleCommandListener.fail();
        }
    }


    public void receiverSuccessOrNext(int packageId, boolean isSuccess, BleSettingFailListener bleSettingFailListener) {

        bleManagerKit.bleWriteNoRead(ble2Class.receiverSuccess(packageId, isSuccess), bleSettingFailListener);
    }


    public void sendLongMessgae(final String message, int type) {
        bleManagerKit.bleWriteAndRead(ble2Class.sendLongMessagePush(type, message), data -> {
            if (data[1] == 0x15 && data[3] == (byte) 0xff) {
                if (data[5] != 0x00) {
                    BleLongPackageThread packageThread = new BleLongPackageThread(message);
                    packageThread.start();
                } else {
                    L.d("暂时不能接收长消息");
                }
            }
        });
    }


    public void sendFirstPackage(int packlenght) {
        bleManagerKit.bleWriteNoRead(ble2Class.sendFirstPackage(packlenght), null);
    }

    public void sendMessagePackage(int itemNumber, byte[] item) {
        bleManagerKit.bleWriteNoRead(ble2Class.sendMsgPackage(itemNumber, item), null);
    }

    public void sendEndPackage(byte[] endItem) {
        bleManagerKit.bleWriteNoRead(ble2Class.sendEndPackage(endItem), null);
    }


    public void sendAlarmPush(boolean isOpen, UserAlarmTab tab, final BleSettingFailListener bleSettingFailListener) {

        bleManagerKit.bleWriteAndRead(ble2Class.setAlarmPush(isOpen, tab), data -> {
            if (data[1] == 0x14) {
                if (data[4] == 0x01) {
                    L.d("闹钟设置成功");
                } else {
                    L.d("闹钟设置失败");
                }
                if (bleSettingFailListener != null)
                    bleSettingFailListener.isSuccess(data[4] == 0x01);
            }
        });
    }


    public void keyOrder(boolean isOpen, int time) {

        bleManagerKit.bleWriteAndRead(ble2Class.keyOrder(isOpen, time), new BleDataListener() {
            @Override
            public void success(byte[] data) {
                if (data[1] == 0x0c && data[3] == 0x0d) {
                    if (data[4] == 0x00) {
                        L.d("0x00表示不在按键锁定模式");
                    } else if (data[4] == 0x01) {
                        L.d("0x01表示在按键锁定模式");
                    } else if (data[4] == 0x02) {
                        L.d("0x02表示出错");
                    }
                }
            }
        });
    }


    public void contorlApp(byte type, boolean isSuccess) {
        bleManagerKit.bleWriteNoRead(ble2Class.contorlApp(type, isSuccess), null);
    }

    public void setSedPush(boolean isOpen, int hour, int min, final BleSettingFailListener bleSettingFailListener) {
        bleManagerKit.bleWriteAndRead(ble2Class.setSedPush(isOpen, hour, min), data -> {
            if (data[1] == 0x14) {
                if (data[4] == 0x01) {
                    L.d("久坐提醒设置成功");
                } else {
                    L.d("设置失败");
                    RxToast.error(mContext.getString(R.string.settingFail));
                }
                bleSettingFailListener.isSuccess(data[4] == 0x01);
            }
        });
    }


    public void queryAlarm(int type, int num, final BleOnReceiveListener bleOnReceiveListener) {
        bleManagerKit.bleWriteAndRead(ble2Class.queryAlarm(type, num), data -> bleOnReceiveListener.onReceiveListener(data));
    }

    public void queryAlarm(int type, int num) {
        bleManagerKit.bleWriteNoRead(ble2Class.queryAlarm(type, num), null);
    }

    public void querySwitchState(int type, BleDataListener bleDataListener) {
        bleManagerKit.bleWriteAndRead(ble2Class.querySwitchState(type), bleDataListener);
    }

    public void setBright(boolean isOpen, final BleSettingFailListener bleSettingFailListener) {
        bleManagerKit.bleWriteAndRead(ble2Class.setBright(isOpen), data -> {
            if (data[1] == 0x0c && data[3] == (byte) 0xfe) {
                if (data[4] == 0x00) {
                    L.d("抬手亮屏操作成功");
                } else {
                    L.d("设置失败");
                    RxToast.error(mContext.getString(R.string.settingFail));
                }
                bleSettingFailListener.isSuccess(data[4] == 0x00);
            }
        });
    }


    public void queryBright(final BleOnReceiveListener bleOnReceiveListener) {
        bleManagerKit.bleWriteAndRead(ble2Class.queryBright(), data -> bleOnReceiveListener.onReceiveListener(data));
    }

    public void queryANCS(BleDataListener bleDataListener) {
        bleManagerKit.bleWriteAndRead(ble2Class.queryANCS(), bleDataListener);
    }

    public void readShakeKit(final BleOnReceiveListener bleOnReceiveListener) {
        bleManagerKit.setBleDataListener(data -> bleOnReceiveListener.onReceiveListener(data));
    }

    public void readShake(final BleOnReceiveListener bleOnReceiveListener) {
        bleManagerKit.setBleOnStepsListener(data -> bleOnReceiveListener.onReceiveListener(data));
    }

    public void queryCharge(final BleSettingFailListener bleSettingFailListener) {
        bleManagerKit.bleWriteAndRead(ble2Class.queryCharge(), data -> {
            L.d("充电：" + HexUtil.encodeHexStr(data));
            bleSettingFailListener.isSuccess(data[4] == 0x01);
        });
    }

    public void querySleepDate(BleDataListener bleDataListener) {
        bleManagerKit.bleWriteAndRead(ble2Class.getSleepDate(), bleDataListener);
    }


    public void setSleepDate(int[] bytes, final BleSettingFailListener bleSettingFailListener) {
        bleManagerKit.bleWriteAndRead(ble2Class.setSleepDate(bytes), data -> {
            L.d("设置睡眠数据：" + HexUtil.encodeHexStr(data));
            if (data[1] == (byte) 0x0d && data[3] == (byte) 0xa0) {
                if (data[4] == 0x00) {
                    L.d("s设置成功");
                } else {
                    L.d("设置失败");
                    RxToast.error(mContext.getString(R.string.settingFail));
                }

                bleSettingFailListener.isSuccess(data[4] == 0x00);
            }
        });
    }

    public void shakeTakePhoto(boolean isOpen) {
        bleManagerKit.bleWriteAndRead(ble2Class.shakeTakeThoto(isOpen), data -> {
            if (data[1] == (byte) 0x0c && data[3] == (byte) 0xe1) {
                if (data[4] == 0x00) {
                    L.d("s设置成功");
                } else {
                    L.d("设置失败");
                    RxToast.error(mContext.getString(R.string.settingFail));
                }
            }
        });
    }

    public void shakeTakePhotoVluse(int mode, final BleSettingFailListener bleSettingFailListener) {
        int value = 0;
        switch (mode) {
            case 0:
                value = 0x07;
                break;
            case 1:
                value = 0x0b;
                break;
            case 2:
                value = 0x10;
                break;
        }
        bleManagerKit.bleWriteAndRead(ble2Class.shakeTakePhotoVluse(value), data -> {
            if (data[3] == (byte) 0xe2) {
                if (data[4] == 0x00) {
                    L.d("s设置成功");
                } else {
                    L.d("设置失败");
                    RxToast.error(mContext.getString(R.string.settingFail));
                }
                bleSettingFailListener.isSuccess(data[4] == 0x00);
            }
        });
    }

    public void queryDeviceVersion(final BleSettingFailListener bleSettingFailListener) {
        bleManagerKit.bleWriteAndRead(ble2Class.queryDeviceVersion(), data -> {
            L.d("查询设备版本：" + HexUtil.encodeHexStr(data));
            if (data[4] != 0x2a) {
                bleSettingFailListener.isSuccess(false);
                return;
            }
            if (data[3] == 0x00) {
                Contents.SDKVersion = Contents.SDK_2D2;
            } else if (data[3] == 0x10) {
                Contents.SDKVersion = Contents.SDK_3D2;
                Contents.postInfo = "Legend_2";
            }
            bleSettingFailListener.isSuccess(true);
        });
    }


    public BleManagerKit getBleManagerKit() {
        return bleManagerKit;
    }


}
