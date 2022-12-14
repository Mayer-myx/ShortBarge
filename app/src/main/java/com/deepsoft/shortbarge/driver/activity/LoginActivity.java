package com.deepsoft.shortbarge.driver.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.gson.LoginInfoGson;
import com.deepsoft.shortbarge.driver.gson.ResultGson;
import com.deepsoft.shortbarge.driver.service.ApiService;
import com.deepsoft.shortbarge.driver.service.MyService;
import com.deepsoft.shortbarge.driver.utils.PressUtils;
import com.deepsoft.shortbarge.driver.utils.RetrofitUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private final static String TAG = "MainActivity";
    private ApiService apiService;

    private TextView login_tv_login, login_tv_forget_pwd;
    private EditText login_et_username, login_et_pwd;
    private CheckBox login_cb_rem_pwd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //启动service
//        Intent service = new Intent(LoginActivity.this, MyService.class);
//        this.startService(service);

        apiService = RetrofitUtils.getInstance().getRetrofit().create(ApiService.class);
        initView();
    }

    private void initView(){
        login_tv_login = findViewById(R.id.login_tv_login);
        login_tv_login.setOnClickListener(this);
        PressUtils.setPressChange(this, login_tv_login);
        login_tv_forget_pwd = findViewById(R.id.login_tv_forget_pwd);
        login_tv_forget_pwd.setOnClickListener(this);
        PressUtils.setPressChange(this, login_tv_forget_pwd);

        login_et_username = findViewById(R.id.login_et_username);
        login_et_pwd = findViewById(R.id.login_et_pwd);
        login_cb_rem_pwd = findViewById(R.id.login_cb_rem_pwd);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.login_tv_login:
                String username = login_et_username.getText().toString().trim();
                String password = login_et_pwd.getText().toString().trim();
                apiService.getLogin(username, password).enqueue(new Callback<LoginInfoGson>() {
                    @Override
                    public void onResponse(Call<LoginInfoGson> call, Response<LoginInfoGson> response) {
                        LoginInfoGson loginInfoGson = response.body();
                        Log.e(TAG, loginInfoGson.toString());
                        Log.e(TAG, loginInfoGson.getData().getToken());
                        LoginActivity.this.finish();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    }

                    @Override
                    public void onFailure(Call<LoginInfoGson> call, Throwable t) {
                        Log.e(TAG, "onFailure:"+t);
                    }
                });
                break;
        }
    }
}