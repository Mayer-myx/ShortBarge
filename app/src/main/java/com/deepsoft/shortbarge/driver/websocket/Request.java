package com.deepsoft.shortbarge.driver.websocket;

import com.google.gson.annotations.SerializedName;

public class Request<T> {
    @SerializedName("type")
    private int type;

    @SerializedName("message")
    private String message;

    private transient int reqCount;

    public Request(String message, int type,  int reqCount) {
        this.type = type;
        this.message = message;
        this.reqCount = reqCount;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getReqCount() {
        return reqCount;
    }

    public void setReqCount(int reqCount) {
        this.reqCount = reqCount;
    }

    public static class Builder<T> {
        private int type;
        private String message;
        private int reqCount;

        public Builder type(int type) {
            this.type = type;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder reqCount(int reqCount) {
            this.reqCount = reqCount;
            return this;
        }

        public Request build() {
            return new Request<T>(message, type, reqCount);
        }

    }
}
