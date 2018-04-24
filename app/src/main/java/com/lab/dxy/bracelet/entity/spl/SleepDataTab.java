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
@Table(name = "SleepDataTabs")
public class SleepDataTab extends Model {

    @Column(name = "date")
    String date="";
    @Column(name = "startTime")
    String startTime = "00:00";

    @Column(name = "endTime")
    String endTime = "00:00";


    public SleepDataTab() {
        super();
    }

    public SleepDataTab(String date, String endTime, String startTime) {
        this.date = date;
        this.endTime = endTime;
        this.startTime = startTime;
    }


    public static List<SleepDataTab> getAll() {
        return new Select()
                .from(SleepDataTab.class)
                .orderBy("Id ASC")
                .execute();
    }

    public static SleepDataTab getSleepDate(String date) {
        return new Select()
                .from(SleepDataTab.class)
                .where("date = ?", date)
                .orderBy("RANDOM()")
                .executeSingle();
    }


    public static void deleteAll() {
        new Delete().from(SleepDataTab.class).execute();
    }


    public String getDate() {
        return date;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }
}
