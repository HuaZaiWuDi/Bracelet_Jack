package com.lab.dxy.bracelet.keepalive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.lab.dxy.bracelet.Contents;
import com.lab.dxy.bracelet.Utils.SystemUtils;
import com.lab.dxy.bracelet.activity.MainActivity_;

/**
 * 1像素Activity
 * <p>
 * Created by jianddongguo on 2017/7/8.
 */

public class SinglePixelActivity extends AppCompatActivity {
    private static final String TAG = "SinglePixelActivity";

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                finish();
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate--->启动1像素保活");
        Window mWindow = getWindow();
        mWindow.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams attrParams = mWindow.getAttributes();
        attrParams.x = 0;
        attrParams.y = 0;
        attrParams.height = 300;
        attrParams.width = 300;
        mWindow.setAttributes(attrParams);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        this.registerReceiver(receiver, filter);

    }

    @Override
    protected void onDestroy() {
            Log.d(TAG, "onDestroy--->1像素保活被终止");
        if (!SystemUtils.isAPPALive(this, Contents.appPackageName)) {
            Intent intentAlive = new Intent(this, MainActivity_.class);
            intentAlive.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentAlive);
            Log.i(TAG, "SinglePixelActivity---->APP被干掉了，我要重启它");
        }
        this.unregisterReceiver(receiver);
        super.onDestroy();
    }
}
