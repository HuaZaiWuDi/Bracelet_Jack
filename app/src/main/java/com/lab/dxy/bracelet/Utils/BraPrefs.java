package com.lab.dxy.bracelet.Utils;

import org.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import org.androidannotations.annotations.sharedpreferences.DefaultInt;
import org.androidannotations.annotations.sharedpreferences.DefaultString;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：Jack
 * 创建时间：2017/5/26
 */
@SharedPref(value= SharedPref.Scope.UNIQUE)
public interface BraPrefs {


    @DefaultString("")
    String bleName();

    @DefaultString("")
    String bleAddr();

    @DefaultInt(0)
    int powerValue();


    @DefaultBoolean(true)
    boolean isFirstIn();

    @DefaultBoolean(false)
    boolean isOpenANCS();

}
