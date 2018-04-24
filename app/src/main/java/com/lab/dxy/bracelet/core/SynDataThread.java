package com.lab.dxy.bracelet.core;

import com.lab.dxy.bracelet.Utils.ByteUtil;
import com.lab.dxy.bracelet.Utils.L;
import com.lab.dxy.bracelet.ble.MyBle;
import com.lab.dxy.bracelet.entity.spl.DayStepsTab;
import com.lab.dxy.bracelet.entity.spl.SleepDataTab2;
import com.lab.dxy.bracelet.entity.spl.UserStepsTab;
import com.syd.oden.odenble.Utils.HexUtil;
import com.syd.oden.odenble.listener.BleOnReceiveListener;

import static com.lab.dxy.bracelet.Utils.Utils.intToString;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：oden
 * 创建时间：2017/9/20
 */
public class SynDataThread extends Thread {
    private int time = 0;
    private byte[] bytes;
    private byte[] handleBytes;
    private String hexData = "";
    public static final int DATA_STEPS = 1;
    public static final int DATA_SLEEP = 2;
    private int mode;
    private int pakeSum;//包数
    int pakeage = 0;//当前包数
    private String date;

    public SynDataThread(int mode, byte[] data) {
        this.mode = mode;
        hexData = "";
        byte[] lenByte = new byte[2];
        System.arraycopy(data, 3, lenByte, 0, 2);
        pakeSum = ByteUtil.bytesToIntG2(lenByte);
    }

    @Override
    public void run() {
        super.run();

        if (mode == DATA_STEPS) {
            synSteps();
        } else {
            synSleep();
        }
    }

    public interface OnSynDataListener {
        void synSuccess();
    }

    private OnSynDataListener onSynDataListener;

    public void setOnSynDataListener(OnSynDataListener onSynDataListener) {
        this.onSynDataListener = onSynDataListener;
    }

    private void synSleep() {

        //第一条：5a0501 0080 0001 d1fd 07 110918 200000963d1e9c
        //第二条：5a0502 00000f02 000f0f02 001e0f02 002d0f02 00
        //第三条：5a0503 3c0f02 004b0f02 005a0f02 00690f02 0078
        //第四条：5a0504 0f02 00870f02 00960f02 00a50f02 00b40f
        //       5a0505 02 00c30f02 00d20f02 00e10f02 00f00f02
        //       5a0506 00ff0f02 010e0f02 011d0f02 012c0f02 01
        //       5a0507 3b0f02 014a0f02 01590f02 01680f02 0177
        //       5a0508 0f02 01860f02 01950f02 01a40f02 01b30f
        //       5a05fe 02 01c20f02 01d10f02
        //04 清醒 ，00深睡，02 浅睡

        MyBle.getInstance().readShakeKit(new BleOnReceiveListener() {
            @Override
            public void onReceiveListener(byte[] data) {
                if (data[1] != 0x05) return;
                if (mode == DATA_STEPS) return;
                byte[] sleep = new byte[4];
                byte[] lenByte = new byte[2];

                if (data[2] == 0x01) {
                    System.arraycopy(data, 3, lenByte, 0, lenByte.length);
                    int Len = ByteUtil.bytesToIntG2(lenByte);
                    L.d("睡眠包长度：" + Len);
                    System.arraycopy(data, 5, lenByte, 0, lenByte.length);
                    pakeage = ByteUtil.bytesToIntG2(lenByte);
                    L.d("包序号：" + pakeage);
                    int year = data[10];
                    int month = data[11];
                    int day = data[12];
                    //date:yyyy-MM-dd
                    date = (2000 + year) + "-" + intToString(month) + "-" + intToString(day);
                    L.d("date:" + date);

                } else {

                    String s = HexUtil.encodeHexStr(data).substring(6);

                    hexData = hexData + s;

                    if (data[2] == (byte) 0xff || data[2] == (byte) 0xfe) {

                        L.d("睡眠数据：" + hexData);

                        bytes = HexUtil.decodeHex(hexData.toCharArray());

                        boolean f = true;
                        int lastTime = ByteUtil.bytesToIntG2(bytes);
                        handleBytes = new byte[bytes.length];
                        for (int i = 0; i < bytes.length; i = i + 4) {
                            System.arraycopy(bytes, i, sleep, 0, 4);
                            int startTime = ByteUtil.bytesToIntG2(sleep);

                            if (startTime - lastTime > 15) {
                                f = false;
                                L.d("昼夜分割处：" + startTime / 60);
                                System.arraycopy(bytes, i, handleBytes, 0, bytes.length - i);
                                System.arraycopy(bytes, 0, handleBytes, bytes.length - i, i);
                                break;
                            } else {
                                lastTime = startTime;
                            }
                        }

                        if (f) handleBytes = bytes;

                        SleepDataTab2.deleteByDate(date);
                        for (int i = 0; i < handleBytes.length; i = i + 4) {
                            System.arraycopy(handleBytes, i, sleep, 0, 4);

                            int startTime = ByteUtil.bytesToIntG2(sleep);
                            int sleepType = (int) sleep[3];
                            if (sleepType == 4) sleepType = 0;
                            else if (sleepType == 0) sleepType = 2;
                            else if (sleepType == 2) sleepType = 1;

                            int endTime = startTime + 15;

                            startTime = startTime / 60 >= 24 ? 00 : startTime;
                            endTime = endTime / 60 >= 24 ? 00 : endTime;

                            L.d("睡眠起始时间：" + startTime / 60 + ":" + startTime % 60 + "---结束时间：" + endTime / 60 + ":" + endTime % 60 + "----睡眠类型" + sleepType);

                            SleepDataTab2 sleepDataTab = new SleepDataTab2(date, sleepType, 15, intToString(startTime / 60) + ":" +
                                    intToString(startTime % 60) + "-" + intToString(endTime / 60) + ":" + intToString(endTime % 60));
                            sleepDataTab.save();
                        }
                        hexData = "";

                        MyBle.getInstance().receiverSuccessOrNext(pakeage, pakeSum == pakeage || pakeage == 0, isSuccess -> {
                            if (pakeSum == pakeage || pakeage == 0) {
                                onSynDataListener.synSuccess();
                            }
                        });
                    }
                }
            }
        });
    }


    private void synSteps() {

        //第一条：5a0501  0030 0000 8700 03 110914 3c(间隔60) 18(24条) 00 40078000
        //第二条：5a0502 0000 0000 0000 0000 0000 0000 0000 0000 00
        //第三条：5a0503 00 0000 0000 0000 0015 0000 010f 0000 0017
        //第四条：5a05ff 0000 0000 0000 0000 0000 0000 0000

        MyBle.getInstance().readShakeKit(new BleOnReceiveListener() {
            @Override
            public void onReceiveListener(byte[] data) {
                if (data[1] != 0x05) return;
                if (mode == DATA_SLEEP) return;
                byte[] steps = new byte[2];

                if (data[2] == 0x01) {
                    System.arraycopy(data, 3, steps, 0, steps.length);
                    int Len = ByteUtil.bytesToIntG2(steps);
                    L.d("步数包长度：" + Len);
                    System.arraycopy(data, 5, steps, 0, steps.length);
                    pakeage = ByteUtil.bytesToIntG2(steps);
                    L.d("包序号：" + pakeage);
                    int year = data[10];
                    int month = data[11];
                    int day = data[12];
                    //date:yyyy-MM-dd
                    date = (2000 + year) + "-" + intToString(month) + "-" + intToString(day);
                    L.d("date:" + date);
                } else {

                    byte[] stepData = new byte[data.length - 3];

                    System.arraycopy(data, 3, stepData, 0, data.length - 3);

                    String s = HexUtil.encodeHexStr(stepData);
                    hexData = hexData + s;


                    if (data[2] == (byte) 0xff || data[2] == (byte) 0xfe) {
                        L.d("步数数据：" + hexData);
                        bytes = HexUtil.decodeHex(hexData.toCharArray());

                        int total = 0;

                        DayStepsTab.deleteforDate(date);
                        UserStepsTab.DeleteAddTime(date);
                        for (int i = 0; i < bytes.length; i = i + 2) {
                            time++;
                            System.arraycopy(bytes, i, steps, 0, 2);
                            int step = ByteUtil.bytesToIntG2(steps);
                            L.d("步数：" + step);

                            total += step;
                            DayStepsTab tab = new DayStepsTab(date, step, false);
                            tab.save();
                        }
                        DayStepsTab tab = new DayStepsTab(date, total, true);
                        tab.save();

                        UserStepsTab stepsTab = new UserStepsTab(date, total);
                        stepsTab.save();

                        hexData = "";

                        MyBle.getInstance().receiverSuccessOrNext(pakeage, pakeSum == pakeage || pakeage == 0, isSuccess -> {
                            if (pakeSum == pakeage || pakeage == 0) {
                                onSynDataListener.synSuccess();
                            }
                        });
                    }
                }
            }
        });
    }
}
