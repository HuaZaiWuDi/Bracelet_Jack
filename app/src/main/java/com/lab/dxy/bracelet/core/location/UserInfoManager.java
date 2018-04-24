package com.lab.dxy.bracelet.core.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.lab.dxy.bracelet.Utils.L;

/**
 * 项目名称：DXYLED_TELINK
 * 类描述：
 * 创建人：oden
 * 创建时间：2016/3/7 9:51
 */
public class UserInfoManager {
    static int WRITE_SETTINGS = 101;
    public static final int BAIDU_MODE = 1;
    public static final int GOOGLE_MODE = 2;
    private Context mContext;
    private int mode;

    public LocationClient mBDLocationClient = null;
    public BDLocationListener myBDListener;

    private final String googleKey = "AIzaSyC1vDL06kxhS40rfzxs-mC6xCDDvaEx6Fs";
    //    private static GoogleLocationQueryer googleLocationQueryer;
    private Location googleLocation;
    private LocationManager googleLocationManager;


    public UserInfoManager(Context mContext, int mode) {
        this.mContext = mContext;
        this.mode = mode;
        if (mode == BAIDU_MODE) {
            initBaiduMode();
        } else {
//                    initGoogleMode();
        }
    }

//    public static void getPhoneInformation(Context mContext) {
//        String model = Build.MODEL;
//        String version = Build.VERSION.RELEASE;
//        String wifiMac;
//
//        WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
//        WifiInfo info = wifi.getConnectionInfo();
//        wifiMac = info.getMacAddress();
//        try {
//            IotClass.mAppVer = mContext.getPackageManager().getPackageInfo(Utils.getVersion, 0).versionName;
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//
////        IotClass.mUserName = wifiMac.replaceAll(":","");
//        IotClass.mUserName = wifiMac;
//        IotClass.mMobileType = model;
//        IotClass.mMobileVer = version;
//        IotClass.mAppName = mContext.getResources().getString(R.string.appName);
//
//        if (IotClass.mUserName != null)
//            CrashReport.setUserId(IotClass.mUserName);
//        L.d("[getPhoneInformation]: mMobileType:" + model);
//        L.d("[getPhoneInformation]: mMobileVer:" + version);
//        L.d("[getPhoneInformation]: wifiMac:" + wifiMac);
//        L.d("[getPhoneInformation]: mAppName:" + IotClass.mAppName);
//        L.d("[getPhoneInformation]: mAppVer:" + IotClass.mAppVer);
//    }


    public void getLocation() {
        if (mode == BAIDU_MODE) {
            L.d("百度定位开始");
            mBDLocationClient.start();
        } else {
//            if ((googleLocation != null)) {
//                Runnable locationQueryerThread = new Runnable() {
//                    @Override
//                    public void run() {
//                        L.d("getLocation");
//                        // TODO Auto-generated method stub
//                        googleLocationQueryer = new GoogleLocationQueryer();
//                        boolean isSuccess = googleLocationQueryer.queryLocation(googleKey, googleLocation.getLatitude(), googleLocation.getLongitude());
//                        System.out.println("isSuccess====" + isSuccess);
//                        if (isSuccess) {
//                            L.d("googleLocationQueryer.getCountry():" + googleLocationQueryer.getQueryResult().get("country"));
//                            L.d("googleLocationQueryer.getProvince():" + googleLocationQueryer.getQueryResult().get("administrative_area_level_1"));
//                            L.d("googleLocationQueryer.getCity():" + googleLocationQueryer.getQueryResult().get("locality"));
//                            L.d("googleLocationQueryer.getDistrict():" + googleLocationQueryer.getQueryResult().get("sublocality_level_1"));
//                            L.d("googleLocationQueryer.getAddrStr():" + googleLocationQueryer.getQueryResult().get("formatted_address"));
//
//                            IotClass.mCountry = googleLocationQueryer.getQueryResult().get("country");
//                            IotClass.mProvince = googleLocationQueryer.getQueryResult().get("administrative_area_level_1");
//                            IotClass.mCity = googleLocationQueryer.getQueryResult().get("locality");
//                            IotClass.mDistrict = googleLocationQueryer.getQueryResult().get("sublocality_level_1");
//                            IotClass.mFormattedAddress = googleLocationQueryer.getQueryResult().get("formatted_address");
//                        }
//                    }
//                };
//                new Thread(locationQueryerThread).start();
//            }
        }
    }


//------------------------------------------------------baidu------------------------------------------

    private void initBaiduMode() {
        MyLocationListener myLocationListener = new MyLocationListener(mContext);
        myLocationListener.setOnGetLocationListener(new MyLocationListener.onGetLocationListener() {
            @Override
            public void onGetLocationFinish() {
                L.d("success");
                mBDLocationClient.stop();
            }
        });
        myBDListener = myLocationListener;
        mBDLocationClient = new LocationClient(mContext.getApplicationContext());     //声明LocationClient类
        mBDLocationClient.registerLocationListener(myBDListener);    //注册监听函数

        initBaiduLocation();
    }

    private void initBaiduLocation() {
        try {
            LocationClientOption option = new LocationClientOption();
            option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
            option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
            int span = 1000;
            option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
            option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
            option.setOpenGps(true);//可选，默认false,设置是否使用gps
            option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
            option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
            option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
            option.setIgnoreKillProcess(false);//可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
            option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
            option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
            mBDLocationClient.setLocOption(option);

        } catch (Exception e) {
            L.d("定位异常");
        }

    }

//---------------------------------------------------google--------------------------------------------------


//    private void initGoogleMode() {
//        googleLocation = initGoogleLocation();
//    }
//
//    private Location initGoogleLocation() {
//        googleLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
//        Criteria criteria = new Criteria();
//        criteria.setAccuracy(Criteria.ACCURACY_FINE);//定位精度高
//        criteria.setAltitudeRequired(false);//海拔
//        criteria.setBearingRequired(false);//方位
//        criteria.setCostAllowed(true);//允许花费
//        criteria.setPowerRequirement(Criteria.POWER_LOW);//低耗电量
//        String positionInfo = googleLocationManager.getBestProvider(criteria, true);//获得位置信息
//        Location location = googleLocationManager.getLastKnownLocation(positionInfo);
//        googleLocationManager.requestLocationUpdates(positionInfo, 2000, 5, listener);
//        return location;
//    }

    //位置监听器
    private final LocationListener listener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderDisabled(String arg0) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String arg0) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
            // TODO Auto-generated method stub
        }
    };
}

