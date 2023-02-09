package com.deepsoft.shortbarge.driver.bean.message;

public class MessageGson {

    private String chatMessageId;
    private Integer msgType;
    private Integer from;
    private Integer to;
    private String msg;
    private String fromName;
    private String fromNameEng;

    public String getChatMessageId() {
        return chatMessageId;
    }

    public void setChatMessageId(String chatMessageId) {
        this.chatMessageId = chatMessageId;
    }

    public Integer getMsgType() {
        return msgType;
    }

    public void setMsgType(Integer msgType) {
        this.msgType = msgType;
    }

    public Integer getFrom() {
        return from;
    }

    public void setFrom(Integer from) {
        this.from = from;
    }

    public Integer getTo() {
        return to;
    }

    public void setTo(Integer to) {
        this.to = to;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getFromNameEng() {
        return fromNameEng;
    }

    public void setFromNameEng(String fromNameEng) {
        this.fromNameEng = fromNameEng;
    }
}
