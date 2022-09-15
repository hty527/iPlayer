package com.android.iplayer.widget.controls;

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.android.iplayer.widget.R;
import com.android.iplayer.base.BaseControlWidget;
import com.android.iplayer.manager.IVideoManager;
import com.android.iplayer.manager.IWindowManager;
import com.android.iplayer.model.PlayerState;
import com.android.iplayer.utils.AnimationUtils;

/**
 * created by hty
 * 2022/8/22
 * Desc:UI控制器-窗口交互控制器,由于窗口有拖拽手势原因，窗口交互时独立的一套UI。其它例如加载中、网络提示、播放失败等会和这个组件排斥。
 */
public class ControWindowView extends BaseControlWidget implements View.OnClickListener {

    private static final int MESSAGE_HIDE_CONTROLLER       = 20;//隐藏控制器
    private static final int DELAYED_INVISIBLE             = 3000;//延时隐藏锁时长
    private View mController;
    private ProgressBar mLoadingView,mProgressBar;//加载中
    //播放按钮,控制器,重新播放
    private View mControllerPlay,mControllerReplay;
    private ImageView mPlayIcon;//右下角的迷你播放状态按钮
    //失败\移动网络播放提示
    private ControlStatusView mControllerStatus;

    public ControWindowView(Context context) {
        super(context);
    }

    @Override
    public int getLayoutId() {
        return R.layout.player_control_window;
    }

    @Override
    public void initViews() {
        hide();
        mController=findViewById(R.id.window_controller);
        findViewById(R.id.window_fullscreen).setOnClickListener(this);
        FrameLayout rootView = findViewById(R.id.window_root_view);
        rootView.setOnClickListener(this);
        mControllerPlay = findViewById(R.id.window_play);
        mControllerPlay.setOnClickListener(this);
        mLoadingView = findViewById(R.id.window_loading);
        mProgressBar = findViewById(R.id.window_progress);
        mControllerStatus=new ControlStatusView(getContext());
        rootView.addView(mControllerStatus);
        mControllerStatus.setVisibility(View.GONE);
        mControllerStatus.setSceneType(1);//窗口适用的UI样式
        mControllerStatus.setOnStatusListener(new ControlStatusView.OnStatusListener() {
            @Override
            public void onEvent(int event) {
                if(null!=mControlWrapper){
                    if(ControlStatusView.SCENE_MOBILE==event){//移动网络播放
                        IVideoManager.getInstance().setMobileNetwork(true);
                        if (null != mControlWrapper) mControlWrapper.togglePlay();
                    }else if(ControlStatusView.SCENE_ERROR==event){//播放失败
                        if (null != mControlWrapper) mControlWrapper.togglePlay();
                    }
                }
            }
        });
        mControllerReplay = findViewById(R.id.window_replay);
        mControllerReplay.setOnClickListener(this);
        mPlayIcon = findViewById(R.id.window_start);
        mPlayIcon.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.window_fullscreen) {
            IWindowManager.getInstance().onClickWindow();
        } else if (id == R.id.window_start || id == R.id.window_replay || id==R.id.window_play) {
            if (null != mControlWrapper) mControlWrapper.togglePlay();//回调给播放器
        } else if (id == R.id.window_root_view) {
            toggleController();
        }
    }

    /**
     * @param isAnimation 控制器显示,是否开启动画
     */
    @Override
    public void showControl(boolean isAnimation) {
        if(isVisible()&&null!=mController){
            if(mController.getVisibility()!=View.VISIBLE){
                if(isAnimation){
                    AnimationUtils.getInstance().startAlphaAnimatioFrom(mController, getAnimationDuration(), false, null);
                }else{
                    mController.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    /**
     * @param isAnimation 控制器隐藏,是否开启动画
     */
    @Override
    public void hideControl(boolean isAnimation) {
        if(isVisible()&&null!=mController){
            if(mController.getVisibility()!=View.GONE){
                if(isAnimation){
                    AnimationUtils.getInstance().startAlphaAnimatioTo(mController, getAnimationDuration(), false, new AnimationUtils.OnAnimationListener() {
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            mController.setVisibility(View.GONE);
                        }
                    });
                }else{
                    mController.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public void onPlayerState(PlayerState state, String message) {
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
                if(null!=mPlayIcon) mPlayIcon.setImageResource(R.mipmap.ic_player_window_pause);
                showControl(false);
                changedUIState(View.GONE,View.GONE,View.GONE,View.GONE,0,null);
                break;
            case STATE_PLAY://缓冲结束恢复播放
            case STATE_ON_PLAY://生命周期\暂停情况下恢复播放
                if(null!=mPlayIcon) mPlayIcon.setImageResource(R.mipmap.ic_player_window_pause);
                changedUIState(View.GONE,View.GONE,View.GONE,View.GONE,0,null);
                break;
            case STATE_PAUSE://人为暂停中
            case STATE_ON_PAUSE://生命周期暂停中
                stopDelayedRunnable();
                if(null!=mPlayIcon) mPlayIcon.setImageResource(R.mipmap.ic_player_window_play);
                changedUIState(View.GONE,View.VISIBLE,View.GONE,View.GONE,0,null);
                break;
            case STATE_COMPLETION://播放结束
                if(null!=mPlayIcon) mPlayIcon.setImageResource(R.mipmap.ic_player_window_play);
                if(null!=mProgressBar) mProgressBar.setProgress(0);
                changedUIState(View.GONE,View.GONE,View.VISIBLE,View.GONE,0,null);
                break;
            case STATE_MOBILE://移动网络播放(如果设置允许4G播放则播放器内部不会回调此状态)
                changedUIState(View.GONE,View.GONE,View.GONE,View.VISIBLE,ControlStatusView.SCENE_MOBILE,null);
                break;
            case STATE_ERROR://播放失败
                if(null!=mPlayIcon) mPlayIcon.setImageResource(R.mipmap.ic_player_window_play);
                changedUIState(View.GONE,View.GONE,View.GONE,View.VISIBLE,ControlStatusView.SCENE_ERROR,null);
                break;
            case STATE_DESTROY://播放器回收
                onDestroy();
                break;
        }
    }

    @Override
    public void onOrientation(int direction) {}

    @Override
    public void onPlayerScene(int playerScene) {
        //仅当窗口模式时启用窗口控制器
        if (isWindowScene()) {
            show();
            findViewById(R.id.window_fullscreen).setVisibility(isWindowGlobalScene(playerScene) ? View.VISIBLE : View.GONE);
        } else {
            hide();
        }
    }

    @Override
    public void onProgress(long currentDurtion, long totalDurtion) {
        if(null!=mProgressBar){
            if(mProgressBar.getMax()==0){
                mProgressBar.setMax((int) (isPreViewScene()? mControlWrapper.getPreViewTotalDuration() :totalDurtion));
            }
            mProgressBar.setProgress((int) currentDurtion);
        }
    }

    @Override
    public void onBuffer(int percent) {
        if(null!= mProgressBar&&mProgressBar.getSecondaryProgress()!=percent) {
            mProgressBar.setSecondaryProgress(percent);
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
        if(null!= mLoadingView) mLoadingView.setVisibility(loadingView);
        if(null!=mControllerPlay) mControllerPlay.setVisibility(playerBtn);
        if(null!=mControllerReplay) mControllerReplay.setVisibility(replayBtn);
        if(null!=mControllerStatus){
            mControllerStatus.setVisibility(statuView);
            if(scene>0) mControllerStatus.setScene(scene,errorMessage);//仅当需要处理状态场景时才更新交互UI
        }
    }

    /**
     * 显示\隐藏控制器
     */
    private void toggleController() {
        stopDelayedRunnable();
        if(null==mController) return;
        if(mController.getVisibility()==View.VISIBLE){
            hideControl(true);
        }else{
            showControl(true);
            startDelayedRunnable();
        }
    }

    /**
     * 结启动延时任务
     */

    private void startDelayedRunnable() {
        startDelayedRunnable(MESSAGE_HIDE_CONTROLLER);
    }

    /**
     * 根据消息通道结启动延时任务
     */
    private void startDelayedRunnable(int msg) {
        if(null!=mExHandel){
            stopDelayedRunnable();
            Message message = mExHandel.obtainMessage();
            message.what= msg;
            mExHandel.sendMessageDelayed(message,DELAYED_INVISIBLE);
        }
    }

    /**
     * 结束延时任务
     */
    private void stopDelayedRunnable() {
        stopDelayedRunnable(0);
    }

    /**
     * 根据消息通道取消延时任务
     * @param msg
     */
    private void stopDelayedRunnable(int msg){
        if(null!=mExHandel) {
            if(0==msg){
                mExHandel.removeCallbacksAndMessages(null);
            }else{
                mExHandel.removeMessages(msg);
            }
        }
    }

    /**
     * 使用这个Handel替代getHandel(),避免多播放器同时工作的相互影响
     */
    private BaseHandel mExHandel=new BaseHandel(Looper.getMainLooper()){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(null!=msg&&MESSAGE_HIDE_CONTROLLER==msg.what){
                hideControl(true);
            }
        }
    };

    /**
     * 重置内部状态
     */
    private void reset(){
        stopDelayedRunnable();
        if(null!=mProgressBar) {
            mProgressBar.setProgress(0);
            mProgressBar.setSecondaryProgress(0);
            mProgressBar.setMax(0);
        }
        if(null!=mExHandel) mExHandel.removeCallbacksAndMessages(null);
        if(null!=mPlayIcon) mPlayIcon.setImageResource(R.mipmap.ic_player_window_play);
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