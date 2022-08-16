package com.android.iplayer.base;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.android.iplayer.R;
import com.android.iplayer.interfaces.IMediaPlayer;
import com.android.iplayer.interfaces.IVideoController;
import com.android.iplayer.interfaces.IVideoPlayerControl;
import com.android.iplayer.model.PlayerState;
import com.android.iplayer.utils.PlayerUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hty
 * 2022/6/28
 * desc: 视图UI交互\手势滑动\弹幕等所有视图交互控制器基类
 * IVideoController:所有子Controller都必须实现Controller的所有方法,完全由子Controller自行处理
 */
public abstract class BaseController extends FrameLayout implements IVideoController {

    protected static final String TAG = BaseController.class.getSimpleName();
    protected IVideoPlayerControl mVideoPlayerControl;//播放器代理人
    protected int mScreenOrientation= IMediaPlayer.ORIENTATION_PORTRAIT;//当前控制器方向
    protected List<BaseController> mControllers;//用户自定义的各控制器
    protected boolean itemPlayerMode,isWindowProperty,isGlobalWindow;//交互控制器是否处于列表播放模式\控制器是否处于窗口模式\是否处于全局悬浮窗或画中画模式
    protected class ExHandel extends Handler{
        public ExHandel(Looper looper){
            super(looper);
        }
    }

    public BaseController(Context context) {
        this(context,null);
    }

    public BaseController(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BaseController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        int layoutId = getLayoutId();
        View rootView = View.inflate(context, R.layout.player_base_controller, this);
        if(0!=layoutId){
            View inflate = View.inflate(context, getLayoutId(), null);
            ((FrameLayout) rootView.findViewById(R.id.player_base_controller)).addView(inflate,new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT, Gravity.CENTER));
        }
        initViews();
    }

    public abstract int getLayoutId();

    public abstract void initViews();

    protected int getOrientation() {
        return mScreenOrientation;
    }

    /**
     * 返回窗口模式
     * @return true:当前正处于窗口模式 false:当前不处于窗口模式
     */
    public boolean isWindowProperty() {
        return isWindowProperty;
    }

    /**
     * 返回窗口类型
     * @return true:全局悬浮窗|画中画 false:Activity window窗口模式
     */
    public boolean isGlobalWindow() {
        return isGlobalWindow;
    }

    /**
     * 是否处于列表播放模式，在开始播放和开启\退出全屏时都需要设置
     * @param itemPlayerMode 是否处于列表播放模式(需要在开始播放之前设置),列表播放模式下首次渲染不会显示控制器,否则首次渲染会显示控制器 true:处于列表播放模式 false:不处于列表播放模式
     */
    public void setListPlayerMode(boolean itemPlayerMode) {
        this.itemPlayerMode =itemPlayerMode;
    }

    /**
     * 是否处于列表模式下播放
     * @return true:是 false:否
     */
    public boolean isListPlayerMode() {
        return itemPlayerMode;
    }

    //返回是否是竖屏状态
    public boolean isOrientationPortrait() {
        return mScreenOrientation==IMediaPlayer.ORIENTATION_PORTRAIT;
    }

    protected String getOrientationStr(){
        return ",Orientation:"+getOrientation();
    }

    /**
     * 返回播放器内部是否正在播放
     * @return 是否正处于播放中(准备\开始播放\播放中\缓冲\) true:播放中 false:不处于播放中状态
     */
    protected boolean isPlayering() {
        if(null!= mVideoPlayerControl){
            return mVideoPlayerControl.isPlaying();
        }
        return false;
    }

    /**
     * 播放器是否正处于工作状态(准备\开始播放\缓冲\手动暂停\生命周期暂停) true:工作中 false:空闲状态
     * @return 是否正处于播放中(准备\开始播放\播放中\缓冲\) true:播放中 false:不处于播放中状态
     */
    protected boolean isWorking() {
        if(null!= mVideoPlayerControl){
            return mVideoPlayerControl.isWorking();
        }
        return false;
    }

    protected Activity getActivity() {
        if(null!= mVideoPlayerControl &&null!= mVideoPlayerControl.getTempContext()){
            return  PlayerUtils.getInstance().getActivity(mVideoPlayerControl.getTempContext());
        }
        return PlayerUtils.getInstance().getActivity(getContext());
    }

    protected Context getParentContext() {
        if(null!= mVideoPlayerControl &&null!= mVideoPlayerControl.getTempContext()){
            return  mVideoPlayerControl.getTempContext();
        }
        return getContext();
    }

    /**
     * 提供给播放器解码器来绑定播放器代理人
     * @param playerControl
     */
    protected void attachedVideoPlayerControl(IVideoPlayerControl playerControl) {
        this.mVideoPlayerControl =playerControl;
    }

    /**
     * 设置视频标题内容
     * @param videoTitle
     */
    public void setVideoTitle(String videoTitle){}

    /**
     * 播放状态
     * @param state
     * @param message
     */
    public void onStatePlayer(PlayerState state, String message){
        onState(state,message);
        if(null!=mControllers&&mControllers.size()>0){
            for (BaseController controller : mControllers) {
                controller.onState(state,message);
            }
        }
    }
    /**
     * 播放进度 子线程回调
     * @param currentDurtion 当前播放进度 单位:视频时长毫秒
     * @param totalDurtion 视频总时长 单位:视频总时长毫秒
     * @param bufferPercent 当前缓冲进度 百分比
     */
    public void onProgressPlayer(long currentDurtion, long totalDurtion, int bufferPercent){
        onProgress(currentDurtion,totalDurtion,bufferPercent);
        if(null!=mControllers&&mControllers.size()>0){
            for (BaseController controller : mControllers) {
                controller.onProgress(currentDurtion,totalDurtion,bufferPercent);
            }
        }
    }

    //实时缓冲进度 百分比
    public void onBufferPlayer(int bufferPercent){
        onBuffer(bufferPercent);
        if(null!=mControllers&&mControllers.size()>0){
            for (BaseController controller : mControllers) {
                controller.onBuffer(bufferPercent);
            }
        }
    }

    //更改内部控制器方向
    public void setScreenOrientationPlayer(int orientation) {
        this.mScreenOrientation=orientation;
        setScreenOrientation(orientation);
        if(null!=mControllers&&mControllers.size()>0){
            for (BaseController controller : mControllers) {
                controller.setScreenOrientation(orientation);
            }
        }
    }

    //更改内部控制器属性
    public void setWindowPropertyPlayer(boolean isWindowProperty,boolean isGlobalWindow) {
        this.isWindowProperty =isWindowProperty;
        this.isGlobalWindow =isGlobalWindow;
        setWindowProperty(isWindowProperty,isGlobalWindow);
        if(null!=mControllers&&mControllers.size()>0){
            for (BaseController controller : mControllers) {
                controller.setWindowProperty(isWindowProperty,isGlobalWindow);
            }
        }
    }

    //控制器所有状态重置(由播放器内部回调,与播放器生命周期无关)
    public void onResetPlayer(){
        onReset();
        if(null!=mControllers&&mControllers.size()>0){
            for (BaseController controller : mControllers) {
                controller.onReset();
            }
        }
    }

    //控制器可见,与播放器生命周期无关
    public void onResumePlayer(){
        onResume();
        if(null!=mControllers&&mControllers.size()>0){
            for (BaseController controller : mControllers) {
                controller.onResume();
            }
        }
    }

    //控制器不可见,与播放器生命周期无关
    public void onPausePlayer(){
        onPause();
        if(null!=mControllers&&mControllers.size()>0){
            for (BaseController controller : mControllers) {
                controller.onPause();
            }
        }
    }

    //播放器被销毁(由播放器内部回调,与播放器生命周期无关)
    public void onDestroyPlayer(){
        onDestroy();
        if(null!=mControllers&&mControllers.size()>0){
            for (BaseController controller : mControllers) {
                controller.onDestroy();
            }
        }
    }

    //进入画中画模式
    public void enterPipWindowPlayer() {
        enterPipWindow();
        if(null!=mControllers&&mControllers.size()>0){
            for (BaseController controller : mControllers) {
                controller.enterPipWindow();
            }
        }
    }

    //退出画中画模式
    public void quitPipWindowPlayer() {
        quitPipWindow();
        if(null!=mControllers&&mControllers.size()>0){
            for (BaseController controller : mControllers) {
                controller.quitPipWindow();
            }
        }
    }

    /**
     * 进入画中画模式
     */
    protected void enterPipWindow(){}

    /**
     * 退出画中画模式
     */
    protected void quitPipWindow(){}

    /**
     * 提供给自定义视频控制器来添加自己的其它功能控制器(如手势控制\弹幕控制器\其它)
     * 调用这个方法添加的控制器位于视频控制器的上层,添加多个就是逐层往上层添加
     * @param controller 继承BaseController的自定义控制器
     */
    @Override
    public void addController(BaseController controller) {
        if(null!=controller){
            if(null==mControllers) mControllers=new ArrayList<>();
            if(!mControllers.contains(controller)){
                controller.attachedVideoPlayerControl(mVideoPlayerControl);//给所有自定义控制器绑定播放器代理人
                FrameLayout.LayoutParams layoutParams=new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                mControllers.add(controller);
                ((FrameLayout) findViewById(R.id.player_base_controller)).addView(controller,layoutParams);
//                ILogger.d(TAG,"addController:已添加控制器:"+controller);
            }else{
//                ILogger.d(TAG,"重复添加:addController-->");
            }
        }
    }

    /**
     * 没有手势交互的控制器 推荐调用此方法将自定义控制器添加到位于视频控制器下方
     * 提供给自定义视频控制器来添加自己的其它功能控制器(如手势控制\弹幕控制器\其它)
     * 调用这个方法添加的控制器位于视频控制器的上层,添加多个就是逐层往上层添加
     * @param index 添加的层级位置,推荐将自定义控制器添加到位于视频控制器下方
     * @param controller 继承BaseController的自定义控制器
     */
    @Override
    public void addController(int index,BaseController controller) {
        if(null!=controller){
            if(null==mControllers) mControllers=new ArrayList<>();
            if(!mControllers.contains(controller)){
                controller.attachedVideoPlayerControl(mVideoPlayerControl);//给所有自定义控制器绑定播放器代理人
                FrameLayout.LayoutParams layoutParams=new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                mControllers.add(controller);
                controller.setId(index);
                ((FrameLayout) findViewById(R.id.player_base_controller)).addView(controller,index,layoutParams);
//                ILogger.d(TAG,"addController:已添加控制器:index:"+index+",controller:"+controller);
            }else{
//                ILogger.d(TAG,"重复添加:addController-->,index:"+index);
            }
        }
    }

    /**
     * 移除某个控制器
     * @param controller 移除这个实例的控制器
     */
    @Override
    public void removeController(BaseController controller) {
        if(null!=controller&&null!=mControllers&&mControllers.size()>0){
            int index = mControllers.indexOf(controller);
//            ILogger.d(TAG,"removeController-->已移除控制器,index:"+index+",controller:"+controller);
            PlayerUtils.getInstance().removeViewFromParent(controller);
            mControllers.remove(index);
        }
    }

    /**
     * 移除所有控制器
     */
    @Override
    public void removeAllController() {
        if(null!=mControllers&&mControllers.size()>0){
            for (BaseController controller : mControllers) {
                controller.onReset();
                PlayerUtils.getInstance().removeViewFromParent(controller);
//                ILogger.d(TAG,"removeAllController-->已移除控制器controller:"+controller);
            }
            mControllers.clear();
        }
    }
}