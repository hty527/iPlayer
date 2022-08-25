package com.android.iplayer.widget;

import android.content.Context;
import android.util.AttributeSet;
import com.android.iplayer.base.BasePlayer;
import com.android.iplayer.controller.VideoController;

/**
 * created by hty
 * 2022/6/22
 * desc:这是一个播放器的实现类，内部封装了如何使用SDK提供的交互组件的调用示例
 * 1、需要功能自定义，请复写父类的方法修改
 * 2、此播放器封装了使用SDK自带的控制器+各UI交互组件，请调用：请调用：{@link #initController()}
 */
public class VideoPlayer extends BasePlayer {

    public VideoPlayer(Context context) {
        super(context);
    }

    public VideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoPlayer(Context context,  AttributeSet attrs, int defStyleAttr) {
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
     * @param showBackBtn 是否显示返回按钮
     */
    public VideoController initController(boolean showBackBtn){
        return initController(showBackBtn,true);
    }


    /**
     * 绑定默认的控制器到播放器
     * @param showBackBtn 是否显示返回按钮
     * @param addWindowWidget 是否添加悬浮窗口交互UI组件
     */
    public VideoController initController(boolean showBackBtn,boolean addWindowWidget){
        //绑定控制器
        VideoController controller = new VideoController(getContext());
        setController(controller);
        //添加SDK内部所有UI交互组件
        controller.initControlComponents(showBackBtn,addWindowWidget);
        return controller;
    }
}