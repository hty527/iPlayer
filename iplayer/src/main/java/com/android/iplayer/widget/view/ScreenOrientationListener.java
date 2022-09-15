package com.android.iplayer.widget.view;

import android.content.Context;
import android.view.OrientationEventListener;

/**
 * created by hty
 * 2022/9/15
 * Desc:监听屏幕四个角度变化
 */
public class ScreenOrientationListener extends OrientationEventListener {

//    private static final String TAG = "ScreenOrientationListener";
    private int mCurrentOrientation;//当前方向
    private OnDisplayOrientationChangedListener mOnDisplayOrientationChangedListener;
    private static final int SENSOR_ANGLE = 10;

    public ScreenOrientationListener(Context context) {
        super(context);
    }

    @Override
    public void onOrientationChanged(int orientation) {
        if(orientation==OrientationEventListener.ORIENTATION_UNKNOWN){
//            ILogger.d(TAG,"onOrientationChanged-->设备平放，无法检测");
            return;
        }
        int displayAngle = getDisplayAngle(orientation);
//        ILogger.d(TAG,"onOrientationChanged-->orientation:"+orientation+",mCurrentOrientation:"+mCurrentOrientation+",displayAngle:"+displayAngle);
        if(isValidAngle(displayAngle)){//检查角度有效性后过滤重复事件
            if(displayAngle!=mCurrentOrientation){
                mCurrentOrientation=displayAngle;
                if(null!=mOnDisplayOrientationChangedListener) mOnDisplayOrientationChangedListener.onAngleChanged(displayAngle);
            }
        }
    }

    /**
     * 检查旋转角度是否有效
     * @param displayAngle
     * @return
     */
    private boolean isValidAngle(int displayAngle) {
        if(0==displayAngle||90==displayAngle||270==displayAngle||180==displayAngle){
            return true;
        }
        return false;
    }

    private int getDisplayAngle(int orientation) {
        int angle=0;
        //下面是手机旋转准确角度与四个方向角度（0 90 180 270）的转换
        if (orientation > 360 - SENSOR_ANGLE || orientation < SENSOR_ANGLE) {
            angle = 0;
        } else if (orientation > 90 - SENSOR_ANGLE && orientation < 90 + SENSOR_ANGLE) {
            angle = 90;
        } else if (orientation > 180 - SENSOR_ANGLE && orientation < 180 + SENSOR_ANGLE) {
            angle = 180;
        } else if (orientation > 270 - SENSOR_ANGLE && orientation < 270 + SENSOR_ANGLE) {
            angle = 270;
        }
        return angle;
    }

    public void setOnDisplayOrientationChangedListener(OnDisplayOrientationChangedListener onDisplayOrientationChangedListener) {
        mOnDisplayOrientationChangedListener = onDisplayOrientationChangedListener;
    }

    public interface OnDisplayOrientationChangedListener{
        void onAngleChanged(int angle);
    }

    public void onReset(){
        mOnDisplayOrientationChangedListener=null;
    }
}