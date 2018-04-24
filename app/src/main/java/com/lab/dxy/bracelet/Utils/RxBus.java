package com.lab.dxy.bracelet.Utils;

import java.util.HashMap;
import java.util.Set;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;
import rx.subscriptions.CompositeSubscription;

/**
 * http://www.jianshu.com/p/3a3462535b4d
 * RxBus
 */
public class RxBus {

    private static volatile RxBus mInstance;
    private Subject<Object, Object> mSubject;
    private HashMap<String, CompositeSubscription> mSubscriptionMap;


    // 这里使用Dagger来处理单例

    private RxBus() {
        mSubject = new SerializedSubject<>(PublishSubject.create());
    }

    public static RxBus getInstance() {
        if (mInstance == null) {
            synchronized (RxBus.class) {
                if (mInstance == null) {
                    mInstance = new RxBus();
                }
            }
        }
        return mInstance;
    }


    /**
     * 发送事件
     *
     * @param o
     */
    public void post(Object o) {
        mSubject.onNext(o);
    }


    public Observable<Object> toObserverable() {
        return mSubject;
    }


    /**
     * 返回指定类型的Observable实例
     *
     * @param type
     * @param <T>
     * @return
     */
    public <T> Observable<T> toObservable(final Class<T> type) {
        return mSubject.ofType(type);
    }

    /**
     * 是否已有观察者订阅
     *
     * @return
     */
    public boolean hasObservers() {
        return mSubject.hasObservers();
    }

    /**
     * 一个默认的订阅方法
     *
     * @param type
     * @param next
     * @param error
     * @param <T>
     * @return
     */
    public <T> Subscription doSubscribe(Class<T> type, Action1<T> next, Action1<Throwable> error) {
        return toObservable(type)
                // 加上背压处理，不然有些地方会有异常，关于背压参考这里：https://gold.xitu.io/post/582d413c8ac24700619cceed
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(next, error);
    }

    /**
     * 一个默认的订阅方法
     *
     * @param type
     * @param next
     * @param <T>
     * @return
     */
    public <T> Subscription doSubscribe(Class<T> type, Action1<T> next) {
        return toObservable(type)
                // 加上背压处理，不然有些地方会有异常，关于背压参考这里：https://gold.xitu.io/post/582d413c8ac24700619cceed
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(next);
    }


    /**
     * 保存订阅后的subscription
     *
     * @param type
     * @param next
     */
    public <T> void addSubscription(Class<T> type, Action1<T> next) {

        Subscription subscription = doSubscribe(type, next);

        if (mSubscriptionMap == null) {
            mSubscriptionMap = new HashMap<>();
        }
        String key = type.getName();
        if (mSubscriptionMap.get(key) != null) {
            mSubscriptionMap.get(key).add(subscription);
        } else {
            CompositeSubscription compositeSubscription = new CompositeSubscription();
            compositeSubscription.add(subscription);
            mSubscriptionMap.put(key, compositeSubscription);
        }
    }


    /**
     * 保存订阅后的subscription
     *
     * @param o
     * @param subscription
     */
    public void addSubscription(Object o, Subscription subscription) {
        if (mSubscriptionMap == null) {
            mSubscriptionMap = new HashMap<>();
        }
        String key = o.getClass().getName();
        if (mSubscriptionMap.get(key) != null) {
            mSubscriptionMap.get(key).add(subscription);
        } else {
            CompositeSubscription compositeSubscription = new CompositeSubscription();
            compositeSubscription.add(subscription);
            mSubscriptionMap.put(key, compositeSubscription);
        }
    }

    /**
     * 取消订阅
     *
     * @param o
     */
    public void unSubscribe(Object o) {
        if (mSubscriptionMap == null) {
            return;
        }

        String key = o.getClass().getName();
        if (!mSubscriptionMap.containsKey(key)) {
            return;
        }
        if (mSubscriptionMap.get(key) != null) {
            mSubscriptionMap.get(key).unsubscribe();
        }

        mSubscriptionMap.remove(key);
    }

    /**
     * 取消所有订阅
     */
    public void clear() {
        if (mSubscriptionMap == null) {
            return;
        }

        Set<String> strings = mSubscriptionMap.keySet();
        for (String key : strings) {
            if (mSubscriptionMap.get(key) != null && !mSubscriptionMap.get(key).isUnsubscribed()) {
                mSubscriptionMap.get(key).clear();
            }
        }
        mSubscriptionMap.clear();
    }

}