package com.lab.dxy.bracelet.entity.spl;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;

import java.io.Serializable;
import java.util.List;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：Jack
 * 创建时间：2017/5/9
 */
@Table(name = "UserAlarmTabs")
public class UserAlarmTab extends Model implements Serializable {

    @Column(name = "addTime")
    public long addTime;

    @Column(name = "hour")
    public int hour;

    @Column(name = "min")
    public int min;

    @Column(name = "mode")
    public int mode;

    @Column(name = "week")
    public int week;

    @Column(name = "type")
    public int type;

    @Column(name = "Enable")
    public boolean Enable = false;


    public UserAlarmTab() {
        super();
    }

    public UserAlarmTab(long addTime, int hour, int min, int mode, int week, int type, boolean enable) {
        this.addTime = addTime;
        this.hour = hour;
        this.min = min;
        this.mode = mode;
        this.week = week;
        this.type = type;
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

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public static List<UserAlarmTab> getAll() {
        return new Select()
                .from(UserAlarmTab.class)
                .orderBy("addTime ASC")
                .execute();
    }

    public static UserAlarmTab getaddTime(long addTime) {
        return new Select()
                .from(UserAlarmTab.class)
                .where("addTime = ?", addTime)
                .orderBy("RANDOM()")
                .executeSingle();
    }

    public static void updateEnable(boolean enable, long addTime) {
        new Update(UserAlarmTab.class).set("Enable = ?", enable).where("addTime = ?", addTime).execute();
    }


    public static void deletByAddedTime(long addedTime) {
        new Delete().from(UserAlarmTab.class).where("addTime = ?", addedTime).execute();
    }

    public static void deleteAll() {
        new Delete().from(UserAlarmTab.class).execute();
    }


    @Override
    public String toString() {
        return "UserAlarmTab{" +
                "addTime=" + addTime +
                ", hour=" + hour +
                ", min=" + min +
                ", mode=" + mode +
                ", week=" + week +
                ", type=" + type +
                ", Enable=" + Enable +
                '}';
    }


}
