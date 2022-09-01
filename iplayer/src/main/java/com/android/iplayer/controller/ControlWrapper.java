package com.android.iplayer.controller;

import com.android.iplayer.interfaces.IVideoController;
import com.android.iplayer.interfaces.IVideoPlayerControl;
import com.android.iplayer.model.PlayerState;

/**
 * created by hty
 * 2022/8/22
 * Desc:一个在{@link IVideoController}(BaseController)和{@link IVideoPlayerControl}(BasePlayer)之间通信交互的桥梁
 */
public class ControlWrapper {

    private IVideoController mController;//控制器
    private IVideoPlayerControl mVideoPlayer;//播放器

    public ControlWrapper(IVideoController controllerControl, IVideoPlayerControl playerControl) {
        this.mController = controllerControl;
        this.mVideoPlayer = playerControl;
    }

    public IVideoController getController() {
        return mController;
    }

    public IVideoPlayerControl getVideoPlayer() {
        return mVideoPlayer;
    }

    //========================================控制器常用功能方法========================================

    /**
     * 是否播放完成
     * @return true:播放完成 false:未播放完成
     */
    public boolean isCompletion() {
        if(null!= mController){
            return mController.isCompletion();
        }
        return false;
    }

    /**
     * 返回播放器\控制器是否处于竖屏状态
     * @return true:处于竖屏状态 false:非竖屏状态
     */
    public boolean isOrientationPortrait() {
        if(null!= mController) {
            return mController.isOrientationPortrait();
        }
        return true;
    }

    /**
     * 返回播放器\控制器是否处于横屏状态
     * @return true:处于竖屏状态 false:非竖屏状态
     */
    public boolean isOrientationLandscape() {
        if(null!= mController) {
            return mController.isOrientationLandscape();
        }
        return false;
    }

    /**
     * 返回控制器当前正处于什么场景
     * @return 返回值参考IVideoController，0：常规状态(包括竖屏、横屏)，1：activity小窗口，2：全局悬浮窗窗口，3：Android8.0的画中画，4：列表 也可自定义更多场景
     */
    public int getPlayerScene() {
        if(null!= mController) {
            return mController.getPlayerScene();
        }
        return 0;
    }

    /**
     * 返回试看模式下的虚拟总时长
     * @return 单位：毫秒
     */
    public long getPreViewTotalTime() {
        if(null!= mController){
            return mController.getPreViewTotalTime();
        }
        return 0;
    }

    /**
     * 有些特殊场景，比如seek后立即改变内部播放器为缓冲状态时，可以手动更改播放器内部的播放状态,慎用！！！
     * @param state 播放器内部状态,请参阅PlayerState
     * @param message 描述信息
     */
    public void onPlayerState(PlayerState state, String message) {
        if(null!= mController) mController.onPlayerState(state,message);
    }

    /**
     * 开始延时任务
     */
    public void startDelayedRunnable() {
        if(null!= mController) mController.startDelayedRunnable();
    }

    /**
     * 结束延时任务
     */
    public void stopDelayedRunnable() {
        if(null!= mController) mController.stopDelayedRunnable();
    }

    /**
     * 重新开始延时任务
     */
    public void reStartDelayedRunnable() {
        if(null!= mController) mController.reStartDelayedRunnable();
    }

    /**
     * @param isAnimation 请求其它所有UI组件隐藏自己的控制器,是否开启动画
     */
    public void hideAllController(boolean isAnimation) {
        if(null!= mController) mController.hideAllController(isAnimation);
    }

    /**
     * @return 返回控制器的各UI组件显示、隐藏动画持续时间戳，单位：毫秒
     */
    public long getAnimationDuration() {
        if(null!= mController) {
            return mController.getAnimationDuration();
        }
        return IVideoController.MATION_DRAUTION;
    }

    //========================================播放器常用功能方法========================================

    /**
     * 开始\暂停播放
     */
    public void togglePlay() {
        if(null!= mVideoPlayer) mVideoPlayer.togglePlay();
    }

    /**
     * 结束播放
     */
    public void stopPlay() {
        if(null!= mVideoPlayer) mVideoPlayer.onStop();
    }

    /**
     * 开启全屏播放
     */
    public void startFullScreen() {
        if(null!= mVideoPlayer) mVideoPlayer.startFullScreen();
    }

    /**
     * 退出全屏播放
     */
    public void quitFullScreen() {
        if(null!= mVideoPlayer) mVideoPlayer.quitFullScreen();
    }

    /**
     * 开启\退出全屏播放
     */
    public void toggleFullScreen() {
        if(null!= mVideoPlayer) mVideoPlayer.toggleFullScreen();
    }

    /**
     * 镜像、取消镜像
     */
    public boolean toggleMirror() {
        if(null!= mVideoPlayer) {
            return mVideoPlayer.toggleMirror();
        }
        return false;
    }

    /**
     * 播放器是否正在播放中
     * @return 是否正处于播放中(准备\开始播放\播放中\缓冲\) true:播放中 false:不处于播放中状态
     */
    public boolean isPlaying() {
        if(null!= mVideoPlayer) {
            return mVideoPlayer.isPlaying();
        }
        return false;
    }

    /**
     * 播放器是否正在工作中
     * @return 播放器是否正处于工作状态(准备\开始播放\缓冲\手动暂停\生命周期暂停) true:工作中 false:空闲状态
     */
    public boolean isWorking() {
        if(null!= mVideoPlayer) {
            return mVideoPlayer.isWorking();
        }
        return false;
    }

    /**
     * 是否开启了静音
     * @return true:已开启静音 false:系统音量
     */
    public boolean isSoundMute() {
        if(null!= mVideoPlayer) {
            return mVideoPlayer.isSoundMute();
        }
        return false;
    }

    /**
     * 设置\取消静音
     * @param soundMute true:静音 false:系统音量
     * @return true:已开启静音 false:系统音量
     */
    public boolean setSoundMute(boolean soundMute) {
        if(null!= mVideoPlayer) {
            return mVideoPlayer.setSoundMute(soundMute);
        }
        return false;
    }

    /**
     * 静音、取消静音
     */
    public boolean toggleMute() {
        if(null!= mVideoPlayer) {
            return mVideoPlayer.toggleMute();
        }
        return false;
    }

    /**
     * 返回视频分辨率-宽
     * @return 单位：像素
     */
    public int getVideoWidth() {
        if(null!= mVideoPlayer) {
            return mVideoPlayer.getVideoWidth();
        }
        return 0;
    }

    /**
     * 返回视频分辨率-高
     * @return 单位：像素
     */
    public int getVideoHeight() {
        if(null!= mVideoPlayer) {
            return mVideoPlayer.getVideoHeight();
        }
        return 0;
    }

    /**
     * 试看模式下调用此方法结束播放
     */
    public void onCompletion() {
        if(null!= mVideoPlayer) mVideoPlayer.onCompletion();
    }

    /**
     * 返回视频文件总时长
     * @return 单位：毫秒
     */
    public long getDuration() {
        if(null!= mVideoPlayer){
            return mVideoPlayer.getDuration();
        }
        return 0;
    }

    /**
     * 返回正在播放的位置
     * @return 单位：毫秒
     */
    public long getCurrentPosition() {
        if(null!= mVideoPlayer){
            return mVideoPlayer.getCurrentPosition();
        }
        return 0;
    }

    /**
     * 返回当前视频缓冲进度
     * @return 单位：百分比
     */
    public int getBuffer() {
        if(null!= mVideoPlayer){
            return mVideoPlayer.getBuffer();
        }
        return 0;
    }

    /**
     * 快进\快退
     * @param msec 毫秒进度条
     */
    public void seekTo(long msec) {
        if(null!= mVideoPlayer) mVideoPlayer.seekTo(msec);
    }
}