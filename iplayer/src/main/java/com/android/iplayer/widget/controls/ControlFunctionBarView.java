package com.android.iplayer.widget.controls;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import com.android.iplayer.R;
import com.android.iplayer.base.BaseControllerWidget;
import com.android.iplayer.controller.ControlWrapper;
import com.android.iplayer.interfaces.IMediaPlayer;
import com.android.iplayer.model.PlayerState;
import com.android.iplayer.utils.AnimationUtils;
import com.android.iplayer.utils.PlayerUtils;

/**
 * created by hty
 * 2022/8/22
 * Desc:UI控制器-底部功能交互控制
 * 1、自定义seekbar相关的控制器需要实现{@link #isSeekBarShowing()}方法，返回显示状态给Controller判断控制器是否正在显示中
 * 2、当单击BaseController空白区域时控制器需要处理显示\隐藏逻辑的情况下需要复写{@link #showControl(boolean)}和{@link #hideControl(boolean)}方法
 * 3、这个seekBar进度条组件还维护了底部的ProgressBar，SDK默认的UI交互是：当播放器处于列表模式时不显示，其它情况都显示
 */
public class ControlFunctionBarView extends BaseControllerWidget implements View.OnClickListener {

    private View mController;//控制器
    private SeekBar mSeekBar;//seek调节控制器
    private ProgressBar mProgressBar;//底部进度条
    private TextView mCurrentDuration,mTotalDuration;//当前播放位置时间\总时间
    private ImageView mPlayIcon;//左下角的迷你播放状态按钮
    //用户手指是否持续拖动中
    private boolean isTouchSeekBar;

    public ControlFunctionBarView(Context context) {
        super(context);
    }

    @Override
    public int getLayoutId() {
        return R.layout.player_control_functionbar;
    }

    @Override
    public void initViews() {
        hide();
        mPlayIcon = findViewById(R.id.controller_start);
        mSeekBar = findViewById(R.id.controller_seek_bar);
        mController = findViewById(R.id.controller_controller);
        mCurrentDuration = findViewById(R.id.controller_current_duration);
        mTotalDuration = findViewById(R.id.controller_total_duration);
        mProgressBar = findViewById(R.id.controller_bottom_progress);
        mPlayIcon.setOnClickListener(this);
        findViewById(R.id.controller_btn_mute).setOnClickListener(this);
        findViewById(R.id.controller_btn_fullscreen).setOnClickListener(this);
        //seekBar监听
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            /**
             * 用户持续拖动进度条,视频总长为虚拟时长时，用户不得滑动阈值超过限制
             * @param seekBar
             * @param progress
             * @param fromUser
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                ILogger.d(TAG,"onProgressChanged-->progress:"+progress+",fromUser:"+fromUser+getOrientationStr());
                //视频虚拟总长度
                if(null!=mCurrentDuration) mCurrentDuration.setText(PlayerUtils.getInstance().stringForAudioTime(progress));
                if(null!=mProgressBar) mProgressBar.setProgress(progress);
            }

            /**
             * 获得焦点-按住了
             * @param seekBar
             */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isTouchSeekBar=true;
                mControlWrapper.stopDelayedRunnable();//取消定时隐藏任务
            }

            /**
             * 失去焦点-松手了
             * @param seekBar
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isTouchSeekBar=false;
                mControlWrapper.startDelayedRunnable();//开启定时隐藏任务
                //当controller_deblocking设置了点击时间，试看结束的拦截都无效
//                ILogger.d(TAG,"onStopTrackingTouch-->,isCompletion:"+isCompletion+",preViewTotalTime:"+mPreViewTotalTime);
                if(null!= mControlWrapper){
                    if(mControlWrapper.isCompletion()&& mControlWrapper.getPreViewTotalTime() >0){//拦截是看结束,让用户解锁
                        if(null!= mControlWrapper) mControlWrapper.onCompletion();
                        return;
                    }
                    int seekBarProgress = seekBar.getProgress();
//                    ILogger.d(TAG,"onStopTrackingTouch-->seekBarProgress:"+seekBarProgress+",ViewTotalTime:"+ mPreViewTotalTime +",duration:"+ mVideoPlayerControl.getDurtion()+getOrientationStr());
                    if(mControlWrapper.getPreViewTotalTime() >0){ //跳转至某处,如果滑动的时长超过真实的试看时长,则直接播放完成需要解锁
                        long durtion = mControlWrapper.getDuration();
                        if(0==seekBarProgress){//重新从头开始播放
                            //改变UI为缓冲状态
                            mControlWrapper.onPlayerState(PlayerState.STATE_BUFFER,"seek");
                            mControlWrapper.seekTo(0);
                        }else{
                            if(seekBarProgress>=durtion){//试看片段,需要解锁
                                mControlWrapper.onCompletion();
                            }else{
                                //改变UI为缓冲状态
                                mControlWrapper.onPlayerState(PlayerState.STATE_BUFFER,"seek");
                                mControlWrapper.seekTo(seekBarProgress);//试看片段内,允许跳转
                            }
                        }
                    }else{
                        //改变UI为缓冲状态
                        mControlWrapper.onPlayerState(PlayerState.STATE_BUFFER,"seek");
                        mControlWrapper.seekTo(seekBarProgress);//真实时长,允许跳转
                    }
                }
            }
        });
    }

    @Override
    public void attachControlWrapper(ControlWrapper controlWrapper) {
        super.attachControlWrapper(controlWrapper);
        if(null!=mTotalDuration) mTotalDuration.setText(PlayerUtils.getInstance().stringForAudioTime(mControlWrapper.getPreViewTotalTime()));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        updateMute();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.controller_start) {
            reStartDelayedRunnable();
            togglePlay();
        }else if (view.getId() == R.id.controller_btn_mute) {
            reStartDelayedRunnable();
            toggleMute();
        }else if (view.getId() == R.id.controller_btn_fullscreen) {
            reStartDelayedRunnable();
            toggleFullScreen();
        }
    }

    @Override
    public boolean isSeekBarShowing() {
        return null!=mController&&mController.getVisibility()==View.VISIBLE;
    }

    /**
     * @param isAnimation 控制器显示,是否开启动画
     */
    @Override
    public void showControl(boolean isAnimation) {
        if(null!=mController){
            if(mController.getVisibility()!=View.VISIBLE){
                if(null!=mProgressBar) mProgressBar.setVisibility(GONE);
                if(isAnimation){
                    AnimationUtils.getInstance().startTranslateBottomToLocat(mController, MATION_DRAUTION,null);
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
        if(null!=mController){
            if(mController.getVisibility()!=View.GONE){
                if(isAnimation){
                    AnimationUtils.getInstance().startTranslateLocatToBottom(mController, MATION_DRAUTION, new AnimationUtils.OnAnimationListener() {
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            mController.setVisibility(GONE);
                            AnimationUtils.getInstance().startAlphaAnimatioFrom(mProgressBar,MATION_DRAUTION,false,null);
//                            if(null!=mProgressBar) mProgressBar.setVisibility(View.VISIBLE);
                        }
                    });
                }else{
                    mController.setVisibility(View.GONE);
                    if(null!=mProgressBar) mProgressBar.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void onPlayerState(PlayerState state, String message) {
        switch (state) {
            case STATE_RESET://初始状态\播放器还原重置
            case STATE_STOP://初始\停止
                onReset();
                break;
            case STATE_PREPARE://初次准备中不显示交互组件
                if(null!=mPlayIcon) mPlayIcon.setImageResource(R.mipmap.ic_player_play);
                hide();
                break;
            case STATE_BUFFER://缓冲中
                if(null!=mPlayIcon) mPlayIcon.setImageResource(R.mipmap.ic_player_play);
                break;
            case STATE_START://首次播放
                //渲染第一帧时，竖屏和横屏都显示
                if(isNoimalScene()){
                    show();
                }
                if(null!=mPlayIcon) mPlayIcon.setImageResource(R.mipmap.ic_player_pause);
                showControl(true);
                break;
            case STATE_PLAY://缓冲结束恢复播放
                if(null!=mPlayIcon) mPlayIcon.setImageResource(R.mipmap.ic_player_pause);
                break;
            case STATE_ON_PLAY://生命周期\暂停情况下恢复播放
                if(null!=mPlayIcon) mPlayIcon.setImageResource(R.mipmap.ic_player_pause);
                break;
            case STATE_PAUSE://人为暂停中
            case STATE_ON_PAUSE://生命周期暂停中
                if(null!=mPlayIcon) mPlayIcon.setImageResource(R.mipmap.ic_player_play);
                break;
            case STATE_COMPLETION://播放结束
                if(null!=mPlayIcon) mPlayIcon.setImageResource(R.mipmap.ic_player_play);
                resetProgressBar();
                break;
            case STATE_MOBILE://移动网络播放(如果设置允许4G播放则播放器内部不会回调此状态)
                break;
            case STATE_ERROR://播放失败
                if(null!=mPlayIcon) mPlayIcon.setImageResource(R.mipmap.ic_player_play);
                onReset();
                break;
            case STATE_DESTROY://播放器回收
                onDestroy();
                break;
        }
    }

    @Override
    public void onOrientation(int direction) {
        if(null==mController) return;
        if(IMediaPlayer.ORIENTATION_LANDSCAPE==direction){
            int margin22 = PlayerUtils.getInstance().dpToPxInt(22f);
            //横屏下处理标题栏和控制栏的左右两侧缩放
            mController.setPadding(margin22,0,margin22,0);
            show();
        }else {
            int margin5 = PlayerUtils.getInstance().dpToPxInt(5f);
            mController.setPadding(margin5, 0, margin5, 0);
            if (isNoimalScene()) {
                show();
            } else {
                //非常规场景不处理
                hide();
            }
        }
    }

    @Override
    public void onPlayerScene(int playerScene) {
        findViewById(R.id.controller_progress).setVisibility(isListPlayerScene() ?View.GONE:View.VISIBLE);
        //当播放器和控制器在专场播放、场景发生变化时，仅当在常规模式下并且正在播放才显示控制器
        if(isNoimalScene()){
            show();
            if(isPlaying()){
                showControl(false);
                reStartDelayedRunnable();
            }
        }else{
            hide();
        }
    }

    @Override
    public void onProgress(long currentDurtion, long totalDurtion) {
        if(null!=mSeekBar&&null!=mControlWrapper){
            if(null!=mProgressBar&&mProgressBar.getMax()==0){
                mProgressBar.setMax((int) (mControlWrapper.getPreViewTotalTime() >0? mControlWrapper.getPreViewTotalTime() :totalDurtion));
            }
            if(null!=mSeekBar){
                if(mSeekBar.getMax()<=0){//总进度总时长只更新一次,如果是虚拟的总时长,则在setViewTotalDuration中更新总时长
                    mSeekBar.setMax((int) (mControlWrapper.getPreViewTotalTime() >0? mControlWrapper.getPreViewTotalTime() :totalDurtion));
                    if(null!=mTotalDuration) mTotalDuration.setText(PlayerUtils.getInstance().stringForAudioTime(mControlWrapper.getPreViewTotalTime() >0? mControlWrapper.getPreViewTotalTime() :totalDurtion));
                }
                if(!isTouchSeekBar) mSeekBar.setProgress((int) currentDurtion);
            }
        }
    }

    @Override
    public void onBuffer(int bufferPercent) {
        if(null!= mControlWrapper){
            int percent = PlayerUtils.getInstance().formatBufferPercent(bufferPercent, mControlWrapper.getDuration());
            if(null!= mSeekBar&&mSeekBar.getSecondaryProgress()!=percent) {
                mSeekBar.setSecondaryProgress(percent);
            }
            if(null!= mProgressBar&&mProgressBar.getSecondaryProgress()!=percent) {
                mProgressBar.setSecondaryProgress(percent);
            }
        }
    }

    @Override
    public void onMute(boolean isMute) {
        ImageView muteImage = (ImageView) findViewById(R.id.controller_btn_mute);
        muteImage.setImageResource(isMute?R.mipmap.ic_player_mute_true:R.mipmap.ic_player_mute_false);
    }

    /**
     * 更新静音状态
     */
    private void updateMute(){
        if(null!= mControlWrapper){
            boolean soundMute = mControlWrapper.isSoundMute();
            ImageView muteImge = (ImageView) findViewById(R.id.controller_btn_mute);
            muteImge.setImageResource(soundMute?R.mipmap.ic_player_mute_true:R.mipmap.ic_player_mute_false);
        }
    }

    /**
     * 是否显示静音按钮
     * @param showSound 是否显示静音按钮,true:显示 false:隐藏
     */
    public void showSoundMute(boolean showSound){
        ImageView muteImage = (ImageView) findViewById(R.id.controller_btn_mute);
        muteImage.setVisibility(showSound?View.VISIBLE:View.GONE);
    }

    /**
     * 是否显示静音按钮
     * @param showSound 是否显示静音按钮,true:显示 false:隐藏
     * @param soundMute 是否静音,true:静音 false:系统原声
     */
    public void showSoundMute(boolean showSound,boolean soundMute){
        ImageView muteImage = (ImageView) findViewById(R.id.controller_btn_mute);
        muteImage.setVisibility(showSound?View.VISIBLE:View.GONE);
        if(null!=mControlWrapper) mControlWrapper.setSoundMute(soundMute);//UI状态将在onMute回调中处理
    }

    /**
     * 是否启用全屏按钮播放功能
     * @param enable true:启用 false:禁止 默认是开启的
     */
    public void enableFullScreen(boolean enable) {
        findViewById(R.id.controller_btn_fullscreen).setVisibility(enable?VISIBLE:GONE);
    }

    private void resetProgressBar(){
        if(null!=mProgressBar) {
            mProgressBar.setProgress(0);
            mProgressBar.setSecondaryProgress(0);
            mProgressBar.setMax(0);
        }
    }

    @Override
    public void onReset() {
        if(null!=mSeekBar) {
            mSeekBar.setProgress(0);
            mSeekBar.setSecondaryProgress(0);
            mSeekBar.setMax(0);
        }
        resetProgressBar();
        hideControl(false);
        if(null!=mTotalDuration) mTotalDuration.setText(PlayerUtils.getInstance().stringForAudioTime(0));
        if(null!=mCurrentDuration) mCurrentDuration.setText(PlayerUtils.getInstance().stringForAudioTime(0));
        if(null!=mPlayIcon) mPlayIcon.setImageResource(R.mipmap.ic_player_play);
    }
}