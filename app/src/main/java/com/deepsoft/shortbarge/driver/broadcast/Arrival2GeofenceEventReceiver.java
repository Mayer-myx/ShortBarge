package com.deepsoft.shortbarge.driver.broadcast;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

public class Arrival2GeofenceEventReceiver extends BroadcastReceiver {

    private final static String TAG = "Arrival2GeofenceEventReceiver";
    private final static String ACTION_TRIGGER_GEOFENCE_ARRIVAL2 = "com.deepsoft.shortbarge.driver.broadcast.Arrival2GeofenceEventReceiver";

    private boolean isEnter = false;

    @SuppressLint("LongLogTag")
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !ACTION_TRIGGER_GEOFENCE_ARRIVAL2.equals(intent.getAction())) {
            return;
        }

        // 进入围栏还是退出围栏
        isEnter = intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, true);
        String tag = intent.getStringExtra("KEY_GEOFENCE_ID");
        double lat = intent.getDoubleExtra("KEY_GEOFENCE_LAT", 0);
        double lng = intent.getDoubleExtra("KEY_GEOFENCE_LNG", 0);

        if(isEnter){
            Toast.makeText(context, "进入围栏"+TAG, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "进入围栏");
        }else{
            Toast.makeText(context, "退出围栏"+TAG, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "退出围栏");
        }
    }

    public boolean getIsEnter() {
        return isEnter;
    }
}
