package com.android.iplayer.model;

/**
 * created by hty
 * 2022/6/28
 * Desc:播放器内部事件
 */
public enum PlayerState {
    STATE_RESET,//初始状态\播放器还原重置
    STATE_PREPARE,//准备中
    STATE_BUFFER,//缓冲中
    STATE_START,//开始首帧播放
    STATE_ON_PAUSE,//生命周期暂停
    STATE_PAUSE,//人为暂停
    STATE_PLAY,//缓冲结束后恢复播放
    STATE_ON_PLAY,//生命周期恢复播放
    STATE_STOP,//停止播放
    STATE_COMPLETION,//播放已完成
    STATE_MOBILE,//移动网络环境下
    STATE_ERROR,//错误
    STATE_DESTROY//播放器回收
}