package com.deepsoft.shortbarge.driver.widget;

import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class LogHandler {

    private static final Lock sLock = new ReentrantLock();
    private static ExecutorService mService = Executors.newFixedThreadPool(2);
    private static ConcurrentLinkedQueue<String> mQueue = new ConcurrentLinkedQueue<>();

    private static File mLogFile;

    private LogHandler() {}


    public static void initLogFile(File file) {
        //删除目录
        if (file.exists() && file.isDirectory()) {
            try {
                FileUtils.deleteDirectory(file);
            } catch (IOException e) {
            }
        }
        //创建文件
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
                Log.i("LOGHANDLER","创建日志文件成功");
                LogHandler.writeFile("LOGHANDLER","创建日志文件成功");
            } catch (IOException e) {
                LogHandler.writeFile("LOGHANDLER","创建日志文件失败" + e.getMessage());
            }
        }

        if (file.exists() && file.isFile()) mLogFile = file;
        else throw new RuntimeException("is not file :" + file);
    }


    public static void writeFile(String tag, String string) {
        mQueue.add(new LocalLog(tag, string).toString() + "\r\n");
        mService.execute(mRunnable);
    }


    private static Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            sLock.lock();
            try {
                StringBuilder msg = new StringBuilder();
                while (!mQueue.isEmpty()) {
                    msg.append(mQueue.poll());
                }
                if (!TextUtils.isEmpty(msg.toString())) {
                    FileUtils.writeStringToFile(mLogFile, msg.toString(), true);
                    msg.delete(0, msg.length());
                }
            } catch (IOException e) {
                e.printStackTrace();
                LogHandler.writeFile("LOGHANDLER","创建日志文件失败" + e.getMessage());
            } finally {
                sLock.unlock();
            }
        }
    };

}

