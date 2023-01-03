package com.deepsoft.shortbarge.driver.view;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.widget.MyDialog;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WaitConnectDialog extends MyDialog {

//    private static WaitConnectDialog waitConnectDialog;
    private long startTime;

    public WaitConnectDialog(@NonNull Context context) {
        super(context);
    }

    public WaitConnectDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

//    private WaitConnectDialog() {}
//
//    public static WaitConnectDialog getInstance(){
//        if(waitConnectDialog == null){
//            synchronized (WaitConnectDialog.class){
//                if(waitConnectDialog == null)
//                    waitConnectDialog = new WaitConnectDialog();
//            }
//        }
//        return waitConnectDialog;
//    }

    public void showWaitConnectDialog(Context context, LayoutInflater layoutInflater){
        View dialog_wait_connect = layoutInflater.inflate(R.layout.dialog_wait_connect, null);
        this.setContentView(dialog_wait_connect);
        this.setCanceledOnTouchOutside(false);
        this.show();
        if(startTime == 0) {
            startTime = System.currentTimeMillis();
            Log.e("1 START TIME", ""+startTime);
        }
    }

    public long getWaitTime(){
        if(startTime == 0){
            return -1;
        }else {
            long endTime = System.currentTimeMillis();
            Log.e("2 START TIME", "" + startTime);
            Log.e("2 END TIME", "" + endTime);
            Log.e("2 second", String.valueOf((endTime - startTime) / 1000));
            return (endTime - startTime) / 1000;
        }
    }
}
