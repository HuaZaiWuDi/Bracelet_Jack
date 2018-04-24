package com.lab.dxy.bracelet.entity.spl;


import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.util.List;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：Jack
 * 创建时间：2017/6/10
 */
@Table(name = "SleepDataTab2s")
public class SleepDataTab2 extends Model {

    @Column(name = "date")
    String date = "";

    @Column(name = "type")
    int type = 0;
    @Column(name = "Duration")
    int Duration = 0;

    @Column(name = "Time")
    String Time = "00:00";


    public SleepDataTab2() {
        super();
    }

    public SleepDataTab2(String date, int type, int duration, String Time) {
        this.date = date;
        this.type = type;
        Duration = duration;
        this.Time = Time;
    }


    public static List<SleepDataTab2> getAll() {
        return new Select()
                .from(SleepDataTab2.class)
                .orderBy("Id ASC")
                .execute();
    }


    public static List<SleepDataTab2> getSleepDate(String date) {
        return new Select()
                .from(SleepDataTab2.class)
                .where("date = ?", date)
                .orderBy("Id ASC")
                .execute();
    }


    public static void deleteAll() {
        new Delete().from(SleepDataTab2.class).execute();
    }

    public static void deleteByDate(String date) {
        new Delete().from(SleepDataTab2.class)
                .where("date = ?", date)
                .execute();
    }


    public int getType() {
        return type;
    }

    public String getDate() {
        return date;
    }

    public int getDuration() {
        return Duration;
    }

    public String getTime() {
        return Time;
    }
}
