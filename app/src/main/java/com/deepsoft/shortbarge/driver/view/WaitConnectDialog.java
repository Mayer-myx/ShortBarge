package com.deepsoft.shortbarge.driver.view;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.widget.MyDialog;

public class WaitConnectDialog extends MyDialog {

//    private static WaitConnectDialog waitConnectDialog;
    private Handler mHandler = new Handler();

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
//        dialog.setCanceledOnTouchOutside(false);
        this.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dismiss();
            }
        },60*1000);
    }
}
