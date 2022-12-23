package com.deepsoft.shortbarge.driver.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.widget.MyDialog;

public class ConnectFailDialog {

    private static ConnectFailDialog connectFailDialog;

    private ConnectFailDialog() {}

    public static ConnectFailDialog getInstance(){
        if(connectFailDialog == null){
            synchronized (ConnectFailDialog.class){
                if(connectFailDialog == null)
                    connectFailDialog = new ConnectFailDialog();
            }
        }
        return connectFailDialog;
    }

    public void showConnectFailDialog(Context context, LayoutInflater layoutInflater){
        View dialog_wait_connect = layoutInflater.inflate(R.layout.dialog_connect_fail, null);
        final MyDialog dialog = new MyDialog(context);
        dialog.setContentView(dialog_wait_connect);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
