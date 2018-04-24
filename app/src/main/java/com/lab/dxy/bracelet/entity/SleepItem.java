package com.lab.dxy.bracelet.entity;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：oden
 * 创建时间：2017/6/21
 */
public class SleepItem {


    int awake;
    int restless;
    int asleep;
    String date;

    public SleepItem() {
    }

    public SleepItem(int awake, int restless, int asleep, String date) {
        this.awake = awake;
        this.restless = restless;
        this.asleep = asleep;
        this.date = date;
    }

    public int getAwake() {
        return awake;
    }

    public int getRestless() {
        return restless;
    }

    public int getAsleep() {
        return asleep;
    }

    public String getDate() {
        return date;
    }
}
