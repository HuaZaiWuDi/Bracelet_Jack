package com.lab.dxy.bracelet.Utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.lab.dxy.bracelet.Contents;
import com.lab.dxy.bracelet.MyApplication;
import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.activity.MainActivity_;
import com.syd.oden.odenble.Utils.HexUtil;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import static com.lab.dxy.bracelet.Contents.bindSign;
import static com.lab.dxy.bracelet.MyApplication.mContext;

/**
 * Created by 华 on 2017/5/2.
 */

public class Utils {


    public static int DATE = 0;
    public static int NUMBER = 1;


    public static String getDEVICE() {
        return Build.DEVICE;
    }

    public static String getDeviceInfo() {
        String handSetInfo =
                "手机型号：" + Build.DEVICE +
                        "\n系统版本：" + Build.VERSION.RELEASE +
                        "\nSDK版本：" + Build.VERSION.SDK_INT;
        return handSetInfo;
    }


    public static String getVersionName(Context context) {
        String versionName = "";
        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            Contents.versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            L.d("appVersionInfo:" + versionName + "code:" + Contents.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }


    /**
     * 判断应用是否处于后台,这个测试一直处于前台,可能跟service有关
     */
    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                    Log.i("后台", appProcess.processName);
                    return true;
                } else {
                    Log.i("前台", appProcess.processName);
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * 判断应用是否处于后台
     */
    public static boolean isApplicationBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
//                Log.i("后台", topActivity.getPackageName());
                return true;
            }
        }
//        Log.i("前台", context.getPackageName());
        return false;
    }


    public static void setStatusBarColor(Activity activity, @ColorInt int color) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                Window window = activity.getWindow();
                //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                //设置状态栏颜色

                window.setStatusBarColor(color);
                ViewGroup mContentView = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
                View mChildView = mContentView.getChildAt(0);
                if (mChildView != null) {
                    //注意不是设置 ContentView 的 FitsSystemWindows, 而是设置 ContentView 的第一个子 View . 预留出系统 View 的空间.
                    ViewCompat.setFitsSystemWindows(mChildView, true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                L.d("问题");
            }
        }
    }


    //图像旋转角度显示，作用于Glide
    public static class RotateTransformation extends BitmapTransformation {

        private float rotateRotationAngle = 0f;

        public RotateTransformation(Context context, float rotateRotationAngle) {
            super(context);

            this.rotateRotationAngle = rotateRotationAngle;
        }

        @Override
        protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            Matrix matrix = new Matrix();

            matrix.postRotate(rotateRotationAngle);

            return Bitmap.createBitmap(toTransform, 0, 0, toTransform.getWidth(), toTransform.getHeight(), matrix, true);
        }

        @Override
        public String getId() {
            return "rotate" + rotateRotationAngle;
        }
    }


    //应用内的广播，广播只在应用内传播，同时也不会接受其他App的同action的广播
    public static void LocalBroadCast(String action) {
        LocalBroadcastManager.getInstance(MyApplication.getmContext())
                .sendBroadcast(new Intent(action));
    }

    public static void broadUpdate(final String action) {
        final Intent intent = new Intent(action);
        intent.setPackage(MyApplication.getmContext().getPackageName());
        MyApplication.getmContext().sendBroadcast(intent);
    }

    public static void broadUpdate(final String action, String key, String value) {
        final Intent intent = new Intent(action);
        intent.putExtra(key, value);
        intent.setPackage(MyApplication.getmContext().getPackageName());
        MyApplication.getmContext().sendBroadcast(intent);
    }

    public static void broadUpdate(final String action, String key, int value) {
        final Intent intent = new Intent(action);
        intent.putExtra(key, value);
        intent.setPackage(MyApplication.getmContext().getPackageName());
        MyApplication.getmContext().sendBroadcast(intent);
    }

    public static void broadUpdate(final String action, String key, byte value) {
        final Intent intent = new Intent(action);
        intent.putExtra(key, value);
        intent.setPackage(MyApplication.getmContext().getPackageName());
        MyApplication.getmContext().sendBroadcast(intent);
    }

    public static void broadUpdate(final String action, String key, int[] value) {
        final Intent intent = new Intent(action);
        intent.putExtra(key, value);
        intent.setPackage(MyApplication.getmContext().getPackageName());
        MyApplication.getmContext().sendBroadcast(intent);
    }

    public static void broadUpdate(final String action, String key, byte[] value) {
        final Intent intent = new Intent(action);
        intent.putExtra(key, value);
        intent.setPackage(MyApplication.getmContext().getPackageName());
        MyApplication.getmContext().sendBroadcast(intent);
    }

    public static void broadUpdate(final String action, String key, long value) {
        final Intent intent = new Intent(action);
        intent.putExtra(key, value);
        intent.setPackage(MyApplication.getmContext().getPackageName());
        MyApplication.getmContext().sendBroadcast(intent);
    }

    public static void mSleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static int[] ccitt_table = new int[]{
            0x0000, 0x1021, 0x2042, 0x3063, 0x4084, 0x50A5, 0x60C6, 0x70E7,
            0x8108, 0x9129, 0xA14A, 0xB16B, 0xC18C, 0xD1AD, 0xE1CE, 0xF1EF, 0x1231, 0x0210, 0x3273, 0x2252, 0x52B5, 0x4294, 0x72F7, 0x62D6,
            0x9339, 0x8318, 0xB37B, 0xA35A, 0xD3BD, 0xC39C, 0xF3FF, 0xE3DE, 0x2462, 0x3443, 0x0420, 0x1401, 0x64E6, 0x74C7, 0x44A4, 0x5485,
            0xA56A, 0xB54B, 0x8528, 0x9509, 0xE5EE, 0xF5CF, 0xC5AC, 0xD58D, 0x3653, 0x2672, 0x1611, 0x0630, 0x76D7, 0x66F6, 0x5695, 0x46B4,
            0xB75B, 0xA77A, 0x9719, 0x8738, 0xF7DF, 0xE7FE, 0xD79D, 0xC7BC, 0x48C4, 0x58E5, 0x6886, 0x78A7, 0x0840, 0x1861, 0x2802, 0x3823,
            0xC9CC, 0xD9ED, 0xE98E, 0xF9AF, 0x8948, 0x9969, 0xA90A, 0xB92B, 0x5AF5, 0x4AD4, 0x7AB7, 0x6A96, 0x1A71, 0x0A50, 0x3A33, 0x2A12,
            0xDBFD, 0xCBDC, 0xFBBF, 0xEB9E, 0x9B79, 0x8B58, 0xBB3B, 0xAB1A, 0x6CA6, 0x7C87, 0x4CE4, 0x5CC5, 0x2C22, 0x3C03, 0x0C60, 0x1C41,
            0xEDAE, 0xFD8F, 0xCDEC, 0xDDCD, 0xAD2A, 0xBD0B, 0x8D68, 0x9D49, 0x7E97, 0x6EB6, 0x5ED5, 0x4EF4, 0x3E13, 0x2E32, 0x1E51, 0x0E70,
            0xFF9F, 0xEFBE, 0xDFDD, 0xCFFC, 0xBF1B, 0xAF3A, 0x9F59, 0x8F78, 0x9188, 0x81A9, 0xB1CA, 0xA1EB, 0xD10C, 0xC12D, 0xF14E, 0xE16F,
            0x1080, 0x00A1, 0x30C2, 0x20E3, 0x5004, 0x4025, 0x7046, 0x6067, 0x83B9, 0x9398, 0xA3FB, 0xB3DA, 0xC33D, 0xD31C, 0xE37F, 0xF35E,
            0x02B1, 0x1290, 0x22F3, 0x32D2, 0x4235, 0x5214, 0x6277, 0x7256, 0xB5EA, 0xA5CB, 0x95A8, 0x8589, 0xF56E, 0xE54F, 0xD52C, 0xC50D,
            0x34E2, 0x24C3, 0x14A0, 0x0481, 0x7466, 0x6447, 0x5424, 0x4405, 0xA7DB, 0xB7FA, 0x8799, 0x97B8, 0xE75F, 0xF77E, 0xC71D, 0xD73C,
            0x26D3, 0x36F2, 0x0691, 0x16B0, 0x6657, 0x7676, 0x4615, 0x5634, 0xD94C, 0xC96D, 0xF90E, 0xE92F, 0x99C8, 0x89E9, 0xB98A, 0xA9AB,
            0x5844, 0x4865, 0x7806, 0x6827, 0x18C0, 0x08E1, 0x3882, 0x28A3, 0xCB7D, 0xDB5C, 0xEB3F, 0xFB1E, 0x8BF9, 0x9BD8, 0xABBB, 0xBB9A,
            0x4A75, 0x5A54, 0x6A37, 0x7A16, 0x0AF1, 0x1AD0, 0x2AB3, 0x3A92, 0xFD2E, 0xED0F, 0xDD6C, 0xCD4D, 0xBDAA, 0xAD8B, 0x9DE8, 0x8DC9,
            0x7C26, 0x6C07, 0x5C64, 0x4C45, 0x3CA2, 0x2C83, 0x1CE0, 0x0CC1, 0xEF1F, 0xFF3E, 0xCF5D, 0xDF7C, 0xAF9B, 0xBFBA, 0x8FD9, 0x9FF8,
            0x6E17, 0x7E36, 0x4E55, 0x5E74, 0x2E93, 0x3EB2, 0x0ED1, 0x1EF0};


     /*   typedef unsigned char u8 ;
        typedef signed char s8;

        typedef unsigned short u16;
        typedef signed short s16;

        typedef int s32;
        typedef unsigned int u32;

        typedef long long s64;
        typedef unsigned long long u64*/

//        int cal_crc16(char *data,int length){
//            int tmp_crc = 0;
//           while(length-- > 0){
//                tmp_crc = ccitt_table[((tmp_crc>> 8) ^ (data++)) & 0xff] ^ (tmp_crc<< 8);
//            }
//            return (tmp_crc & 0xFFFF);
//        }


    public byte[] cal_crc16(byte[] data, int length) {
        int tmp_crc = 0;
        int i = 0;
        while (length-- > 0) {
            tmp_crc = ccitt_table[((tmp_crc >> 8) ^ (data[i])) & 0xff] ^ (tmp_crc << 8);
            i++;
        }
        return intToBytes(tmp_crc & 0xFFFF);
    }


    /**
     * 将int数值转换为占四个字节的byte数组，本方法适用于(低位在前，高位在后)的顺序。 和bytesToInt（）配套使用
     *
     * @param value 要转换的int值
     * @return byte数组
     */
    public static byte[] intToBytes(int value) {
        byte[] src = new byte[4];
        src[3] = (byte) ((value >> 24) & 0xFF);
        src[2] = (byte) ((value >> 16) & 0xFF);
        src[1] = (byte) ((value >> 8) & 0xFF);
        src[0] = (byte) (value & 0xFF);
        return src;
    }


    /**
     * 将int数值转换为占四个字节的byte数组，本方法适用于(高位在前，低位在后)的顺序。  和bytesToInt2（）配套使用
     */
    public static byte[] intToBytes2(int value) {
        byte[] src = new byte[4];
        src[0] = (byte) ((value >> 24) & 0xFF);
        src[1] = (byte) ((value >> 16) & 0xFF);
        src[2] = (byte) ((value >> 8) & 0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }

    /**
     * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序，和和intToBytes（）配套使用
     *
     * @param src    byte数组
     * @param offset 从数组的第offset位开始
     * @return int数值
     */
    public static int bytesToInt(byte[] src, int offset) {
        int value;
        value = (int) ((src[offset] & 0xFF)
                | ((src[offset + 1] & 0xFF) << 8)
                | ((src[offset + 2] & 0xFF) << 16)
                | ((src[offset + 3] & 0xFF) << 24));
        return value;
    }


    /**
     * byte数组中取int数值，本方法适用于(高位在前，低位在后)的顺序，和和intToBytes（）配套使用
     *
     * @param src    byte数组
     * @param offset 从数组的第offset位开始
     * @return int数值
     */
    public static int bytesToInt2(byte[] src, int offset) {
        int value;
        value = (int) (((src[offset] & 0xFF) << 24)
                | ((src[offset + 1] & 0xFF) << 16)
                | ((src[offset + 2] & 0xFF) << 8)
                | (src[offset + 3] & 0xFF));
        return value;
    }


    /**
     * int到byte[]
     * <p>
     * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序，bytes2ToInt（）配套使用
     *
     * @param i
     * @return
     */
    public static byte[] intToByteArray2(int i) {
        byte[] result = new byte[2];

        result[0] = (byte) (i & 0xFF);
        result[1] = (byte) ((i >> 8) & 0xFF);
//        L.d("result: " + Tools.encodeHexStr(result));
        return result;
    }


    public static int bytes2ToInt(byte[] bytes) {
        int value = 0;
        value = (int) ((bytes[0] & 0xFF)
                | ((bytes[1] & 0xFF) << 8));
        return value;
    }


    public static String setRepeatString(byte data) {
        String repeat = "";
        L.d("setRepeat: " + data);

        switch (data) {
            case 0x01:
                repeat = mContext.getString(R.string.Sun);
                break;
            case 0x02:
                repeat = mContext.getString(R.string.Mon);
                break;
            case 0x04:
                repeat = mContext.getString(R.string.Tue);
                break;
            case 0x08:
                repeat = mContext.getString(R.string.Wed);
                break;
            case 0x10:
                repeat = mContext.getString(R.string.Thu);
                break;
            case 0x20:
                repeat = mContext.getString(R.string.Fri);
                break;
            case 0x40:
                repeat = mContext.getString(R.string.Sat);
                break;
        }
        return repeat;
    }


    public static byte setWeekToByte(byte dayOfWeek) {
        byte repeatData = 0;
        switch (dayOfWeek) {
            case 1:
                repeatData = 0x06;
                break;
            case 2:
                repeatData = 0x00;
                break;
            case 3:
                repeatData = 0x01;
                break;
            case 4:
                repeatData = 0x02;
                break;
            case 5:
                repeatData = 0x03;
                break;
            case 6:
                repeatData = 0x04;
                break;
            case 7:
                repeatData = 0x05;
                break;
        }
        return repeatData;
    }


    public static String setRepeatStringToDay(byte data) {
        String repeat = "";
        L.d("setRepeat: " + data);

        if ((data & 0x02) == 0x02) {
            repeat = mContext.getString(R.string.Mon);
        }
        if ((data & 0x04) == 0x04) {
            repeat = repeat + mContext.getString(R.string.Tue);
        }
        if ((data & 0x08) == 0x08) {
            repeat = repeat + mContext.getString(R.string.Wed);
        }
        if ((data & 0x10) == 0x10) {
            repeat = repeat + mContext.getString(R.string.Thu);
        }
        if ((data & 0x20) == 0x20) {
            repeat = repeat + mContext.getString(R.string.Fri);
        }
        if ((data & 0x40) == 0x40) {
            repeat = repeat + mContext.getString(R.string.Sat);
        }
        if ((data & 0x01) == 0x01) {
            repeat = repeat + mContext.getString(R.string.Sun);
        }
        if (data == 0x00) {
            repeat = mContext.getString(R.string.Never);
        }
        if (data == 0x7f) {
            repeat = mContext.getString(R.string.Every);
        }
        return repeat;
    }

    public static byte[] setWeekToBytes(byte data) {
        byte[] week = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        if ((data & 0x02) == 0x02) {
            week[0] = 0x01;
        }
        if ((data & 0x04) == 0x04) {
            week[1] = 0x01;
        }
        if ((data & 0x08) == 0x08) {
            week[2] = 0x01;
        }
        if ((data & 0x10) == 0x10) {
            week[3] = 0x01;
        }
        if ((data & 0x20) == 0x20) {
            week[4] = 0x01;
        }
        if ((data & 0x40) == 0x40) {
            week[5] = 0x01;
        }
        if ((data & 0x01) == 0x01) {
            week[6] = 0x01;
        }

        L.d("week:" + HexUtil.encodeHexStr(week));
        return week;
    }

    public static byte setWeekToBytes(byte[] data) {
        byte repeatData = 0x00;

        for (int i = 0; i < data.length; i++) {
            if (data[i] == 0x01) {
                repeatData = (byte) (repeatData | (0x01 << (i == data.length - 1 ? 0 : (i + 1))));
            }
        }

        L.d("repeatData:" + repeatData);
        return repeatData;
    }

    public static boolean bytesIsEmpty(byte[] bytes) {
        if (bytes.length == 0) return true;
        int i = 0;
        for (byte b : bytes) {
            if (b == 0x00) {
                i++;
            }
        }
        return i == bytes.length;
    }


    public static String setTypeString(byte type) {
        String typeString = "";
        switch (type) {
            case 0x00:
                typeString = "未用";
                break;
            case 0x01:
                typeString = "一次";
                break;
            case 0x02:
                typeString = "连续";
                break;
        }
        return typeString;
    }


    public static String setModeString(byte mode) {
        String modeString = "";
        switch (mode) {
            case 0x00:
                modeString = "没有事件";
                break;
            case 0x01:
                modeString = "马达震动";
                break;
            case 0x02:
                modeString = "LED闪烁";
                break;
            case 0x03:
                modeString = "闹铃";
                break;
        }
        return modeString;
    }


    //自动安装apk
    public static void openFile(Context mContext, File file) {
        // TODO Auto-generated method stub

//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        // 由于没有在Activity环境下启动Activity,设置下面的标签
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//        if (Build.VERSION.SDK_INT >= 24) { //判读版本是否在7.0以上
//            //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
//            Uri apkUri = FileProvider.getUriForFile(mContext, "com.jack.dxy.bracelet", file);
//            //添加这一句表示对目标应用临时授权该Uri所代表的文件
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
//        } else {
//            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
//        }
//        mContext.startActivity(intent);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri contentUri;
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            contentUri = FileProvider.getUriForFile(mContext, "com.lab.dxy.bracelet", file);
        } else {
            contentUri = Uri.fromFile(file);

        }
        intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        mContext.startActivity(intent);
        openApk(mContext, file);
    }


    /**
     * 打开已经安装好的apk
     */
    private static void openApk(Context context, File file) {
        L.d("file:" + file.getPath());
        PackageManager manager = context.getPackageManager();
        // 这里的是你下载好的文件路径
        PackageInfo info = manager.getPackageArchiveInfo(file.getPath(), PackageManager.GET_ACTIVITIES);
        if (info != null) {
            Intent intent = manager.getLaunchIntentForPackage(info.applicationInfo.packageName);
            context.startActivity(intent);
        }
    }

    //zh：汉语  en：英语
    public static boolean isZh(Context mContext) {
        Locale locale = mContext.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh"))
            return true;
        else
            return false;
    }

    public static String setFormat(long value, String format, int Type) {
        if (Type == DATE) {
            return new SimpleDateFormat(format, Locale.getDefault()).format(value);
        } else if (Type == NUMBER) {
            return new DecimalFormat(format).format(value);
        }
        return "";
    }

    public static Date setParseDate(String value, String format) {

        try {
            return new SimpleDateFormat(format, Locale.getDefault()).parse(value);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Number setParseNumber(String value, String format) {
        try {
            return new DecimalFormat(format).parse(value);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    // 获取当前时间所在周的开始日期
    public static Date getFirstDayOfWeek(Date date) {
        Calendar c = new GregorianCalendar();
        c.setFirstDayOfWeek(Calendar.SUNDAY);
        c.setTime(date);
        c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek()); // Monday
        Date time = c.getTime();
        time.getDay();
        return c.getTime();
    }

    // 获取当前时间所在周的结束日期
    public static Date getLastDayOfWeek(Date date) {
        Calendar c = new GregorianCalendar();
        c.setFirstDayOfWeek(Calendar.SUNDAY);
        c.setTime(date);
        c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek() + 6); // Sunday
        return c.getTime();
    }

    // 获取当前时间所在周的结束日期
    public static Date getDayOfWeekEvery(Date date, int day) {
        Calendar c = new GregorianCalendar();
        c.setFirstDayOfWeek(Calendar.SUNDAY);
        c.setTime(date);
        c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek() + day); // day==0为Sunday
        return c.getTime();
    }

    public static Date getEveryDayOfMonth(Date date, int day) {
        Calendar c = new GregorianCalendar();
        c.setTime(date);
        c.add(Calendar.MONTH, 0);
        c.set(Calendar.DAY_OF_MONTH, day);//设置为1号,当前日期既为本月第一天
        return c.getTime();
    }


    public static String intToString(int i) {
        if (i < 10) {
            return "0" + i;
        } else {
            return i + "";
        }
    }

    /**
     * 得到app名字
     *
     * @param context 上下文
     * @return String
     */
    public static String getAppName(Context context) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if (packageInfo != null) {
                return packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * 得到app图标
     *
     * @param context 上下文
     * @return Drawable
     */
    public static Drawable getAppIcon(Context context) {
        try {
            return context.getPackageManager().getApplicationIcon(context.getPackageName());


        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 得到androidId
     *
     * @param context 上下文
     * @return byte[]
     */
    public static void getAndroidId(Context context) {
        try {
            bindSign = ByteUtil.hexStringToBytes(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
            L.d("app唯一标识：" + HexUtil.encodeHexStr(bindSign));
        } catch (Exception e) {
            throw new RuntimeException("获取androidId异常");
        }
    }


    //中文转码GBK格式
    public static String toGBK(String source) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        byte[] bytes = source.getBytes("GBK");
        L.d("GBK编码：" + HexUtil.encodeHexStr(bytes));
        for (byte b : bytes) {
            sb.append("%" + Integer.toHexString((b & 0xff)).toUpperCase());
        }
        return sb.toString();
    }


    //通过电话号码得到联系人姓名
    public static String getContactNameFromPhoneBook(Context context, String phoneNum) {
        String contactName = "";
        ContentResolver cr = context.getContentResolver();
        Cursor pCur = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?",
                new String[]{phoneNum}, null);
        if (pCur.moveToFirst()) {
            contactName = pCur
                    .getString(pCur
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            pCur.close();
        }
        L.d("联系人姓名：" + contactName);
        if (TextUtils.isEmpty(contactName)) {
            return phoneNum;
        }
        return contactName;
    }

    //sdcard是否可读写
    public static boolean IsCanUseSdCard() {
        try {
            return Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    //sim卡是否可读
    public static boolean isCanUseSim(Context context) {
        try {
            TelephonyManager mgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            return TelephonyManager.SIM_STATE_READY == mgr.getSimState();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    //给图片上色
    public static Drawable tintDrawableWithColor(Context context, @DrawableRes int drawableRes, @ColorRes int colorRes) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableRes);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(context, colorRes));
        return drawable;
    }


    //如果输入法在窗口上已经显示，则隐藏，反之则显示
    public static void openKey() {
        InputMethodManager systemService = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        systemService.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }


    /**
     * 动态隐藏软键盘
     *
     * @param activity activity
     */
    public static void hideSoftInput(Activity activity) {
        View view = activity.getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputmanger = (InputMethodManager) activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    public static void showNotify(Context context, String mag) {
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle(context.getString(R.string.app_name))
                .setContentText(mag)
                .setLargeIcon(drawableToBitmap(getAppIcon(context)))//信息栏显示的图标
                .setSmallIcon(R.mipmap.bracelet)//状态栏显示的图标
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, MainActivity_.class), PendingIntent.FLAG_CANCEL_CURRENT))
                .setAutoCancel(true)
                .setOngoing(true);
        notify(context, builder.build());
    }


    private static final String NOTIFICATION_TAG = "NewMessage";

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private static void notify(final Context context, final Notification notification) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.notify(NOTIFICATION_TAG, 0, notification);
        } else {
            nm.notify(NOTIFICATION_TAG.hashCode(), notification);
        }
    }

    /**
     * Cancels any notifications of this type previously shown using
     * {@link # notify(Context, String, int)}.
     */
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public static void cancel(final Context context) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.cancel(NOTIFICATION_TAG, 0);
        } else {
            nm.cancel(NOTIFICATION_TAG.hashCode());
        }
    }

    /**
     * drawable 转 Bitmap
     *
     * @param drawable
     * @return Drawable
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {


        Bitmap bitmap = Bitmap.createBitmap(

                drawable.getIntrinsicWidth(),

                drawable.getIntrinsicHeight(),

                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888

                        : Bitmap.Config.RGB_565);

        Canvas canvas = new Canvas(bitmap);

        //canvas.setBitmap(bitmap);

        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

        drawable.draw(canvas);

        return bitmap;

    }


    //一些工具方法
    public static int dp2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics());
    }

    public static int sp2px(Context context, int sp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                sp,
                context.getResources().getDisplayMetrics());
    }


}