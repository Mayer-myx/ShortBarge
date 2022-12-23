package com.deepsoft.shortbarge.driver.view;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.utils.NavigationBarUtil;
import com.deepsoft.shortbarge.driver.utils.PressUtil;

public class ServerConfigActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView sercon_tv_cancel, sercon_tv_confirm;
    private EditText sercon_et_add, sercon_et_port;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavigationBarUtil.focusNotAle(getWindow());
        setContentView(R.layout.activity_server_config);
        NavigationBarUtil.hideNavigationBar(getWindow());
        NavigationBarUtil.clearFocusNotAle(getWindow());

        initView();
    }

    private void initView(){
        sercon_tv_cancel=  findViewById(R.id.sercon_tv_cancel);
        sercon_tv_cancel.setOnClickListener(this);
        PressUtil.setPressChange(this, sercon_tv_cancel);

        sercon_tv_confirm = findViewById(R.id.sercon_tv_confirm);
        sercon_tv_confirm.setOnClickListener(this);
        PressUtil.setPressChange(this, sercon_tv_confirm);

        sercon_et_add = findViewById(R.id.sercon_et_add);
        sercon_et_port = findViewById(R.id.sercon_et_port);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.sercon_tv_cancel:
                ServerConfigActivity.this.finish();
                break;
            case R.id.sercon_tv_confirm:
                String add = sercon_et_add.getText().toString();
                String port = sercon_et_port.getText().toString();
                ServerConfigActivity.this.finish();
                break;
        }
    }
}