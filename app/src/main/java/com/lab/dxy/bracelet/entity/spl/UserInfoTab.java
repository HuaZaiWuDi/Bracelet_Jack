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
 * 创建时间：2017/5/26
 */
@Table(name = "UserInfoTabs")
public class UserInfoTab extends Model{

    @Column(name = "macAddr")
    public String macAddr;

    @Column(name = "BraName")
    public String BraName;

    @Column(name = "userImage")
    public String userImage;

    @Column(name = "UserInfoTab")
    public UserAlarmTab userAlarmTab;


    public UserInfoTab() {
        super();
    }

    public UserInfoTab(String macAddr, String braName, String userImage) {
        this.macAddr = macAddr;
        BraName = braName;
        this.userImage = userImage;
    }

    public UserInfoTab(String macAddr, UserAlarmTab userAlarmTab) {
        this.macAddr=macAddr;
        this.userAlarmTab = userAlarmTab;
    }

    public static List<UserInfoTab> getAll() {
        return new Select()
                .from(UserInfoTab.class)
                .orderBy("macAddr ASC")
                .execute();
    }

    public static UserInfoTab getByMacAddr(String macAddr) {
        return new Select().from(UserInfoTab.class).where("macAddr = ?", macAddr).orderBy("RANDOM()").executeSingle();
    }

    public static void deletByMacAddr(String macAddr) {
        new Delete().from(UserInfoTab.class).where("macAddr = ?", macAddr).execute();
    }
    public static void deleteAll() {
        new Delete().from(UserInfoTab.class).execute();
    }

    public String getMacAddr() {
        return macAddr;
    }

    public void setMacAddr(String macAddr) {
        this.macAddr = macAddr;
    }

    public String getBraName() {
        return BraName;
    }

    public void setBraName(String braName) {
        BraName = braName;
    }


    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public UserAlarmTab getUserAlarmTab() {
        return userAlarmTab;
    }

    public void setUserAlarmTab(UserAlarmTab userAlarmTab) {
        this.userAlarmTab = userAlarmTab;
    }
}
