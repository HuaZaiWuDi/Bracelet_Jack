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
 * 创建时间：2017/7/6
 */
@Table(name = "SleepSettingTabs")
public class SleepSettingTab extends Model{

    @Column(name = "date")
    String date="";
    @Column(name = "startTime")
    String startTime = "0";

    @Column(name = "endTime")
    String endTime = "0";


    public SleepSettingTab() {
        super();
    }

    public SleepSettingTab(String date, String startTime, String endTime) {
        this.date = date;
        this.endTime = endTime;
        this.startTime = startTime;
    }


    public static List<SleepSettingTab> getAll() {
        return new Select()
                .from(SleepSettingTab.class)
                .orderBy("Id ASC")
                .execute();
    }



    public static SleepSettingTab getSleepDate(String date) {
        return new Select()
                .from(SleepSettingTab.class)
                .where("date = ?", date)
                .orderBy("RANDOM()")
                .executeSingle();
    }

    public static void deleteAll() {
        new Delete().from(SleepSettingTab.class).execute();
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
