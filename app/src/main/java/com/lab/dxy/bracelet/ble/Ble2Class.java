package com.lab.dxy.bracelet.ble;

import android.util.Log;

import com.lab.dxy.bracelet.Contents;
import com.lab.dxy.bracelet.Utils.ByteUtil;
import com.lab.dxy.bracelet.Utils.L;
import com.lab.dxy.bracelet.Utils.RegexUtils;
import com.lab.dxy.bracelet.Utils.Utils;
import com.lab.dxy.bracelet.entity.spl.UserAlarmTab;
import com.syd.oden.odenble.Utils.HexUtil;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import static com.lab.dxy.bracelet.Contents.bindSign;
import static com.lab.dxy.bracelet.Utils.ByteUtil.bit2byte;


/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：Jack
 * 创建时间：2017/9/18
 */
public class Ble2Class {
    String TGA = "[BleClass]:";


    public byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};


    public byte[] deviceVersion = {0x5a, 0x10, 0x00};


    //age：男的23，女的-23
    //位置：表示设备的配戴位置，依次为：1手腕(默认),2脖子，3腰，4脚
    //模式：表示运动模式，依次为：1步行(默认)，2睡觉，3骑车，4游泳，5网球，6篮球，7足球
    public byte[] setSynPrams(int stepsAim, int location, int mode, boolean isFirst, boolean isMan, int age, int weight, int height,
                              boolean[] Function) {

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);
        int sec = c.get(Calendar.SECOND);
        Log.d(TGA, "年：" + year);
        Log.d(TGA, "月：" + month);
        Log.d(TGA, "日：" + day);
        Log.d(TGA, "时：" + hour);
        Log.d(TGA, "分：" + min);
        Log.d(TGA, "秒：" + sec);
        Log.d(TGA, "运动目标：" + stepsAim);
        Log.d(TGA, "位置：" + location);
        Log.d(TGA, "模式：" + mode);
        Log.d(TGA, "性别：" + (isMan ? "男" : "女"));
        Log.d(TGA, "年龄：" + age);
        Log.d(TGA, "体重：" + weight);
        Log.d(TGA, "身高：" + height);

        byte[] bytes = new byte[20];
        byte[] head = singleHead((byte) 0x01);
        System.arraycopy(head, 0, bytes, 0, 3);
        bytes[3] = (byte) (year - 2000);
        bytes[4] = (byte) month;
        bytes[5] = (byte) day;
        bytes[6] = (byte) hour;
        bytes[7] = (byte) min;
        bytes[8] = (byte) sec;

        byte[] stepsBytes = ByteUtil.intToBytesG2(stepsAim);
        System.arraycopy(stepsBytes, 0, bytes, 9, 2);
        bytes[11] = (byte) location;
        bytes[12] = (byte) mode;
        bytes[13] = isFirst ? (byte) 0x78 : 0x00;
        bytes[14] = 0x00;
        bytes[15] = 0x00;
        if (Math.abs(age) > 127) age = 0;

        bytes[16] = bit2byte(binaryStringToHexString(isMan, age));
        bytes[17] = (byte) weight;
        bytes[18] = (byte) height;

        if (Function != null) {
            char[] hex2 = new char[8];
            for (int i = 0; i < Function.length; i++) {
                if (Function[i]) {
                    hex2[i] = '1';
                } else {
                    hex2[i] = '0';
                }
            }
            String bs = String.valueOf(hex2);
            bytes[19] = bit2byte(bs);
            L.d("参数：" + bs);
        } else {
            bytes[19] = 0x00;
        }

        L.d("同步参数：" + HexUtil.encodeHexStr(bytes));
        return bytes;
    }


    public byte[] error(byte cmd, byte sn) {
        return new byte[]{0x5a, cmd, sn};
    }


    /**
     * 内容：需要恢复的内容，
     * 00：恢复全部参数为出厂设置，并重启
     * 01：仅重启设备
     * 02：查询已存储的上一条指令的执行结果（“回传”字段固定为0）
     * 03：清除计步数据
     * 04：清除睡眠数据
     *  回传：指定设备是否需要回传指令执行结果
     * 00：不需要回传
     * 01：需要回传
     *  结果：
     * 00：接收到指令
     * 01：仅重启完成
     * 02：指令执行正常完成
     * FF：指令执行失败
     */
    public byte[] restart(int content, boolean isReturn) {
        byte r = (byte) (isReturn ? 0x01 : 0x00);
        return new byte[]{0x5a, 0x0f, 0x00, (byte) content, r};
    }

    public byte[] queryBind() {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        defaultArray[1] = 0x0b;
        defaultArray[3] = 0x00;
        System.arraycopy(Contents.bindSign, 0, defaultArray, 4, 8);

        L.d("查询设备绑定状态：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }

    public byte[] requestBind() {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[1] = 0x0b;
        defaultArray[3] = 0x01;
        System.arraycopy(bindSign, 0, defaultArray, 4, 8);

        L.d("请求设备绑定：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }

    public byte[] veriftBind(byte Result) {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[0] = 0x5b;
        defaultArray[1] = 0x0b;
        defaultArray[3] = 0x02;
        defaultArray[4] = Result;
//        System.arraycopy(deviceNumber, 0, defaultArray, 4, 8);

        L.d("验证设备绑定结果：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }

    public byte[] removeBind(int action) {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[1] = 0x0b;
        defaultArray[3] = 0x03;
        System.arraycopy(bindSign, 0, defaultArray, 4, 8);
        defaultArray[12] = (byte) action;
        L.d("解除设备绑定：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }

    public byte[] last(boolean isOpen, int time) {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[1] = 0x0c;
        defaultArray[3] = 0x03;
        defaultArray[4] = (byte) (isOpen ? 0x01 : 0x00);
        byte[] times = ByteUtil.intToBytesG2(time);
        defaultArray[5] = times[0];
        defaultArray[6] = times[1];
        L.d("设备防丢：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }


    public byte[] find() {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[1] = 0x0c;
        defaultArray[3] = 0x06;
        L.d("查找设备：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }

    public byte[] sendPush(int type, byte[] message) {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[1] = 0x15;
        defaultArray[3] = (byte) type;

        if (message.length > 0)
            System.arraycopy(message, 0, defaultArray, 4, message.length);
        L.d("发送消息提醒：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }

    public byte[] pushPhone(String phone) {
        int len = phone.length();
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        defaultArray[1] = 0x15;
        defaultArray[3] = 0x01;
        defaultArray[4] = 0x08;
        if (!RegexUtils.checkMobile(phone) || !RegexUtils.checkPhone(phone)) return defaultArray;
        if (RegexUtils.checkPhone(phone)) {
            phone = phone.replace("+", "a");
        }
        for (int i = 0; i < 16 - len; i++) {
            phone = phone + "f";
        }
        defaultArray[4] = (byte) phone.length();
        L.d("发送电话号码：" + HexUtil.encodeHexStr(ByteUtil.hexStringToBytes(phone)));
        System.arraycopy(ByteUtil.hexStringToBytes(phone), 0, defaultArray, 5, 8);
        L.d("发送电话消息提醒：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }


    public byte[] readPrower() {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[1] = 0x0d;
        defaultArray[3] = (byte) 0x80;
        L.d("读取手环电量：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }

    public byte[] queryDeviceNumber() {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[1] = 0x0d;
        defaultArray[3] = (byte) 0x81;

        L.d("查询设备序号：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }

    public byte[] SetDeviceNumber(byte[] deviceNumber) {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[1] = 0x0d;
        defaultArray[3] = (byte) 0x82;

        System.arraycopy(deviceNumber, 0, defaultArray, 4, 16);

        L.d("设置设备序号：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }

    public byte[] pushPhoneName(String name) {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[1] = 0x0d;
        defaultArray[3] = (byte) 0x83;

        try {
            byte[] bytes = name.getBytes("UTF-8");
            System.arraycopy(bytes, 0, defaultArray, 4, bytes.length <= 16 ? bytes.length : 16);
            L.d("姓名：" + new String(bytes));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        L.d("发送来电备注：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }

    public byte[] pushPhoneName_DXY(String name) {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[1] = 0x15;
        defaultArray[3] = (byte) 0x10;

        try {
            byte[] bytes = name.getBytes("GBK");
            System.arraycopy(bytes, 0, defaultArray, 4, bytes.length <= 16 ? bytes.length : 16);
            L.d("姓名：" + new String(bytes));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        L.d("发送来电备注：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }


    public byte[] queryOneDaySteps(int year, int month, int day) {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[1] = 0x0d;
        defaultArray[3] = (byte) 0x86;
        defaultArray[4] = (byte) year;
        defaultArray[5] = (byte) month;
        defaultArray[6] = (byte) day;

        L.d("查询某天的总步数：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }


    public byte[] queryDeviceTime() {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[1] = 0x0d;
        defaultArray[3] = (byte) 0x87;

        L.d("查询设备时间：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }

    //time:前3位为起始年月日，后三位为结束年月日
    public byte[] synStepsData(byte[] time) {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[1] = 0x03;
        System.arraycopy(time, 0, defaultArray, 3, 6);
        System.arraycopy(Contents.deviceNumber, 0, defaultArray, 9, 8);

        L.d("开始同步计步时间：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }

    //同上
    public byte[] synSleepData(byte[] time) {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[1] = 0x07;
        System.arraycopy(time, 0, defaultArray, 3, 6);
        System.arraycopy(Contents.deviceNumber, 0, defaultArray, 9, 8);

        L.d("开始同步睡眠时间：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }

    byte[] ccitt;

    public byte[] sendLongMessagePush(int type, String message) {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[1] = 0x15;
        defaultArray[3] = (byte) 0xff;
        defaultArray[4] = (byte) type;

        byte[] msglen = ByteUtil.intToBytesG4(message.length());
        System.arraycopy(msglen, 0, defaultArray, 5, 4);

        ccitt = TalClass.crc_ccitt(message.getBytes(), message.length());
        defaultArray[9] = ccitt[0];
        defaultArray[10] = ccitt[1];
        byte[] packageLenght = ByteUtil.intToBytesG2(message.length());
        defaultArray[11] = packageLenght[0];
        defaultArray[12] = packageLenght[1];

        L.d("开始发送长包请求：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }


    public byte[] sendFirstPackage(int packLen) {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[1] = 0x05;
        defaultArray[2] = 0x01;
        byte[] packageLenght = ByteUtil.intToBytesG2(packLen);
        defaultArray[3] = packageLenght[0];
        defaultArray[4] = packageLenght[1];
        defaultArray[7] = ccitt[0];
        defaultArray[8] = ccitt[1];

        defaultArray[9] = 0x15;
        defaultArray[10] = (byte) (packLen % 16 == 0 ? (packLen / 16) : (packLen / 16 + 1));

        L.d("开始发送长包第一包：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }


    public byte[] sendMsgPackage(int itemNumber, byte[] itemData) {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[1] = 0x05;
        defaultArray[2] = (byte) (itemNumber + 2);

        System.arraycopy(itemData, 0, defaultArray, 3, itemData.length);

        L.d("开始发送数据包：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }


    public byte[] sendEndPackage(byte[] itemData) {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[1] = 0x05;
        defaultArray[2] = (byte) 0xff;

        System.arraycopy(itemData, 0, defaultArray, 3, itemData.length);

        L.d("开始结尾包数据包：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }

    public byte[] verfitLongMsg() {
        byte[] bytes = new byte[4];
        bytes[0] = 0x5a;
        bytes[1] = 0x05;
        bytes[2] = 0x00;
        bytes[3] = 0x00;
        bytes[4] = 0x00;
        L.d("发送方确认收到回应：" + HexUtil.encodeHexStr(bytes));
        return bytes;
    }

    public byte[] receiverSuccess(int packageId, boolean isSuccess) {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[1] = 0x06;
        byte[] bytes = ByteUtil.intToBytesG2(packageId);
        defaultArray[3] = bytes[0];
        defaultArray[4] = bytes[1];
        if (isSuccess) {
            defaultArray[3] = (byte) 0xff;
            defaultArray[4] = (byte) 0xff;
        }

        L.d("接收方确认完成:" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }


    public byte[] setAlarmPush(boolean isOpen, UserAlarmTab tab) {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] week = Utils.setWeekToBytes((byte) tab.getWeek());

        defaultArray[1] = 0x14;
        defaultArray[3] = 0x00;
        defaultArray[4] = 0x03;
        defaultArray[5] = (byte) (isOpen ? 0x01 : 0x00);
        defaultArray[6] = (byte) tab.getHour();
        defaultArray[7] = (byte) tab.getMin();
        if (tab.getType() != 1)
            System.arraycopy(week, 0, defaultArray, 8, 7);

        L.d("闹钟提醒：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }


    public byte[] setSedPush(boolean isOpen, int hour, int min) {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        defaultArray[1] = 0x14;
        defaultArray[4] = 0x02;
        defaultArray[5] = (byte) (isOpen ? 0x01 : 0x00);
        defaultArray[6] = (byte) hour;
        defaultArray[7] = (byte) min;
        defaultArray[8] = (byte) 8;
        defaultArray[9] = (byte) 0;
        defaultArray[10] = (byte) 23;
        defaultArray[11] = (byte) 0;

        L.d("久坐提醒：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }


    /**
     * timer提醒次数
     * 0:不提醒
     * 1：单次提醒
     * 2~0xFE：多次提醒的次数
     * 0xFF：无限次提醒
     */
    public byte[] setDainkPush(boolean isOpen, int number, int startHour, int startMin, int time, int cycler, int timer) {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        defaultArray[1] = 0x14;
        defaultArray[4] = 0x01;
        defaultArray[5] = (byte) (isOpen ? 0x01 : 0x00);
        defaultArray[6] = (byte) number;
        defaultArray[7] = (byte) startHour;
        defaultArray[8] = (byte) startMin;
        defaultArray[9] = (byte) time;
        defaultArray[10] = (byte) cycler;
        defaultArray[11] = (byte) timer;

        L.d("饮水提醒：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }


    //type:(0x04~0x1f)
    public byte[] eventPush(byte type, boolean isOpen, int month, int day, int hour, int min, int cycler) {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        defaultArray[1] = 0x14;
        defaultArray[4] = type;
        defaultArray[5] = (byte) (isOpen ? 0x01 : 0x00);
        defaultArray[6] = (byte) month;
        defaultArray[7] = (byte) day;
        defaultArray[8] = (byte) hour;
        defaultArray[9] = (byte) min;
        defaultArray[10] = (byte) cycler;

        L.d("事件提醒内容：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }


    public byte[] queryAlarm(int type, int num) {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[1] = 0x14;
        defaultArray[3] = 0x01;
        defaultArray[4] = (byte) type;
        defaultArray[5] = (byte) num;

        L.d("查询提醒类型参数：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }

    public byte[] querySwitchState(int type) {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[1] = 0x14;
        defaultArray[3] = 0x02;
        defaultArray[4] = (byte) type;

        L.d("查询定时提醒开关状态：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }

    public byte[] contorlApp(byte type, boolean isSuccess) {
        byte[] defaultArray = {0x5b, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[1] = 0x16;
        defaultArray[3] = type;
        defaultArray[4] = (byte) (isSuccess ? 0x01 : 0x00);

        L.d("反馈设备消息：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }

    public byte[] keyOrder(boolean isOpen, int time) {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[1] = 0x0c;
        defaultArray[3] = 0x04;
        defaultArray[4] = (byte) (isOpen ? 0x01 : 0x00);
        byte[] bytes = ByteUtil.intToBytesG2(time);
        defaultArray[5] = bytes[0];
        defaultArray[6] = bytes[1];

        L.d("安静锁指令：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }

    public byte[] setBright(boolean isOpen) {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[1] = 0x0c;
        defaultArray[3] = (byte) 0xfe;
        defaultArray[4] = (byte) (isOpen ? 0x01 : 0x00);

        L.d("抬手亮屏：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }

    public byte[] queryBright() {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[1] = 0x0c;
        defaultArray[3] = (byte) 0xfd;

        L.d("查询抬手亮屏：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }

    public byte[] queryANCS() {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[1] = 0x0c;
        defaultArray[3] = (byte) 0xfc;

        L.d("查询ANCS：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }


    public byte[] queryCharge() {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[1] = 0x0d;
        defaultArray[3] = (byte) 0x90;

        L.d("查询充电状态：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }

    public byte[] setSleepDate(int[] bytes) {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[1] = 0x0d;
        defaultArray[3] = (byte) 0xa0;
        defaultArray[4] = (byte) bytes[0];
        defaultArray[6] = (byte) bytes[1];

        L.d("设置睡眠时间：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }


    public byte[] getSleepDate() {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[1] = 0x0d;
        defaultArray[3] = (byte) 0xa1;

        L.d("获取睡眠时间：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }

    public byte[] shakeTakeThoto(boolean isOpen) {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[1] = 0x0c;
        defaultArray[3] = (byte) 0xe1;
        defaultArray[4] = (byte) (isOpen ? 0x01 : 0x00);

        L.d("摇一摇拍照：" + isOpen + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }

    public byte[] shakeTakePhotoVluse(int vluse) {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[1] = 0x0c;
        defaultArray[3] = (byte) 0xe2;
        defaultArray[4] = (byte) vluse;

        L.d("摇一摇拍照阈值：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }

    public byte[] queryDeviceVersion() {
        byte[] defaultArray = {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        defaultArray[1] = 0x10;
        L.d("查询设备版本：" + HexUtil.encodeHexStr(defaultArray));
        return defaultArray;
    }


    private byte[] singleHead(byte sign) {
        return new byte[]{0x5a, sign, 0x00};
    }

    private byte[] moreHead(byte sign, byte num) {
        return new byte[]{0x5a, sign, num};
    }


    public String binaryStringToHexString(boolean isMan, int age) {
        String binaryString = Integer.toBinaryString(age);
        if (binaryString.length() < 7) {
            for (int i = 0; i < 7 - binaryString.length(); i++) {
                binaryString = "0" + binaryString;
            }
        } else {
            binaryString.substring(binaryString.lastIndexOf(-7));
        }
        binaryString = isMan ? "0" + binaryString : "1" + binaryString;
        return binaryString;
    }


}
