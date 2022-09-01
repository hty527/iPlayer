package com.android.iplayer.listener;

import com.android.iplayer.interfaces.IMediaPlayer;

/**
 * created by hty
 * 2022/9/1
 * Desc:播放器内部各种事件回调
 */
public interface OnMediaEventListener {

    /**
     * 播放器异步准备好了
     * @param mp 播放器
     */
    void onPrepared(IMediaPlayer mp);

    /**
     * 播放器缓冲进度
     * @param mp 播放器
     * @param percent 缓冲进度，单位：百分比
     */
    void onBufferUpdate(IMediaPlayer mp, int percent);

    /**
     * seek跳转播放成功
     * @param mp 播放器
     */
    void onSeekComplete(IMediaPlayer mp);

    /**
     * 视频的宽高发生变化
     * @param mp 播放器
     * @param width 视频宽，单位：分辨率
     * @param height 视频高，单位：分辨率
     * @param sar_num 视频比例X
     * @param sar_den 视频比例Y
     */
    void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den);

    /**
     * 消息监听器,会将关于播放器的消息告知开发者,例如:视频渲染、音频渲染等
     * @param mp 播放器
     * @param what code码
     * @param extra 角度等其它参数
     * @return
     */
    boolean onInfo(IMediaPlayer mp, int what, int extra);

    /**
     * 播放完成，仅当setLoop为false回调
     * @param mp 播放器
     */
    void onCompletion(IMediaPlayer mp);

    /**
     * 错误监听器,播放器遇到错误时会将相应的错误码通过此回调接口告知开发者
     * @param mp 播放器
     * @param what 错误码
     * @param extra 错误信息
     * @return
     */
    boolean onError(IMediaPlayer mp, int what, int extra);
}