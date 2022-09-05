package com.android.iplayer.interfaces;

import com.android.iplayer.model.PlayerState;

/**
 * created by hty
 * 2022/7/3
 * Desc:Controller控制器拓展接口
 */
public interface IVideoController {

    /** 播放器的顶部title组件默认标识 */
    String TARGET_CONTROL_TOOL          =   "toolBar";

    /** 播放器的底部功能菜单组件默认标识 */
    String TARGET_CONTROL_FUNCTION      =   "functionBar";

    /** 播放器的window组件默认标识 */
    String TARGET_CONTROL_WINDOW        =   "windowBar";

    /** 播放器的播放完成组件默认标识 */
    String TARGET_CONTROL_COMPLETION    =   "completionBar";

    /** 播放器的各种状态默认标识 */
    String TARGET_CONTROL_STATUS        =   "statusBar";

    /** 播放器的loading组件默认标识 */
    String TARGET_CONTROL_LOADING       =   "loadingBar";

    /** 播放器的手势交互组件默认标识 */
    String TARGET_CONTROL_GESTURE       =   "gestureBar";

    /**
     * 播放器/控制器场景-常规（包括竖屏、横屏两种状态）
     */
    int SCENE_NOIMAL            =0;

    /**
     * 播放器/控制器场景-activity小窗口
     */
    int SCENE_ACTIVITY_WINDOW   =1;

    /**
     * 播放器/控制器场景-全局悬浮窗窗口
     */
    int SCENE_GLOBAL_WINDOW     =2;

    /**
     * 播放器/控制器场景-画中画窗口
     */
    int SCENE_PIP_WINDOW        =3;

    /**
     * 播放器/控制器场景-列表
     */
    int SCENE_LISTS             =4;

    /**
     * 各控制器/UI组件显示/隐藏动画持续时长，单位：时间戳
     */
    long MATION_DRAUTION =300;


    //=======================================生命周期及状态回调=========================================

    /**
     * 控制器创建成功，此时已经被绑定到播放器了
     */
    void onCreate();

    /**
     * 播放器的内部状态发生变化
     * @param state 播放器的内部状态 状态码 参考:PlayerState
     * @param message 描述信息
     */
    void onPlayerState(PlayerState state, String message);

    /**
     * @param currentDurtion 播放进度 主线程回调：当前播放位置,单位：总进度的毫秒进度
     * @param totalDurtion 总时长,单位：毫秒
     */
    void onProgress(long currentDurtion, long totalDurtion);

    /**
     * @param bufferPercent 缓冲进度 主线程回调,单位:百分比
     */
    void onBuffer(int bufferPercent);

    /**
     * @param orientation 更新控制器方向状态 0:竖屏 1:横屏
     */
    void onScreenOrientation(int orientation);

    /**
     * 播放器/控制器的场景变化
     * @param playerScene 播放器/控制器的场景变化 0：常规状态(包括竖屏、横屏)，1：activity小窗口，2：全局悬浮窗窗口，3：Android8.0的画中画，4：列表 其它：自定义场景
     */
    void onPlayerScene(int playerScene);

    /**
     * @param isMute 当静音状态发生了变化回调，true:处于静音状态 false:处于非静音状态
     */
    void onMute(boolean isMute);

    /**
     * @param isMirror 当播放器的内部画面渲染镜像状态发生了变化回调， true:处于镜像状态 false:处于非镜像状态
     */
    void onMirror(boolean isMirror);

    /**
     * @param zoomModel 当播放器内部渲染缩放模式发生了变化回调，，当初始化和播放器缩放模式设置发生变化时回调，参考IMediaPlayer类
     */
    void onZoomModel(int zoomModel);

    /**
     * 生命周期可见,和播放状态无关
     */
    void onResume();

    /**
     * 生命周期不可见,和播放状态无关
     */
    void onPause();

    /**
     * 释放
     */
    void onReset();

    /**
     * 销毁
     */
    void onDestroy();

    //========================================控制器常用方法==========================================

    /**
     * @param controllerView 添加自定义UI组件，必须是实现{@link IControllerView}接口的UI组件
     */
    void addControllerWidget(IControllerView controllerView);

    /**
     * @param target 唯一的标识，设置此值后可在不同的场景下找到此值对应的Widget组件
     * @param controllerView 添加自定义UI组件，必须是实现{@link IControllerView}接口的UI组件
     */
    void addControllerWidget(IControllerView controllerView,String target);

    /**
     * @param index 添加的层级位置,默认是将UI控制组件添加到控制器上层
     * @param controllerView 添加自定义UI组件，必须是实现{@link IControllerView}接口的UI组件
     */
    void addControllerWidget(IControllerView controllerView,int index);

    /**
     * @param target 唯一的标识，设置此值后可在不同的场景下找到此值对应的Widget组件
     * @param index 添加的层级位置,默认是将UI控制组件添加到控制器上层
     * @param controllerView 添加自定义UI组件，必须是实现{@link IControllerView}接口的UI组件
     */
    void addControllerWidget(IControllerView controllerView,String target,int index);

    /**
     * @param iControllerViews 添加多个自定义UI组件，必须是实现{@link IControllerView}接口的UI组件
     */
    void addControllerWidget(IControllerView... iControllerViews);

    /**
     * @param controllerView 移除这个实例的控制器，必须是实现{@link IControllerView}接口的UI组件
     */
    void removeControllerWidget(IControllerView controllerView);

    /**
     * 根据组件tag标识寻找组件实例
     * @param target 根据组件tag标识寻找组件实例,target为组件的唯一标识
     * @return 组件实例化的对象
     */
    IControllerView findControlWidgetByTag(String target);

    /**
     * 移除所有控制器
     */
    void removeAllControllerWidget();

    //======================================下列方法为不常用方法========================================

    /**
     * 设置视频标题
     * @param videoTitle 视频标题
     */
    void setTitle(String videoTitle);

    /**
     * 返回是否播放\试看完成
     * @return true:播放完成 false:未播放完成
     */
    boolean isCompletion();

    /**
     * 返回seekBar控制器是否正在被显示状态中
     * @return seekBar控制器是否正在被显示状态中
     */
    boolean isControllerShowing();

    /**
     * 返回控制器是否处于竖屏状态
     * @return true:处于竖屏状态 false:非竖屏状态
     */
    boolean isOrientationPortrait();

    /**
     * 返回控制器是否处于横屏状态
     * @return true:处于横屏状态 false:非横屏状态
     */
    boolean isOrientationLandscape();

    /**
     * 更新播放器场景
     * @param playerScene 更新播放器场景，自定义场景可调用此方法设置，设置后会同步通知到所有实现IControllerView接口的UI组件中的onPlayerScene方法
     *                    播放器\控制器场景 0：常规状态(包括竖屏、横屏)，1：activity小窗口，2：全局悬浮窗窗口，3：列表，4：Android8.0的画中画 其它：自定义场景
     */
    void setPlayerScene(int playerScene);

    /**
     * 返回控制器当前正处于什么场景，各UI组件初始化后会收到回调：onPlayerScene
     * @return 播放器\控制器场景 0：常规状态(包括竖屏、横屏)，1：activity小窗口，2：全局悬浮窗窗口，3：列表，4：Android8.0的画中画 其它：自定义场景
     */
    int getPlayerScene();

    /**
     * 设置试看|收费模式下的虚拟总时长
     * @return 设置试看|收费模式下的虚拟总时长，单位：毫秒
     */
    void setPreViewTotalDuration(long preTotalDuration);

    /**
     * 返回试看模式下的试看时长
     * @return 返回试看模式下的试看时长，单位：毫秒
     */
    long getPreViewTotalDuration();

    /**
     * 设置控制器为列表模式
     * @param listPlayerScene 设置控制器为列表模式 true：列表模式 false：非列表模式
     */
    void setListPlayerMode(boolean listPlayerScene);

    /**
     * 进入画中画模式
     */
    void enterPipWindow();

    /**
     * 退出画中画模式
     */
    void quitPipWindow();

    /**
     * 开始延时任务
     */
    void startDelayedRunnable();

    /**
     * 取消延时任务
     */
    void stopDelayedRunnable();

    /**
     * 重新开始延时任务。适用于：当有组件产生了交互后，需要重新开始倒计时关闭控制任务时的场景
     */
    void reStartDelayedRunnable();

    /**
     * @param isAnimation 请求其它所有UI组件隐藏自己的控制器,是否开启动画
     */
    void hideAllController(boolean isAnimation);

    /**
     * 设置控制器的各UI组件显示、隐藏动画持续时间戳
     * @param animationDuration 设置控制器的各UI组件显示、隐藏动画持续时间戳，单位：毫秒
     */
    void setAnimationDuration(long animationDuration);

    /**
     * @return 返回控制器的各UI组件显示、隐藏动画持续时间戳，单位：毫秒
     */
    long getAnimationDuration();
}