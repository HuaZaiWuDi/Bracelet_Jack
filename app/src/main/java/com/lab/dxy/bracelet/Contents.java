package com.lab.dxy.bracelet;

import java.util.Calendar;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：oden
 * 创建时间：2017/11/14
 */
public class Contents {

    public Contents() {
        throw new RuntimeException(" is not init ");
    }


    ///////////////////////////////////////////////////////////////////////////
    // 社交提醒
    ///////////////////////////////////////////////////////////////////////////

    public static final String CALL = "com.android.incallui";
    public static final String MSM = "com.android.mms";
    public static final String QQ = "com.tencent.mobileqq";
    public static final String WECHAR = "com.tencent.mm";
    public static final String WHATSAPP = "com.whatsapp";
    public static final String WEIBO = "com.sina.weibo";

    ///////////////////////////////////////////////////////////////////////////
    // 广播
    ///////////////////////////////////////////////////////////////////////////

    public static final String ACTION_BLE_CONNECTED = "com.lab.dxy.bracelet.ACTION_BLE_CONNECTED";
    public static final String ACTION_BLE_DISCONNECT = "com.lab.dxy.bracelet.ACTION_BLE_DISCONNECTED";
    public static final String ACTION_BLE_BIND = "com.lab.dxy.bracelet.ACTION_BLE_BIND";
    public static final String ACTION_DATA_INIT = "com.lab.dxy.bracelet.ACTION_DATA_INIT";
    public static final String ACTION_READ_RSSI = "com.lab.dxy.bracelet.ACTION_READ_RSSI";


    //是否息屏
    public static final String SCREEN_ON = "android.intent.action.SCREEN_ON";
    public static final String SCREEN_OFF = "android.intent.action.SCREEN_OFF";


    ///////////////////////////////////////////////////////////////////////////
    // 公共参数
    ///////////////////////////////////////////////////////////////////////////
    public static boolean isDebug = true;
    public static boolean isleakcanary = false;
    public static boolean isAppALive = true;

    public static int SDK_3D2 = 0;
    public static int SDK_2D2 = 1;
    public static int SDKVersion = SDK_3D2;

    public static final int NOTICE_ID = 100;

    //手环信号绝对值大于之隔值则震动以防丢
    public static int BraAntiLostMinTime = 85;

    //切换不同的蓝牙连接库
    public static boolean isVersion_DXY = false;

    public static String barFilterName = "DXY-";

    //保存本地文件夹
    public static final String DOWNLOAD_FOLDER = "/braceletDownload/";
    public static final String CAMERA_FOLDER = "/braceletCamera/";


    //应用包名
    public final static String appPackageName = "com.lab.dxy.bracelet";

    public static int versionCode = 3;
    public static int otaVersion = 0;
    //0x00——DXY,0x03--佳琪,0x01--美盛通，0x02--幽蓝

    public final static String BRA_DXY = "21";//DXY-手环
    public final static String BRA_MST = "22";//美盛通
    public final static String BRA_YL = "23";//优蓝
    public final static String BRA_JQ = "24";//佳琪

    public static String postInfo = "OhuqY";
    public static String postAppInfo = "OhuqY";

    public final static int BRA_DEVICE_DXY = 0x00;//DXY-手环
    public final static int BRA_DEVICE_MST = 0x01;//美盛通
    public final static int BRA_DEVICE_YL = 0x02;//优蓝
    public final static int BRA_DEVICE_JQ = 0x03;//佳琪

    public static int deviceVersion = 0;

    public static double mLatitude = 0;
    public static double mLongitude = 0;
    public static String mCountry = "";
    public static String mCity = "";

    //设备编号
    public static byte[] deviceNumber = new byte[8];

    //绑定信息
    public static byte[] bindSign = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};

    public static void main(String[] a) {
        Calendar c = Calendar.getInstance();
        System.out.println(c.getFirstDayOfWeek());
        System.out.println(c.getWeeksInWeekYear());
        System.out.println(c.getMinimalDaysInFirstWeek());
        System.out.println(c.getWeekYear());
    }

}
