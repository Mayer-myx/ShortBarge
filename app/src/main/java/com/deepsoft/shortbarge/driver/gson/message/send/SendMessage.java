package com.deepsoft.shortbarge.driver.gson.message.send;

public class SendMessage {
    //在MessageResponse的message里的结构，type=2，msg=具体消息内容，msgType=1文字消息2语音消息
    private String msg;
    private int msgType;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }
}
