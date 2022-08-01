package com.android.iplayer.interfaces;

import com.android.iplayer.base.BaseController;
import com.android.iplayer.model.PlayerState;

/**
 * created by hty
 * 2022/7/3
 * Desc:各子Controller需要实现的接口
 */
public interface IVideoController {

    //控制器内部状态事件
//    int STATE_RESET          = 0;//初始状态\播放器还原重置
//    int STATE_PREPARE        = 1;//准备中
//    int STATE_BUFFER         = 2;//缓冲中
//    int STATE_START          = 3;//开始首帧播放
//    int STATE_ON_PAUSE       = 4;//生命周期暂停
//    int STATE_PAUSE          = 5;//人为暂停
//    int STATE_PLAY           = 6;//缓冲结束后恢复播放
//    int STATE_ON_PLAY        = 7;//生命周期\暂停情况下恢复播放
//    int STATE_STOP           = 8;//停止播放
//    int STATE_MOBILE         = 9;//移动网络播放(如果设置允许4G播放则播放器内部不会回调此状态)
//    int STATE_COMPLETION     = 10;//播放已完成
//    int STATE_ERROR          = 11;//播放失败
//    int STATE_DESTROY        = 12;//播放器回收

    /**
     * @param state 控制器内部状态 状态码 参考:PlayerState
     * @param message 描述信息
     */
    void onState(PlayerState state, String message);

    /**
     * @param currentDurtion 播放进度 子线程回调：当前播放位置,单位：总进度的毫秒进度
     * @param totalDurtion 总时长,单位：毫秒
     * @param bufferPercent 已缓冲进度,单位：百分比
     */
    void progress(long currentDurtion, long totalDurtion, int bufferPercent);

    /**
     * @param bufferPercent 缓冲进度 主线程回调,单位:百分比
     */
    void onBuffer(int bufferPercent);

    /**
     * @param orientation 更新控制器方向状态 0:竖屏 1:横屏
     */
    void setScreenOrientation(int orientation);

    /**
     * @param isWindowProperty 控制器是否处于窗口模式中 true:当前窗口属性显示 false:非窗口模式。当处于创库模式时，所有控制器都处于不可见状态,所有控制器手势都将被window播放器截获
     * @param isGlobalWindow true:全局悬浮窗窗口|画中画模式 false:Activity局部悬浮窗窗口模式
     */
    void setWindowProperty(boolean isWindowProperty,boolean isGlobalWindow);

    /**
     * @param controller 继承BaseController的自定义控制器
     * 提供给自定义视频控制器来添加自己的其它功能控制器(如手势控制\弹幕控制器\其它)
     * 调用这个方法添加的控制器位于视频控制器的上层,添加多个就是逐层往上层添加
     */
    void addController(BaseController controller);

    /**
     * @param index 添加的层级位置,推荐将自定义控制器添加到位于视频控制器下方
     * @param controller 继承BaseController的自定义控制器
     * 推荐调用此方法将自定义控制器添加到位于视频控制器下方
     * 提供给自定义视频控制器来添加自己的其它功能控制器(如手势控制\弹幕控制器\其它)
     * 调用这个方法添加的控制器位于视频控制器的上层,添加多个就是逐层往上层添加
     */
    void addController(int index,BaseController controller);

    /**
     * @param controller 移除这个实例的控制器
     */
    void removeController(BaseController controller);

    /**
     * 移除所有控制器
     */
    void removeAllController();

    /**
     * 生命周期可见,和播放状态无关
     */
    void onResume();

    /**
     * 生命周期不可见,和播放状态无关
     */
    void onPause();

    /**
     * 释放
     */
    void onReset();

    /**
     * 销毁
     */
    void onDestroy();
}