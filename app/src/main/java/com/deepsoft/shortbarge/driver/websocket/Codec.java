package com.deepsoft.shortbarge.driver.websocket;

import com.deepsoft.shortbarge.driver.gson.message.receive.UserReceiveMessage;
import com.deepsoft.shortbarge.driver.gson.message.receive.UserSendMessage;
import com.deepsoft.shortbarge.driver.gson.message.send.LocationMessage;
import com.deepsoft.shortbarge.driver.gson.message.MessageResponse;
import com.deepsoft.shortbarge.driver.gson.message.send.NotifyMessage;
import com.deepsoft.shortbarge.driver.gson.message.send.ReceiveMessage;
import com.deepsoft.shortbarge.driver.gson.message.send.SendMessage;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Codec {

    public static MessageResponse decoder(String text) {
        MessageResponse messageResponse = new MessageResponse();
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(text);
        if (element.isJsonObject()) {
            JsonObject obj = (JsonObject) element;
            messageResponse.setMessage(decoderStr(obj, "message"));
            messageResponse.setType(decoderInt(obj, "type"));
            return messageResponse;
        }
        return messageResponse;
    }

    private static int decoderInt(JsonObject obj, String name) {
        int result = -1;
        JsonElement element = obj.get(name);
        if (null != element) {
            try {
                result = element.getAsInt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private static String decoderStr(JsonObject obj, String name) {
        String result = "";
        try {
            JsonElement element = obj.get(name);
            if (null != element && element.isJsonPrimitive()) {
                result = element.getAsString();
            } else if (null != element && element.isJsonObject()) {
                result = element.getAsJsonObject().toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    //web发送聊天消息过来
    public static UserSendMessage decoderUserSendMessage(String jsonStr) {
        UserSendMessage childResponse = new UserSendMessage();
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(jsonStr);
        if (element.isJsonObject()) {
            JsonObject jsonObject = (JsonObject) element;
            childResponse.setMsg(decoderStr(jsonObject, "msg"));
            childResponse.setTo(decoderInt(jsonObject, "to"));
            childResponse.setMsgType(decoderInt(jsonObject, "msgType"));
        }
        return childResponse;
    }

    //type=1 返回经纬度坐标
    public static ReceiveMessage decoderReceiveMessage(String jsonStr) {
        ReceiveMessage childResponse = new ReceiveMessage();
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(jsonStr);
        if (element.isJsonObject()) {
            JsonObject jsonObject = (JsonObject) element;
            childResponse.setDriverId(decoderInt(jsonObject, "driverId"));
        }
        return childResponse;
    }

    //type=3 通知服务端收到消息
    public static UserReceiveMessage decoderUserReceiveMessage(String jsonStr) {
        UserReceiveMessage childResponse = new UserReceiveMessage();
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(jsonStr);
        if (element.isJsonObject()) {
            JsonObject jsonObject = (JsonObject) element;
            childResponse.setChatMessageId(decoderInt(jsonObject, "chatMessageId"));
            childResponse.setFrom(decoderInt(jsonObject, "from"));
            childResponse.setMsgType(decoderInt(jsonObject, "msgType"));
            childResponse.setMsg(decoderStr(jsonObject, "msg"));
        }
        return childResponse;
    }
}


