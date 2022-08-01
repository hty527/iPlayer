package com.android.iplayer.media;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import com.android.iplayer.base.BasePlayer;
import com.android.iplayer.controller.VideoController;

/**
 * created by hty
 * 2022/6/22
 * 注意:
 * 1、如需播放器支持全屏、小窗口、全局悬浮窗口播放功能，则VideoPlayerView需要一个ViewGroup容器来包裹。
 * 2、如播放视频场景是打开一个Activity直接全屏\直接小窗口模式播放(按返回键直接关闭Activity),则只能new 一个播放器对象来使用。
 * Desc:支持横竖屏切换\弹幕\试看\4G网络提示\失败交互的默认播放器
 * 默认的播放器是不包含视图控制器的,如需使用默认控制器,请调用initController方法
 */
public class VideoPlayer extends BasePlayer {

    public VideoPlayer(@NonNull Context context) {
        super(context);
    }

    public VideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initViews() {}

    /**
     * 绑定默认的控制器到播放器
     */
    public VideoController initController(){
        return initController(false);
    }

    /**
     * 绑定默认的控制器到播放器
     * @param showBackBtn 是否显示返回按钮 true:是 false:否
     */
    public VideoController initController(boolean showBackBtn){
        //绑定控制器
        VideoController controller = new VideoController(getContext());
        controller.showBackBtn(showBackBtn);
        setController(controller);
        return controller;
    }
}