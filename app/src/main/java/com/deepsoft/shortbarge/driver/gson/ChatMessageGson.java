package com.deepsoft.shortbarge.driver.gson;

import java.util.List;

public class ChatMessageGson {

    //获取聊天消息
    private List<Content> list;
    private int total;

    public List<Content> getList() {
        return list;
    }

    public void setList(List<Content> list) {
        this.list = list;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public static class Content {
        private int chatMessageId;
        private int msgType;
        private int from;
        private int to;
        private String msg;

        public int getChatMessageId() {
            return chatMessageId;
        }

        public void setChatMessageId(int chatMessageId) {
            this.chatMessageId = chatMessageId;
        }

        public int getMsgType() {
            return msgType;
        }

        public void setMsgType(int msgType) {
            this.msgType = msgType;
        }

        public int getFrom() {
            return from;
        }

        public void setFrom(int from) {
            this.from = from;
        }

        public int getTo() {
            return to;
        }

        public void setTo(int to) {
            this.to = to;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }
}
