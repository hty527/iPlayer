package com.android.iplayer.widget;

import com.android.iplayer.base.BaseController;
import com.android.iplayer.interfaces.IVideoController;
import com.android.iplayer.widget.controls.ControWindowView;
import com.android.iplayer.widget.controls.ControlCompletionView;
import com.android.iplayer.widget.controls.ControlFunctionBarView;
import com.android.iplayer.widget.controls.ControlGestureView;
import com.android.iplayer.widget.controls.ControlLoadingView;
import com.android.iplayer.widget.controls.ControlStatusView;
import com.android.iplayer.widget.controls.ControlToolBarView;

/**
 * created by hty
 * 2022/9/14
 * Desc:UI交互组件工厂，为方便使用SDK提供的默认UI交互组件，仍然支持一键使用默认UI交互组件的使用
 */
public class WidgetFactory {

    /**
     * 绑定默认UI交互组件到控制器
     * @param controller 控制器
     */
    public static void bindDefaultControls(BaseController controller){
        bindDefaultControls(controller,false,false);
    }

    /**
     * 绑定默认UI交互组件到控制器
     * @param controller 控制器
     * @param showBack 竖屏状态下是否显示返回按钮，true：显示，false：不显示
     * @param addWindowWidget 是否添加window窗口UI交互组件
     */
    public static void bindDefaultControls(BaseController controller,boolean showBack,boolean addWindowWidget){
        if(null!=controller){
            //顶部标题栏
            ControlToolBarView toolBarView=new ControlToolBarView(controller.getContext());
            toolBarView.setTarget(IVideoController.TARGET_CONTROL_TOOL);
            toolBarView.showBack(showBack);
            //底部播放时间进度、progressBar、seekBae、静音、全屏等功能栏
            ControlFunctionBarView functionBarView=new ControlFunctionBarView(controller.getContext());
            functionBarView.setTarget(IVideoController.TARGET_CONTROL_FUNCTION);
            //手势控制屏幕亮度、系统音量、快进、快退UI交互
            ControlGestureView gestureView=new ControlGestureView(controller.getContext());
            gestureView.setTarget(IVideoController.TARGET_CONTROL_GESTURE);
            //播放完成、重试
            ControlCompletionView completionView=new ControlCompletionView(controller.getContext());
            completionView.setTarget(IVideoController.TARGET_CONTROL_COMPLETION);
            //移动网络播放提示、播放失败、试看完成
            ControlStatusView statusView=new ControlStatusView(controller.getContext());
            statusView.setTarget(IVideoController.TARGET_CONTROL_STATUS);
            //加载中、开始播放
            ControlLoadingView loadingView=new ControlLoadingView(controller.getContext());
            loadingView.setTarget(IVideoController.TARGET_CONTROL_LOADING);
            //悬浮窗窗口播放器的窗口样式
            if(addWindowWidget){
                ControWindowView windowView=new ControWindowView(controller.getContext());
                windowView.setTarget(IVideoController.TARGET_CONTROL_WINDOW);
                //将所有UI组件添加到控制器
                controller.addControllerWidget(toolBarView,functionBarView,gestureView,completionView,statusView,loadingView,windowView);
            }else{
                //将所有UI组件添加到控制器
                controller.addControllerWidget(toolBarView,functionBarView,gestureView,completionView,statusView,loadingView);
            }
        }
    }
}