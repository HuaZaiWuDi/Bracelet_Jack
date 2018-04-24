package com.android.rcc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LauncherBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent service = new Intent(context, AutoConnectService.class);
            context.startService(service);
        }
    }
}
