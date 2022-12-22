package com.deepsoft.shortbarge.driver.view;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.client.JWebSocketClient;
import com.deepsoft.shortbarge.driver.constant.ReadyState;
import com.deepsoft.shortbarge.driver.utils.PressUtil;
import com.deepsoft.shortbarge.driver.widget.MyDialog;

import java.net.URI;

public class MessageDialog implements View.OnClickListener{

    private static MessageDialog messageDialog;
    private MyDialog dialog;
    private View dialog_voice_message;
    private ImageView dialog_vm_iv_close;
    private TextView dialog_vm_tv_sent;
    private RecyclerView dialog_vm_rv;


    private MessageDialog(){
    }


    public static MessageDialog getInstance(){
        if(messageDialog == null){
            synchronized (MessageDialog.class){
                if(messageDialog == null)
                    messageDialog = new MessageDialog();
            }
        }
        return messageDialog;
    }


    public void showMessageDialog(Context context, LayoutInflater layoutInflater){
        dialog_voice_message = layoutInflater.inflate(R.layout.dialog_voice_message, null);
        dialog_vm_iv_close = dialog_voice_message.findViewById(R.id.dialog_vm_iv_close);
        dialog_vm_iv_close.setOnClickListener(this);
        PressUtil.setPressChange(context, dialog_vm_iv_close);
        dialog_vm_tv_sent = dialog_voice_message.findViewById(R.id.dialog_vm_tv_sent);
        dialog_vm_tv_sent.setOnClickListener(this);
        PressUtil.setPressChange(context, dialog_vm_tv_sent);
        dialog_vm_rv = dialog_voice_message.findViewById(R.id.dialog_vm_rv);

        dialog = new MyDialog(context);
        dialog.setContentView(dialog_voice_message);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.dialog_vm_iv_close:
                dialog.dismiss();
                break;
            case R.id.dialog_vm_tv_sent:
                break;
        }
    }
}
