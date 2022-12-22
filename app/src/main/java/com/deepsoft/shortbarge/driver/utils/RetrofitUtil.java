package com.deepsoft.shortbarge.driver.utils;

import com.deepsoft.shortbarge.driver.BuildConfig;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitUtil {

    private static RetrofitUtil retrofitUtils;

    private RetrofitUtil() {}

    public static RetrofitUtil getInstance() {
        if (retrofitUtils == null) {
            synchronized (RetrofitUtil.class) {
                if (retrofitUtils == null) {
                    retrofitUtils = new RetrofitUtil();
                }
            }
        }
        return retrofitUtils;
    }

    //返回Retrofit
    public Retrofit getRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.SERVICE_HOST)
                .client(OkHttpUtil.getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
//                .client(OkHttpUtil.getOkHttpClient())
//                .addConverterFactory(MyGsonConverterFactory.create())
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        return retrofit;
    }
}
