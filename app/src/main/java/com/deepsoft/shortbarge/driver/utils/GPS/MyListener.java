package com.deepsoft.shortbarge.driver.utils.GPS;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;

public class MyListener implements GPSLocationListener {

    private final static String TAG = "MyListener";
    private Location myLocation;

    public Location getMyLocation() {
        return myLocation;
    }

    @Override
    public void UpdateLocation(Location location) {
        if (location != null) {
            Log.e(TAG, "经度：" + location.getLongitude() + "\n纬度：" + location.getLatitude());
            myLocation = location;
        }
    }

    @Override
    public void UpdateStatus(String provider, int status, Bundle extras) {
        if ("gps".equals(provider)) {
            Log.e(TAG, "定位类型：" + provider);
        }
    }

    @Override
    public void UpdateGPSProviderStatus(int gpsStatus) {
        switch (gpsStatus) {
            case GPSProviderStatus.GPS_ENABLED:
                Log.e(TAG,  "GPS开启");
                break;
            case GPSProviderStatus.GPS_DISABLED:
                Log.e(TAG, "GPS关闭");
                break;
            case GPSProviderStatus.GPS_OUT_OF_SERVICE:
                Log.e(TAG, "GPS不可用");
                break;
            case GPSProviderStatus.GPS_TEMPORARILY_UNAVAILABLE:
                Log.e(TAG, "GPS暂时不可用");
                break;
            case GPSProviderStatus.GPS_AVAILABLE:
                Log.e(TAG, "GPS可用啦");
                break;
        }
    }
}