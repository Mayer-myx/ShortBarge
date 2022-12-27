package com.deepsoft.shortbarge.driver.view;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.client.BaseWebSocketClient;
import com.deepsoft.shortbarge.driver.websocket.WsManager;

import org.jetbrains.annotations.NotNull;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class TestActivity extends AppCompatActivity {

    private final static String TAG = "TestActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        String token = getIntent().getStringExtra("token");
        WsManager.getInstance().init(token);

//        initSocket();
//        webSocket();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        closeSocket();
        WsManager.getInstance().disconnect();
    }

}