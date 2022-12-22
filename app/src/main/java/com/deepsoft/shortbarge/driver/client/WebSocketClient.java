package com.deepsoft.shortbarge.driver.client;

import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketClient {
    private final static String TAG = "WebSocketClient";
    private Request request;
    private OkHttpClient client;
    private WebSocket webSocket;

    public WebSocketClient(String token) {
        client = new OkHttpClient();
        request = new Request.Builder()
                .url("ws://221.12.170.99:8081/websocket/wsDriver/")
                .addHeader("token", token)
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
