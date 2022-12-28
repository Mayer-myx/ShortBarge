package com.deepsoft.shortbarge.driver.constant;

public class Action {
    private String message;
    private int type;
    private Class respClazz;


    public Action(String message, int type, Class respClazz) {
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
