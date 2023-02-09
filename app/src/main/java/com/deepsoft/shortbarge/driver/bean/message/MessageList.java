package com.deepsoft.shortbarge.driver.bean.message;

import java.util.ArrayList;
import java.util.List;

public class MessageList {

    private Object list;
    private Integer total;

    public Object getList() {
        return list;
    }

    public void setList(List<MessageGson> list) {
        this.list = list;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}
