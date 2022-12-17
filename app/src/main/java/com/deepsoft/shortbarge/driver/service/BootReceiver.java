package com.deepsoft.shortbarge.driver.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.deepsoft.shortbarge.driver.view.LoginActivity;

public class BootReceiver extends BroadcastReceiver {

    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("myx", "BootReceiver start");
        //启动MainActivity
        Intent intent1 = new Intent(context, LoginActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//添加标记
        context.startActivity(intent1);
        Log.v("myx", "BootReceiver end");
    }
}
