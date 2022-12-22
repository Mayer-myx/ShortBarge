package com.deepsoft.shortbarge.driver.widget;

import android.app.Application;
import android.content.Context;

public class BaseApplication extends Application {

    private static BaseApplication application;
    private static Context context;

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
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        //在Application创建时,读取Application
        application = this;
        context = getApplicationContext();
    }

}
