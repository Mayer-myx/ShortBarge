package com.deepsoft.shortbarge.driver.client;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class BaseWebSocketClient {
    private final static String TAG = "WebSocketClient";
    private Request request;
    private OkHttpClient client;
    private WebSocket webSocket;

    public BaseWebSocketClient(String token) {
        client = new OkHttpClient.Builder()
                .writeTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();
        // 连接地址：/websocket/wsDriver/{token}  ws://221.12.170.99:8081/websocket/wsDriver/
        request = new Request.Builder()
                .url("ws://121.40.165.18:8800")
                .addHeader("token", token)
                .addHeader("Upgrade", "websocket")
                .addHeader("Connection", "Upgrade")
                .build();
    }

    public OkHttpClient getClient() {
        return client;
    }

    public void start(WebSocketListener listener) {
        client.dispatcher().cancelAll();
        Log.e(TAG, "request id = " + request.toString());
        Log.e(TAG, "listener id = " + listener.toString());
        webSocket = client.newWebSocket(request, listener);
        Log.e(TAG, "webSocket id = " + webSocket.toString());
    }

    public void close() {
        if (webSocket != null) {
            webSocket.close(1000, null);
        }
        client.dispatcher().executorService().shutdown();
    }
}
