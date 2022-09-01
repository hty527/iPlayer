package com.android.iplayer.interfaces;

import android.view.View;
import com.android.iplayer.base.AbstractMediaPlayer;

/**
 * created by hty
 * 2022/9/1
 * Desc:如需实现自定义画面渲染器，请实现此接口
 */
public interface IVideoRenderView {

    /**
     * 绑定解码器
     * @param mediaPlayer 绑定解码器 解码器
     */
    void attachMediaPlayer(AbstractMediaPlayer mediaPlayer);

    /**
     * 返回TextureView或SurfaceView
     * @return
     */
    View getView();

    /**
     * 视频的宽高更新
     * @param width 视频的宽更新，单位：分辨率像素
     * @param height 视频的高更新，单位：分辨率像素
     */
    void setVideoSize(int width,int height);

    /**
     * 画面缩放或裁剪模式，请参阅IMediaPlayer类中定义的常量值
     * @param zoomMode 画面缩放或裁剪模式，请参阅IMediaPlayer类中定义的常量值
     */
    void setZoomMode(int zoomMode);

    /**
     * 设置视频画面旋转角度
     * @param degree 旋转角度
     */
    void setDegree(int degree);

    /**
     * 设置View旋转角度
     * @param rotation 旋转角度
     */
    void setViewRotation(int rotation);

    /**
     * 设置画面比例
     * @param sarNum 设置画面比例,比例x
     * @param sarDen 设置画面比例,比例y
     */
    void setSarSize(int sarNum,int sarDen);

    /**
     * 设置画面镜像旋转
     * @param mirror 设置画面镜像旋转 true:画面翻转 false:正常
     * @return true:画面翻转 false:正常
     */
    boolean setMirror(boolean mirror);

    /**
     * 开启、关闭画面镜像旋转
     * @return 是否镜像,true:镜像音 false:正常
     */
    boolean toggleMirror();

    /**
     * 请求重绘布局
     */
    void requestDrawLayout();

    /**
     * 释放画面渲染器
     */
    void release();
}