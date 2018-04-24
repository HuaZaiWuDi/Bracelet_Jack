package com.lab.dxy.bracelet.core;

import com.lab.dxy.bracelet.entity.spl.DayStepsTab;
import com.lab.dxy.bracelet.entity.spl.SleepDataTab;
import com.lab.dxy.bracelet.entity.spl.SleepDataTab2;
import com.lab.dxy.bracelet.entity.spl.SleepSettingTab;
import com.lab.dxy.bracelet.entity.spl.UserAlarmTab;
import com.lab.dxy.bracelet.entity.spl.UserInfoTab;
import com.lab.dxy.bracelet.entity.spl.UserStepsTab;

import static com.lab.dxy.bracelet.MyApplication.aCache;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：oden
 * 创建时间：2017/7/3
 */
public class MySqlManager {


    public static void DeleteAllInfo() {
        //本地数据库
        SleepDataTab.deleteAll();
        SleepDataTab2.deleteAll();
        UserStepsTab.DeleteAll();
        UserInfoTab.deleteAll();
        UserAlarmTab.deleteAll();
        SleepSettingTab.deleteAll();
        DayStepsTab.deleteAll();

    }
}
