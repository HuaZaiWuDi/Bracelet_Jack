package com.lab.dxy.bracelet.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：oden
 * 创建时间：2017/8/31
 */
public class AppInfoItem implements Serializable {

    List<AppInfo> appInfos;


    public AppInfoItem() {
    }

    public AppInfoItem(List<AppInfo> appInfos) {
        this.appInfos = appInfos;
    }

    public List<AppInfo> getAppInfos() {
        return appInfos;
    }

    public void setAppInfos(List<AppInfo> appInfos) {
        this.appInfos = appInfos;
    }

    @Override
    public String toString() {
        return "AppInfoItem{" +
                "appInfos=" + appInfos +
                '}';
    }
}
