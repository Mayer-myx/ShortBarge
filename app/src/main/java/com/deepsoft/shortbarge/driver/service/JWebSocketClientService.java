package com.deepsoft.shortbarge.driver.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.deepsoft.shortbarge.driver.client.JWebSocketClient;

import java.net.URI;

public class JWebSocketClientService extends Service {

    private URI uri;
    public JWebSocketClient client;
    private JWebSocketClientBinder mBinder = new JWebSocketClientBinder();

    //用于Activity和service通讯
    class JWebSocketClientBinder extends Binder {
        public JWebSocketClientService getService() {
            return JWebSocketClientService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}