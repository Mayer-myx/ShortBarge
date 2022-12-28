package com.deepsoft.shortbarge.driver.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.deepsoft.shortbarge.driver.BuildConfig;
import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.gson.DriverInfoGson;
import com.deepsoft.shortbarge.driver.gson.LoginInfoGson;
import com.deepsoft.shortbarge.driver.gson.ResultGson;
import com.deepsoft.shortbarge.driver.retrofit.ApiInterface;
import com.deepsoft.shortbarge.driver.utils.GsonConvertUtil;
import com.deepsoft.shortbarge.driver.utils.NavigationBarUtil;
import com.deepsoft.shortbarge.driver.utils.OkHttpUtil;
import com.deepsoft.shortbarge.driver.utils.PressUtil;
import com.deepsoft.shortbarge.driver.utils.RetrofitUtil;
import com.deepsoft.shortbarge.driver.utils.SwitchUtil;
import com.google.gson.Gson;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks{

    private final static String TAG = "LoginActivity";
    private String PERMISSION_STORAGE_MSG = "请授予权限，否则影响部分使用功能";
    private int PERMISSION_STORAGE_CODE = 10001;
    private String[] PERMS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO, Manifest.permission.WAKE_LOCK};

    private ApiInterface apiInterface;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    private boolean is_rem_pwd = false;
    private String username, password, lang;
    private int login_chances;

    private TextView login_tv_login, login_tv_forget_pwd;
    private EditText login_et_username, login_et_pwd;
    private CheckBox login_cb_rem_pwd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavigationBarUtil.focusNotAle(getWindow());
        setContentView(R.layout.activity_login);
        NavigationBarUtil.hideNavigationBar(getWindow());
        NavigationBarUtil.clearFocusNotAle(getWindow());

        //启动service
        //Intent service = new Intent(LoginActivity.this, MyService.class);
        //this.startService(service);

        sp = getSharedPreferences("Di-Truck", Context.MODE_PRIVATE);
        editor = sp.edit();

        is_rem_pwd = sp.getBoolean("is_rem", false);
        username = sp.getString("username", "");
        password = sp.getString("password", "");
        login_chances = sp.getInt("login_chances", 10);
        lang = sp.getString("locale_language", "en");
        lang = lang.equals("en") ? "1": "2";

        initView();

        login_tv_login.setClickable(false);
        login_tv_forget_pwd.setClickable(false);
        if (EasyPermissions.hasPermissions(this, PERMS)) {
            login_tv_login.setClickable(true);
            login_tv_forget_pwd.setClickable(true);
        } else {
            // 没有申请过权限，现在去申请
            /**
             *@param host Context对象
             *@param rationale  权限弹窗上的提示语。
             *@param requestCode 请求权限的唯一标识码
             *@param perms 一系列权限
             */
            SwitchUtil.checkGpsIsOpen(this, "需要打开定位功能才能使用本系统");
            EasyPermissions.requestPermissions(this, PERMISSION_STORAGE_MSG, PERMISSION_STORAGE_CODE, PERMS);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private void initView(){
        login_tv_login = findViewById(R.id.login_tv_login);
        login_tv_login.setOnClickListener(this);
        PressUtil.setPressChange(this, login_tv_login);

        login_tv_forget_pwd = findViewById(R.id.login_tv_forget_pwd);
        login_tv_forget_pwd.setOnClickListener(this);
        PressUtil.setPressChange(this, login_tv_forget_pwd);

        login_et_username = findViewById(R.id.login_et_username);
        login_et_username.setText(username);
        login_et_pwd = findViewById(R.id.login_et_pwd);
        login_et_pwd.setText(password);

        login_cb_rem_pwd = findViewById(R.id.login_cb_rem_pwd);
        login_cb_rem_pwd.setChecked(is_rem_pwd);
        login_cb_rem_pwd.setOnClickListener(this);
    }


    private void getLogin(String username, String password){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.SERVICE_HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        apiInterface = retrofit.create(ApiInterface.class);
        apiInterface.getLogin(username, password).enqueue(new Callback<ResultGson>() {
            @Override
            public void onResponse(Call<ResultGson> call, Response<ResultGson> response) {
                Log.i(TAG, "getDriverInfo run: get同步请求 " + "code=" + response.body().getCode() + " msg=" + response.body().getMsg());
                ResultGson resultGson = response.body();
                Gson gson = new Gson();
                if (resultGson.getSuccess() && login_chances >= 0) {
                    LoginInfoGson loginInfoGson = gson.fromJson(resultGson.getData().toString(), LoginInfoGson.class);
                    editor.putString("token", loginInfoGson.getToken());
                    if (is_rem_pwd) {
                        editor.putString("username", username);
                        editor.putString("password", password);
                    }
                    editor.putInt("login_chances", 10);
                    editor.commit();
                    LoginActivity.this.finish();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                } else {
                    if (login_chances == 0) {
                        if(lang.equals("1")){
                            Toast.makeText(LoginActivity.this, "Locked, contact your administrator.", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(LoginActivity.this, "已锁定，请联系管理员。", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        login_chances--;
                        editor.putInt("login_chances", login_chances);
                        editor.commit();
                        if(lang.equals("1")){
                            Toast.makeText(LoginActivity.this, resultGson.getMsg() + ". You have " + login_chances + "more chances", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(LoginActivity.this, resultGson.getMsg() + "，您还有" + login_chances + "次机会", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ResultGson> call, Throwable t) {
                Log.e(TAG, "getLogin onFailure:" + t);
            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.login_tv_login:
                // 登录后获取司机信息+跳转
                username = login_et_username.getText().toString().trim();
                password = login_et_pwd.getText().toString().trim();
                if(username.length() != 0 && password.length() != 0) {
                    getLogin(username, password);
                }else{
                    if(lang.equals("1")) {
                        Toast.makeText(this, "Please enter a username or password", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(this, "请输入用户名或密码", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.login_cb_rem_pwd:
                is_rem_pwd = login_cb_rem_pwd.isChecked();
                editor.putBoolean("is_rem", is_rem_pwd);
                editor.commit();
                break;
            case R.id.login_tv_forget_pwd:
                startActivity(new Intent(LoginActivity.this, ForgetPwdActivity.class));
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //将结果转发给EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    /**
     * 申请成功时调用
     * @param requestCode 请求权限的唯一标识码
     * @param perms 一系列权限
     */
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
    }


    /**
     * 申请拒绝时调用
     * @param requestCode 请求权限的唯一标识码
     * @param perms 一系列权限
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        /**
         * 若是在权限弹窗中，用户勾选了'不在提示'，且拒绝权限。
         * 这时候，需要跳转到设置界面去，让用户手动开启。
         */
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            //从设置页面返回，判断权限是否申请。
            if (EasyPermissions.hasPermissions(this, PERMS)) {
                Log.e(TAG, "权限申请成功!");
            } else {
                Log.e(TAG, "权限申请失败!");
            }
        }
    }


    @AfterPermissionGranted(10001)  //与上面的 PERMISSION_STORAGE_CODE= 10001; 保持一致
    public void onPermissionSuccess(){
        Log.e(TAG, "AfterPermission调用成功了");
    }
}