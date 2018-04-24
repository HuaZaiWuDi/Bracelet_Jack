package com.lab.dxy.bracelet.core.net;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：oden
 * 创建时间：2017/5/24
 */
public interface RetrofitService {
    String BASE_URL = UpdateURL.BASE_URL;

    @FormUrlEncoded
    @POST("update/bin")
    Observable<String> getUpgradeVersion(@Field("r") String postInfo);


    @FormUrlEncoded
    @POST("update/android")
    Observable<String> getUpgradeAppVersion(@Field("r") String postInfo);

}
