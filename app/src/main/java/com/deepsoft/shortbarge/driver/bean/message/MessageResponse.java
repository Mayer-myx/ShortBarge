package com.deepsoft.shortbarge.driver.bean.message;

import com.google.gson.annotations.SerializedName;

public class MessageResponse {

    //type=0-WebSocket连接成功；1-获取经纬度；2-聊天消息；3-通知服务端收到聊天消息；
    @SerializedName("message")
    private String message;

    @SerializedName("type")
    private int type;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}


