package com.deepsoft.shortbarge.driver.view;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
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
    private HsjWebSocketListener listener = new HsjWebSocketListener();
    private StringBuilder stringBuilder = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        URI uri = URI.create("ws://echo.websocket.org");

        initSocket();
        webSocket();
    }

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
        stringBuilder.append(System.currentTimeMillis() + "-onClick ");
        output();
        webSocketClient.start(listener);
    }

    public void closeSocket() {
        webSocketClient.close();
    }

    private class HsjWebSocketListener extends WebSocketListener {
        @Override
        public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
            stringBuilder.append(System.currentTimeMillis() + " ");
            String json = "{\n" +
                    "     \"message\": \"{\\\"msg\\\":\\\"hello111112222!!!\\\",\\\"msgType\\\":1}\",\n" +
                    "     \"type\": 2\n" +
                    " }";
            webSocket.send(json);
//            webSocket.send("hello world");
//            webSocket.send(ByteString.decodeHex("adef"));
            webSocket.close(1000, "");
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            stringBuilder.append(System.currentTimeMillis() + "-onMessage: " + text + " ");
            output();
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
            stringBuilder.append(System.currentTimeMillis() + "-onMessage byteString: " + bytes + " ");
            output();
        }

        @Override
        public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
            Log.d(TAG, "onFailure: " + t.getMessage());
            stringBuilder.append(System.currentTimeMillis() + "-onFailure: " + t.getMessage() + " ");
            output();
        }

        @Override
        public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            stringBuilder.append(System.currentTimeMillis() + "-onClosing: " + code + "/" + reason + " ");
            output();
        }

        @Override
        public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            stringBuilder.append(System.currentTimeMillis() + "-onClosed: " + code + "/" + reason + " ");
            output();
        }
    }

    private void output() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, stringBuilder.toString());
            }
        });
    }
}