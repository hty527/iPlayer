package com.danikula.videocache;

import android.util.Log;

/**
 * created by hty
 * 2022/12/28
 * Desc:
 */
public class CacheLog {

    private static final String TAG = "CacheLog";

    public static void log(String content){
        Log.d(TAG,content);
    }


    public static void warn(String content) {
        warn(content,null);
    }

    public static void warn(String content, Exception e) {
        try {
            Log.w(TAG,content+(null!=e?e.getMessage():""));
        }catch (Throwable e1){}
    }

    public static void debug(String content) {
        Log.d(TAG,content);
    }

    public static void error(String error) {
        error(error,null);
    }

    public static void error(String error, Throwable e) {
        try {
            Log.e(TAG,error+(null!=e?e.getMessage():""));
        }catch (Throwable e1){}
    }

    public static void info(String content) {
        Log.i(TAG,content);
    }
}
