package com.android.iplayer.utils;

import android.util.Log;
import com.android.iplayer.BuildConfig;

/**
 * created by hty
 * 2022/6/28
 * Desc:
 */
public class ILogger {

    public static boolean DEBUG = true;

    public static void setDebug(boolean debug){
        DEBUG=debug;
    }

    public static String getVersion(){
        return BuildConfig.VERSION_NAME;
    }

    public static void pd(String TAG, String message){
        if(DEBUG){
            Log.println(Log.DEBUG,TAG,message);
        }
    }

    public static void pe(String TAG, String message){
        if(DEBUG){
            Log.println(Log.ERROR,TAG,message);
        }
    }

    public static void pw(String TAG, String message){
        if(DEBUG){
            Log.println(Log.WARN,TAG,message);
        }
    }

    public static void pi(String TAG, String message){
        if(DEBUG){
            Log.println(Log.INFO,TAG,message);
        }
    }
    public static void d(String TAG, String message) {
        if(DEBUG){
            Log.d(TAG,message);
        }
    }

    public static void e(String TAG, String message) {
        if(DEBUG){
            Log.e(TAG,message);
        }
    }

    public static void v(String TAG, String message) {
        if(DEBUG){
            Log.e(TAG,message);
        }
    }

    public static void w(String TAG, String message) {
        if(DEBUG){
            Log.w(TAG,message);
        }
    }

    public static void i(String TAG, String message) {
        if(DEBUG){
            Log.i(TAG,message);
        }
    }
}