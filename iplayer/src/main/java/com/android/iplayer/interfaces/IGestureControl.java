package com.android.iplayer.interfaces;

/**
 * created by hty
 * 2022/8/5
 * Desc:如果你的自定义UI交互组件还需要处理手势交互，则需要实现此接口
 * 1、你的控制器需要继承GestureController
 * 2、你的自定义UI交互组件除了需要继承BaseControlWidget外还需要实现IGestureControl接口，你的ViewGroup才会收到此接口的回调
 */
public interface IGestureControl {

    /**
     * 开始滑动
     */
    void onStartSlide();

    /**
     * 结束滑动
     */
    void onStopSlide();

    /**
     * 滑动调整进度
     * @param slidePosition 滑动进度
     * @param currentPosition 当前播放进度
     * @param duration 视频总长度
     */
    void onPositionChange(int slidePosition, int currentPosition, int duration);

    /**
     * 滑动调整亮度
     * @param percent 亮度百分比
     */
    void onBrightnessChange(int percent);

    /**
     * 滑动调整音量
     * @param percent 音量百分比
     */
    void onVolumeChange(int percent);
}