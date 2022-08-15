package com.android.videoplayer.video.listener;

/**
 * created by hty
 * 2022/7/11
 * Desc:菜单功能交互监听器
 */
public interface OnMenuActionListener {
    /**
     * @param speed 倍速 从0.5f-2.0f
     */
    void onSpeed(float speed);

    /**
     * @param zoomModel 画面缩放模式,参考IMediaPlayer定义的常量
     */
    void onZoom(int zoomModel);

    /**
     * @param scale 画面显示比例
     */
    void onScale(int scale);

    /**
     * @param mute 是否静音 true:无声 false:跟随系统音量
     */
    void onMute(boolean mute);

    /**
     * @param mirror 是否镜像翻转 true:镜像翻转 false:正常
     */
    void onMirror(boolean mirror);
}