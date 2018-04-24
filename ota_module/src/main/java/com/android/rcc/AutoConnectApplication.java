package com.android.rcc;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AutoConnectApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("------------", "onCreate");
        /*Context context = getApplicationContext();
        Intent service = new Intent(getApplicationContext(), AutoConnectService.class);
        context.startService(service);*/
    }
}
