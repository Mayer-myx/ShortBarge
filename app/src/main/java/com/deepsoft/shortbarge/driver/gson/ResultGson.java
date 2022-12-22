package com.deepsoft.shortbarge.driver.gson;

public class ResultGson {

    //通用结果gson 在获取司机姓名api中data=司机name

    /**
     * 1.不管是jsonArray还是jsonObject,统一用Object data接收
     * 2.使用Gson对CanLoanBook字段统一换成List，反射赋值CanLoanBook
     * 3.最后对CanLoanBook强转成对应类型
     */
    private String code;
    private String msg;
    private Object data;
    private Boolean success;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
