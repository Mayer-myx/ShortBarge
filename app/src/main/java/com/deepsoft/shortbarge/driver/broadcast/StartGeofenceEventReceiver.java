package com.deepsoft.shortbarge.driver.broadcast;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.fence.GeoFence;
import com.deepsoft.shortbarge.driver.widget.BaseApplication;


public class StartGeofenceEventReceiver extends BroadcastReceiver {

    private final static String TAG = "StartGeofenceEventReceiver";
    private final static String ACTION_TRIGGER_GEOFENCE_START = "com.deepsoft.shortbarge.driver.broadcast.StartGeofenceEventReceiver";

    private boolean isEnter = false;
    private double lat, lng;

    @SuppressLint("LongLogTag")
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !ACTION_TRIGGER_GEOFENCE_START.equals(intent.getAction())) {
            Log.e(TAG, ""+intent.getAction());
            return;
        }

        Bundle bundle = intent.getExtras();

        int status = bundle.getInt(GeoFence.BUNDLE_KEY_FENCESTATUS);
        String customId = bundle.getString(GeoFence.BUNDLE_KEY_CUSTOMID);
        String fenceId = bundle.getString(GeoFence.BUNDLE_KEY_FENCEID);
        int code = bundle.getInt(GeoFence.BUNDLE_KEY_LOCERRORCODE);
        GeoFence fence = bundle.getParcelable(GeoFence.BUNDLE_KEY_FENCE);

        Log.e(TAG, ""+status+" "+customId+" "+fenceId+" "+fence.getType());
        Toast.makeText(BaseApplication.getContext(), ""+status+" "+customId+" "+fenceId+" "+fence.getType(), Toast.LENGTH_SHORT).show();

        switch (status) {
            case GeoFence.STATUS_LOCFAIL:
                Log.e(TAG, "定位失败"+code);
                Toast.makeText(BaseApplication.getContext(), "定位失败"+code, Toast.LENGTH_SHORT).show();
                break;
            case GeoFence.STATUS_IN:
                Log.e(TAG, "进入围栏"+fence.getType());
                Toast.makeText(BaseApplication.getContext(), "进入围栏"+fence.getType(), Toast.LENGTH_SHORT).show();
                break;
            case GeoFence.STATUS_OUT:
                Log.e(TAG, "离开围栏"+fence.getType());
                Toast.makeText(BaseApplication.getContext(), "离开围栏"+fence.getType(), Toast.LENGTH_SHORT).show();
                break;
            case GeoFence.STATUS_STAYED:
                Log.e(TAG, "停留在围栏内 10min");
                Toast.makeText(BaseApplication.getContext(), "停留在围栏内 10min", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    @SuppressLint("LongLogTag")
    public boolean getIsEnter() {
        return isEnter;
    }
}