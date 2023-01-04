package com.deepsoft.shortbarge.driver.view;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.widget.MyDialog;

public class ConnectFailDialog extends MyDialog{

    public ConnectFailDialog(@NonNull Context context) {
        super(context);
    }

    public ConnectFailDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

//    private static ConnectFailDialog connectFailDialog;
//
//    private ConnectFailDialog() {}
//
//    public static ConnectFailDialog getInstance(){
//        if(connectFailDialog == null){
//            synchronized (ConnectFailDialog.class){
//                if(connectFailDialog == null)
//                    connectFailDialog = new ConnectFailDialog();
//            }
//        }
//        return connectFailDialog;
//    }

    public void showConnectFailDialog(Context context, LayoutInflater layoutInflater, Intent intent){
        View dialog_wait_connect = layoutInflater.inflate(R.layout.dialog_connect_fail, null);
        this.setContentView(dialog_wait_connect);
        this.show();

        TextView dialog_fail_tv_exit = dialog_wait_connect.findViewById(R.id.dialog_fail_tv_exit);
        dialog_fail_tv_exit.setOnClickListener(v->{
            this.dismiss();
            ((AppCompatActivity)(context)).finish();
            context.startActivity(new Intent(context, LoginActivity.class));
        });
        TextView dialog_fail_tv_retry = dialog_wait_connect.findViewById(R.id.dialog_fail_tv_retry);
        dialog_fail_tv_retry.setOnClickListener(v->{
            this.dismiss();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            ((AppCompatActivity)(context)).finish();
            context.startActivity(intent);
        });
    }
}
