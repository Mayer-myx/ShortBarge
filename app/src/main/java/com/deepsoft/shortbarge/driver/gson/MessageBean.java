package com.deepsoft.shortbarge.driver.gson;

public class MessageBean {
    // websocket消息
    // type：true我，false车间

    private String msg;
    private String url;
    private boolean type;

    public MessageBean(String msg, boolean type) {
        this.msg = msg;
        this.type = type;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean getType() {
        return type;
    }

    public void setType(boolean type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isType() {
        return type;
    }
}
