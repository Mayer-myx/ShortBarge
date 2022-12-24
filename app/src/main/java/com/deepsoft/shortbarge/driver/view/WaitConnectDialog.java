package com.deepsoft.shortbarge.driver.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.widget.MyDialog;

public class WaitConnectDialog {

    private static WaitConnectDialog waitConnectDialog;

    private WaitConnectDialog() {}

    public static WaitConnectDialog getInstance(){
        if(waitConnectDialog == null){
            synchronized (WaitConnectDialog.class){
                if(waitConnectDialog == null)
                    waitConnectDialog = new WaitConnectDialog();
            }
        }
        return waitConnectDialog;
    }

    public void showWaitConnectDialog(Context context, LayoutInflater layoutInflater){
        View dialog_wait_connect = layoutInflater.inflate(R.layout.dialog_wait_connect, null);
        final MyDialog dialog = new MyDialog(context);
        dialog.setContentView(dialog_wait_connect);
//        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
