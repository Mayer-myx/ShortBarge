package com.deepsoft.shortbarge.driver.utils;

import com.deepsoft.shortbarge.driver.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitUtils {

    private static RetrofitUtils retrofitUtils;

    private RetrofitUtils() {}

    public static RetrofitUtils getInstance() {
        if (retrofitUtils == null) {
            synchronized (RetrofitUtils.class) {
                if (retrofitUtils == null) {
                    retrofitUtils = new RetrofitUtils();
                }
            }
        }
        return retrofitUtils;
    }

    //返回Retrofit
    public Retrofit getRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.SERVICE_HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit;
    }
}
