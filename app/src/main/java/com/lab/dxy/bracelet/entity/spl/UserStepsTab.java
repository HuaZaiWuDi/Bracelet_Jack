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
@Table(name = "UserStepsTabs")
public class UserStepsTab extends Model {

    @Column(name = "addTime")
    String addTime;

    @Column(name = "steps")
    int steps;

    @Column(name = "distanceAll")
    int distanceAll;

    @Column(name = "standardDay")
    int standardDay;

    public UserStepsTab() {
        super();
    }

    public UserStepsTab(String addTime, int steps) {
        this.addTime = addTime;
        this.steps = steps;
    }

    public UserStepsTab(String addTime, int steps, int distanceAll, int standardDay) {
        this.addTime = addTime;
        this.steps = steps;
        this.distanceAll = distanceAll;
        this.standardDay = standardDay;
    }

    public static List<UserStepsTab> getAll() {
        return new Select()
                .from(UserStepsTab.class)
                .orderBy("Id ASC")
                .execute();
    }


    public static UserStepsTab getAddTime(String addTime) {
        return new Select()
                .from(UserStepsTab.class)
                .where("addTime = ?", addTime)
                .orderBy("Id ASC")
                .executeSingle();
    }


    public static void DeleteAll() {
        new Delete()
                .from(UserStepsTab.class)
                .execute();
    }

    public static void DeleteAddTime(String addTime) {
        new Delete()
                .from(UserStepsTab.class)
                .where("addTime = ?", addTime)
                .execute();
    }


    public String getAddTime() {
        return addTime;
    }

    public int getSteps() {
        return steps;
    }

    public int getStandardDay() {
        return standardDay;
    }

    public int getDistanceAll() {
        return distanceAll;
    }


    @Override
    public String toString() {
        return "UserStepsTab{" +
                "addTime='" + addTime + '\'' +
                ", steps=" + steps +
                ", distanceAll=" + distanceAll +
                ", standardDay=" + standardDay +
                '}';
    }
}
