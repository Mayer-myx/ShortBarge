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

public class ArrivalGeofenceEventReceiver extends BroadcastReceiver {

    private final static String TAG = "ArrivalGeofenceEventReceiver";
    private final static String ACTION_TRIGGER_GEOFENCE_ARRIVAL = "com.deepsoft.shortbarge.driver.broadcast.ArrivalGeofenceEventReceiver";

    private boolean isEnter = false;
    private double lat, lng;

    @SuppressLint("LongLogTag")
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !ACTION_TRIGGER_GEOFENCE_ARRIVAL.equals(intent.getAction())) {
            Log.e(TAG, ""+intent.getAction());
            return;
        }

        Bundle bundle = intent.getExtras();

        int status = bundle.getInt(GeoFence.BUNDLE_KEY_FENCESTATUS);
        String customId = bundle.getString(GeoFence.BUNDLE_KEY_CUSTOMID);
        String fenceId = bundle.getString(GeoFence.BUNDLE_KEY_FENCEID);
        GeoFence fence = bundle.getParcelable(GeoFence.BUNDLE_KEY_FENCE);
        Log.e(TAG, ""+status+" "+customId+" "+fenceId+" "+fence.getType());
        Toast.makeText(BaseApplication.getContext(), ""+status+" "+customId+" "+fenceId+" "+fence.getType(), Toast.LENGTH_SHORT).show();


        // 进入围栏还是退出围栏
//        isEnter = intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, false);
//        String tag = intent.getStringExtra("KEY_GEOFENCE_ID");
//        lat = intent.getDoubleExtra("KEY_GEOFENCE_LAT", 0);
//        lng = intent.getDoubleExtra("KEY_GEOFENCE_LNG", 0);
//        Log.e(TAG, "lat="+lat+" lng="+lng);
//        if(isEnter){
//            Toast.makeText(BaseApplication.getContext(), "进入围栏"+TAG, Toast.LENGTH_SHORT).show();
//            Log.e(TAG, "进入围栏");
//        }else{
////            Toast.makeText(BaseApplication.getContext(), "退出围栏"+TAG, Toast.LENGTH_SHORT).show();
//            Log.e(TAG, "退出围栏");
//        }
    }

    @SuppressLint("LongLogTag")
    public boolean getIsEnter() {
        return isEnter;
    }
}
