package com.lab.dxy.bracelet.Utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.lab.dxy.bracelet.entity.AppInfo;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 应用信息获取类
 *
 * @author 野虎
 * @时间 2016年2月23日下午3:47:13
 */
public class ApplicationInfoUtil {
    public static final int DEFAULT = 0; // 默认 所有应用  
    public static final int SYSTEM_APP = DEFAULT + 1; // 系统应用  
    public static final int NONSYSTEM_APP = DEFAULT + 2; // 非系统应用  

    /**
     * 根据包名获取相应的应用信息
     *
     * @param context
     * @param packageName
     * @return 返回包名所对应的应用程序的名称。
     */
    public static String getProgramNameByPackageName(Context context,
                                                     String packageName) {
        PackageManager pm = context.getPackageManager();
        String name = null;
        try {
            name = pm.getApplicationLabel(
                    pm.getApplicationInfo(packageName,
                            PackageManager.GET_META_DATA)).toString();
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return name;
    }

    /**
     * 获取手机所有应用信息
     *
     * @param allApplist
     * @param context
     */
    public static void getAllProgramInfo(List<AppInfo> allApplist,
                                         Context context) {
        getAllProgramInfo(allApplist, context, DEFAULT);
    }

    /**
     * 获取手机所有应用信息
     *
     * @param applist
     * @param context
     * @param type    标识符 是否区分系统和非系统应用
     */
    public static void getAllProgramInfo(List<AppInfo> applist,
                                         Context context, int type) {
        ArrayList<AppInfo> appList = new ArrayList<AppInfo>(); // 用来存储获取的应用信息数据  
        List<PackageInfo> packages = context.getPackageManager()
                .getInstalledPackages(0);

        for (int i = 0; i < packages.size(); i++) {
            PackageInfo packageInfo = packages.get(i);
            AppInfo tmpInfo = new AppInfo();
            tmpInfo.appName = packageInfo.applicationInfo.loadLabel(
                    context.getPackageManager()).toString();
            tmpInfo.packageName = packageInfo.packageName;
            tmpInfo.versionName = packageInfo.versionName;
            tmpInfo.versionCode = packageInfo.versionCode;
            tmpInfo.appIcon = drawableToSQLite(packageInfo.applicationInfo.loadIcon(context
                    .getPackageManager()));
            switch (type) {
                case NONSYSTEM_APP:
                    if (!isSystemAPP(packageInfo)) {
                        applist.add(tmpInfo);
                    }
                    break;
                case SYSTEM_APP:
                    if (isSystemAPP(packageInfo)) {
                        applist.add(tmpInfo);
                    }
                    break;
                default:
                    applist.add(tmpInfo);
                    break;
            }
        }
    }

    /**
     * 获取所有系统应用信息
     *
     * @param context
     * @return
     */
    public static List<AppInfo> getAllSystemProgramInfo(Context context) {
        List<AppInfo> systemAppList = new ArrayList<AppInfo>();
        getAllProgramInfo(systemAppList, context, SYSTEM_APP);
        return systemAppList;
    }


    /**
     * 获取所有非系统应用信息
     *
     * @param context
     * @return
     */
    public static List<AppInfo> getAllNonsystemProgramInfo(Context context) {
        List<AppInfo> nonsystemAppList = new ArrayList<AppInfo>();
        getAllProgramInfo(nonsystemAppList, context, NONSYSTEM_APP);
        return nonsystemAppList;
    }

    /**
     * 判断是否是系统应用
     *
     * @param packageInfo
     * @return
     */
    public static Boolean isSystemAPP(PackageInfo packageInfo) {
        if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) { // 非系统应用  
            return false;
        } else { // 系统应用  
            return true;
        }
    }


//    public BitmapDrawable getAppIcon() {
//        return bytesToDrawable(appIcon);
//    }


    public static byte[] drawableToSQLite(Drawable drawable) {

        //第一步，将Drawable对象转化为Bitmap对象
        Bitmap bmp = (((BitmapDrawable) drawable).getBitmap());
        //第二步，声明并创建一个输出字节流对象
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        //第三步，调用compress将Bitmap对象压缩为PNG格式，第二个参数为PNG图片质量，第三个参数为接收容器，即输出字节流os
        bmp.compress(Bitmap.CompressFormat.PNG, 80, os);
        //第四步，将输出字节流转换为字节数组，并直接进行存储数据库操作，注意，所对应的列的数据类型应该是BLOB类型
//        ContentValues values = new ContentValues();
//        values.put("image", os.toByteArray());
//        db.insert("apps", null, values);
//        db.close();

        return os.toByteArray();
    }


    public static BitmapDrawable bytesToDrawable(byte[] bytes) {
        if (bytes == null) return null;
//        //第一步，从数据库中读取出相应数据，并保存在字节数组中
//        byte[] blob = cursor.getBlob(cursor.getColumnIndex("image"));

        //第二步，调用BitmapFactory的解码方法decodeByteArray把字节数组转换为Bitmap对象
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        //第三步，调用BitmapDrawable构造函数生成一个BitmapDrawable对象，该对象继承Drawable对象，所以在需要处直接使用该对象即可
        BitmapDrawable bd = new BitmapDrawable(bmp);
        return bd;
    }


}