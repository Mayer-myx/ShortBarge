package com.deepsoft.shortbarge.driver.widget;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;


final class LocalLog {

    private Date mLogTime;
    private String mLogTag;
    private String mLogMessage;

    public LocalLog(String logTag, String logMessage) {
        mLogTag = logTag;
        mLogMessage = logMessage;
        mLogTime = new Date();
    }

    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @Override
    public String toString() {
        String date = format1.format(mLogTime);
        return "[" + date + "] [" + mLogTag + "] " + " " + mLogMessage;
    }
}
