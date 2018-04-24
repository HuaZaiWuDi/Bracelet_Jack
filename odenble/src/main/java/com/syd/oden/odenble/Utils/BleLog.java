package com.syd.oden.odenble.Utils;

import android.util.Log;

/**
 * 项目名称：OdenDemo
 * 类描述：
 * 创建人：oden
 * 创建时间：2016/6/8 15:31
 */
public class BleLog {
    private  boolean isDebug = true;
    private  final String TAG = "OdenBle";
    private  String classTag = "";
    private  String bleTag = "";

    public BleLog(String classTag) {
        this.classTag = this.classTag + classTag;
    }

    public void d(String msg)
    {
        if (isDebug)
            Log.d(TAG, classTag + bleTag + msg);
    }

    public void e(String msg)
    {
        if (isDebug)
            Log.e(TAG, classTag + bleTag + msg);
    }

    public void w(String msg)
    {
        if (isDebug)
            Log.w(TAG, classTag + bleTag + msg);
    }

    public void setBleTag(String bleTag) {
        this.bleTag = bleTag;
    }
}
