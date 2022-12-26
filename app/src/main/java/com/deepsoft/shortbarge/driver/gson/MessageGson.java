package com.deepsoft.shortbarge.driver.gson;

public class MessageGson {
    // websocket发消息
    // type：0-WebSocket连接成功；2-聊天消息；3-通知服务端收到聊天消息
    // 1.聊天
    /**
     *  from：司机id
     *  msgType：1-文字消息；2-语音消息
     *  msg：消息内容（语音消息时，先把音频文件上传，将上传后返回的url作为消息内容）
     */

    // 2.经纬度
    // {"message":"{\"driverId\":1,\"truckId\":1,\"lng\":10.12,\"lat\":12.11}","type":1}

    private String msg;
    private int type;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
