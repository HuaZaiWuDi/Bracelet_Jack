package com.lab.dxy.bracelet.Utils;

import org.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import org.androidannotations.annotations.sharedpreferences.DefaultInt;
import org.androidannotations.annotations.sharedpreferences.DefaultLong;
import org.androidannotations.annotations.sharedpreferences.DefaultString;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

/**
 * Created by 华 on 2017/5/3.
 */
@SharedPref(value = SharedPref.Scope.UNIQUE)
public interface MyPrefs {

    @DefaultBoolean(true)
    boolean isFirstIn();

    @DefaultString("null")
    String ignoreVersion();

    //蓝牙信息记录
    @DefaultString("1111111")
    String blePassword();

    @DefaultString("")
    String bleName();

    @DefaultString("")
    String bleAddr();

    //手机状态
    @DefaultInt(0)
    int language();

    @DefaultString("未命名")
    String userName();

    @DefaultString("null")
    String userImg_Url();

    @DefaultInt(5000)
    int runAim();

    @DefaultInt(170)
    int Height();

    @DefaultInt(60)
    int Weight();

    @DefaultInt(0)
    int userSex();

    @DefaultInt(20)
    int userAge();

    @DefaultInt(1)
    int SeekValue();

    @DefaultInt(0)
    int PushEvent();

    @DefaultInt(0)
    int SedentaryEvent();

    @DefaultBoolean(false)
    boolean stepsIsOpen();

    @DefaultBoolean(false)
    boolean BrightIsOpen();

    @DefaultBoolean(false)
    boolean SedentaryIsOpen();

    @DefaultInt(30)
    int SedTime();

    @DefaultBoolean(false)
    boolean shakePhoto();

    @DefaultInt(0)
    int steps();

    @DefaultInt(0)
    int distanceAll();

    @DefaultInt(0)
    int calorie();

    @DefaultInt(0)
    int powerValue();

    @DefaultBoolean(true)
    boolean CallisOpen();

    @DefaultBoolean(true)
    boolean MSMisOpen();

    @DefaultBoolean(true)
    boolean whatAPPisOpen();

    @DefaultBoolean(true)
    boolean QQisOpen();

    @DefaultBoolean(true)
    boolean WeCharisOpen();

    @DefaultBoolean(false)
    boolean ClockisOpen();

    @DefaultBoolean(false)
    boolean SleepOpen();

    @DefaultInt(0)
    int camera();

    @DefaultString("23-8")
    String sleepData();


    @DefaultInt(0)
    int stepsTime();

    @DefaultInt(0)
    int stepsTimes();

    @DefaultInt(0)
    int sleepCleck();

    @DefaultString("")
    String userInfo();

    @DefaultBoolean(false)
    boolean isOpenAntiLast();

    @DefaultBoolean(false)
    boolean isOpenBGDisconnect();

    @DefaultLong(0)
    long isFirstSynTime();

    @DefaultInt(0)
    int otaVersion();

    @DefaultInt(0)
    int deviceVersion();

    @DefaultBoolean(true)
    boolean isAutoConnect();

    @DefaultInt(0)
    int heartRate();


}
