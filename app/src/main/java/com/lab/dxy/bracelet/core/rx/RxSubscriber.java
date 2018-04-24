package com.lab.dxy.bracelet.core.rx;

import android.content.Context;

import com.lab.dxy.bracelet.MyApplication;
import com.lab.dxy.bracelet.Utils.L;

import java.io.IOException;

import rx.Subscriber;

/**
 * 项目名称：BleCar
 * 类描述：
 * 创建人：oden
 * 创建时间：2016/8/30 11:27
 */
public abstract class RxSubscriber<T> extends Subscriber<T> {
    private Context context;

    public RxSubscriber() {
        context = MyApplication.mContext;
    }

    @Override
    public void onCompleted() {
        L.d("onCompleted");
    }

    @Override
    public void onError(Throwable e) {
        if (e instanceof IOException) {
            L.e("onError: " + e.toString() + ",网络链接异常");
//            MyToast.showShort(context, "error: " + e.toString());  UI线程才可用
        } else {
            L.d("onError: " + e.toString());
        }
        _onError(e);
    }

    @Override
    public void onNext(T t) {
        _onNext(t);
    }

    public Context getContext() {
        return context;
    }

    protected abstract void _onError(Throwable e);

    protected abstract void _onNext(T t);
}
