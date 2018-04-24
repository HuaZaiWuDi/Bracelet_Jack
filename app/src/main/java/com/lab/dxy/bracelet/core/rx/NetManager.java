package com.lab.dxy.bracelet.core.rx;

import android.util.Log;

import com.lab.dxy.bracelet.BuildConfig;
import com.lab.dxy.bracelet.core.net.UpdateURL;

import java.lang.reflect.Field;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * 项目名称：BleCar
 * 类描述：
 * 创建人：oden
 * 创建时间：2016/8/30 11:57
 */
public class NetManager {
    private static NetManager netManager = null;
    private static Retrofit retrofit = null;

    private NetManager() {
        retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(UpdateURL.BASE_URL)
                .build();
    }

    public synchronized static NetManager getInstance() {
        if (netManager == null) {
            netManager = new NetManager();
        }
        return netManager;
    }

    public <S> S create(Class<S> service) {
        return retrofit.create(service);
    }

    /**
     * 返回原始的值
     * @param service
     * @param <S>
     * @return
     */
    public <S> S createString(Class<S> service) {

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (BuildConfig.DEBUG) {
            //日志显示级别
            HttpLoggingInterceptor.Level level = HttpLoggingInterceptor.Level.BODY;
            //新建log拦截器
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                    Log.e("【NetManager】", message);
                }
            });
            loggingInterceptor.setLevel(level);
            //OkHttp进行添加拦截器loggingInterceptor
            builder.addInterceptor(loggingInterceptor);
        }

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(builder.build())
                .baseUrl(getBaseUrl(service))
                .build();
        return retrofit.create(service);
    }

    /**
     * URL不一致时调用
     * @param service
     * @param <S>
     * @return
     */
    public <S> S createWithUrl(Class<S> service) {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(getBaseUrl(service))
                .build();
        return retrofit.create(service);
    }

    /**
     * 解析接口中的BASE_URL，解决BASE_URL不一致的问题
     *
     * @param service
     * @param <S>
     * @return
     */
    private <S> String getBaseUrl(Class<S> service) {
        try {
            Field field = service.getField("BASE_URL");
            return (String) field.get(service);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}
