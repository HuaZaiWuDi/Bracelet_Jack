package com.lab.dxy.bracelet.entity.spl;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;

import java.util.Arrays;
import java.util.List;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：Jack
 * 创建时间：2017/10/16
 */

@Table(name = "AppInfoTabs")
public class AppInfoTab extends Model {


    @Column(name = "appName")
    public String appName = ""; // 应用名

    @Column(name = "packageName")
    public String packageName = ""; // 包名


    public String versionName; // 版本名
    public int versionCode = 0; // 版本号

    @Column(name = "appIcon")
    public byte[] appIcon = null; // 应用图标

    @Column(name = "isOpen")
    public boolean isOpen = false;


    public AppInfoTab() {
        super();
    }

    public AppInfoTab(boolean isOpen, byte[] appIcon, String appName, String packageName) {
        this.isOpen = isOpen;
        this.appIcon = appIcon;
        this.appName = appName;
        this.packageName = packageName;
    }

    public AppInfoTab(String appName, String packageName, boolean isOpen) {
        this.appName = appName;
        this.packageName = packageName;
        this.isOpen = isOpen;
        appIcon = new byte[0];
    }

    public static List<AppInfoTab> getAll() {
        return new Select()
                .from(AppInfoTab.class)
                .orderBy("Id ASC")
                .execute();
    }

    public static List<AppInfoTab> getIsOpen() {
        return new Select()
                .from(AppInfoTab.class)
                .where("isOpen = ?", true)
                .orderBy("RANDOM()")
                .execute();
    }

    public static AppInfoTab getPackage(String packageName) {
        return new Select()
                .from(AppInfoTab.class)
                .where("packageName = ?", packageName)
                .orderBy("RANDOM()")
                .executeSingle();
    }


    public static void updateIsOpen(String packageName, boolean isOpen) {
        new Update(AppInfoTab.class)
                .set("isOpen=?", isOpen)
                .where("packageName = ?", packageName)
                .execute();
    }


    public static void deleteAll() {
        new Delete().from(AppInfoTab.class).execute();
    }

    public static void deleteforPackage(String packageName) {
        new Delete().from(AppInfoTab.class)
                .where("packageName = ?", packageName)
                .execute();
    }


    @Override
    public String toString() {
        return "AppInfoTab{" +
                "appName='" + appName + '\'' +
                ", packageName='" + packageName + '\'' +
                ", versionName='" + versionName + '\'' +
                ", versionCode=" + versionCode +
                ", appIcon=" + Arrays.toString(appIcon) +
                ", isOpen=" + isOpen +
                '}';
    }
}
