package com.deepsoft.shortbarge.driver.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.gson.DriverInfoGson;
import com.deepsoft.shortbarge.driver.gson.LoginInfoGson;
import com.deepsoft.shortbarge.driver.gson.ResultGson;
import com.deepsoft.shortbarge.driver.retrofit.ApiInterface;
import com.deepsoft.shortbarge.driver.utils.GsonConvertUtil;
import com.deepsoft.shortbarge.driver.utils.LocationUtil;
import com.deepsoft.shortbarge.driver.utils.NavigationBarUtil;
import com.deepsoft.shortbarge.driver.utils.PressUtil;
import com.deepsoft.shortbarge.driver.utils.RetrofitUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks{

    private final static String TAG = "LoginActivity";
    private String PERMISSION_STORAGE_MSG = "请授予权限，否则影响部分使用功能";
    private int PERMISSION_STORAGE_CODE = 10001;
    private String[] PERMS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private ApiInterface apiInterface;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private boolean is_rem_pwd = false;
    private String username, password;
    private LocationUtil locationUtils;

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
        apiInterface = RetrofitUtil.getInstance().getRetrofit().create(ApiInterface.class);
        sp = getSharedPreferences("Di-Truck", Context.MODE_PRIVATE);
        editor = sp.edit();
        is_rem_pwd = sp.getBoolean("is_rem", false);
        username = sp.getString("username", "");
        password = sp.getString("password", "");
        initView();

        if (EasyPermissions.hasPermissions(this, PERMS)) {
            locationUtils = LocationUtil.getInstance(LoginActivity.this);
            locationUtils.getLocation(new LocationUtil.LocationCallBack() {
                @Override
                public void setLocation(Location location) {
                    if (location != null){
                        Log.e(TAG, "经度:" + location.getLongitude());
                        Log.e(TAG, "纬度:" + location.getLatitude());
                        editor.putString("Longitude",String.valueOf(location.getLongitude()));
                        editor.putString("Latitude",String.valueOf(location.getLatitude()));
                        editor.commit();
                    }
                }
            }, 2);
        } else {
            // 没有申请过权限，现在去申请
            /**
             *@param host Context对象
             *@param rationale  权限弹窗上的提示语。
             *@param requestCode 请求权限的唯一标识码
             *@param perms 一系列权限
             */
            EasyPermissions.requestPermissions(this, PERMISSION_STORAGE_MSG, PERMISSION_STORAGE_CODE, PERMS);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationUtils.stop();
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


    private void getDriverInfo(){
        apiInterface.getDriverInfo().enqueue(new Callback<ResultGson>() {
            @Override
            public void onResponse(Call<ResultGson> call, Response<ResultGson> response) {
                Log.i(TAG, "getDriverInfo run: get同步请求 "+ "code="+response.body().getCode()+" msg="+response.body().getMsg());
                ResultGson resultGson = response.body();
                if(resultGson.getSuccess()){
                    List<DriverInfoGson> list = GsonConvertUtil.performTransform(resultGson.getData(), DriverInfoGson.class);
                    DriverInfoGson driverInfoGson = list.get(0);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("driverId", driverInfoGson.getDriverId());
                    intent.putExtra("truckId", Integer.toString((int)Double.parseDouble(driverInfoGson.getTruckId())));
                    intent.putExtra("licensePlate", driverInfoGson.getLicensePlate());
                    intent.putExtra("emergencyContact", driverInfoGson.getEmergencyContact());
                    intent.putExtra("emergencyContactEng", driverInfoGson.getEmergencyContactEng());
                    intent.putExtra("emergencyPhone", driverInfoGson.getEmergencyPhone());
                    startActivity(intent);
                    LoginActivity.this.finish();
                }else{
                    Toast.makeText(LoginActivity.this, "getDriverInfo连接成功 数据申请失败， msg="+resultGson.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResultGson> call, Throwable t) {
                Log.e(TAG, "getDriverInfo onFailure:"+t);
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
                apiInterface.getLogin(username, password).enqueue(new Callback<ResultGson>() {
                    @Override
                    public void onResponse(Call<ResultGson> call, Response<ResultGson> response) {
                        Log.i(TAG, "getDriverInfo run: get同步请求 "+ "code="+response.body().getCode()+" msg="+response.body().getMsg());
                        ResultGson resultGson = response.body();
                        Gson gson = new Gson();
                        if(resultGson.getSuccess()){
                            LoginInfoGson loginInfoGson = gson.fromJson(resultGson.getData().toString(), LoginInfoGson.class);
                            editor.putString("token", loginInfoGson.getToken());
                            if(is_rem_pwd){
                                editor.putString("username", username);
                                editor.putString("password", password);
                            }
                            editor.commit();
                        }else{
                            Toast.makeText(LoginActivity.this, "getLogin连接成功 数据申请失败， msg="+resultGson.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                        getDriverInfo();
                    }
                    @Override
                    public void onFailure(Call<ResultGson> call, Throwable t) {
                        Log.e(TAG, "getLogin onFailure:"+t);
                    }
                });
                break;
            case R.id.login_cb_rem_pwd:
                is_rem_pwd = login_cb_rem_pwd.isChecked();
                editor.putBoolean("is_rem", is_rem_pwd);
                editor.commit();
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