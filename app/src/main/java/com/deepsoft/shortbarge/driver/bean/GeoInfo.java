package com.deepsoft.shortbarge.driver.bean;

public class GeoInfo {

    private double lng, lat;
    private float r;

    public GeoInfo(double lng, double lat, float r) {
        this.lat = lat;
        this.lng = lng;
        this.r = r;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public float getR() {
        return r;
    }

    public void setR(float r) {
        this.r = r;
    }
}
