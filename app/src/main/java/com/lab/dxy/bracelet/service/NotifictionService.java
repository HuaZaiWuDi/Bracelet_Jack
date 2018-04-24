//package com.jack.dxy.bracelet.service;
//
//import android.annotation.TargetApi;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.os.Build;
//import android.os.IBinder;
//import android.service.notification.NotificationListenerService;
//import android.service.notification.StatusBarNotification;
//import android.telephony.PhoneStateListener;
//import android.telephony.TelephonyManager;
//import android.text.TextUtils;
//
//import com.jack.dxy.bracelet.Utils.L;
//import com.jack.dxy.bracelet.Utils.LL;
//import com.jack.dxy.bracelet.Utils.MyPrefs_;
//import com.jack.dxy.bracelet.Utils.Utils;
//import com.jack.dxy.bracelet.entity.spl.AppInfoTab;
//
//import org.androidannotations.annotations.EService;
//import org.androidannotations.annotations.sharedpreferences.Pref;
//
//import java.util.List;
//
//import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;
//import static com.jack.dxy.bracelet.activity.MainActivity.isBg;
//import static com.jack.dxy.bracelet.activity.MainActivity.isServiceRunning;
//import static com.jack.dxy.bracelet.service.BleService.isConnected;
//import static com.jack.dxy.bracelet.service.BleService.myBle;
//import static com.syd.oden.odenble.LiteBle.ACTION_BLE_DISCONNECT;
//
///**
// * 项目名称：Bracelet
// * 类描述：
// * 创建人：Jack
// * 创建时间：2017/5/23
// */
//@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
//@EService
//public class NotifictionService extends NotificationListenerService {
//    public static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
//    List<AppInfoTab> isOpenList;
//
//    //是否息屏
//    public static final String SCREEN_ON = "android.intent.action.SCREEN_ON";
//    public static final String SCREEN_OFF = "android.intent.action.SCREEN_OFF";
//
//
//    public static final String CALL = "com.android.incallui";
//    public static final String MSM = "com.android.mms";
//    public static final String QQ = "com.tencent.mobileqq";
//    public static final String WECHAR = "com.tencent.mm";
//    public static final String WHATSAPP = "com.whatsapp";
//    public static final String WEIBO = "com.sina.weibo";
//
//
//    @Pref
//    MyPrefs_ myPrefs;
//
//
//    @Override
//    public void onNotificationRemoved(StatusBarNotification sbn) {
////        super.onNotificationRemoved(sbn);
//        LL.d("getPackageName:" + sbn.getPackageName());
//        removedNotify(sbn);
//    }
//
//    @Override
//    public void onNotificationPosted(StatusBarNotification sbn) {
////        super.onNotificationPosted(sbn);
//        LL.d("getPackageName:" + sbn.getPackageName());
//        LL.d("onNotificationPosted");
//        LL.d("getPackageName:" + sbn.getPackageName());
//        LL.d("tickerText:" + sbn.getNotification().tickerText);
//        LL.d("title:" + sbn.getNotification().extras.get("android.title"));
//        LL.d("text:" + sbn.getNotification().extras.get("android.text"));
//        sendNotity(sbn);
//    }
//
//
//    private void sendNotity(StatusBarNotification sbn) {
//        String packageName = sbn.getPackageName();
//        String tickerText = (String) sbn.getNotification().tickerText;
//
//        //当版本大于18时候
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            if (TextUtils.isEmpty(tickerText)) return;
//            if (myPrefs.stepsIsOpen().get())
//                if (!isBg) return;
//            if (!isConnected) {
//                if (myBle != null)
//                    myBle.getBleManagerKit().connectDevice();
//                Utils.broadUpdate(NotifictionService.this, ACTION_BLE_DISCONNECT);
//            }
//            while (!isConnected) {
//                Utils.mSleep(200);
//            }
//            //短信通知
//            if (packageName.equals(MSM)) {
//                if (myPrefs.MSMisOpen().get())
//                    linear_sms();
//            } else if (packageName.equals(QQ)) {
//                if (myPrefs.QQisOpen().get())
//                    Linear_QQ();
//            } else if (packageName.equals(WECHAR)) {
//
//                if (myPrefs.WeCharisOpen().get())
//                    Linear_Wechat();
//            } else if (packageName.equals(WHATSAPP)) {
//
//                if (myPrefs.whatAPPisOpen().get())
//                    linear_app();
//            } else if (doOtherPush(packageName)) {
//                linear_app();
//            }
//        } else {
//            LL.d("BuildVersion小于18");
//        }
//    }
//
//    private void removedNotify(StatusBarNotification sbn) {
//        //当版本大于18时候
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//        } else {
//            LL.d("BuildVersion小于18");
//        }
//    }
//
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        ListenerPhone();
//    }
//
//    private boolean doOtherPush(String packageName) {
//        isOpenList = AppInfoTab.getIsOpen();
//        L.d("isOpenList：" + isOpenList.size());
//        for (AppInfoTab appInfo : isOpenList) {
//            if (appInfo.packageName.equals(packageName)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//
//        reStartService();
//        return START_STICKY;
//    }
//
//    private void reStartService() {
//        if (!isServiceRunning(NotifictionService.class, getApplicationContext())) {
//            toggleNotificationListenerService();
//        }
//    }
//
//    //重新开启NotificationMonitor
//    private void toggleNotificationListenerService() {
//        ComponentName thisComponent = new ComponentName(this, NotifictionService_.class);
//        PackageManager pm = getPackageManager();
//        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
//        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
//        L.d("重新开启Notification");
//        if (Build.VERSION.SDK_INT >= 24) {
//            requestRebind(thisComponent);
//        }
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        //重启自己----应用保活
//        Intent intent = new Intent(getApplicationContext(), NotifictionService_.class);
//        startService(intent);
//    }
//
//    private void ListenerPhone() {
//        TelephonyManager telephony = (TelephonyManager) getSystemService(
//                Context.TELEPHONY_SERVICE);
//        telephony.listen(new OnePhoneStateListener(),
//                PhoneStateListener.LISTEN_CALL_STATE);
//    }
//
//    /**
//     * 电话状态监听.
//     *
//     * @author stephen
//     */
//    class OnePhoneStateListener extends PhoneStateListener {
//        @Override
//        public void onCallStateChanged(int state, String incomingNumber) {
//            LL.i("[Listener]电话号码:" + incomingNumber);
//            switch (state) {
//                case TelephonyManager.CALL_STATE_RINGING:
//                    LL.i(TAG, "[Listener]等待接电话:" + incomingNumber);
//                    if (isConnected && myPrefs.CallisOpen().get()) {
//                        String name = Utils.getContactNameFromPhoneBook(NotifictionService.this, incomingNumber);
//                        linear_phone(incomingNumber, name);
//                    }
//                    break;
//                case TelephonyManager.CALL_STATE_IDLE:
//                    LL.i(TAG, "[Listener]电话挂断:" + incomingNumber);
//                    break;
//                case TelephonyManager.CALL_STATE_OFFHOOK:
//                    LL.i(TAG, "[Listener]通话中:" + incomingNumber);
//                    break;
//            }
//            super.onCallStateChanged(state, incomingNumber);
//        }
//    }
//
//
//    void linear_phone(String phone, String name) {
//        myBle.pushPhone(phone, name);
//    }
//
//    void linear_app() {
//        myBle.sendPush(5, "WhatsApp消息");
//    }
//
//    void linear_sms() {
//        myBle.sendPush(2, "SMS消息");
//    }
//
//    void Linear_Wechat() {
//        myBle.sendPush(4, "微信消息");
//    }
//
//    void Linear_QQ() {
//        myBle.sendPush(3, "QQ消息");
//    }
//
//
//}
