package com.deepsoft.shortbarge.driver.broadcast;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import com.deepsoft.shortbarge.driver.widget.BaseApplication;


public class StartGeofenceEventReceiver extends BroadcastReceiver {

    private final static String TAG = "StartGeofenceEventReceiver";
    private final static String ACTION_TRIGGER_GEOFENCE_START = "com.deepsoft.shortbarge.driver.broadcast.StartGeofenceEventReceiver";

    private boolean isEnter = true;
    private double lat, lng;

    @SuppressLint("LongLogTag")
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !ACTION_TRIGGER_GEOFENCE_START.equals(intent.getAction())) {
            return;
        }

        // 进入围栏还是退出围栏
        isEnter = intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, true);
        String tag = intent.getStringExtra("KEY_GEOFENCE_ID");
        lat = intent.getDoubleExtra("KEY_GEOFENCE_LAT", 0);
        lng = intent.getDoubleExtra("KEY_GEOFENCE_LNG", 0);
    }

    @SuppressLint("LongLogTag")
    public boolean getIsEnter() {
        if(isEnter){
//            Toast.makeText(BaseApplication.getContext(), "进入围栏"+TAG, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "进入围栏");
        }else{
            Toast.makeText(BaseApplication.getContext(), "退出围栏"+TAG, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "退出围栏");
        }
        return isEnter;
    }
}