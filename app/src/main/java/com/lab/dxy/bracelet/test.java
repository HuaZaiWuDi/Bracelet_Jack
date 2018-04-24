///*
//package com.lab.dxy.bracelet;
//
//import android.util.Log;
//
//import com.lab.dxy.bracelet.Utils.ByteUtil;
//import com.lab.dxy.bracelet.Utils.L;
//import com.lab.dxy.bracelet.ble.MyBle;
//import com.lab.dxy.bracelet.ble.listener.BleCommandListener;
//import com.lab.dxy.bracelet.entity.spl.SleepDataTab2;
//import com.syd.oden.odenble.Utils.HexUtil;
//import com.syd.oden.odenble.listener.BleOnReceiveListener;
//
//import java.util.Calendar;
//
//import static com.lab.dxy.bracelet.Contents.BRA_DEVICE_DXY;
//import static com.lab.dxy.bracelet.Contents.BRA_DEVICE_JQ;
//import static com.lab.dxy.bracelet.Contents.BRA_DEVICE_MST;
//import static com.lab.dxy.bracelet.Contents.BRA_DEVICE_YL;
//import static com.lab.dxy.bracelet.Contents.deviceNumber;
//import static com.lab.dxy.bracelet.Utils.ByteUtil.bit2byte;
//
//*/
///**
// * 项目名称：Bracelet
// * 类描述：
// * 创建人：Jack
// * 创建时间：2018/3/13
// *//*
//
//public class test {
//
//
//    private static String TGA = "test";
//    ///////////////////////////////////////////////////////////////////////////
//    // 发送命令（以下以同步参数为例）
//    ///////////////////////////////////////////////////////////////////////////
//
//
//    //发送数据拼接的方法
//    //age：男的23，女的-23
//    //位置：表示设备的配戴位置，依次为：1手腕(默认),2脖子，3腰，4脚
//    //模式：表示运动模式，依次为：1步行(默认)，2睡觉，3骑车，4游泳，5网球，6篮球，7足球
//    public byte[] setSynPrams(int stepsAim, int location, int mode, boolean isFirst, boolean isMan, int age, int weight, int height,
//                              boolean[] Function) {
//
//        Calendar c = Calendar.getInstance();
//        int year = c.get(Calendar.YEAR);
//        int month = c.get(Calendar.MONTH) + 1;
//        int day = c.get(Calendar.DAY_OF_MONTH);
//        int hour = c.get(Calendar.HOUR_OF_DAY);
//        int min = c.get(Calendar.MINUTE);
//        int sec = c.get(Calendar.SECOND);
//        Log.d(TGA, "年：" + year);
//        Log.d(TGA, "月：" + month);
//        Log.d(TGA, "日：" + day);
//        Log.d(TGA, "时：" + hour);
//        Log.d(TGA, "分：" + min);
//        Log.d(TGA, "秒：" + sec);
//        Log.d(TGA, "运动目标：" + stepsAim);
//        Log.d(TGA, "位置：" + location);
//        Log.d(TGA, "模式：" + mode);
//        Log.d(TGA, "性别：" + (isMan ? "男" : "女"));
//        Log.d(TGA, "年龄：" + age);
//        Log.d(TGA, "体重：" + weight);
//        Log.d(TGA, "身高：" + height);
//
//        byte[] bytes = new byte[20];
//        byte[] head = new byte[2];
////        byte[] head = singleHead((byte) 0x01);
//        System.arraycopy(head, 0, bytes, 0, 3);
//        bytes[3] = (byte) (year - 2000);
//        bytes[4] = (byte) month;
//        bytes[5] = (byte) day;
//        bytes[6] = (byte) hour;
//        bytes[7] = (byte) min;
//        bytes[8] = (byte) sec;
//
//        byte[] stepsBytes = ByteUtil.intToBytesG2(stepsAim);
//        System.arraycopy(stepsBytes, 0, bytes, 9, 2);
//        bytes[11] = (byte) location;
//        bytes[12] = (byte) mode;
//        bytes[13] = isFirst ? (byte) 0x78 : 0x00;
//        bytes[14] = 0x00;
//        bytes[15] = 0x00;
//        if (Math.abs(age) > 127) age = 0;
//
////        bytes[16] = bit2byte(binaryStringToHexString(isMan, age));
//        bytes[17] = (byte) weight;
//        bytes[18] = (byte) height;
//
//        if (Function != null) {
//            char[] hex2 = new char[8];
//            for (int i = 0; i < Function.length; i++) {
//                if (Function[i]) {
//                    hex2[i] = '1';
//                } else {
//                    hex2[i] = '0';
//                }
//            }
//            String bs = String.valueOf(hex2);
//            bytes[19] = bit2byte(bs);
//            L.d("参数：" + bs);
//        } else {
//            bytes[19] = 0x00;
//        }
//
//        L.d("同步参数：" + HexUtil.encodeHexStr(bytes));
//        return bytes;
//    }
//
//
//    //接收数据解析的方法。
//    private void receive(byte[] data, BleCommandListener bleCommandListener) {
//        L.d("回调的数据：" + HexUtil.encodeHexStr(data));
//        if (bleCommandListener != null) {
//            L.d("接受同步参数返回:" + HexUtil.encodeHexStr(data));
//            if (data[1] == 0x01) {
//                bleCommandListener.success();
//                byte[] agreementVersion = new byte[2];
//                byte[] deviceType = new byte[5];
//
//                System.arraycopy(data, 3, deviceNumber, 0, deviceNumber.length);
//                System.arraycopy(data, 11, agreementVersion, 0, agreementVersion.length);
//                System.arraycopy(data, 15, deviceType, 0, deviceType.length);
//
//                Contents.deviceVersion = data[13];
//                switch (data[13]) {
//                    case BRA_DEVICE_DXY:
//                        Contents.postInfo = "OhuqY";
//                        break;
//                    case BRA_DEVICE_MST:
//                        break;
//                    case BRA_DEVICE_YL:
//                        break;
//                    case BRA_DEVICE_JQ:
//                        Contents.postInfo = "Legend";
//                        break;
//                    default:
//                        Contents.postInfo = "OhuqY";
//                        break;
//                }
//
//                Contents.otaVersion = data[14];
//
//                L.d("设备编号：" + HexUtil.encodeHexStr(deviceNumber));
//                L.d("协议版本号：" + HexUtil.encodeHexStr(agreementVersion));
//                L.d("设备类型：" + HexUtil.encodeHexStr(deviceType));
//                L.d("固件版本：" + Contents.otaVersion);
//                L.d("设备厂商：" + Contents.deviceVersion);
//            } else {
//                bleCommandListener.fail();
//            }
//        }
//    }
//
//
//    ///////////////////////////////////////////////////////////////////////////
//    // 同步长包数据解析
//    ///////////////////////////////////////////////////////////////////////////
//
//
//    //time:前3位为起始年月日，后三位为结束年月日   全部为0表示同步所有
//    public byte[] startLongPackage(byte[] time) {
//        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
//        defaultArray[1] = 0x03;
//        System.arraycopy(time, 0, defaultArray, 3, 6);
//        System.arraycopy(Contents.deviceNumber, 0, defaultArray, 9, 8);
//
//        L.d("开始同步计步时间：" + HexUtil.encodeHexStr(defaultArray));
//        return defaultArray;
//    }
//
//
//    //设备收到开始长包命令之后，会反馈是否有睡眠或计步数据，如果有数据设备会将每天数据，按一天一个包通过notify发送过来
//    private void synSleep() {
//
//        //第一条：5a0501 0080 0001 d1fd 07 110918 200000963d1e9c
//        //第二条：5a0502 00000f02 000f0f02 001e0f02 002d0f02 00
//        //第三条：5a0503 3c0f02 004b0f02 005a0f02 00690f02 0078
//        //第四条：5a0504 0f02 00870f02 00960f02 00a50f02 00b40f
//        //       5a0505 02 00c30f02 00d20f02 00e10f02 00f00f02
//        //       5a0506 00ff0f02 010e0f02 011d0f02 012c0f02 01
//        //       5a0507 3b0f02 014a0f02 01590f02 01680f02 0177
//        //       5a0508 0f02 01860f02 01950f02 01a40f02 01b30f
//        //       5a05fe 02 01c20f02 01d10f02
//        //04 清醒 ，00深睡，02 浅睡
//
//        MyBle.getInstance().readNotify(new BleOnReceiveListener() {
//            @Override
//            public void onReceiveListener(byte[] data) {
//                if (data[1] != 0x05) return;
////                if (mode == DATA_STEPS) return;
//                byte[] sleep = new byte[4];
//                byte[] lenByte = new byte[2];
//
//                if (data[2] == 0x01) {
//                    System.arraycopy(data, 3, lenByte, 0, lenByte.length);
//                    int Len = ByteUtil.bytesToIntG2(lenByte);
//                    L.d("睡眠包长度：" + Len);
//                    System.arraycopy(data, 5, lenByte, 0, lenByte.length);
//                    package = ByteUtil.bytesToIntG2(lenByte);
//                    L.d("包序号：" + package);
//                    int year = data[10];
//                    int month = data[11];
//                    int day = data[12];
//                    //date:yyyy-MM-dd
//                    date = (2000 + year) + "-" + intToString(month) + "-" + intToString(day);
//                    L.d("日期时间:" + date);
//
//                } else {
//
//                    String s = HexUtil.encodeHexStr(data).substring(6);
//
//                    hexData = hexData + s;
//
//                    if (data[2] == (byte) 0xff || data[2] == (byte) 0xfe) {
//
//                        L.d("睡眠数据：" + hexData);
//
//                        bytes = HexUtil.decodeHex(hexData.toCharArray());
//
//                        boolean f = true;
//                        int lastTime = ByteUtil.bytesToIntG2(bytes);
//                        handleBytes = new byte[bytes.length];
//                        for (int i = 0; i < bytes.length; i = i + 4) {
//                            System.arraycopy(bytes, i, sleep, 0, 4);
//                            int startTime = ByteUtil.bytesToIntG2(sleep);
//
//                            if (startTime - lastTime > 15) {
//                                f = false;
//                                L.d("昼夜分割处：" + startTime / 60);
//                                System.arraycopy(bytes, i, handleBytes, 0, bytes.length - i);
//                                System.arraycopy(bytes, 0, handleBytes, bytes.length - i, i);
//                                break;
//                            } else {
//                                lastTime = startTime;
//                            }
//                        }
//
//                        if (f) handleBytes = bytes;
//
//                        SleepDataTab2.deleteByDate(date);
//                        for (int i = 0; i < handleBytes.length; i = i + 4) {
//                            System.arraycopy(handleBytes, i, sleep, 0, 4);
//
//                            int startTime = ByteUtil.bytesToIntG2(sleep);
//                            int sleepType = (int) sleep[3];
//                            if (sleepType == 4) sleepType = 0;
//                            else if (sleepType == 0) sleepType = 2;
//                            else if (sleepType == 2) sleepType = 1;
//
//                            int endTime = startTime + 15;
//
//                            startTime = startTime / 60 >= 24 ? 00 : startTime;
//                            endTime = endTime / 60 >= 24 ? 00 : endTime;
//
//                            L.d("睡眠起始时间：" + startTime / 60 + ":" + startTime % 60 + "---结束时间：" + endTime / 60 + ":" + endTime % 60 + "----睡眠类型" + sleepType);
//
//                            SleepDataTab2 sleepDataTab = new SleepDataTab2(date, sleepType, 15, intToString(startTime / 60) + ":" +
//                                    intToString(startTime % 60) + "-" + intToString(endTime / 60) + ":" + intToString(endTime % 60));
//                            sleepDataTab.save();
//                        }
//                        hexData = "";
//
//                        MyBle.getInstance().receiverSuccessOrNext(package, packSum == package || package == 0, isSuccess -> {
//                            if (packSum == package || package == 0) {
//                                onSynDataListener.synSuccess();
//                            }
//                        });
//                    }
//                }
//            }
//        });
//    }
//
//
//}
//*/
