package com.lab.dxy.bracelet.entity;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

import static com.lab.dxy.bracelet.Utils.ApplicationInfoUtil.drawableToSQLite;

/**
 * @author 野虎
 *         2016年2月23日上午11:53:52
 */
public class AppInfo implements Serializable {
    public String appName; // 应用名
    public String packageName; // 包名
    public String versionName; // 版本名
    public int versionCode = 0; // 版本号
    public byte[] appIcon; // 应用图标
    public boolean isOpen;


    public AppInfo() {
    }

    public AppInfo(String appName, String packageName, String versionName, int versionCode, byte[] appIcon, boolean isOpen) {
        this.appName = appName;
        this.packageName = packageName;
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.appIcon = appIcon;
        this.isOpen = isOpen;
    }

    public AppInfo(AppInfo info, boolean isOpen) {
        this.appName = info.appName;
        this.packageName = info.packageName;
        this.versionName = info.versionName;
        this.versionCode = info.versionCode;
        this.appIcon = info.appIcon;
        this.isOpen = isOpen;
    }


    public AppInfo(String packageName, String appName, Drawable appIcon, boolean isOpen) {
        this.packageName = packageName;
        this.appName = appName;
        this.appIcon = drawableToSQLite(appIcon);
        this.isOpen = isOpen;
    }

    public AppInfo(String appName, String packageName, int versionCode, boolean isOpen) {
        this.appName = appName;
        this.packageName = packageName;
        this.versionCode = versionCode;
        this.isOpen = isOpen;
    }

    @Override
    public String toString() {
        return "AppInfo{" +
                "appName='" + appName + '\'' +
                ", packageName='" + packageName + '\'' +
                ", versionName='" + versionName + '\'' +
                ", versionCode=" + versionCode +
                ", appIcon=" + appIcon +
                ", isOpen=" + isOpen +
                '}';
    }

}