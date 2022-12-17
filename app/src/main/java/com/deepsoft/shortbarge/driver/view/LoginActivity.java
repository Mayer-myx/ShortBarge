package com.deepsoft.shortbarge.driver.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.gson.LoginInfoGson;
import com.deepsoft.shortbarge.driver.service.ApiService;
import com.deepsoft.shortbarge.driver.utils.PressUtils;
import com.deepsoft.shortbarge.driver.utils.RetrofitUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private final static String TAG = "LoginActivity";
    private ApiService apiService;

    private SharedPreferences sp = getSharedPreferences("Di-Truck", Context.MODE_PRIVATE);
    private SharedPreferences.Editor editor = sp.edit();
    private boolean is_rem_pwd = false;
    private String username, password;

    private TextView login_tv_login, login_tv_forget_pwd;
    private EditText login_et_username, login_et_pwd;
    private CheckBox login_cb_rem_pwd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //启动service
        //Intent service = new Intent(LoginActivity.this, MyService.class);
        //this.startService(service);

        apiService = RetrofitUtils.getInstance().getRetrofit().create(ApiService.class);
        is_rem_pwd = sp.getBoolean("is_rem", false);
        username = sp.getString("username", "");
        password = sp.getString("password", "");
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
        login_cb_rem_pwd.setChecked(is_rem_pwd);
        login_cb_rem_pwd.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.login_tv_login:
                username = login_et_username.getText().toString().trim();
                password = login_et_pwd.getText().toString().trim();
                apiService.getLogin(username, password).enqueue(new Callback<LoginInfoGson>() {
                    @Override
                    public void onResponse(Call<LoginInfoGson> call, Response<LoginInfoGson> response) {
                        LoginInfoGson loginInfoGson = response.body();
                        Log.e(TAG, loginInfoGson.getData().getToken());
                        editor.putString("token", loginInfoGson.getData().getToken());
                        editor.commit();
                        LoginActivity.this.finish();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    }
                    @Override
                    public void onFailure(Call<LoginInfoGson> call, Throwable t) {
                        Log.e(TAG, "onFailure:"+t);
                    }
                });
                break;
            case R.id.login_cb_rem_pwd:
                is_rem_pwd = login_cb_rem_pwd.isChecked();
                editor.putBoolean("is_rem", is_rem_pwd);
                if(is_rem_pwd == true){
                    username = login_et_username.getText().toString().trim();
                    password = login_et_pwd.getText().toString().trim();
                    editor.putString("username", username);
                    editor.putString("password", password);
                }
                editor.commit();
                break;
        }
    }
}