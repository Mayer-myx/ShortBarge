package com.deepsoft.shortbarge.driver.view;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.client.WebSocketClient;

import org.jetbrains.annotations.NotNull;

import java.net.URI;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class TestActivity extends AppCompatActivity {

    private final static String TAG = "TestActivity";

    private WebSocketClient webSocketClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

//        URI uri = URI.create("ws://221.12.170.99:8081/websocket/wsDriver/{token}");
        URI uri = URI.create("ws://echo.websocket.org");

        initSocket();
        webSocket();
    }


    HsjWebSocketListener listener = new HsjWebSocketListener();
    StringBuilder stringBuilder = new StringBuilder();

    public void initSocket() {
        String token = getIntent().getStringExtra("token");
        webSocketClient = new WebSocketClient(token);
    }

    public void webSocket() {
        if (webSocketClient == null) {
            Toast.makeText(this, "请初始化Socket", Toast.LENGTH_SHORT).show();
            return;
        }
        stringBuilder.setLength(0);
        stringBuilder.append(System.currentTimeMillis() + "-onClick\n");
        output();
        webSocketClient.start(listener);
    }

    public void closeSocket() {
        webSocketClient.close();
    }

    private class HsjWebSocketListener extends WebSocketListener {
        @Override
        public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
            stringBuilder.append(System.currentTimeMillis() + "\n");
            webSocket.send("hello world");
//            webSocket.send("welcome");
//            webSocket.send(ByteString.decodeHex("adef"));
            webSocket.close(1000, "byebye");
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            stringBuilder.append(System.currentTimeMillis() + "-onMessage: " + text + "\n");
            output();
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
            stringBuilder.append(System.currentTimeMillis() + "-onMessage byteString: " + bytes + "\n");
            output();
        }

        @Override
        public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
            Log.d(TAG, "onFailure: " + t.getMessage());
            stringBuilder.append(System.currentTimeMillis() + "-onFailure: " + t.getMessage() + "\n");
            output();
        }

        @Override
        public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            stringBuilder.append(System.currentTimeMillis() + "-onClosing: " + code + "/" + reason + "\n");
            output();
        }

        @Override
        public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            stringBuilder.append(System.currentTimeMillis() + "-onClosed: " + code + "/" + reason + "\n");
            output();
        }
    }

    private void output() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, stringBuilder.toString());
                Toast.makeText(TestActivity.this, stringBuilder.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}