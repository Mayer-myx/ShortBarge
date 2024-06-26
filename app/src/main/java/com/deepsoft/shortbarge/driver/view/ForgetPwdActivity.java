package com.deepsoft.shortbarge.driver.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.deepsoft.shortbarge.driver.BuildConfig;
import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.bean.AdminGson;
import com.deepsoft.shortbarge.driver.bean.ResultGson;
import com.deepsoft.shortbarge.driver.retrofit.ApiInterface;
import com.deepsoft.shortbarge.driver.utils.GsonConvertUtil;
import com.deepsoft.shortbarge.driver.utils.NavigationBarUtil;
import com.deepsoft.shortbarge.driver.utils.PressUtil;
import com.deepsoft.shortbarge.driver.utils.RetrofitUtil;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ForgetPwdActivity extends AppCompatActivity implements View.OnClickListener{

    private final static String TAG = "ForgetPwdActivity";

    private SharedPreferences sp;
    private ApiInterface apiInterface;

    private TextView forpwd_tv_sercon, forpwd_tv_confirm, forpwd_tv_phone, forpwd_tv_name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavigationBarUtil.focusNotAle(getWindow());
        setContentView(R.layout.activity_forget_pwd);
        NavigationBarUtil.hideNavigationBar(getWindow());
        NavigationBarUtil.clearFocusNotAle(getWindow());

        sp = getSharedPreferences("Di-Truck", Context.MODE_PRIVATE);
        initView();
        getDriverInfo();
    }

    private void initView(){
        forpwd_tv_sercon = findViewById(R.id.forpwd_tv_sercon);
        forpwd_tv_sercon.setOnClickListener(this);
        PressUtil.setPressChange(this, forpwd_tv_sercon);

        forpwd_tv_confirm = findViewById(R.id.forpwd_tv_confirm);
        forpwd_tv_confirm.setOnClickListener(this);
        PressUtil.setPressChange(this, forpwd_tv_confirm);

        forpwd_tv_phone = findViewById(R.id.forpwd_tv_phone);
        forpwd_tv_name = findViewById(R.id.forpwd_tv_name);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.forpwd_tv_confirm:
                ForgetPwdActivity.this.finish();
                break;
            case R.id.forpwd_tv_sercon:
                startActivity(new Intent(ForgetPwdActivity.this, ServerConfigActivity.class));
                break;
        }
    }


    private void getDriverInfo(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RetrofitUtil.getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        apiInterface = retrofit.create(ApiInterface.class);
        apiInterface.getAdminUser().enqueue(new Callback<ResultGson>() {
            @Override
            public void onResponse(Call<ResultGson> call, Response<ResultGson> response) {
                Log.i(TAG, "getAdminUser run: get同步请求 "+ "code="+response.body().getCode()+" msg="+response.body().getMsg());
                ResultGson resultGson = response.body();
                if(resultGson.getSuccess()){
                    List<AdminGson> list = GsonConvertUtil.performTransform(resultGson.getData(), AdminGson.class);
                    AdminGson adminGson = list.get(0);
                    forpwd_tv_phone.setText(adminGson.getPhone());
                    String lang = sp.getString("locale_language", "en");
                    lang = lang.equals("en") ? "1": "2";
                    if(lang.equals("1")){
                        forpwd_tv_name.setText(adminGson.getNameEng());
                    }else {
                        forpwd_tv_name.setText(adminGson.getName());
                    }
                }else{
                    Log.i(TAG, "getDriverInfo连接成功 数据申请失败， msg="+resultGson.getMsg());
                }
            }
            @Override
            public void onFailure(Call<ResultGson> call, Throwable t) {
                Log.e(TAG, "getDriverInfo onFailure:"+t);
            }
        });
    }

}