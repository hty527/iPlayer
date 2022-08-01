package com.android.iplayer.listener;

import com.android.iplayer.base.BasePlayer;

/**
 * created by hty
 * 2022/7/4
 * Desc:全局的悬浮窗窗口播放器关闭\点击事件监听器
 */
public interface OnWindowActionListener {

    /**
     * 持续移动中
     * @param x
     * @param y
     */
    void onMovie(float x,float y);

    /**
     * 点击悬浮窗回调
     * @param basePlayer 播放器实例
     * @param coustomParams 自定义参数
     */
    void onClick(BasePlayer basePlayer,Object coustomParams);


    /**
     * 关闭事件
     */
    void onClose();
}