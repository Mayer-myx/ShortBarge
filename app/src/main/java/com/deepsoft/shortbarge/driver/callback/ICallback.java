package com.deepsoft.shortbarge.driver.callback;

public interface ICallback<T> {

    void onSuccess(T t);

    void onFail(String msg);

}
