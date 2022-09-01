package com.android.iplayer.base;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import com.android.iplayer.controller.ControlWrapper;
import com.android.iplayer.interfaces.IControllerView;
import com.android.iplayer.interfaces.IMediaPlayer;
import com.android.iplayer.interfaces.IVideoController;
import com.android.iplayer.interfaces.IVideoPlayerControl;
import com.android.iplayer.model.PlayerState;
import com.android.iplayer.utils.PlayerUtils;
import com.android.iplayer.widget.controls.ControWindowView;
import com.android.iplayer.widget.controls.ControlCompletionView;
import com.android.iplayer.widget.controls.ControlFunctionBarView;
import com.android.iplayer.widget.controls.ControlGestureView;
import com.android.iplayer.widget.controls.ControlLoadingView;
import com.android.iplayer.widget.controls.ControlStatusView;
import com.android.iplayer.widget.controls.ControlToolBarView;
import java.util.LinkedList;

/**
 * Created by hty
 * 2022/6/28
 * desc: 视频播放器UI控制器交互基类
 * 1、此控制器维护所有UI组件，负责传达和处理播放器以及UI组件的事件
 * 2、控制器的所有UI组件都支持自定义，调用{@link #addControllerWidget(IControllerView)}添加你的自定义UI组件
 * 3、播放器只能有一个控制器，但一个控制器可以有多个UI交互组件
 * 4、这个基类封装了一些播放器常用的功能方法，请阅读此类的method
 */
public abstract class BaseController extends FrameLayout implements IVideoController {

    protected static final String TAG = BaseController.class.getSimpleName();
    protected IVideoPlayerControl mVideoPlayerControl;//播放器代理人
    protected int mScreenOrientation= IMediaPlayer.ORIENTATION_PORTRAIT,mPlayerScene=IVideoController.SCENE_NOIMAL;//当前控制器(播放器)方向\当前控制器(播放器)场景
    protected LinkedList<IControllerView> mIControllerViews =new LinkedList<>();//所有自定义UI控制器组件
    private ControlWrapper mControlWrapper;
    protected long mAnimationDuration=MATION_DRAUTION;
    protected boolean isCompletion;//是否播放(试看)完成
    protected long mPreViewTotalTime;//试看模式下总时长


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
        if(0!=layoutId){
            View inflate = View.inflate(context, getLayoutId(), null);
            addView(inflate,new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT, Gravity.CENTER));
        }
        initViews();
    }

    public abstract int getLayoutId();

    public abstract void initViews();

    /**
     * 提供给播放器解码器来绑定播放器代理人
     * @param playerControl
     */
    protected void attachedPlayer(IVideoPlayerControl playerControl) {
        this.mVideoPlayerControl =playerControl;
    }

    //=================下列方法由播放器内部回调，请不要随意调用！！！子类复写方法请重载super方法=================

    /**
     * 组件初始化完成，组件已被添加到播放器
     */
    @Override
    public void onCreate() {}

    /**
     * 播放器的内部状态发生变化
     * @param state 播放器的内部状态 状态码 参考:PlayerState
     * @param message 描述信息
     */
    @Override
    public void onPlayerState(PlayerState state, String message) {
        for (IControllerView iControllerView : mIControllerViews) {
            iControllerView.onPlayerState(state,message);
        }
    }

    /**
     * 子类如需关心此回调请复写处理
     * @param currentDurtion 播放进度 主线程回调：当前播放位置,单位：总进度的毫秒进度
     * @param totalDurtion 总时长,单位：毫秒
     */
    @Override
    public void onProgress(long currentDurtion, long totalDurtion) {
        for (IControllerView iControllerView : mIControllerViews) {
            iControllerView.onProgress(currentDurtion,totalDurtion);
        }
    }

    /**
     * 子类如需关心此回调请复写处理
     * @param bufferPercent 缓冲进度 主线程回调,单位:百分比
     */
    @Override
    public void onBuffer(int bufferPercent) {
        for (IControllerView iControllerView : mIControllerViews) {
            iControllerView.onBuffer(bufferPercent);
        }
    }

    /**
     * 竖屏状态下,如果用户设置返回按钮可见仅显示返回按钮,切换到横屏模式下播放时初始都不显示
     * @param orientation 更新控制器方向状态 0:竖屏 1:横屏
     */
    @Override
    public void onScreenOrientation(int orientation) {
        this.mScreenOrientation=orientation;
        for (IControllerView iControllerView : mIControllerViews) {
            iControllerView.onOrientation(orientation);
        }
    }

    /**
     * 播放器/控制器的场景变化
     * @param playerScene 播放器/控制器的场景变化 0：常规状态(包括竖屏、横屏)，1：activity小窗口，2：全局悬浮窗窗口，3：Android8.0的画中画，4：列表 其它：自定义场景
     */
    @Override
    public void onPlayerScene(int playerScene) {
        for (IControllerView iControllerView : mIControllerViews) {
            iControllerView.onPlayerScene(getPlayerScene());
        }
    }

    /**
     * 当静音状态发生了变化回调
     * @param isMute 当静音状态发生了变化回调，true:处于静音状态 false:处于非静音状态
     */
    @Override
    public void onMute(boolean isMute) {
        for (IControllerView iControllerView : mIControllerViews) {
            iControllerView.onMute(isMute);
        }
    }

    /**
     * 当播放器的内部画面渲染镜像状态发生了变化回调
     * @param isMirror 当播放器的内部画面渲染镜像状态发生了变化回调， true:处于镜像状态 false:处于非镜像状态
     */
    @Override
    public void onMirror(boolean isMirror) {
        for (IControllerView iControllerView : mIControllerViews) {
            iControllerView.onMirror(isMirror);
        }
    }

    /**
     * 当播放器内部渲染缩放模式发生了变化回调
     * @param zoomModel 当播放器内部渲染缩放模式发生了变化回调，，当初始化和播放器缩放模式设置发生变化时回调，参考IMediaPlayer类
     */
    @Override
    public void onZoomModel(int zoomModel) {
        for (IControllerView iControllerView : mIControllerViews) {
            iControllerView.onZoomModel(zoomModel);
        }
    }

    /**
     * 生命周期可见,在播放器宿主调用播放的onResume方法后回调
     */
    @Override
    public void onResume() {
        for (IControllerView iControllerView : mIControllerViews) {
            iControllerView.onResume();
        }
    }

    /**
     * 生命周期不可见,在播放器宿主调用播放的onPause方法后回调
     */
    @Override
    public void onPause() {
        for (IControllerView iControllerView : mIControllerViews) {
            iControllerView.onPause();
        }
    }

    /**
     * 控制器所有状态重置(由播放器内部回调,与播放器生命周期无关)
     */
    @Override
    public void onReset() {
        for (IControllerView iControllerView : mIControllerViews) {
            iControllerView.onReset();
        }
    }

    /**
     * 播放器被销毁(由播放器内部回调)
     */
    @Override
    public void onDestroy() {
        for (IControllerView iControllerView : mIControllerViews) {
            iControllerView.onDestroy();
        }
        removeAllControllerWidget();
    }

    /**
     * 是否处于列表播放模式，在开始播放和开启\退出全屏时都需要设置
     * @param listPlayerScene 是否处于列表播放模式(需要在开始播放之前设置),列表播放模式下首次渲染不会显示控制器,否则首次渲染会显示控制器 true:处于列表播放模式 false:不处于列表播放模式
     */
    @Override
    public void setListPlayerMode(boolean listPlayerScene) {
        setPlayerScene(listPlayerScene?SCENE_LISTS:SCENE_NOIMAL);
    }

    /**
     * 进入画中画模式
     */
    @Override
    public void enterPipWindow() {
        setPlayerScene(SCENE_PIP_WINDOW);
    }

    /**
     * 退出画中画模式
     */
    @Override
    public void quitPipWindow() {
        setPlayerScene(SCENE_NOIMAL);
    }

    /**
     * 当切换至小窗口模式播放,取消可能存在的定时器隐藏控制器任务,强制隐藏控制器
     * @param isActivityWindow 控制器是否处于Activity级别窗口模式中
     * @param isGlobalWindow 控制器是否处于全局悬浮窗窗口模式中
     */
    public void setWindowPropertyPlayer(boolean isActivityWindow, boolean isGlobalWindow) {
        if(isActivityWindow){
            setPlayerScene(SCENE_ACTIVITY_WINDOW);
        }else if(isGlobalWindow){
            setPlayerScene(SCENE_GLOBAL_WINDOW);
        }else{
            setPlayerScene(SCENE_NOIMAL);
        }
    }

    //===================================控制器提供给宿主或子类调用======================================

    /**
     * 使用默认播放器的全部UI样式，可调用此方法，如需局部组件或全部组件自定义，请调用addControllerWidget(IControllerView)添加你的UI组件
     */
    public void initControlComponents() {
        initControlComponents(false);
    }

    /**
     * 1、使用默认播放器的全部UI样式，可调用此方法，如需局部组件或全部组件自定义，请调用addControllerWidget(IControllerView)添加你的UI组件
     * 2、这里为控制器添加UI组件时，每个UI组件都绑定了一个setTarget，方便在转场播放或其它场景调用findControlWidgetByTag(String)找到此组件
     * @param showBack 是否显示返回按钮
     */
    public void initControlComponents(boolean showBack) {
        initControlComponents(showBack,true);

    }

    /**
     * 1、使用默认播放器的全部UI样式，可调用此方法，如需局部组件或全部组件自定义，请调用addControllerWidget(IControllerView)添加你的UI组件
     * 2、这里为控制器添加UI组件时，每个UI组件都绑定了一个setTarget，方便在转场播放或其它场景调用findControlWidgetByTag(String)找到此组件
     * @param showBack 是否显示返回按钮
     * @param addWindowWidget 是否添加悬浮窗口交互UI组件
     */
    public void initControlComponents(boolean showBack,boolean addWindowWidget) {
        //顶部标题栏
        ControlToolBarView toolBarView=new ControlToolBarView(getContext());
        toolBarView.setTarget(IVideoController.TARGET_CONTROL_TOOL);
        toolBarView.showBack(showBack);
        //底部播放时间进度、progressBar、seekBae、静音、全屏等功能栏
        ControlFunctionBarView functionBarView=new ControlFunctionBarView(getContext());
        functionBarView.setTarget(IVideoController.TARGET_CONTROL_FUNCTION);
        //手势控制屏幕亮度、系统音量、快进、快退UI交互
        ControlGestureView gestureView=new ControlGestureView(getContext());
        gestureView.setTarget(IVideoController.TARGET_CONTROL_GESTURE);
        //播放完成、重试
        ControlCompletionView completionView=new ControlCompletionView(getContext());
        completionView.setTarget(IVideoController.TARGET_CONTROL_COMPLETION);
        //移动网络播放提示、播放失败、试看完成
        ControlStatusView statusView=new ControlStatusView(getContext());
        statusView.setTarget(IVideoController.TARGET_CONTROL_STATUS);
        //加载中、开始播放
        ControlLoadingView loadingView=new ControlLoadingView(getContext());
        loadingView.setTarget(IVideoController.TARGET_CONTROL_LOADING);
        //悬浮窗窗口播放器的窗口样式
        if(addWindowWidget){
            ControWindowView windowView=new ControWindowView(getContext());
            windowView.setTarget(IVideoController.TARGET_CONTROL_WINDOW);
            //将所有UI组件添加到控制器
            addControllerWidget(toolBarView,functionBarView,gestureView,completionView,statusView,loadingView,windowView);
        }else{
            //将所有UI组件添加到控制器
            addControllerWidget(toolBarView,functionBarView,gestureView,completionView,statusView,loadingView);
        }
    }

    /**
     * 向视频播放器控制器添加自定义UI组件
     * @param controllerView 添加自定义UI组件，必须是实现{@link IControllerView}接口的UI组件
     */
    @Override
    public void addControllerWidget(IControllerView controllerView) {
        addControllerWidget(controllerView,-1);
    }

    /**
     * 向视频播放器控制器添加自定义UI组件
     * @param controllerView 添加自定义UI组件，必须是实现{@link IControllerView}接口的UI组件
     * @param target 唯一的标识，设置此值后可在不同的场景下找到此值对应的Widget组件
     */
    @Override
    public void addControllerWidget(IControllerView controllerView, String target) {
        addControllerWidget(controllerView,target,-1);
    }

    /**
     * 向视频播放器控制器添加自定义UI组件
     * @param controllerView 添加自定义UI组件，必须是实现{@link IControllerView}接口的UI组件
     * @param index 添加的层级位置,默认是将UI控制组件添加到控制器上层
     */
    @Override
    public void addControllerWidget(IControllerView controllerView, int index) {
        addControllerWidget(controllerView,null,index);
    }

    /**
     * 向视频播放器控制器添加自定义UI组件
     * @param controllerView 添加自定义UI组件，必须是实现{@link IControllerView}接口的UI组件
     * @param target 唯一的标识，设置此值后可在不同的场景下找到此值对应的Widget组件
     * @param index 添加的层级位置,默认是将UI控制组件添加到控制器上层
     */
    @Override
    public void addControllerWidget(IControllerView controllerView, String target, int index) {
        if(null==controllerView) return;
        FrameLayout.LayoutParams layoutParams=new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        if(null==mControlWrapper){
            mControlWrapper = new ControlWrapper(this,mVideoPlayerControl);
        }
        controllerView.attachControlWrapper(mControlWrapper);
        if(TextUtils.isEmpty(controllerView.getTarget())){//未设置target情况下才主动为IControllerView添加target
            controllerView.setTarget(target);
        }
        mIControllerViews.add(controllerView);
        if(-1==index){
            addView(controllerView.getView(),layoutParams);
        }else{
            addView(controllerView.getView(),index,layoutParams);
        }
        //组件创建完成，各自定义UI组件可在这里初始化自己的逻辑
        controllerView.onCreate();
        controllerView.onOrientation(getOrientation());//初始化播放器横竖屏状态
        controllerView.onPlayerScene(getPlayerScene());//初始化播放器应用场景
    }

    /**
     * 向视频播放器控制器批量添加自定义UI组件
     * @param iControllerViews 添加多个自定义UI组件，必须是实现IControllerView接口的UI组件
     */
    @Override
    public void addControllerWidget(IControllerView... iControllerViews) {
        if(null!=iControllerViews&&iControllerViews.length>0){
            for (IControllerView iControllerView : iControllerViews) {
                addControllerWidget(iControllerView);
            }
        }
    }

    /**
     * 根据组件tag标识寻找组件实例
     * @param target 标识
     * @return 组件实例化的对象
     */
    @Override
    public IControllerView findControlWidgetByTag(String target) {
        for (IControllerView iControllerView : mIControllerViews) {
            if(target.equals(iControllerView.getTarget())){
                return iControllerView;
            }
        }
        return null;
    }

    /**
     * 移除已添加的自定义UI组件
     * @param controllerView 移除这个实例的控制器
     */
    @Override
    public void removeControllerWidget(IControllerView controllerView) {
        if(null!=controllerView) removeView(controllerView.getView());
        if(null!=mIControllerViews) mIControllerViews.remove(controllerView);
    }

    /**
     * 移除所有已添加的自定义UI组件
     */
    @Override
    public void removeAllControllerWidget() {
        if(null!=mIControllerViews){
            for (IControllerView iControllerView : mIControllerViews) {
                removeView(iControllerView.getView());
            }
            mIControllerViews.clear();
        }
    }

    /**
     * 是否播放完成
     * @return true:播放完成 false:未播放完成
     */
    @Override
    public boolean isCompletion() {
        return isCompletion;
    }

    /**
     * 返回是否是竖屏状态
     * @return true:竖屏状态 false:非竖屏状态
     */
    @Override
    public boolean isOrientationPortrait() {
        return mScreenOrientation==IMediaPlayer.ORIENTATION_PORTRAIT;
    }

    /**
     * 返回控制器是否处于竖屏状态
     * @return true:处于竖屏状态 false:非竖屏状态
     */
    @Override
    public boolean isOrientationLandscape() {
        return mScreenOrientation==IMediaPlayer.ORIENTATION_LANDSCAPE;
    }

    /**
     * 更新播放器场景
     * @param playerScene 更新播放器场景，自定义场景可调用此方法设置，设置后会同步通知到所有实现IControllerView接口的UI组件中的onPlayerScene方法
     */
    @Override
    public void setPlayerScene(int playerScene) {
        this.mPlayerScene=playerScene;
        onPlayerScene(mPlayerScene);
    }

    /**
     * 返回控制器当前正处于什么场景，各UI组件初始化后会收到回调：onPlayerScene
     * @return 播放器\控制器场景 0：常规状态(包括竖屏、横屏)，1：activity小窗口，2：全局悬浮窗窗口，3：列表，4：Android8.0的画中画 其它：自定义场景
     */
    @Override
    public int getPlayerScene() {
        return mPlayerScene;
    }

    /**
     * 返回试看模式下的虚拟总时长
     * @return
     */
    @Override
    public long getPreViewTotalTime() {
        return mPreViewTotalTime;
    }

    protected Activity getActivity() {
        if(null!= mVideoPlayerControl &&null!= mVideoPlayerControl.getParentContext()){
            return  PlayerUtils.getInstance().getActivity(mVideoPlayerControl.getParentContext());
        }
        return PlayerUtils.getInstance().getActivity(getContext());
    }

    protected Context getParentContext() {
        if(null!= mVideoPlayerControl &&null!= mVideoPlayerControl.getParentContext()){
            return  mVideoPlayerControl.getParentContext();
        }
        return getContext();
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


    /**
     * 返回视频文件总时长
     * @return 单位：毫秒
     */
    protected long getDuration(){
        if(null!=mVideoPlayerControl){
            return mVideoPlayerControl.getDuration();
        }
        return 0;
    }

    /**
     * 返回正在播放的位置
     * @return 单位：毫秒
     */
    protected long getCurrentPosition(){
        if(null!=mVideoPlayerControl){
            return mVideoPlayerControl.getCurrentPosition();
        }
        return 0;
    }

    /**
     * 返回视频分辨率-宽
     * @return 单位：像素
     */
    protected int getVideoWidth(){
        if(null!=mVideoPlayerControl){
            return mVideoPlayerControl.getVideoWidth();
        }
        return 0;
    }

    /**
     * 返回视频分辨率-高
     * @return 单位：像素
     */
    protected int getVideoHeight(){
        if(null!=mVideoPlayerControl){
            return mVideoPlayerControl.getVideoHeight();
        }
        return 0;
    }

    /**
     * 返回当前视频缓冲的进度
     * @return 单位：百分比
     */
    protected int getBuffer(){
        if(null!=mVideoPlayerControl){
            return mVideoPlayerControl.getBuffer();
        }
        return 0;
    }

    /**
     * 快进\快退
     * @param msec 毫秒进度条
     */
    protected void seekTo(long msec){
        if(null!=mVideoPlayerControl){
            mVideoPlayerControl.seekTo(msec);
        }
    }

    /**
     * 开始\暂停播放
     */
    protected void togglePlay() {
        if(null!=mVideoPlayerControl) mVideoPlayerControl.togglePlay();
    }

    /**
     * 结束播放
     */
    protected void stopPlay() {
        if(null!=mVideoPlayerControl) mVideoPlayerControl.onStop();
    }

    /**
     * 开启全屏播放
     */
    protected void startFullScreen() {
        if(null!=mVideoPlayerControl) mVideoPlayerControl.startFullScreen();
    }

    /**
     * 开启\退出全屏播放
     */
    protected void toggleFullScreen() {
        if(null!=mVideoPlayerControl) mVideoPlayerControl.toggleFullScreen();
    }

    /**
     * 是否开启了静音
     * @return true:已开启静音 false:系统音量
     */
    protected boolean isSoundMute() {
        if(null!=mVideoPlayerControl) {
            return mVideoPlayerControl.isSoundMute();
        }
        return false;
    }

    /**
     * 设置\取消静音
     * @param soundMute true:静音 false:系统音量
     * @return true:已开启静音 false:系统音量
     */
    protected boolean setSoundMute(boolean soundMute) {
        if(null!=mVideoPlayerControl) {
            return mVideoPlayerControl.setSoundMute(soundMute);
        }
        return false;
    }

    /**
     * 静音、取消静音
     */
    protected boolean toggleMute() {
        if(null!=mVideoPlayerControl) {
            return mVideoPlayerControl.toggleMute();
        }
        return false;
    }

    /**
     * 镜像、取消镜像
     */
    protected boolean toggleMirror() {
        if(null!=mVideoPlayerControl) {
            return mVideoPlayerControl.toggleMirror();
        }
        return false;
    }

    /**
     * 返回播放器交互方向
     * @return
     */
    protected int getOrientation() {
        return mScreenOrientation;
    }

    protected String getOrientationStr(){
        return ",Orientation:"+getOrientation();
    }

    /**
     * 返回是否是Activity悬浮窗窗口模式
     * @return true:当前正处于Activity窗口模式 false:当前不处于Activity窗口模式
     */
    protected boolean isActivityWindow() {
        return SCENE_ACTIVITY_WINDOW ==getPlayerScene();
    }

    /**
     * 返回是否是全局悬浮窗窗口模式
     * @return true:当前正处于全局悬浮窗窗口模式 false:当前不处于全局悬浮窗窗口模式
     */
    protected boolean isGlobalWindow() {
        return SCENE_GLOBAL_WINDOW==getPlayerScene();
    }

    /**
     * 返回是否是画中画窗口模式
     * @return true:当前正处于画中画窗口模式 false:当前不处于画中画窗口模式
     */
    protected boolean isPipWindow() {
        return SCENE_PIP_WINDOW==getPlayerScene();
    }

    /**
     * 是否处于列表模式下播放
     * @return true:是 false:否
     */
    protected boolean isListPlayerScene() {
        return SCENE_LISTS==getPlayerScene();
    }

    /**
     * 单击事件下-控制器组件显示
     * @param isAnimation 是否启用动画
     */
    protected void showWidget(boolean isAnimation){
        for (IControllerView iControllerView : mIControllerViews) {
            iControllerView.showControl(isAnimation);
        }
    }

    /**
     * 单击事件下-控制器组件隐藏
     * @param isAnimation 是否启用动画
     */
    protected void hideWidget(boolean isAnimation){
        for (IControllerView iControllerView : mIControllerViews) {
            iControllerView.hideControl(isAnimation);
        }
    }

    /**
     * 返回ID对应的字符串
     * @param resId 资源ID
     * @return
     */
    protected String getString(int resId){
        return getContext().getResources().getString(resId);
    }

    //==================下面这些方法时不常用的，子类如果需要处理下列方法,请复写实现自己的逻辑====================

    /**
     * 返回seek控制器是否正在显示中
     * @return
     */
    @Override
    public boolean isControllerShowing() {
        boolean isShowing=false;
        for (IControllerView iControllerView : mIControllerViews) {
            if(iControllerView.isSeekBarShowing()){
                isShowing=true;
                break;
            }
        }
        return isShowing;
    }

    /**
     * 请求其它所有UI组件隐藏自己的控制器,是否开启动画
     * @param isAnimation 请求其它所有UI组件隐藏自己的控制器,是否开启动画
     */
    @Override
    public void hideAllController(boolean isAnimation) {
        for (IControllerView iControllerView : mIControllerViews) {
            iControllerView.hideControl(isAnimation);
        }
    }

    //设置视频标题内容
    @Override
    public void setTitle(String videoTitle) {
        for (IControllerView iControllerView : mIControllerViews) {
            iControllerView.setTitle(videoTitle);
        }
    }

    //开始延时任务
    @Override
    public void startDelayedRunnable() {}

    //取消延时任务
    @Override
    public void stopDelayedRunnable() {}

    //重新开始延时任务。适用于：当有组件产生了交互后，需要重新开始倒计时关闭控制任务时的场景
    @Override
    public void reStartDelayedRunnable() {}

    //设置控制器的各UI组件显示、隐藏动画持续时间戳，单位：毫秒
    @Override
    public void setAnimationDuration(long animationDuration) {
        mAnimationDuration = animationDuration;
    }

    //返回控制器的各UI组件显示、隐藏动画持续时间戳，单位：毫秒
    @Override
    public long getAnimationDuration() {
        return mAnimationDuration;
    }
}