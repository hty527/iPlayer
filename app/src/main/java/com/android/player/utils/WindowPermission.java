package com.android.player.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import com.android.player.ui.activity.WindowPermissionActivity;

/**
 * TinyHung@outlook.com
 * 2020/2/17
 * 悬浮窗权限获取辅助管理者
 */

public class WindowPermission {

    private static final String TAG = "WindowPermission";
    private volatile static WindowPermission mInstance;
    private OnRuntimePermissionListener mListener;

    public static synchronized WindowPermission getInstance() {
        synchronized (WindowPermission.class) {
            if (null == mInstance) {
                mInstance = new WindowPermission();
            }
        }
        return mInstance;
    }

    public interface OnRuntimePermissionListener {
        /**
         * 所有权限获取状态
         * @param success true：已授予 fakse：未授予
         */
        void onRequstPermissionResult(boolean success);
    }

    /**
     * 检查指定的权限是否已经都获取了
     * @param context 上下文
     * @param permission 待检查的权限
     * @return
     */
    public boolean isAllRequestedPermission(Context context, String permission) {
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(context, permission)) {
            return false;
        }
        return true;
    }

    /**
     * 开始权限请求
     * @param listener 状态监听器
     */
    public void startRequstPermission(Context context, OnRuntimePermissionListener listener) {
        this.mListener =listener;
        Intent intent=new Intent(context, WindowPermissionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 所有权限获取状态
     * @param success true：已授予 fakse：未授予
     */
    public void onRequstPermissionResult(boolean success) {
        if (null != mListener) {
            mListener.onRequstPermissionResult(success);
        }
    }

    /**
     * 返回监听实例
     * @return 监听器
     */
    public OnRuntimePermissionListener getListenerInstance(){
        return mListener;
    }

    /**
     * 释放重置
     */
    public void onReset() {
        mListener =null;
    }
}