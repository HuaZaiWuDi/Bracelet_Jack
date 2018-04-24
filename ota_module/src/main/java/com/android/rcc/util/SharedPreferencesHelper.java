package com.android.rcc.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2016/10/25.
 */
public class SharedPreferencesHelper {
    public final class Keys {
        public final static String SHARED_NAME = "com.android.rcc.Telink_OTA";
        public final static String SCAN_PERIOD = "com.android.rcc.ScanPeriod"; // 扫描时间
        public final static String OTA_START_DELAY = "com.android.rcc.OTA_START_DELAY"; // 开启OTA delay
        public final static String OTA_DELAY = "com.android.rcc.OTA_DELAY"; // Ota delay
        public final static String IS_NEED_PAIR = "com.android.rcc.IS_NEED_PAIR"; // 是否需要配对
        public final static String IS_READ_SUPPORT = "com.android.rcc.IS_READ_SUPPORT"; // 是否支持read


        private final static long DEFAULT_SCAN_PERIOD_SECOND = 10; // s
        private final static long DEFAULT_OTA_START_DELAY_SECOND = 0; // s
        private final static long DEFAULT_PKT_DELAY_MILL_SECOND = 20; // ms
    }

    public static void saveScanPeriod(Context context, long period) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Keys.SHARED_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putLong(Keys.SCAN_PERIOD, period).apply();
    }

    public static long getScanPeriod(Context context) {
        return context.getSharedPreferences(Keys.SHARED_NAME, Context.MODE_PRIVATE).getLong(Keys.SCAN_PERIOD, Keys.DEFAULT_SCAN_PERIOD_SECOND);
    }

    public static void saveOTAStartDelay(Context context, long delay) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Keys.SHARED_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putLong(Keys.OTA_START_DELAY, delay).apply();
    }

    public static long getOTAStartDelay(Context context) {
        return context.getSharedPreferences(Keys.SHARED_NAME, Context.MODE_PRIVATE).getLong(Keys.OTA_START_DELAY, Keys.DEFAULT_OTA_START_DELAY_SECOND);
    }


    public static void savePktDelay(Context context, long delay) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Keys.SHARED_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putLong(Keys.OTA_DELAY, delay).apply();
    }

    public static long getPktDelay(Context context) {
        return context.getSharedPreferences(Keys.SHARED_NAME, Context.MODE_PRIVATE).getLong(Keys.OTA_DELAY, Keys.DEFAULT_PKT_DELAY_MILL_SECOND);
    }


    public static void savePairInfo(Context context, boolean pair) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Keys.SHARED_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(Keys.IS_NEED_PAIR, pair).apply();
    }

    public static boolean getPairInfo(Context context) {
        return context.getSharedPreferences(Keys.SHARED_NAME, Context.MODE_PRIVATE).getBoolean(Keys.IS_NEED_PAIR, false);
    }

    public static void saveReadSupport(Context context, boolean support) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Keys.SHARED_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(Keys.IS_READ_SUPPORT, support).apply();
    }

    public static boolean getReadSupport(Context context) {
        return context.getSharedPreferences(Keys.SHARED_NAME, Context.MODE_PRIVATE).getBoolean(Keys.IS_READ_SUPPORT, true);
    }

}
