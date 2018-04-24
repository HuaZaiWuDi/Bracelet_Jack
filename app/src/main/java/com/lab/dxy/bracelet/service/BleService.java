package com.lab.dxy.bracelet.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.lab.dxy.bracelet.Contents;
import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.Utils.BraPrefs_;
import com.lab.dxy.bracelet.Utils.L;
import com.lab.dxy.bracelet.Utils.MyPrefs_;
import com.lab.dxy.bracelet.Utils.RxLogUtils;
import com.lab.dxy.bracelet.Utils.Utils;
import com.lab.dxy.bracelet.Utils.timer.MyPeriodTimer;
import com.lab.dxy.bracelet.activity.MainActivity_;
import com.lab.dxy.bracelet.ble.MyBle;
import com.lab.dxy.bracelet.ble.listener.BleRssiListener;
import com.lab.dxy.bracelet.entity.AppInfo;
import com.lab.dxy.bracelet.entity.AppInfoItem;
import com.lab.dxy.bracelet.keepalive.SinglePixelActivity;
import com.syd.oden.odenble.Utils.HexUtil;

import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.List;

import static android.service.notification.NotificationListenerService.requestRebind;
import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;
import static com.inuker.bluetooth.library.Constants.ACTION_CHARACTER_CHANGED;
import static com.inuker.bluetooth.library.Constants.EXTRA_BYTE_VALUE;
import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;
import static com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED;
import static com.lab.dxy.bracelet.Contents.ACTION_BLE_CONNECTED;
import static com.lab.dxy.bracelet.Contents.ACTION_BLE_DISCONNECT;
import static com.lab.dxy.bracelet.Contents.NOTICE_ID;
import static com.lab.dxy.bracelet.Contents.isAppALive;
import static com.lab.dxy.bracelet.MyApplication.aCache;
import static com.lab.dxy.bracelet.MyApplication.bluetoothClient;
import static com.lab.dxy.bracelet.MyApplication.mContext;
import static com.lab.dxy.bracelet.Utils.Utils.showNotify;
import static com.lab.dxy.bracelet.activity.MainActivity.isServiceRunning;
import static com.lab.dxy.bracelet.ble.BleManagerKit.MAC;
import static com.lab.dxy.bracelet.service.MyNotificationListenerService.ACTIVE_PUSH_MESG;
import static com.lab.dxy.bracelet.service.MyNotificationListenerService.EXTRA_PUSH_MESG;

/**
 * 项目名称：bracelet
 * 类描述：
 * 创建人：Jack
 * 创建时间：2016/8/26 10:39
 */
@EService
public class BleService extends Service {
    public MyBle myBle;
    public static boolean isBg = false;
    public static boolean isConnected = false;
    public static boolean isLost = false;


    @Receiver(actions = ACTION_BLE_DISCONNECT)
    protected void onActionDisconnected() {
        isConnected = false;
        powerTimer.stopTimer();
        doStartForeground();
    }


    @Receiver(actions = ACTION_BLE_CONNECTED)
    protected void onActionConnected() {
        isConnected = true;
        powerTimer.startTimer();
        doStartForeground();
    }


    @Receiver(actions = Contents.SCREEN_ON)
    void screenOn() {
        L.d("亮屏");
        isBg = false;
    }

    @Receiver(actions = Contents.SCREEN_OFF)
    void screenOff() {
        L.d("息屏");
        isBg = true;
        if (isAppALive) {
            Log.d(TAG, "准备启动SinglePixelActivity...");
            Intent intent = new Intent(mContext, SinglePixelActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }


    //监听系统蓝牙开启
    @Receiver(actions = BluetoothAdapter.ACTION_STATE_CHANGED)
    void blueToothisOpen(@Receiver.Extra(BluetoothAdapter.EXTRA_STATE) int state) {
        if (state == BluetoothAdapter.STATE_OFF) {
            Utils.broadUpdate(ACTION_BLE_DISCONNECT);
        } else if (state == BluetoothAdapter.STATE_ON) {

        }
    }

    //5a0d009001000000000000000000000000000000
    @Receiver(actions = ACTION_CHARACTER_CHANGED)
    protected void onActionCharacterChanged(@Receiver.Extra(EXTRA_BYTE_VALUE) byte[] value) {
        L.d("特征值改变：" + HexUtil.encodeHexStr(value));
    }


    @Receiver(actions = ACTIVE_PUSH_MESG)
    protected void onPushANCS(@Receiver.Extra(EXTRA_PUSH_MESG) String packageName) {
        L.d("消息提醒：" + packageName);
        SendMeg(packageName);
    }

    public void SendMeg(String packageName) {
        if (myPrefs.stepsIsOpen().get())
            if (!isBg) return;
        AppInfoItem item = (AppInfoItem) aCache.getAsObject("addApp");
        if (item != null) {
            List<AppInfo> infos = item.getAppInfos();
            if (infos != null)
                for (AppInfo info : infos) {
                    if (packageName.equals(info.packageName) && info.isOpen) {
                        //短信通知
                        if (packageName.equals(Contents.MSM)) {
                            myBle.sendPush(2, "SMS消息");
                        } else if (packageName.equals(Contents.QQ)) {
                            myBle.sendPush(3, "QQ消息");
                        } else if (packageName.equals(Contents.WECHAR)) {
                            myBle.sendPush(4, "微信消息");
                        } else if (packageName.equals(Contents.WHATSAPP)) {
                            myBle.sendPush(5, "WhatsApp消息");
                        } else {
                            myBle.sendPush(5, "WhatsApp消息");
                        }
                    }
                }
        }
    }


    private void ListenerPhone() {
        TelephonyManager telephony = (TelephonyManager) getSystemService(
                Context.TELEPHONY_SERVICE);
        telephony.listen(new OnePhoneStateListener(),
                PhoneStateListener.LISTEN_CALL_STATE);
    }

    /**
     * 电话状态监听.
     *
     * @author stephen
     */
    class OnePhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            L.i("[Listener]电话号码:" + incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    L.i(TAG, "[Listener]等待接电话:" + incomingNumber);
                    if (isConnected && myPrefs.CallisOpen().get()) {
                        String name = Utils.getContactNameFromPhoneBook(BleService.this, incomingNumber);
                        myBle.pushPhone(incomingNumber, name);
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    L.i(TAG, "[Listener]电话挂断:" + incomingNumber);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    L.i(TAG, "[Listener]通话中:" + incomingNumber);
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }


    @Pref
    MyPrefs_ myPrefs;

    @Pref
    BraPrefs_ braPrefs;

    private void init() {
        myBle = MyBle.getInstance();
        MAC = braPrefs.bleAddr().get();
        bluetoothClient.registerConnectStatusListener(MAC, listener);
        timer.startTimer();
        ListenerPhone();
    }


    MyPeriodTimer powerTimer = new MyPeriodTimer(30 * 60 * 1000, 30 * 60 * 1000, () -> {
        if (isConnected)
            myBle.queryPower(data -> {
            });
    });


    MyPeriodTimer timer = new MyPeriodTimer(1000, 10 * 1000, () -> {
        showRssi();

        myBle.getBleManagerKit().connectDevice();
        reStartService();
    });


    BleConnectStatusListener listener = new BleConnectStatusListener() {
        @Override
        public void onConnectStatusChanged(String mac, int status) {
            if (status == STATUS_CONNECTED) {
                L.d("STATUS_CONNECTED");
                myPrefs.bleName().put(braPrefs.bleName().get());
                myPrefs.bleAddr().put(braPrefs.bleAddr().get());
                RxLogUtils.e("Bracelet", "------手环已连接");
            } else if (status == STATUS_DISCONNECTED) {
                L.d("STATUS_DISCONNECTED");
                if (isConnected) {
//                    L.e("手环已断连");
                    isConnected = false;
                    RxLogUtils.e("Bracelet", "------手环已断开----connectStates：" + bluetoothClient.getConnectStatus(MAC));
                }
                isConnected = false;
                Utils.broadUpdate(ACTION_BLE_DISCONNECT);
            }
        }
    };


    private void reStartService() {
        if (!isServiceRunning(MyNotificationListenerService.class, getApplicationContext())) {
            startService(new Intent(BleService.this, MyNotificationListenerService.class));
            L.d("我死掉了");
        }
    }


    //重新开启NotificationMonitor
    private void toggleNotificationListenerService() {
        ComponentName thisComponent = new ComponentName(this, MyNotificationListenerService.class);
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        L.d("重新开启Notification");
        if (Build.VERSION.SDK_INT >= 24) {
            requestRebind(thisComponent);
        }
    }

    /**
     **/
    private void showRssi() {
        myBle.getBleManagerKit().readRssi(new BleRssiListener() {
            @Override
            public void rssiValue(int rssi) {
                L.d("蓝牙信号：" + rssi);
            }

            @Override
            public void fail() {
                L.d("读取信号失败");
                isConnected = false;
                Utils.broadUpdate(ACTION_BLE_DISCONNECT);
            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();
        L.d("onCreate");
        init();

        doStartForeground();

    }

    private void doStartForeground() {
        if (isAppALive)
            //如果API大于18，需要弹出一个可见通知
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                Notification.Builder builder = new Notification.Builder(this);
                builder.setSmallIcon(R.mipmap.bracelet)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(isConnected ? getString(R.string.barConnect) : getString(R.string.barDisConnect))
                        .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity_.class), PendingIntent.FLAG_CANCEL_CURRENT))
                        .setAutoCancel(true)
                        .setOngoing(true);
                startForeground(NOTICE_ID, builder.build());
                L.d("开启前台服务");
//                // 如果觉得常驻通知栏体验不好,实测在小米mix2,Android7.1.1上去不掉，在三星C5000,6.0.1上可以去掉
//                Intent intent = new Intent(this, CancelNoticeService.class);
//                startService(intent);
            } else {
                startForeground(NOTICE_ID, new Notification());
            }
        else
            showNotify(this, isConnected ? getString(R.string.barConnect) : getString(R.string.barDisConnect));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        L.d("onStartCommand");

        toggleNotificationListenerService();
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        L.d("onDestroy");
        timer.stopTimer();
        bluetoothClient.unregisterConnectStatusListener(MAC, listener);
        myBle.getBleManagerKit().disConnect();
//        重启自己----应用保活
        if (isAppALive) {
            L.d("重启自己  BleService");
            Intent intent = new Intent(getApplicationContext(), BleService_.class);
            startService(intent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                stopForeground(true);
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
