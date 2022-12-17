package com.deepsoft.shortbarge.driver.view;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.client.JWebSocketClient;
import com.deepsoft.shortbarge.driver.widget.MyDialog;

import java.net.URI;

public class MessageDialog {

    private static URI uri;
    private static JWebSocketClient client;

    public MessageDialog(){
        uri = URI.create("ws://websocket/wsDriver/{token}");
        client = new JWebSocketClient(uri) {
            @Override
            public void onMessage(String message) {
                // message就是接收到的消息
                Log.e("JWebSClientService", message);
            }
        };
    }

    public static void showMessageDialog(Context context, LayoutInflater layoutInflater){
        try {
            client.connectBlocking();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (client != null && client.isOpen()) {
            client.send("你好");
        }


        View dialog_wait_connect = layoutInflater.inflate(R.layout.dialog_voice_message, null);
        final MyDialog dialog = new MyDialog(context);
        dialog.setContentView(dialog_wait_connect);
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
}
