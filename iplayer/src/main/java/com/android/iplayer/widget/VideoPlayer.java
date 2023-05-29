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
 * 2、此播放器提供使用默认控制器作为播放器控制器方法，请调用：{@link #initController()}
 * 3、如需使用默认UI交互，需集成implementation 'com.github.hty527.iPlayer:widget:lastversion'后，使用WidgetFactory.bindDefaultControls(controller);将UI交互组件绑定到控制器。
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
     * @return
     */
    public VideoController initController(){
        VideoController controller = new VideoController(getContext());
        setController(controller);
        return controller;
    }
}