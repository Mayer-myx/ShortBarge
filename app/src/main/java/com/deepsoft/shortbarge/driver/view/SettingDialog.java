package com.deepsoft.shortbarge.driver.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.gson.DriverInfoGson;
import com.deepsoft.shortbarge.driver.gson.ResultGson;
import com.deepsoft.shortbarge.driver.gson.UserInfoGson;
import com.deepsoft.shortbarge.driver.retrofit.ApiInterface;
import com.deepsoft.shortbarge.driver.utils.PressUtil;
import com.deepsoft.shortbarge.driver.utils.RetrofitUtil;
import com.deepsoft.shortbarge.driver.widget.MyDialog;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingDialog extends MyDialog implements View.OnClickListener{

    private final static String TAG = "SettingDialog";
    private ApiInterface apiInterface;
    private Context context;
    private DriverInfoGson driverInfoGson;
    private String name;
    private UserInfoGson userInfoGson;

    private TextView dialog_set_tv_serset, dialog_set_tv_logout, dialog_set_tv_lang,
            dialog_set_tv_dn, dialog_set_tv_pn, dialog_set_tv_ln, dialog_set_tv_cn,
            dialog_set_tv_ln2, dialog_set_tv_gps, dialog_set_tv_server;

    public SettingDialog(@NonNull Context context) {
        super(context);
    }

    public SettingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public SettingDialog(@NonNull Context context, DriverInfoGson driverInfoGson,
                         UserInfoGson userInfoGson, String name) {
        super(context);
        this.driverInfoGson = driverInfoGson;
        this.name = name;
        this.userInfoGson = userInfoGson;
    }


//    private SettingDialog() {}

//    public static SettingDialog getInstance(){
//        if(settingDialog == null){
//            synchronized (SettingDialog.class){
//                if(settingDialog == null)
//                    settingDialog = new SettingDialog();
//            }
//        }
//        return settingDialog;
//    }

    public void showSettingDialog(Context context, LayoutInflater layoutInflater){
        View dialog_setting = layoutInflater.inflate(R.layout.dialog_setting, null);
        this.setContentView(dialog_setting);
        this.show();

        this.context = context;

        dialog_set_tv_serset = dialog_setting.findViewById(R.id.dialog_set_tv_serset);
        dialog_set_tv_serset.setOnClickListener(this);
        PressUtil.setPressChange(context, dialog_set_tv_serset);
        dialog_set_tv_logout = dialog_setting.findViewById(R.id.dialog_set_tv_logout);
        dialog_set_tv_logout.setOnClickListener(this);
        PressUtil.setPressChange(context, dialog_set_tv_logout);
        dialog_set_tv_lang = dialog_setting.findViewById(R.id.dialog_set_tv_lang);
        dialog_set_tv_lang.setOnClickListener(this);

        dialog_set_tv_dn = dialog_setting.findViewById(R.id.dialog_set_tv_dn);
        dialog_set_tv_dn.setText(name);
        dialog_set_tv_pn = dialog_setting.findViewById(R.id.dialog_set_tv_pn);
        dialog_set_tv_pn.setText(driverInfoGson.getEmergencyPhone());
        dialog_set_tv_ln = dialog_setting.findViewById(R.id.dialog_set_tv_ln);
        dialog_set_tv_ln.setText(driverInfoGson.getLicensePlate());

        dialog_set_tv_cn = dialog_setting.findViewById(R.id.dialog_set_tv_cn);
        dialog_set_tv_cn.setText(driverInfoGson.getDriverId());
        dialog_set_tv_ln2 = dialog_setting.findViewById(R.id.dialog_set_tv_ln2);
        dialog_set_tv_ln2.setText(driverInfoGson.getLicensePlate());
        dialog_set_tv_gps = dialog_setting.findViewById(R.id.dialog_set_tv_gps);
        dialog_set_tv_gps.setText("Connected");
        dialog_set_tv_server = dialog_setting.findViewById(R.id.dialog_set_tv_server);
        dialog_set_tv_server.setText("Connected");
    }

    private void getLogout(){
        apiInterface = RetrofitUtil.getInstance().getRetrofit().create(ApiInterface.class);
        apiInterface.getLogout().enqueue(new Callback<ResultGson>() {
            @Override
            public void onResponse(Call<ResultGson> call, Response<ResultGson> response) {
                ResultGson resultGson = response.body();
                Log.i(TAG, "getLogout onResponse: "+resultGson.getMsg());
                dismiss();
                ((AppCompatActivity)context).finish();
                context.startActivity(new Intent(context, LoginActivity.class));
            }
            @Override
            public void onFailure(Call<ResultGson> call, Throwable t) {
                Log.i(TAG, "getLogout onFailure: "+t);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.dialog_set_tv_serset:
                dismiss();
                ((AppCompatActivity)context).finish();
                context.startActivity(new Intent(context, ServerConfigActivity.class));
                break;
            case R.id.dialog_set_tv_logout:
                getLogout();
                break;
            case R.id.dialog_set_tv_lang:
                String lang = dialog_set_tv_lang.getText().toString();
                if(lang.equals("English")){
                    dialog_set_tv_lang.setText("中文");
                }else if(lang.equals("中文")){
                    dialog_set_tv_lang.setText("English");
                }
                break;
        }
    }
}
