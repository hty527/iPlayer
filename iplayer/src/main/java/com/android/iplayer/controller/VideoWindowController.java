package com.android.iplayer.controller;

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.android.iplayer.R;
import com.android.iplayer.base.BaseController;
import com.android.iplayer.manager.IVideoManager;
import com.android.iplayer.manager.IWindowManager;
import com.android.iplayer.model.PlayerState;
import com.android.iplayer.utils.ILogger;
import com.android.iplayer.utils.PlayerUtils;
import com.android.iplayer.widget.ControllerStatusView;

/**
 * created by hty
 * 2022/7/8
 * Desc:默认的视频窗口播放器的控制器
 * 功能菜单：开始\暂停\进度条\恢复到常规屏幕显示
 */
public class VideoWindowController extends BaseController {

    public static final String TAG="VideoWindowController";
    public static final int SCENE_MOBILE     =1;//移动网络播放提示
    public static final int SCENE_COMPLETION =2;//试看结束
    public static final int SCENE_ERROR      =3;//播放失败
    private static final int MESSAGE_HIDE_CONTROLLER      = 100;//隐藏控制器
    protected ProgressBar mControllerLoading;
    //播放按钮,控制器,重新播放
    protected View mControllerPlay,mControllerController,mControllerReplay;
    protected ImageView mPlayIcon;//右下角的迷你播放状态按钮
    //失败\移动网络
    private ControllerStatusView mControllerStatus;

    public VideoWindowController(Context context) {
        super(context);
    }

    @Override
    public int getLayoutId() {
        return R.layout.player_video_window_controller;
    }

    @Override
    public void initViews() {
        OnClickListener onClickListener=new OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = view.getId();
                if (id == R.id.window_fullscreen) {
                    IWindowManager.getInstance().onClickWindow();
                } else if (id == R.id.window_start || id == R.id.window_replay || id==R.id.window_play) {
                    if (null != mVideoPlayerControl) mVideoPlayerControl.togglePlay();//回调给播放器
                } else if (id == R.id.window_root_view) {
                    toggleController(false);
                }
            }
        };
        findViewById(R.id.window_fullscreen).setOnClickListener(onClickListener);
        findViewById(R.id.window_root_view).setOnClickListener(onClickListener);
        mControllerPlay = findViewById(R.id.window_play);
        mControllerPlay.setOnClickListener(onClickListener);
        mControllerLoading = findViewById(R.id.window_loading);
        mControllerController = findViewById(R.id.window_controller);
        mControllerStatus = findViewById(R.id.window_status);
        mControllerStatus.setSceneType(1);//窗口适用的UI样式
        mControllerStatus.setOnStatusListener(new ControllerStatusView.OnStatusListener() {
            @Override
            public void onEvent(int event) {
                if(SCENE_MOBILE==event){//移动网络播放
                    IVideoManager.getInstance().setMobileNetwork(true);
                    if (null != mVideoPlayerControl) mVideoPlayerControl.togglePlay();
                }else if(SCENE_ERROR==event){//播放失败
                    if (null != mVideoPlayerControl) mVideoPlayerControl.togglePlay();
                }
            }
        });
        mControllerReplay = findViewById(R.id.window_replay);
        mControllerReplay.setOnClickListener(onClickListener);
        mPlayIcon = findViewById(R.id.window_start);
        mPlayIcon.setOnClickListener(onClickListener);
    }

    @Override
    public void onState(PlayerState state, String message) {
        ILogger.d(TAG,"onState-->state:"+state+getOrientationStr()+",message:"+message);
        switch (state) {
            case STATE_RESET://初始状态\播放器还原重置
            case STATE_STOP://初始\停止
                if(null!=mPlayIcon) mPlayIcon.setImageResource(R.mipmap.ic_player_window_play);
                onReset();
                break;
            case STATE_PREPARE://准备中
            case STATE_BUFFER://缓冲中
                changedUIState(View.VISIBLE,View.GONE,View.GONE,View.GONE,0,null);
                break;
            case STATE_START://首次播放
            case STATE_PLAY://缓冲结束恢复播放
            case STATE_ON_PLAY://生命周期\暂停情况下恢复播放
                changedUIState(View.GONE,View.GONE,View.GONE,View.GONE,0,null);
                if(null!=mPlayIcon) mPlayIcon.setImageResource(R.mipmap.ic_player_window_pause);
                break;
            case STATE_PAUSE://人为暂停中
            case STATE_ON_PAUSE://生命周期暂停中
                changedUIState(View.GONE,View.VISIBLE,View.GONE,View.GONE,0,null);
                if(null!=mPlayIcon) mPlayIcon.setImageResource(R.mipmap.ic_player_window_play);
                break;
            case STATE_COMPLETION://播放结束
                if(null!=mPlayIcon) mPlayIcon.setImageResource(R.mipmap.ic_player_window_play);
                changedUIState(View.GONE,View.GONE,View.VISIBLE,View.GONE,0,null);
                break;
            case STATE_MOBILE://移动网络播放(如果设置允许4G播放则播放器内部不会回调此状态)
                changedUIState(View.GONE,View.GONE,View.GONE,View.VISIBLE,SCENE_MOBILE,null);
                break;
            case STATE_ERROR://播放失败
                if(null!=mPlayIcon) mPlayIcon.setImageResource(R.mipmap.ic_player_window_play);
                changedUIState(View.GONE,View.GONE,View.GONE,View.VISIBLE,SCENE_ERROR,null);
                break;
            case STATE_DESTROY://播放器回收
                onDestroy();
                break;
        }
    }

    @Override
    public void onProgress(long currentDurtion, long totalDurtion, int bufferPercent) {

    }

    @Override
    public void onBuffer(int bufferPercent) {

    }

    @Override
    public void setScreenOrientation(int orientation) {

    }

    @Override
    public void setWindowProperty(boolean isWindowProperty, boolean isGlobalWindow) {
        findViewById(R.id.window_fullscreen).setVisibility(isGlobalWindow?View.VISIBLE:View.GONE);
        if(null!=mVideoPlayerControl&&null!=mPlayIcon){
            mPlayIcon.setImageResource(mVideoPlayerControl.isPlaying()?R.mipmap.ic_player_window_pause:R.mipmap.ic_player_window_play);
        }
    }

    /**
     * 改变UI状态
     * @param loadingView 加载状态
     * @param playerBtn 播放按钮状态
     * @param replayBtn 重新播放
     * @param statuView 移动网络播放\试看结束\播放失败 状态
     * @param scene 状态场景类型,提供给回调判断
     * @param errorMessage 当播放错误或scene==SCENE_ERROR时不为空
     */
    private void changedUIState(int loadingView,int playerBtn,int replayBtn,int statuView,int scene,String errorMessage){
        if(null!=mControllerLoading) mControllerLoading.setVisibility(loadingView);
        if(null!=mControllerPlay) mControllerPlay.setVisibility(playerBtn);
        if(null!=mControllerReplay) mControllerReplay.setVisibility(replayBtn);
        if(null!=mControllerStatus){
            mControllerStatus.setVisibility(statuView);
            if(scene>0) mControllerStatus.setScene(scene,errorMessage);//仅当需要处理状态场景时才更新交互UI
        }
    }

    /**
     显示\隐藏视图控制器
     * 横屏标题栏页参与显示隐藏,竖屏不处理标题栏
     * @param isHide 标题栏和菜单控制器是否强制隐藏
     */
    private void toggleController(boolean isHide) {
        removeDelayedControllerRunnable();
        if(null!=mControllerController){
            if(isHide){//强制隐藏
                mControllerController.setVisibility(GONE);
                return;
            }
            //控制栏显示中
            if(mControllerController.getVisibility()==View.VISIBLE){
                mControllerController.setVisibility(GONE);
            }else{
                mControllerController.setVisibility(VISIBLE);
            }
            delayedInvisibleController();
        }
    }

    /**
     * 使用这个Handel替代getHandel(),避免多播放器同时工作的相互影响
     */
    private ExHandel mExHandel=new ExHandel(Looper.getMainLooper()){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(null!=msg&&MESSAGE_HIDE_CONTROLLER==msg.what){
                if(null!=mControllerController&&mControllerController.getVisibility()==View.VISIBLE){
                    PlayerUtils.getInstance().startAlphaAnimation(mControllerController, 200, false, new PlayerUtils.OnAnimationListener() {
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            if(null!=mControllerController) mControllerController.setVisibility(GONE);
                        }
                    });
                }
            }
        }
    };

    /**
     * 取消控制器隐藏延时任务
     */
    private void removeDelayedControllerRunnable(){
        if(null!=mExHandel) mExHandel.removeCallbacksAndMessages(null);
    }

    /**
     * 启动延时隐藏控制器任务
     */
    private void delayedInvisibleController() {
        try {
            if(null!=mControllerController&&null!=mExHandel){
                Message obtain = Message.obtain();
                obtain.what=MESSAGE_HIDE_CONTROLLER;
                mExHandel.sendMessageDelayed(obtain,3000);
            }
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    /**
     * 重置内部状态
     */
    private void reset(){
        removeDelayedControllerRunnable();
        if(null!=mExHandel) mExHandel.removeCallbacksAndMessages(null);
        if(null!=mPlayIcon) mPlayIcon.setImageResource(R.mipmap.ic_player_window_play);
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onReset() {
        reset();
        changedUIState(View.GONE,View.VISIBLE,View.GONE,View.GONE,0,null);
    }

    @Override
    public void onDestroy() {
        reset();
        changedUIState(View.GONE,View.VISIBLE,View.GONE,View.GONE,0,null);
    }
}
