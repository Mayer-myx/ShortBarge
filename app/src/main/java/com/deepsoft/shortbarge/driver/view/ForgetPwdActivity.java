package com.deepsoft.shortbarge.driver.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.utils.NavigationBarUtil;
import com.deepsoft.shortbarge.driver.utils.PressUtil;

public class ForgetPwdActivity extends AppCompatActivity implements View.OnClickListener{

    private final static String TAG = "ForgetPwdActivity";

    private SharedPreferences sp;

    private TextView forpwd_tv_sercon, forpwd_tv_confirm, forpwd_tv_pwd, forpwd_tv_name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavigationBarUtil.focusNotAle(getWindow());
        setContentView(R.layout.activity_forget_pwd);
        NavigationBarUtil.hideNavigationBar(getWindow());
        NavigationBarUtil.clearFocusNotAle(getWindow());

        sp = getSharedPreferences("Di-Truck", Context.MODE_PRIVATE);
        initView();
    }

    private void initView(){
        forpwd_tv_sercon = findViewById(R.id.forpwd_tv_sercon);
        forpwd_tv_sercon.setOnClickListener(this);
        PressUtil.setPressChange(this, forpwd_tv_sercon);

        forpwd_tv_confirm = findViewById(R.id.forpwd_tv_confirm);
        forpwd_tv_confirm.setOnClickListener(this);
        PressUtil.setPressChange(this, forpwd_tv_confirm);

        forpwd_tv_pwd = findViewById(R.id.forpwd_tv_pwd);
        forpwd_tv_pwd.setText(sp.getString("password", ""));
        forpwd_tv_name = findViewById(R.id.forpwd_tv_name);
        forpwd_tv_name.setText(sp.getString("username", ""));
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
}