package com.syd.oden.odenble.exception.hanlder;

import android.content.Context;
import android.widget.Toast;

import com.syd.oden.odenble.Utils.BleToast;
import com.syd.oden.odenble.exception.ConnectException;
import com.syd.oden.odenble.exception.GattException;
import com.syd.oden.odenble.exception.InitiatedException;
import com.syd.oden.odenble.exception.OtherException;
import com.syd.oden.odenble.exception.TimeoutException;

/**
 * 项目名称：OdenDemo
 * 类描述：
 * 创建人：oden
 * 创建时间：2016/6/12 20:20
 */
public class DefaultBleExceptionHandler extends BleExceptionHandler {
    private Context context;

    public DefaultBleExceptionHandler(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    protected void onConnectException(ConnectException e) {
        BleToast.showShort(context, e.getDescription());
    }

    @Override
    protected void onGattException(GattException e) {
        BleToast.showShort(context, e.getDescription());
    }

    @Override
    protected void onTimeoutException(TimeoutException e) {
        BleToast.showShort(context, e.getDescription());
    }

    @Override
    protected void onInitiatedException(InitiatedException e) {
        BleToast.showShort(context, e.getDescription());
    }

    @Override
    protected void onOtherException(OtherException e) {
        BleToast.showShort(context, e.getDescription());
    }
}
