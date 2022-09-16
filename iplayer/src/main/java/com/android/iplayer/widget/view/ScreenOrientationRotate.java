package com.android.iplayer.widget.view;

import android.content.Context;
import android.view.OrientationEventListener;

/**
 * created by hty
 * 2022/9/15
 * Desc:监听屏幕四个角度变化
 */
public class ScreenOrientationRotate extends OrientationEventListener {

    private long mLastTime;
    private OnDisplayOrientationListener mOnDisplayOrientationListener;

    public ScreenOrientationRotate(Context context) {
        super(context);
    }

    @Override
    public void onOrientationChanged(int orientation) {
        long currentTimeMillis = System.currentTimeMillis();
        //500毫秒检测一次
        if(currentTimeMillis-mLastTime<500){
            return;
        }
        if(null!=mOnDisplayOrientationListener) mOnDisplayOrientationListener.onOrientationChanged(orientation);
        mLastTime=currentTimeMillis;
    }

    public void setOnDisplayOrientationListener(OnDisplayOrientationListener onDisplayOrientationListener) {
        mOnDisplayOrientationListener = onDisplayOrientationListener;
    }

    public interface OnDisplayOrientationListener {
        void onOrientationChanged(int angle);
    }

    public void onReset(){
        mOnDisplayOrientationListener =null;
    }
}