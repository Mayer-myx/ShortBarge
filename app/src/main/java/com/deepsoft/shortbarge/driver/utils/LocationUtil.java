package com.deepsoft.shortbarge.driver.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;

/**
 * @className: LocationUtils
 * @classDescription: 定位工具类
 */
public class LocationUtil {

    // GPS定位
    private final static String GPS_LOCATION = LocationManager.GPS_PROVIDER;
    // 网络定位
    private final static String NETWORK_LOCATION = LocationManager.NETWORK_PROVIDER;
    // 解码经纬度最大结果数目
    private final static int MAX_RESULTS = 1;
    // 时间更新间隔，单位：ms
    private static long MIN_TIME = 1000;
    // 位置刷新距离，单位：m
    private final static float MIN_DISTANCE = (float) 0.01;
    // singleton
    private static LocationUtil instance;
    // 定位回调
    private LocationCallBack mLocationCallBack;
    // 定位管理实例
    LocationManager mLocationManager;
    // 上下文
    private Context mContext;

    /**
     * 构造函数
     * @author leibing
     */
    private LocationUtil(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * singleton
     */
    public static LocationUtil getInstance(Context mContext) {
        if (instance == null) {
            instance = new LocationUtil(mContext);
        }
        return instance;
    }

    public void setMinTime(long time) {
        this.MIN_TIME = time;
    }

    /**
     * 获取定位
     * @param mLocationCallBack 定位回调
     */
    @SuppressWarnings("MissingPermission")
    public void getLocation(LocationCallBack mLocationCallBack, int type) {
        this.mLocationCallBack = mLocationCallBack;
        if (mLocationCallBack == null)
            return;
        // 定位管理初始化
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS定位，较精确，也比较耗电
        LocationProvider gpsProvider = mLocationManager.getProvider(LocationManager.GPS_PROVIDER);
        // 通过网络定位，对定位精度度不高或省点情况可考虑使用
        LocationProvider netProvider = mLocationManager.getProvider(LocationManager.NETWORK_PROVIDER);
        if(type == 1){
            // 优先考虑GPS定位，其次网络定位。
            if (gpsProvider != null){
                gpsLocation();
            }else if(netProvider != null){
                netWorkLocation();
            }else {
                mLocationCallBack.setLocation(null);
            }
        } else {
            // 优先网络定位
            if (netProvider != null) {
                netWorkLocation();
            } else if (gpsProvider != null) {
                gpsLocation();
            } else {
                mLocationCallBack.setLocation(null);
            }
        }
    }

    /**
     * GPS定位
     */
    @SuppressWarnings("MissingPermission")
    private void gpsLocation(){
        if (mLocationManager == null)
            mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(GPS_LOCATION, MIN_TIME, MIN_DISTANCE, mLocationListener);
    }

    /**
     * 网络定位
     */
    @SuppressWarnings("MissingPermission")
    private void netWorkLocation(){
        if (mLocationManager == null)
            mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(NETWORK_LOCATION, MIN_TIME, MIN_DISTANCE, mLocationListener);
    }

    // 定位监听
    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (mLocationCallBack != null){
                mLocationCallBack.setLocation(location);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
            // 如果gps定位不可用,改用网络定位
            if (provider.equals(LocationManager.GPS_PROVIDER)){
                netWorkLocation();
            }
        }
    };

    public void stop(){
        if (mContext != null) {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mLocationManager.removeUpdates(mLocationListener);
        }
    }

    /**
     * @className: LocationCallBack
     */
    public interface LocationCallBack{
        void setLocation(Location location);
    }
}