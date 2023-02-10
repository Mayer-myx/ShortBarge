package com.deepsoft.shortbarge.driver.view;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.constant.ConstantGlobal;
import com.deepsoft.shortbarge.driver.bean.DriverInfoGson;
import com.deepsoft.shortbarge.driver.bean.ResultGson;
import com.deepsoft.shortbarge.driver.retrofit.ApiInterface;
import com.deepsoft.shortbarge.driver.utils.MultiLanguageUtil;
import com.deepsoft.shortbarge.driver.utils.PressUtil;
import com.deepsoft.shortbarge.driver.utils.RetrofitUtil;
import com.deepsoft.shortbarge.driver.utils.SpUtil;
import com.deepsoft.shortbarge.driver.websocket.WsManager;
import com.deepsoft.shortbarge.driver.widget.BaseApplication;
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
    private String server;

    private TextView dialog_set_tv_cancel, dialog_set_tv_logout, dialog_set_tv_edit, dialog_set_tv_switch,
            dialog_set_tv_dn, dialog_set_tv_account, dialog_set_tv_car, dialog_set_tv_ln,
            dialog_set_tv_lang_label, dialog_set_tv_server, dialog_set_tv_lang;


    public SettingDialog(@NonNull Context context) {
        super(context);
        this.driverInfoGson = new DriverInfoGson();
        this.server = context.getString(R.string.state_connected);
        this.context = context;
    }

    public SettingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }


    public void showSettingDialog(Context context, LayoutInflater layoutInflater){
        View dialog_setting = layoutInflater.inflate(R.layout.dialog_setting, null);
        this.setContentView(dialog_setting);
        this.show();

        this.context = context;

        dialog_set_tv_switch = dialog_setting.findViewById(R.id.dialog_set_tv_switch);
        dialog_set_tv_switch.setOnClickListener(this);
        dialog_set_tv_cancel = dialog_setting.findViewById(R.id.dialog_set_tv_switch);
        dialog_set_tv_edit = dialog_setting.findViewById(R.id.dialog_set_tv_edit);
        dialog_set_tv_edit.setOnClickListener(this);
        PressUtil.setPressChange(context, dialog_set_tv_edit);
        dialog_set_tv_cancel = dialog_setting.findViewById(R.id.dialog_set_tv_cancel);
        dialog_set_tv_cancel.setOnClickListener(this);
        PressUtil.setPressChange(context, dialog_set_tv_cancel);
        dialog_set_tv_logout = dialog_setting.findViewById(R.id.dialog_set_tv_logout);
        dialog_set_tv_logout.setOnClickListener(this);
        PressUtil.setPressChange(context, dialog_set_tv_logout);

        dialog_set_tv_dn = dialog_setting.findViewById(R.id.dialog_set_tv_dn);
        dialog_set_tv_account = dialog_setting.findViewById(R.id.dialog_set_tv_account);
        dialog_set_tv_account.setText(driverInfoGson.getNameEng());
        dialog_set_tv_car = dialog_setting.findViewById(R.id.dialog_set_tv_car);
        dialog_set_tv_car.setText(""+driverInfoGson.getTruckId());
        dialog_set_tv_ln = dialog_setting.findViewById(R.id.dialog_set_tv_ln);
        dialog_set_tv_ln.setText(driverInfoGson.getLicense());
        dialog_set_tv_server = dialog_setting.findViewById(R.id.dialog_set_tv_server);
        dialog_set_tv_server.setText(server);
        dialog_set_tv_lang = dialog_setting.findViewById(R.id.dialog_set_tv_lang);

        dialog_set_tv_lang_label = dialog_setting.findViewById(R.id.dialog_set_tv_lang_label);
        String lang = dialog_set_tv_lang_label.getText().toString();
        if(lang.equals("Language")){
            dialog_set_tv_dn.setText(driverInfoGson.getNameEng());
            dialog_set_tv_lang.setText("English");
        }else{
            dialog_set_tv_dn.setText(driverInfoGson.getName());
            dialog_set_tv_lang.setText("中文");
        }
    }

    private void getLogout(){
        apiInterface = RetrofitUtil.getInstance().getRetrofit().create(ApiInterface.class);
        apiInterface.getLogout().enqueue(new Callback<ResultGson>() {
            @Override
            public void onResponse(Call<ResultGson> call, Response<ResultGson> response) {
                ResultGson resultGson = response.body();
                Log.i(TAG, "getLogout onResponse: "+resultGson.getMsg());
                if(resultGson.getSuccess()) {
                    dismiss();
                    WsManager.getInstance().disconnect();
                    ((MainActivity) context).finish();
                    context.startActivity(new Intent(context, LoginActivity.class));
                }else{
                    Toast.makeText(context, "登出失败", Toast.LENGTH_SHORT).show();
                }
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
            case R.id.dialog_set_tv_cancel:
                this.dismiss();
                break;
            case R.id.dialog_set_tv_logout:
                getLogout();
                break;
            case R.id.dialog_set_tv_edit:
                context.startActivity(new Intent(context, ServerConfigActivity.class));
                break;
            case R.id.dialog_set_tv_switch:
                String lang = dialog_set_tv_lang_label.getText().toString();
                if(lang.equals("Language")){
                    changeLanguage("zh", "CN");
                    dialog_set_tv_dn.setText(driverInfoGson.getNameEng());
                    dialog_set_tv_lang.setText("English");
                }else{
                    changeLanguage("en", "US");
                    dialog_set_tv_dn.setText(driverInfoGson.getName());
                    dialog_set_tv_lang.setText("中文");
                }
                break;
        }
    }

    /**
     * 修改应用内语言设置
     */
    private void changeLanguage(String language, String area) {
        if (TextUtils.isEmpty(language) && TextUtils.isEmpty(area)) {
            //如果语言和地区都是空，那么跟随系统
            SpUtil.saveString(ConstantGlobal.LOCALE_LANGUAGE, "");
            SpUtil.saveString(ConstantGlobal.LOCALE_COUNTRY, "");
        } else {
            //不为空，那么修改app语言，并true是把语言信息保存到sp中，false是不保存到sp中
            Locale newLocale = new Locale(language, area);
            MultiLanguageUtil.changeAppLanguage(context, newLocale, true);
            MultiLanguageUtil.changeAppLanguage(BaseApplication.getContext(), newLocale, true);
        }
        //重启app,这一步一定要加上，如果不重启app，可能打开新的页面显示的语言会不正确
        Intent intent = new Intent(BaseApplication.getContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        BaseApplication.getContext().startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setDriverInfoGson(DriverInfoGson driverInfoGson) {
        this.driverInfoGson = driverInfoGson;
    }
}
