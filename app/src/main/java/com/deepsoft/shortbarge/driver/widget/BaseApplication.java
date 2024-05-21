package com.deepsoft.shortbarge.driver.widget;

import android.app.Application;
import android.content.Context;

import com.deepsoft.shortbarge.driver.constant.ConstantGlobal;
import com.deepsoft.shortbarge.driver.utils.CrashHandler;
import com.deepsoft.shortbarge.driver.utils.MultiLanguageUtil;
import com.deepsoft.shortbarge.driver.utils.SpUtil;
import com.deepsoft.shortbarge.driver.callback.ForegroundCallbacks;
import com.deepsoft.shortbarge.driver.websocket.WsManager;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationManagerOptions;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class BaseApplication extends Application {

    private static BaseApplication application;
    private static Context context;

    @Override
    protected void attachBaseContext(Context base) {
        //系统语言等设置发生改变时会调用此方法，需要要重置app语言
        super.attachBaseContext(MultiLanguageUtil.attachBaseContext(base));
    }

    /**
     * 返回application
     **/
    public static BaseApplication getApplication(){
        return application;
    }

    /**
     * 返回context
     **/
    public static Context getContext() {
        return context;
    }

    /**
     * 对于一个应用来说 android入口并不是Activity中的OnCreate()而是Application里面的Oncreate()
     * 也就相当于是java中的Main方法，只不过这个方法被封装了
     **/
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        //在Application创建时,读取Application
        application = this;
        context = getApplicationContext();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        CrashHandler.getInstance(getApplicationContext()).setCrashLogDir(getCrashLogDir());
//        LogHandler.initLogFile(new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/log" + "/goods_" + LocalDateTime.now().format(formatter) + ".log"));

        initAppStatusListener();
        //注册Activity生命周期监听回调，此部分一定加上，因为有些版本不加的话多语言切换不回来
        registerActivityLifecycleCallbacks(callbacks);
        changeLanguage();
    }

    protected String getCrashLogDir() {
        String sb = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/log";
        return sb;
    }
    
    
    private void changeLanguage() {
        String spLanguage = SpUtil.getString(getApplicationContext(), ConstantGlobal.LOCALE_LANGUAGE);
        String spCountry = SpUtil.getString(getApplicationContext(), ConstantGlobal.LOCALE_COUNTRY);
        if (!TextUtils.isEmpty(spLanguage) && !TextUtils.isEmpty(spCountry)) {
            // 如果有一个不同
            if (!MultiLanguageUtil.isSameWithSetting(this)) {
                Locale locale = new Locale(spLanguage, spCountry);
                MultiLanguageUtil.changeAppLanguage(getApplicationContext(), locale, false);
            }
        }
    }

    ActivityLifecycleCallbacks callbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            String language = SpUtil.getString(getApplicationContext(), ConstantGlobal.LOCALE_LANGUAGE);
            String country = SpUtil.getString(getApplicationContext(), ConstantGlobal.LOCALE_COUNTRY);
            if (!TextUtils.isEmpty(language) && !TextUtils.isEmpty(country)) {
                //强制修改应用语言
                if (!MultiLanguageUtil.isSameWithSetting(activity)) {
                    Locale locale = new Locale(language, country);
                    MultiLanguageUtil.changeAppLanguage(activity, locale, false);
//                    activity.recreate();
                }
            }
        }


        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
        //Activity 其它生命周期的回调
    };

    private void initAppStatusListener() {
        ForegroundCallbacks.init(this).addListener(new ForegroundCallbacks.Listener() {
            @Override
            public void onBecameForeground() {
                Log.d("WsManager", "应用回到前台调用重连方法");
                WsManager.getInstance().reconnect();
            }

            @Override
            public void onBecameBackground() {

            }
        });
    }
}
