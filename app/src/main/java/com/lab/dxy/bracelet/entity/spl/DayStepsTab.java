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
 * 创建人：oden
 * 创建时间：2017/10/26
 */

@Table(name = "DayStepsTabs")
public class DayStepsTab extends Model {

    @Column(name = "date")
    String date = null;


    @Column(name = "steps")
    int steps = 0;

    @Column(name = "isTotal")
    boolean isTotal = false;


    public DayStepsTab() {
    }

    public DayStepsTab(String date, int steps, boolean isTotal) {
        this.date = date;
        this.steps = steps;
        this.isTotal = isTotal;
    }

    public static List<DayStepsTab> getAll() {
        return new Select()
                .from(DayStepsTab.class)
                .orderBy("Id ASC")
                .execute();
    }


    public static List<DayStepsTab> getByDate(String date) {
        return new Select()
                .from(DayStepsTab.class)
                .where("date = ?", date)
                .orderBy("Id ASC")
                .execute();
    }


    public static DayStepsTab getTotalByDate(String date) {
        return new Select()
                .from(DayStepsTab.class)
                .where("date = ?  and  isTotal = ? ", date, true)
                .executeSingle();
    }


    public static void deleteAll() {
        new Delete().from(DayStepsTab.class).execute();
    }


    public static void deleteforDate(String date) {
        new Delete().from(DayStepsTab.class)
                .where("date = ?", date)
                .execute();
    }


    public boolean isTotal() {
        return isTotal;
    }

    public void setTotal(boolean total) {
        isTotal = total;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    @Override
    public String toString() {
        return "DayStepsTab{" +
                "date='" + date + '\'' +
                ", steps=" + steps +
                '}';
    }
}
