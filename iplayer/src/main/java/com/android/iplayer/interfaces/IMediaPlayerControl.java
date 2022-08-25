package com.android.iplayer.interfaces;

import android.content.Context;
import com.android.iplayer.base.AbstractMediaPlayer;
import com.android.iplayer.base.BasePlayer;
import com.android.iplayer.model.PlayerState;

/**
 * created by hty
 * 2022/7/3
 * Desc:解码器持有的播放器代理人
 */
public interface IMediaPlayerControl {

    /**
     * 返回播放器的上下文
     * @return
     */
    Context getContext();

    /**
     * 返回一个继承自AbstractMediaPlayer的播放器解码器
     * @return
     */
    AbstractMediaPlayer getMediaPlayer();

    /**
     * 宿主返回一个装载视频播放器的容器
     * @return
     */
    BasePlayer getVideoPlayer();

    /**
     * 播放内部各种事件
     * @param state 播放器内部状态
     * @param message 状态说明
     */
    void onPlayerState(PlayerState state, final String message);

    /**
     * 缓冲进度 主线程回调
     * @param percent 百分比
     */
    void onBuffer(int percent);

    /**
     * 视频宽高
     * @param width 视频宽
     * @param height 视频高
     */
    void onVideoSizeChanged(int width, int height);

    /**
     * 播放进度 主线程回调
     * @param currentDurtion 当前播放位置,单位：总进度的毫秒进度
     * @param totalDurtion 总时长,单位：毫秒
     */
    void onProgress(long currentDurtion, long totalDurtion);
}