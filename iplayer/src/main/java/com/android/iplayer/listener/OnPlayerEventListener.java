package com.android.iplayer.listener;

import com.android.iplayer.base.AbstractMediaPlayer;
import com.android.iplayer.model.PlayerState;

/**
 * created by hty
 * 2022/6/28
 * Desc:简单的播放器内部事件回调,提供给播放器容器的宿主实现监听
 */
public abstract class OnPlayerEventListener {
    /**
     * 宿主必须实现,返回一个自定义多媒体解码器,如果返回为空,则适用内部默认的DefaultMediaPlayer解码器
     * @return 一个自定义的多媒体解码器
     */
    public abstract AbstractMediaPlayer createMediaPlayer();

    /**
     * 播放器内部各状态
     * @param state 状态请参考PlayerState中定义的状态
     * @param message 状态描述
     */
    public void onPlayerState(PlayerState state,String message){}

    /**
     * 视频宽高
     * @param width 视频宽
     * @param height 视频高
     */
    public void onVideoSizeChanged(int width, int height){}

    /**
     * 播放进度实时回调,回调到主线程
     * @param currentDurtion 当前播放进度,单位:毫秒时间戳
     * @param totalDurtion 总时长,单位:毫秒时间戳
     */
    public void onProgress(long currentDurtion, long totalDurtion) {}

    /**
     * @param isMute 当静音状态发生了变化回调，true:处于静音状态 false:处于非静音状态
     */
    public void onMute(boolean isMute) {}

    /**
     * @param isMirror 当播放器的内部画面渲染镜像状态发生了变化回调， true:处于镜像状态 false:处于非镜像状态
     */
    public void onMirror(boolean isMirror) {}

    /**
     * @param zoomModel 当播放器内部渲染缩放模式发生了变化回调，，当初始化和播放器缩放模式设置发生变化时回调，参考IMediaPlayer类
     */
    public void onZoomModel(int zoomModel) {}
}