package com.lab.dxy.bracelet.service;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import com.lab.dxy.bracelet.Utils.L;
import com.lab.dxy.bracelet.Utils.Utils;

import static com.lab.dxy.bracelet.Contents.isAppALive;

/**
 * Created by Anyway on 2017/2/10.
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MyNotificationListenerService extends NotificationListenerService {
    public static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    public static final String ACTIVE_PUSH_MESG = "enabled_notification_listeners";
    public static final String EXTRA_PUSH_MESG = "enabled_notification_listeners";


    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn, RankingMap rankingMap) {
        super.onNotificationPosted(sbn, rankingMap);
        String packageName = sbn.getPackageName();
        String tickerText = (String) sbn.getNotification().tickerText;

        L.d("getPackageName:" + sbn.getPackageName());
        L.d("onNotificationPosted");
        L.d("getPackageName:" + sbn.getPackageName());
        L.d("tickerText:" + sbn.getNotification().tickerText);
        L.d("title:" + sbn.getNotification().extras.get("android.title"));
        L.d("text:" + sbn.getNotification().extras.get("android.text"));
//        Notification notification = sbn.getNotification();
//        if (notification == null) {
//            return;
//        }
        if (TextUtils.isEmpty(tickerText)) return;
        Utils.broadUpdate( ACTIVE_PUSH_MESG, EXTRA_PUSH_MESG, packageName);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        L.d("MyNotificationListenerService is running");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //        重启自己----应用保活
        if (isAppALive) {
            Intent intent = new Intent(getApplicationContext(), MyNotificationListenerService.class);
            startService(intent);
        }

    }
}
