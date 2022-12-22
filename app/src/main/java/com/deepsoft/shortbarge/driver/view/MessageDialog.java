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

    private static URI uri;
    private static MessageDialog messageDialog;
    private static JWebSocketClient client;

    private MyDialog dialog;
    private View dialog_voice_message;
    private ImageView dialog_vm_iv_close;
    private TextView dialog_vm_tv_sent;
    private RecyclerView dialog_vm_rv;


    private MessageDialog(){
        uri = URI.create("ws://192.168.31.167");
        client = new JWebSocketClient(uri) {
            @Override
            public void onMessage(String message) {
                // message就是接收到的消息
                Log.e("JWebSClientService", message);
            }
        };
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
        if (client != null && !client.isOpen()){
            if (client.getReadyState().equals(ReadyState.NOT_YET_CONNECTED)){
                try {
                    client.connectBlocking();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (client.getReadyState().equals(ReadyState.CLOSING) || client.getReadyState().equals(ReadyState.CLOSED)){
                try {
                    client.reconnectBlocking();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if (client != null && client.isOpen()) {
            client.send("你好");
        }

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


    /**
     * 断开连接
     */
    private void closeConnect() {
        try {
            if (null != client) {
                client.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client = null;
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.dialog_vm_iv_close:
                closeConnect();
                dialog.dismiss();
                break;
            case R.id.dialog_vm_tv_sent:
                break;
        }
    }
}
