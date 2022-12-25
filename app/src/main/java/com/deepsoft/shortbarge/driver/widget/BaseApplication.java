package com.deepsoft.shortbarge.driver.widget;

import android.app.Application;
import android.content.Context;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class BaseApplication extends Application {

    private static BaseApplication application;
    private static Context context;

    /**
     * 返回application
     **/
    public static BaseApplication getApplication(){
        return application;
    }

    /**
     * 返回context
     **/
    public static Context getContext() {
        return context;
    }

    /**
     * 对于一个应用来说 android入口并不是Activity中的OnCreate()而是Application里面的Oncreate()
     * 也就相当于是java中的Main方法，只不过这个方法被封装了
     **/
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        //在Application创建时,读取Application
        application = this;
        context = getApplicationContext();

        //全局处理证书问题
        handleSSLHandshake();
    }

    /**
     * 全局处理证书问题
     * 忽略https的证书校验
     * 避免Glide加载https图片报错：
     * javax.net.ssl.SSLHandshakeException: java.security.cert.CertPathValidatorException: Trust anchor for certification path not found.
     */
    public static void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
            };

            SSLContext sc = SSLContext.getInstance("TLS");
            // trustAllCerts信任所有的证书
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

        } catch (Exception e) {
        }
    }
}
