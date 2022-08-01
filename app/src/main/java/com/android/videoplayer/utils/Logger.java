package com.android.videoplayer.utils;

import android.util.Log;
import com.android.videoplayer.BuildConfig;

/**
 * created by hty
 * 2022/7/1
 * Desc:
 */
public class Logger {

    public static void pd(String TAG, String message){
        if(BuildConfig.DEBUG){
            Log.println(Log.DEBUG,TAG,message);
        }
    }

    public static void pe(String TAG, String message){
        if(BuildConfig.DEBUG){
            Log.println(Log.ERROR,TAG,message);
        }
    }

    public static void pw(String TAG, String message){
        if(BuildConfig.DEBUG){
            Log.println(Log.WARN,TAG,message);
        }
    }

    public static void pi(String TAG, String message){
        if(BuildConfig.DEBUG){
            Log.println(Log.INFO,TAG,message);
        }
    }
    public static void d(String TAG, String message) {
        if(BuildConfig.DEBUG){
            Log.d(TAG,message);
        }
    }

    public static void e(String TAG, String message) {
        if(BuildConfig.DEBUG){
            Log.e(TAG,message);
        }
    }

    public static void v(String TAG, String message) {
        if(BuildConfig.DEBUG){
            Log.e(TAG,message);
        }
    }

    public static void w(String TAG, String message) {
        if(BuildConfig.DEBUG){
            Log.w(TAG,message);
        }
    }

    public static void i(String TAG, String message) {
        if(BuildConfig.DEBUG){
            Log.i(TAG,message);
        }
    }
}