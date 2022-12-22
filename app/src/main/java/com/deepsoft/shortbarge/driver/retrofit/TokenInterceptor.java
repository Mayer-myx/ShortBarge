package com.deepsoft.shortbarge.driver.retrofit;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.deepsoft.shortbarge.driver.widget.BaseApplication;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * token拦截器
 */
public class TokenInterceptor implements Interceptor {

    private String token; //用于添加的请求头


    @Override
    public Response intercept(Chain chain) throws IOException {

        //从SharePreferences中获取token
        SharedPreferences sharedPreferences = BaseApplication.getContext().getSharedPreferences("Di-Truck", Context.MODE_PRIVATE);

        token = sharedPreferences.getString("token", "");

        Request request = chain.request()
                .newBuilder()
                .addHeader("token", token)
                .build();

        Response response = chain.proceed(request);
        //Log.e("返回数据：",response.body().string());
        return response;
    }

}