package com.android.iplayer.interfaces;

import android.view.View;
import com.android.iplayer.controller.ControlWrapper;
import com.android.iplayer.model.PlayerState;

/**
 * created by hty
 * 2022/8/22
 * Desc:控制器的所有自定义UI组件都必须实现的接口
 */
public interface IControllerView {

    /**
     * 播放器/控制器场景-常规（包括竖屏、横屏两种状态）
     */
    int SCENE_NOIMAL            =0;

    /**
     * 播放器/控制器场景-activity小窗口
     */
    int SCENE_WINDOW            =1;

    /**
     * 播放器/控制器场景-全局悬浮窗窗口
     */
    int SCENE_GLOBAL_WINDOW     =2;

    /**
     * 播放器/控制器场景-列表
     */
    int SCENE_LISTS             =3;

    /**
     * 绑定控制器代理人
     * @param controlWrapper 控制器+播放器代理中间人
     */
    void attachControlWrapper(ControlWrapper controlWrapper);

    /**
     * 返回自定义组件的UI
     * @return 组件UI View
     */
    View getView();

    /**
     * 组件已经完全创建并绑定到控制器完成,各UI控制器可在这里初始化自己的逻辑
     */
    void onCreate();

    /**
     * 当前View组件显示
     */
    void show();

    /**
     * 当前View组件隐藏
     */
    void hide();

    /**
     * 设置标识，方便在转场播放场景下找到对应的UI组件
     * @param tag 任意类型标识
     */
    void setTarget(String tag);

    /**
     * 返回当前UI组件任意类型的tag标识
     * @return tag标识
     */
    String getTarget();

    /**
     * 返回主要的(标题、底部控制栏)是否正在显示,其它的组件不要返回true
     * @return true:正在显示种 false:非显示种
     */
    boolean isSeekBarShowing();

    /**
     * 控制器UI交互组件显示
     * @param isAnimation 控制器显示,是否开启动画
     */
    void showControl(boolean isAnimation);

    /**
     * 控制器UI交互组件隐藏
     * @param isAnimation 控制器隐藏,是否开启动画
     */
    void hideControl(boolean isAnimation);

    /**
     * 设置视频标题
     * @param title 视频标题
     */
    void setTitle(String title);

    /**
     * 播放器内部状态回调(初始化状态不回调)
     * @param state 播放器内部状态，请阅读PlayerState
     * @param message 事件描述
     */
    void onPlayerState(PlayerState state, String message);

    /**
     * 播放器\控制器的方向
     * @param direction 播放器\控制器的方向发生了变化(组件初始化也会回调) 0:竖屏 1:横屏
     */
    void onOrientation(int direction);

    /**
     * 播放器场景回调(组件初始化、场景发生变化时都会回调)
     * @param scene 播放器场景 0：竖屏 1：横屏 2：activity小窗口 4：全局悬浮窗窗口 5：列表
     */
    void onPlayerScene(int scene);

    /**
     * 视频播放进度
     * @param currentDurtion 播放进度 主线程回调：当前播放位置,单位：总进度的毫秒进度
     * @param totalDurtion 总时长,单位：毫秒
     */
    void onProgress(long currentDurtion, long totalDurtion);

    /**
     * 视频缓冲进度
     * @param percent 缓冲进度 主线程回调,单位:百分比
     */
    void onBuffer(int percent);

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
     * 当播放器被重置回调
     */
    void onReset();

    /**
     * 当播放器生命周期处于可见回调,需播放器宿主在Activity的onResume中调用BasePlayer.onResume生效
     */
    void onResume();

    /**
     * 当播放器生命周期处于不可见回调,需播放器宿主在Activity的onPause中调用BasePlayer.onPause生效
     */
    void onPause();

    /**
     * 当播放器生命周期处于销毁状态时回调,直接开启窗口、全屏 或 需播放器宿主在Activity的onResume中调用BasePlayer.onResume生效
     */
    void onDestroy();
}