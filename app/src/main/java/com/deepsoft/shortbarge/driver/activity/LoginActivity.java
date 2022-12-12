package com.deepsoft.shortbarge.driver.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.service.MyService;
import com.deepsoft.shortbarge.driver.utils.PressUtils;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView login_tv_login;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //启动service
//        Intent service = new Intent(LoginActivity.this, MyService.class);
//        this.startService(service);

        initView();
    }

    private void initView(){
        login_tv_login = findViewById(R.id.login_tv_login);
        login_tv_login.setOnClickListener(this);

        PressUtils.setPressChange(this, login_tv_login);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.login_tv_login:
                this.finish();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                break;
        }
    }
}