package com.lab.dxy.bracelet.core.rx;

import com.lab.dxy.bracelet.Utils.L;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 项目名称：BleCar
 * 类描述：
 * 创建人：oden
 * 创建时间：2016/8/30 11:06
 */
public class RxManager {
    private static RxManager rxManager = null;

    private RxManager() {

    }

    public synchronized static RxManager getInstance() {
        if (rxManager == null) {
            rxManager = new RxManager();
        }
        return rxManager;
    }

    public Subscription doUnifySubscribe(Observable<HttpResult> observable, Subscriber<HttpResult> subscriber) {
        return observable
                .map(new Func1<HttpResult, HttpResult>() {
                    @Override
                    public HttpResult call(HttpResult httpResult) {
                        if (!httpResult.isStatus()) {
                            L.d("运行异常");
                        }
                        return httpResult;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public <T> Subscription doSubscribeT(Observable<HttpResultT<T>> observable, Subscriber<T> subscriber) {
        return observable
                .map(new Func1<HttpResultT<T>, T>() {
                    @Override
                    public T call(HttpResultT<T> httpResult) {
                        if (!httpResult.isStatus()) {
                            L.d("运行异常");
                        }
                        return httpResult.getData();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public <T> Subscription doSubscribe(Observable<T> observable, Subscriber<T> subscriber) {
        return observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public <T> Subscription doIoSubscribe(Observable<T> observable, Subscriber<T> subscriber) {
        return observable
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(subscriber);
    }

    public <T> Observable<T> doSubscribe(Observable<T> observable) {
        return observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

}
