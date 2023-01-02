package com.deepsoft.shortbarge.driver.widget;

public class Status {
    public interface OnChangeListener {	// 创建interface类
        void onChange();                // 值改变
    }

    private static OnChangeListener onChangeListener;	                // 声明interface接口
    public static void setOnChangeListener(OnChangeListener onChange){	// 创建setListener方法
        onChangeListener = onChange;
    }
    public static void removeChangeListener(){	// 注销Listener
        onChangeListener = null;
    }


    private static String gps, server;
    public static String getGps() {
        return gps;
    }

    public static void setGps(String gps) {
        Status.gps = gps;
        onChangeListener.onChange();
    }

    public static String getServer() {
        return server;
    }

    public static void setServer(String server) {
        Status.server = server;
        onChangeListener.onChange();
    }
}
