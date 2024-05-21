package com.deepsoft.shortbarge.driver.bean;

import android.os.Bundle;

import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentPoi;

import java.util.List;

public class MyLocation implements TencentLocation {
    private double mLatitude;
    private double mLongitude;

    public MyLocation(double latitude, double longitude) {
        mLatitude = latitude;
        mLongitude = longitude;
    }

    @Override
    public String getProvider() {
        return null;
    }

    @Override
    public String getSourceProvider() {
        return null;
    }

    @Override
    public double getLatitude() {
        return mLatitude;
    }

    @Override
    public double getLongitude() {
        return mLongitude;
    }

    @Override
    public double getAltitude() {
        return 0;
    }

    @Override
    public float getAccuracy() {
        return 0;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getAddress() {
        return null;
    }

    @Override
    public String getNation() {
        return null;
    }

    @Override
    public int getNationCode() {
        return 0;
    }

    @Override
    public String getProvince() {
        return null;
    }

    @Override
    public String getCity() {
        return null;
    }

    @Override
    public String getDistrict() {
        return null;
    }

    @Override
    public String getTown() {
        return null;
    }

    @Override
    public String getVillage() {
        return null;
    }

    @Override
    public String getStreet() {
        return null;
    }

    @Override
    public String getStreetNo() {
        return null;
    }

    @Override
    public Integer getAreaStat() {
        return null;
    }

    @Override
    public List<TencentPoi> getPoiList() {
        return null;
    }

    @Override
    public float getBearing() {
        return 0;
    }

    @Override
    public float getSpeed() {
        return 0;
    }

    @Override
    public long getTime() {
        return 0;
    }

    @Override
    public long getElapsedRealtime() {
        return 0;
    }

    @Override
    public int getGPSRssi() {
        return 0;
    }

    @Override
    public String getIndoorBuildingId() {
        return null;
    }

    @Override
    public String getIndoorBuildingFloor() {
        return null;
    }

    @Override
    public int getIndoorLocationType() {
        return 0;
    }

    @Override
    public double getDirection() {
        return 0;
    }

    @Override
    public String getCityCode() {
        return null;
    }

    @Override
    public String getadCode() {
        return null;
    }

    @Override
    public String getCityPhoneCode() {
        return null;
    }

    @Override
    public int getCoordinateType() {
        return 0;
    }

    @Override
    public int getFakeReason() {
        return 0;
    }

    @Override
    public float getFakeProbability() {
        return 0;
    }

    @Override
    public int isMockGps() {
        return 0;
    }

    @Override
    public Bundle getExtra() {
        return null;
    }
}
