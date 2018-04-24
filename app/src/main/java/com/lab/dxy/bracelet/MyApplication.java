package com.lab.dxy.bracelet;

import android.app.Application;
import android.content.Context;

import com.activeandroid.ActiveAndroid;
import com.inuker.bluetooth.library.BluetoothClient;
import com.lab.dxy.bracelet.Utils.Utils;
import com.lab.dxy.bracelet.core.cache.ACache;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.tencent.bugly.crashreport.CrashReport;

import me.shaohui.shareutil.ShareConfig;
import me.shaohui.shareutil.ShareManager;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：Jack
 * 创建时间：2017/5/13
 */
public class MyApplication extends Application {
    public static Context mContext;
    public static BluetoothClient bluetoothClient;
    private final String QQ_ID = "1106404886";
    private final String WECHAR_ID = "wx5eb5ed3cb219593c";
    private final String WECHAR_SECRET = "560776ca29d1176948b72629cc40f813";
    private final String WB_ID = "1952223317";
    private final String BUGLY_ID = "27a0e43d92";

    public static boolean isFirstIn = true;
    public static ACache aCache;

    private RefWatcher refWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this.getApplicationContext();

        if (BuildConfig.USE_CANARY)
            refWatcher = LeakCanary.install(this);

        ActiveAndroid.initialize(this);
//        在开发测试阶段，可以在初始化Bugly之前通过以下接口把调试设备设置成“开发设备”。
        CrashReport.setIsDevelopmentDevice(this, true);
        CrashReport.initCrashReport(getApplicationContext(), BUGLY_ID, BuildConfig.LOG_DEBUG);
        aCache = ACache.get(this);

        bluetoothClient = new BluetoothClient(this);
        bluetoothClient.openBluetooth();
        Utils.getAndroidId(this);//获取手机唯一标识

        initShare();

    }

    public static Context getmContext() {
        return mContext;
    }

    public static RefWatcher getRefWatcher(Context context) {
        MyApplication application = (MyApplication) context.getApplicationContext();
        return application.refWatcher;
    }


    private void initShare() {
        ShareConfig config = ShareConfig.instance()
                .qqId(QQ_ID)
                .weiboId(WB_ID)
                .wxSecret(WECHAR_SECRET)
                .wxId(WECHAR_ID);

        ShareManager.init(config);

    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        ActiveAndroid.dispose();
    }
}
