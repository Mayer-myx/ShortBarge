package com.deepsoft.shortbarge.driver.websocket;

import android.util.Log;

import com.deepsoft.shortbarge.driver.bean.message.MessageResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

public class NotifyListenerManager {
    private final String TAG = this.getClass().getSimpleName();
    private volatile static NotifyListenerManager manager;
    private Map<String, INotifyListener> map = new HashMap<>();

    private NotifyListenerManager() {
        regist();
    }

    public static NotifyListenerManager getInstance() {
        if (manager == null) {
            synchronized (NotifyListenerManager.class) {
                if (manager == null) {
                    manager = new NotifyListenerManager();
                }
            }
        }
        return manager;
    }

    private void regist() {
        map.put("notifyAnnounceMsg", new AnnounceMsgListener());
    }

    public void fire(MessageResponse messageResponse) {
        String message = messageResponse.getMessage();
        int type = messageResponse.getType();
        INotifyListener listener = map.get(message);
        if (listener == null) {
            Log.d(TAG, "no found notify listener");
            return;
        }

        NotifyClass notifyClass = listener.getClass().getAnnotation(NotifyClass.class);
        Class<?> clazz = notifyClass.value();
        Object result = null;
        try {
            result = new Gson().fromJson(message, clazz);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        Log.d(TAG, result.toString());
        listener.fire(result);
    }


    //抽象接口
    public interface INotifyListener<T> {
        void fire(T t);
    }


    //标记注解
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface NotifyClass {
        Class<?> value();
    }


    //具体逻辑对应的处理子类
    @NotifyClass(AnnounceMsgNotify.class)
    public class AnnounceMsgListener implements INotifyListener<AnnounceMsgNotify> {

        @Override
        public void fire(AnnounceMsgNotify announceMsgNotify) {
            //这里处理具体的逻辑
        }
    }


    //对应数据bean
    public class AnnounceMsgNotify {
        @SerializedName("msg_version")
        private String msgVersion;

        public String getMsgVersion() {
            return msgVersion;
        }

        public void setMsgVersion(String msgVersion) {
            this.msgVersion = msgVersion;
        }
    }

}

