package com.deepsoft.shortbarge.driver.callback;

import com.deepsoft.shortbarge.driver.constant.Action;
import com.deepsoft.shortbarge.driver.websocket.Request;

public interface IWsCallback<T> {
    void onSuccess(T t);
    void onError(String msg, Request request, Action action);
    void onTimeout(Request request, Action action);
}
