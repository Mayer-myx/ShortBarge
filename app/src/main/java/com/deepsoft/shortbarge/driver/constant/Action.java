package com.deepsoft.shortbarge.driver.constant;

public enum Action {
    LOGIN("{\n" +
            "     \"message\": \"{\\\"msg\\\":\\\"hello111112222!!!\\\",\\\"msgType\\\":1}\",\n" +
            "     \"type\": 2\n" +
            " }", 2, null),
    LOCATION("{\"message\":\"{\\\"driverId\\\":1,\\\"truckId\\\":1,\\\"lng\\\":10.12,\\\"lat\\\":12.11}\",\"type\":1}", 1, null),
    NOTIFY("{\"message\":\"{\\\"chatMessageId\\\":8,\\\"from\\\":8,\\\"msg\\\":\\\"hello test 你好\\\",\\\"msgType\\\":1,\\\"to\\\":1}\",\"type\":3}", 3, null),
    HEARTBEAT("heartbeat", 1, null),
    SYNC("sync", 1, null);

    private String message;
    private int type;
    private Class respClazz;


    Action(String message, int type, Class respClazz) {
        this.message = message;
        this.type = type;
        this.respClazz = respClazz;
    }

    public String getMessage() {
        return message;
    }

    public int getType() {
        return type;
    }

    public Class getRespClazz() {
        return respClazz;
    }
}
