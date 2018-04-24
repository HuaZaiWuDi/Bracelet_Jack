package com.lab.dxy.bracelet.entity;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：oden
 * 创建时间：2017/5/9
 */
public class AlarmItem {

    long addTime;
    byte type;
    byte hour;
    byte min;
    byte mode;
    byte week;
    boolean Enable;

    public AlarmItem() {
    }

    public AlarmItem(long addTime, byte type, byte hour, byte min, byte mode, byte week, boolean enable) {
        this.addTime = addTime;
        this.type = type;
        this.hour = hour;
        this.min = min;
        this.mode = mode;
        this.week = week;
        Enable = enable;
    }

    public boolean isEnable() {
        return Enable;
    }

    public void setEnable(boolean enable) {
        Enable = enable;
    }

    public long getAddTime() {
        return addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte getHour() {
        return hour;
    }

    public void setHour(byte hour) {
        this.hour = hour;
    }

    public byte getMin() {
        return min;
    }

    public void setMin(byte min) {
        this.min = min;
    }

    public byte getMode() {
        return mode;
    }

    public void setMode(byte mode) {
        this.mode = mode;
    }

    public byte getWeek() {
        return week;
    }

    public void setWeek(byte week) {
        this.week = week;
    }
}
