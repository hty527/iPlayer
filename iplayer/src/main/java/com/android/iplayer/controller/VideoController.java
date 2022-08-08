package com.android.iplayer.controller;

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.android.iplayer.R;
import com.android.iplayer.interfaces.IGestureControl;
import com.android.iplayer.manager.IVideoManager;
import com.android.iplayer.model.PlayerState;
import com.android.iplayer.utils.ILogger;
import com.android.iplayer.utils.PlayerUtils;
import com.android.iplayer.widget.ControllerStatusView;
import com.android.iplayer.widget.GesturePositionView;

/**
 * created by hty
 * 2022/6/28
 * Desc:默认的视频控制器弹窗
 * 功能菜单：控制栏和底部进度条二选一显示,播放中时允许点击显示\隐藏,列表模式下播放视频首帧渲染不显示控制器和标题栏,只显示底部播放进度条
 */
public class VideoController extends GestureController implements IGestureControl {

    public static final int SCENE_MOBILE     =1;//移动网络播放提示
    public static final int SCENE_COMPLETION =2;//试看结束
    public static final int SCENE_ERROR      =3;//播放失败
    private static final int MESSAGE_HIDE_CONTROLLER      = 100;//隐藏控制器
    private static final int MESSAGE_HIDE_LOCKER          = 101;//隐藏控制锁
    //播放按钮,控制器,标题栏,重新播放
    protected View mControllerPlay,mControllerController,mControllerTitle,mControllerReplay;
    private ImageView mControllerLocker;//控制锁
    private ControllerStatusView mControllerStatus;
    protected ProgressBar mControllerLoading;
    protected TextView mCurrentDuration,mTotalDuration;
    protected SeekBar mSeekBar;
    protected ProgressBar mProgressBar;//底部进度条
    //用户手指是否持续拖动中\是否播放(试看)完成\是否显示返回按钮(竖屏生效,默认不显示,横屏强制显示)\是否显示投屏按钮\是否显示悬浮窗按钮\是否显示菜单按钮
    protected boolean isTouchSeekBar,isCompletion,showBackBtn,showTv=false,showWindow=false,showMenu=false;
    protected long mPreViewTotalTime;//给用户看的预览总时长
    protected ImageView mPlayIcon;//左下角的迷你播放状态按钮
    private int mTitleOffset=0;//标题栏距离顶部偏移量
    private int MATION_DRAUTION=300;
    //小窗口模式交互控制器
    private VideoWindowController mWindowController;
    //手势UI交互
    private GesturePositionView mGesturePositionView;

    public VideoController(Context context) {
        super(context);
    }

    @Override
    public int getLayoutId() {
        return R.layout.player_video_controller;
    }

    @Override
    public void initViews() {
        super.initViews();
        OnClickListener onClickListener=new OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = view.getId();
                if (id == R.id.controller_title_back) {
                    if (isOrientationPortrait()) {
                        if (null != mControllerListener) mControllerListener.onBack();//竖屏回调给宿主界面
                    } else {
                        if(null!= mVideoPlayerControl) mVideoPlayerControl.quitFullScreen();//横屏回调给播放器做退出全屏处理
                    }
                } else if (id == R.id.controller_btn_fullscreen) {
                    if(null!=mVideoPlayerControl){
                        if(isOrientationPortrait()&&!mVideoPlayerControl.isWork()){//当播放器处于待命状态时不处理全屏逻辑
                            return;
                        }
                    }
                    mVideoPlayerControl.toggleFullScreen();//回调给播放器处理横\竖屏逻辑
                } else if (id == R.id.controller_play || id == R.id.controller_start || id == R.id.controller_replay) {
                    if (null != mVideoPlayerControl) mVideoPlayerControl.togglePlay();//回调给播放器
                } else if (id == R.id.controller_root_view) {
                    toggleController(false);
                } else if (id == R.id.controller_title_menu) {//功能菜单
                    if (null != mControllerListener) mControllerListener.onMenu();
                }else if (id == R.id.controller_title_tv) {//投屏
                    if(null!=mControllerListener) mControllerListener.onTv();
                }else if (id == R.id.controller_title_window) {//开启全局悬浮窗窗口播放
                    if (null != mControllerListener) mControllerListener.onGobalWindow();//回调给宿主界面
                }else if (id == R.id.controller_locker) {//屏幕锁
                    if(isLocked()){
                        setLocker(false);
                        mControllerLocker.setImageResource(R.mipmap.ic_player_locker_false);
                        Toast.makeText(getContext(),getResources().getString(R.string.player_locker_flase),Toast.LENGTH_SHORT).show();
                        //立刻恢复所有控制器
                        toggleController(false,true);
                    }else{
                        setLocker(true);
                        mControllerLocker.setImageResource(R.mipmap.ic_player_locker_true);
                        Toast.makeText(getContext(),getResources().getString(R.string.player_locker_true),Toast.LENGTH_SHORT).show();
                        //立刻屏蔽所有控制器屏蔽
                        toggleController(true);
                    }
                    delayedInvisibleLocker();
                }
            }
        };
        findViewById(R.id.controller_title_back).setOnClickListener(onClickListener);
        findViewById(R.id.controller_btn_fullscreen).setOnClickListener(onClickListener);
//        findViewById(R.id.controller_root_view).setOnClickListener(onClickListener);//启用手势识别器后不能消费点击事件了
        mControllerPlay = findViewById(R.id.controller_play);
        mControllerPlay.setOnClickListener(onClickListener);
        mControllerReplay = findViewById(R.id.controller_replay);
        mControllerReplay.setOnClickListener(onClickListener);
        mControllerLoading = findViewById(R.id.controller_loading);
        mControllerController = findViewById(R.id.controller_controller);
        mControllerTitle = findViewById(R.id.controller_title_view);
        mCurrentDuration = findViewById(R.id.controller_current_duration);
        mTotalDuration = findViewById(R.id.controller_total_duration);
        mPlayIcon = findViewById(R.id.controller_start);
        mControllerLocker = findViewById(R.id.controller_locker);
        mPlayIcon.setOnClickListener(onClickListener);
        mControllerLocker.setOnClickListener(onClickListener);
        findViewById(R.id.controller_title_tv).setOnClickListener(onClickListener);
        findViewById(R.id.controller_title_window).setOnClickListener(onClickListener);
        findViewById(R.id.controller_title_menu).setOnClickListener(onClickListener);
        //各种状态交互
        mControllerStatus = findViewById(R.id.controller_status);
        mControllerStatus.setOnStatusListener(new ControllerStatusView.OnStatusListener() {
            @Override
            public void onEvent(int event) {
                if(SCENE_MOBILE==event){//移动网络播放
                    IVideoManager.getInstance().setMobileNetwork(true);
                    if (null != mVideoPlayerControl) mVideoPlayerControl.togglePlay();
                }else if(SCENE_COMPLETION==event){//试看结束
                    if(null!= mControllerListener) mControllerListener.onCompletion();//回调给宿主界面
                }else if(SCENE_ERROR==event){//播放失败
                    if (null != mVideoPlayerControl) mVideoPlayerControl.togglePlay();
                }
            }
        });
        mProgressBar = findViewById(R.id.controller_bottom_progress);
        mSeekBar = findViewById(R.id.controller_seek_bar);
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
                removeDelayedRunnable(MESSAGE_HIDE_CONTROLLER);//取消定时隐藏任务
            }

            /**
             * 失去焦点-松手了
             * @param seekBar
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isTouchSeekBar=false;
                delayedInvisibleController();//开启定时隐藏任务
                //当controller_deblocking设置了点击时间，试看结束的拦截都无效
//                ILogger.d(TAG,"onStopTrackingTouch-->,isCompletion:"+isCompletion+",preViewTotalTime:"+mPreViewTotalTime);
                if(null!=mVideoPlayerControl){
                    if(isCompletion&& mPreViewTotalTime >0){//拦截是看结束,让用户解锁
                        if(null!= mVideoPlayerControl) mVideoPlayerControl.onCompletion();
                        return;
                    }
                    int seekBarProgress = seekBar.getProgress();
//                    ILogger.d(TAG,"onStopTrackingTouch-->seekBarProgress:"+seekBarProgress+",ViewTotalTime:"+ mPreViewTotalTime +",duration:"+ mVideoPlayerControl.getDurtion()+getOrientationStr());
                    if(mPreViewTotalTime >0){ //跳转至某处,如果滑动的时长超过真实的试看时长,则直接播放完成需要解锁
                        long durtion = mVideoPlayerControl.getDuration();
                        if(0==seekBarProgress){//重新从头开始播放
                            //改变UI为缓冲状态
                            onState(PlayerState.STATE_BUFFER,"seek");
                            mVideoPlayerControl.seekTo(0);
                        }else{
                            if(seekBarProgress>=durtion){//试看片段,需要解锁
                                mVideoPlayerControl.onCompletion();
                            }else{
                                //改变UI为缓冲状态
                                onState(PlayerState.STATE_BUFFER,"seek");
                                mVideoPlayerControl.seekTo(seekBarProgress);//试看片段内,允许跳转
                            }
                        }
                    }else{
                        //改变UI为缓冲状态
                        onState(PlayerState.STATE_BUFFER,"seek");
                        mVideoPlayerControl.seekTo(seekBarProgress);//真实时长,允许跳转
                    }
                }
            }
        });
        //手势交互控制
        mGesturePositionView = (GesturePositionView) findViewById(R.id.gesture_present_layout);
        setDoubleTapTogglePlayEnabled(true);//横屏竖屏状态下都允许双击开始\暂停播放
    }

    @Override
    public void onState(PlayerState state, String message) {
        ILogger.d(TAG,"onState-->state:"+state+getOrientationStr()+",message:"+message);
        removeDelayedRunnable(MESSAGE_HIDE_CONTROLLER);
        switch (state) {
            case STATE_RESET://初始状态\播放器还原重置
            case STATE_STOP://初始\停止
                onReset();
                break;
            case STATE_PREPARE://准备中
            case STATE_BUFFER://缓冲中
                if(null!=mPlayIcon) mPlayIcon.setImageResource(R.mipmap.ic_player_play);
                if(null!=mControllerController&&mControllerController.getVisibility()==VISIBLE){//如果开始缓冲的时候控制器是显示的
                    if(null!=mControllerLoading) mControllerLoading.setVisibility(VISIBLE);
                    delayedInvisibleController();
                }else{
                    changedUIState(View.VISIBLE, View.GONE, View.GONE, View.GONE,View.GONE,View.GONE,0);
                }
                break;
            case STATE_START://首次播放
                if(itemPlayerMode){//列表播放模式
                    changedUIState(View.GONE, View.GONE, View.GONE,View.GONE,View.GONE,View.GONE,0);
                    if(null!=mProgressBar) mProgressBar.setVisibility(VISIBLE);
                }else if(isWindowProperty){//窗口播放模式（这里可以不改变,窗口整体控制器是不可见的）
                    changedUIState(View.GONE, View.GONE, View.GONE,View.GONE,View.GONE,View.GONE,0);
                    if(null!=mProgressBar) mProgressBar.setVisibility(VISIBLE);//悬浮窗口的底部进度条必须显示
                }else{//正常模式
                    changedUIState(View.GONE, View.GONE, View.VISIBLE,View.VISIBLE,View.GONE,View.GONE,0);
                }
                if(null!=mPlayIcon) mPlayIcon.setImageResource(R.mipmap.ic_player_pause);
                if(null!=mControllerListener) mControllerListener.onStart();
                break;
            case STATE_PLAY://缓冲结束恢复播放
                if(null!=mPlayIcon) mPlayIcon.setImageResource(R.mipmap.ic_player_pause);
                if(null!=mControllerController&&mControllerController.getVisibility()==VISIBLE){//如果缓冲结束时候控制器是显示的,只改变缓冲状态,并且定时隐藏控制器
                    if(null!=mControllerLoading) mControllerLoading.setVisibility(GONE);
                    delayedInvisibleController();
                }else{
                    if(isWindowProperty){//窗口模式控制器操作栏是不可见的
                        changedUIState(View.GONE, View.GONE, View.GONE,View.GONE,View.GONE,View.GONE,0);
                    }else{
                        changedUIState(View.GONE, View.GONE, View.GONE,View.GONE,View.GONE,View.GONE,0);
                    }
                }
                break;
            case STATE_ON_PLAY://生命周期\暂停情况下恢复播放
                if(null!=mPlayIcon) mPlayIcon.setImageResource(R.mipmap.ic_player_pause);
                if(isWindowProperty){//窗口模式控制器操作栏是不可见的
                    changedUIState(View.GONE, View.GONE, View.GONE,View.GONE,View.GONE,View.GONE,0);
                }else if(itemPlayerMode){//列表播放模式
                    changedUIState(View.GONE, View.GONE, View.VISIBLE,View.GONE,View.GONE,View.GONE,0);
                }else{
                    changedUIState(View.GONE, View.GONE, View.VISIBLE,View.VISIBLE,View.GONE,View.GONE,0);
                }
                break;
            case STATE_PAUSE://人为暂停中
            case STATE_ON_PAUSE://生命周期暂停中
                if(null!=mPlayIcon) mPlayIcon.setImageResource(R.mipmap.ic_player_play);
                if(itemPlayerMode){
                    changedUIState(View.GONE, View.VISIBLE, View.VISIBLE,View.GONE,View.GONE,View.GONE,0);
                }else{
                    changedUIState(View.GONE, View.VISIBLE, View.VISIBLE,View.VISIBLE,View.GONE,View.GONE,0);
                }
                break;
            case STATE_COMPLETION://播放结束
                isCompletion=true;
                if(null!=mProgressBar) mProgressBar.setVisibility(GONE);
                if(mPreViewTotalTime >0){
                    changedUIState(View.GONE, View.GONE, View.GONE,View.GONE,View.GONE,View.VISIBLE,SCENE_COMPLETION);
                }else{
                    changedUIState(View.GONE, View.GONE, View.GONE,View.GONE,View.VISIBLE,View.GONE,0);
                }
                if(null!=mPlayIcon) mPlayIcon.setImageResource(R.mipmap.ic_player_play);
                if(isOrientationPortrait()&&null!=mControllerListener){
                    mControllerListener.onCompletion();
                }
                break;
            case STATE_MOBILE://移动网络播放(如果设置允许4G播放则播放器内部不会回调此状态)
                changedUIState(View.GONE, View.GONE, View.GONE,View.GONE,View.GONE,View.VISIBLE,SCENE_MOBILE);
                break;
            case STATE_ERROR://播放失败
                changedUIState(View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.VISIBLE,SCENE_ERROR,message);
                if(null!=mPlayIcon) mPlayIcon.setImageResource(R.mipmap.ic_player_play);
                break;
            case STATE_DESTROY://播放器回收
                onDestroy();
                break;
        }
    }

    @Override
    public void onProgress(final long currentDurtion, final long totalDurtion, int bufferPercent) {
        if(null!=mSeekBar){
            if(null!=mProgressBar&&mProgressBar.getMax()==0){
                mProgressBar.setMax((int) (mPreViewTotalTime >0? mPreViewTotalTime :totalDurtion));
            }
            if(null!=mSeekBar){
                if(mSeekBar.getMax()<=0){//总进度总时长只更新一次,如果是虚拟的总时长,则在setViewTotalDuration中更新总时长
                    mSeekBar.setMax((int) (mPreViewTotalTime >0? mPreViewTotalTime :totalDurtion));
                    if(null!=mTotalDuration) mTotalDuration.setText(PlayerUtils.getInstance().stringForAudioTime(mPreViewTotalTime >0? mPreViewTotalTime :totalDurtion));
                }
                if(!isTouchSeekBar) mSeekBar.setProgress((int) currentDurtion);
            }
        }
    }

    /**
     * 缓冲进度
     * @param bufferPercent 缓冲进度 主线程回调,单位:百分比
     */
    @Override
    public void onBuffer(int bufferPercent) {
//        ILogger.d(TAG,"onBuffer-->"+bufferPercent);
        if(null!=mVideoPlayerControl){
            int percent = PlayerUtils.getInstance().formatBufferPercent(bufferPercent, mVideoPlayerControl.getDuration());
            if(null!= mSeekBar&&mSeekBar.getSecondaryProgress()!=percent) {
                mSeekBar.setSecondaryProgress(percent);
            }
            if(null!= mProgressBar&&mProgressBar.getSecondaryProgress()!=percent) {
                mProgressBar.setSecondaryProgress(percent);
            }
        }
    }

    /**
     * 竖屏状态下,如果用户设置返回按钮可见仅显示返回按钮,切换到横屏模式下播放时初始都不显示
     * @param orientation 更新控制器方向状态 0:竖屏 1:横屏
     */
    @Override
    public void setScreenOrientation(int orientation) {
        super.setScreenOrientation(orientation);
        ILogger.d(TAG,"setScreenOrientation-->"+getOrientationStr());
        findViewById(R.id.controller_title).setVisibility(isOrientationPortrait()?View.GONE:View.VISIBLE);
        LinearLayout titleBar = (LinearLayout) findViewById(R.id.controller_title_bar);
        LinearLayout controllerBar = (LinearLayout) findViewById(R.id.controller_controller);
        int margin = PlayerUtils.getInstance().dpToPxInt(22f);
        if(isOrientationPortrait()){
            findViewById(R.id.controller_title_tv).setVisibility(showTv?View.VISIBLE:View.GONE);
            findViewById(R.id.controller_title_window).setVisibility(showWindow?View.VISIBLE:View.GONE);
            findViewById(R.id.controller_title_menu).setVisibility(showMenu?View.VISIBLE:View.GONE);
            //控制锁不可用
            toggleLocker(true);
            setLocker(false);
            if(null!=mGesturePositionView) mGesturePositionView.enterPortrait();
            //标题栏间距处理
            findViewById(R.id.controller_title_back).setVisibility(showBackBtn?View.VISIBLE:View.GONE);
            findViewById(R.id.controller_title_margin).getLayoutParams().height=mTitleOffset;
            //竖屏下处理标题栏和控制栏的左右两侧缩放
            titleBar.setPadding(0,0,0,0);
            controllerBar.setPadding(0,0,0,0);
        }else{
            findViewById(R.id.controller_title_tv).setVisibility(View.GONE);
            findViewById(R.id.controller_title_window).setVisibility(View.GONE);
            findViewById(R.id.controller_title_menu).setVisibility(View.GONE);
            //标题栏间距处理
            findViewById(R.id.controller_title_back).setVisibility(View.VISIBLE);
            findViewById(R.id.controller_title_margin).getLayoutParams().height=0;
            if(null!=mGesturePositionView) mGesturePositionView.enterLandscape();
            //横屏下处理标题栏和控制栏的左右两侧缩放
            titleBar.setPadding(margin,0,margin,0);
            controllerBar.setPadding(margin,0,margin,0);
            toggleLocker(false);
        }
        toggleController(true);//控制器不可见
    }

    /**
     * 当切换至小窗口模式播放,取消可能存在的定时器隐藏控制器任务,强制隐藏控制器
     * @param isWindowProperty 控制器是否处于窗口模式中 true:当前窗口属性显示 false:非窗口模式。当处于创库模式时，所有控制器都处于不可见状态,所有控制器手势都将被window播放器截获
     * @param isGlobalWindow true:全局悬浮窗窗口|画中画模式 false:Activity局部悬浮窗窗口模式
     */
    @Override
    public void setWindowProperty(boolean isWindowProperty, boolean isGlobalWindow) {
//        ILogger.d(TAG,"setWindowProperty-->isWindowProperty:"+isWindowProperty);
        removeDelayedRunnable(MESSAGE_HIDE_CONTROLLER);
        if(null==mControllerController) return;
        if(isWindowProperty){
            mControllerController.setVisibility(GONE);
        }
        //改变控制器状态，窗口模式隐藏所有控制器,只显示底部进度条
        findViewById(R.id.controller_container).setVisibility(isWindowProperty?GONE:VISIBLE);
        if(null!=mProgressBar){
            if(isWindowProperty){
                mProgressBar.setVisibility(VISIBLE);
            }else{
                mProgressBar.setVisibility(mControllerController.getVisibility()==VISIBLE?GONE:VISIBLE);//底部进度条总是和控制器是相反的
            }
        }
        //添加窗口控制器
        if(isWindowProperty){
            PlayerUtils.getInstance().removeViewFromParent(mWindowController);
            removeController(mWindowController);
            if(null!=mWindowController) mWindowController.onReset();
            mWindowController=new VideoWindowController(getContext());
            mWindowController.setWindowProperty(true,isGlobalWindow);
            addController(mWindowController);
        }else{
            if(null!=mWindowController){
                mWindowController.onReset();
                removeController(mWindowController);
                mWindowController=null;
            }
        }
    }

    /**
     * 改变UI状态
     * @param loadingView 加载状态
     * @param playerBtn 播放按钮状态
     * @param controllerLayout 控制器状态
     * @param titleBar 标题栏菜单栏状态
     * @param replayBtn 重新播放
     * @param statuView 移动网络播放\试看结束\播放失败 状态
     * @param scene 状态场景类型,提供给回调判断
     */
    private void changedUIState(int loadingView, int playerBtn, int controllerLayout,int titleBar,int replayBtn,int statuView,int scene) {
        changedUIState(loadingView,playerBtn,controllerLayout,titleBar,replayBtn,statuView,scene,null);
    }

    /**
     * 改变UI状态
     * @param loadingView 加载状态
     * @param playerBtn 播放按钮状态
     * @param controllerLayout 控制器状态
     * @param titleBar 标题栏菜单栏状态
     * @param replayBtn 重新播放
     * @param statuView 移动网络播放\试看结束\播放失败 状态
     * @param scene 状态场景类型,提供给回调判断
     * @param errorMessage 当播放错误或scene==SCENE_ERROR时不为空
     */
    private void changedUIState(int loadingView, int playerBtn, int controllerLayout,int titleBar,int replayBtn,int statuView,int scene,String errorMessage) {
        if(null!=mControllerLoading) mControllerLoading.setVisibility(loadingView);
        if(null!=mControllerPlay) mControllerPlay.setVisibility(playerBtn);
        if(null!=mControllerController) mControllerController.setVisibility(controllerLayout);
        if(null!=mControllerReplay) mControllerReplay.setVisibility(replayBtn);
        if(null!=mControllerTitle) mControllerTitle.setVisibility(titleBar);
        if(controllerLayout==VISIBLE&&null!=mProgressBar&&mProgressBar.getVisibility()==VISIBLE&&!isWindowProperty){//非窗口播放模式下不禁止底部进度条
            mProgressBar.setVisibility(GONE);
        }
        if(null!=mControllerStatus){
            mControllerStatus.setVisibility(statuView);
            if(scene>0) mControllerStatus.setScene(scene,errorMessage);//仅当需要处理状态场景时才更新交互UI
        }
        if(controllerLayout==View.VISIBLE){
            delayedInvisibleController();//当控制器显示时,启动定时关闭控制器
        }
    }

    /**
     显示\隐藏视图控制器
     * 横屏标题栏页参与显示隐藏,竖屏不处理标题栏
     * @param isHide 标题栏和菜单控制器是否强制隐藏
     */
    private void toggleController(boolean isHide){
        toggleController(isHide,false);
    }

    /**
     显示\隐藏视图控制器
     * 横屏标题栏页参与显示隐藏,竖屏不处理标题栏
     * @param isHide 标题栏和菜单控制器是否强制隐藏
     * @param isShow 标题栏和菜单控制器是否强制显示
     */
    private void toggleController(boolean isHide,boolean isShow) {
        removeDelayedRunnable(0);
//        ILogger.d(TAG,"toggleController-->isHide:"+isHide+",isShow:"+isShow);
        if(null!=mControllerController&&null!=mControllerTitle){
            if(isHide){//强制隐藏
                mControllerController.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                mControllerTitle.setVisibility(View.GONE);
                return;
            }
            //强制显示全部控制器
            if(isShow){
                PlayerUtils.getInstance().startAlphaAnimatioFrom(mControllerController, MATION_DRAUTION, false, null);
                if(!itemPlayerMode){//列表模式下menu栏不可用
                    PlayerUtils.getInstance().startAlphaAnimatioFrom(mControllerTitle, MATION_DRAUTION, false, null);
                }
                delayedInvisibleController();
                mProgressBar.setVisibility(View.GONE);
                return;
            }
            boolean controllerShow=false;
            //控制栏显示中
            if(mControllerController.getVisibility()==View.VISIBLE){
                controllerShow=true;
                PlayerUtils.getInstance().startAlphaAnimatioTo(mControllerController, MATION_DRAUTION, false, new PlayerUtils.OnAnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if(null!=mControllerController) mControllerController.setVisibility(GONE);
                        mProgressBar.setVisibility(View.VISIBLE);
                    }
                });
            }else{
                mProgressBar.setVisibility(View.GONE);
                PlayerUtils.getInstance().startAlphaAnimatioFrom(mControllerController, MATION_DRAUTION, false, null);
            }
            if(controllerShow){//标题栏跟随控制器栏
                PlayerUtils.getInstance().startAlphaAnimatioTo(mControllerTitle, MATION_DRAUTION, false, new PlayerUtils.OnAnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if(null!=mControllerTitle) mControllerTitle.setVisibility(GONE);
                    }
                });
            }else{
                if(isOrientationPortrait()){
                    if(!itemPlayerMode){//列表模式下menu栏不可用
                        PlayerUtils.getInstance().startAlphaAnimatioFrom(mControllerTitle, MATION_DRAUTION, false, null);
                    }
                }else{
                    PlayerUtils.getInstance().startAlphaAnimatioFrom(mControllerTitle, MATION_DRAUTION, false, null);
                }
            }
            //控制锁
            if(null!=mControllerLocker&&!isOrientationPortrait()){
                if(controllerShow){
                    PlayerUtils.getInstance().startAlphaAnimatioTo(mControllerLocker, MATION_DRAUTION, false, new PlayerUtils.OnAnimationListener() {
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            if(null!=mControllerLocker) mControllerLocker.setVisibility(GONE);
                        }
                    });
                }else{
                    if(mControllerLocker.getVisibility()!=View.VISIBLE){
                        PlayerUtils.getInstance().startAlphaAnimatioFrom(mControllerLocker, MATION_DRAUTION, false, null);
                    }
                }
            }
            //所有交互处理完成启动定时隐藏控制器任务
            if(!controllerShow){
                delayedInvisibleController();
                if(!isOrientationPortrait()){
                    delayedInvisibleLocker();
                }
            }
        }
    }

    /**
     * 手势交互时取消所有控制器
     */
    private void hideController(){
        if(null!=mControllerController&&mControllerController.getVisibility()==View.VISIBLE){
            removeDelayedRunnable(0);
            PlayerUtils.getInstance().startAlphaAnimatioTo(mControllerController, MATION_DRAUTION, false, new PlayerUtils.OnAnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    if(null!=mControllerController) mControllerController.setVisibility(GONE);
                    if(null!=mProgressBar) mProgressBar.setVisibility(View.VISIBLE);
                }
            });
            PlayerUtils.getInstance().startAlphaAnimatioTo(mControllerTitle, MATION_DRAUTION, false, new PlayerUtils.OnAnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    if(null!=mControllerTitle) mControllerTitle.setVisibility(GONE);
                }
            });

        }
    }

    /**
     显示\隐藏控制锁
     * @param isHide 标题栏和菜单控制器是否强制隐藏
     */
    private void toggleLocker(boolean isHide) {
        removeDelayedRunnable(MESSAGE_HIDE_LOCKER);
//        ILogger.d(TAG,"toggleLocker-->isHide:"+isHide);
        if(null!=mControllerLocker){
            if(isHide){//强制隐藏
                mControllerLocker.setVisibility(View.GONE);
                return;
            }
            boolean controllerShow=false;
            //控制锁显示中
            if(mControllerLocker.getVisibility()==View.VISIBLE){
                PlayerUtils.getInstance().startAlphaAnimatioTo(mControllerLocker, MATION_DRAUTION, false, new PlayerUtils.OnAnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if(null!=mControllerLocker) mControllerLocker.setVisibility(GONE);
                    }
                });
            }else{
                controllerShow=true;
                PlayerUtils.getInstance().startAlphaAnimatioFrom(mControllerLocker, MATION_DRAUTION, false, null);
            }
            //所有交互处理完成启动定时隐藏控制器任务
            if(controllerShow){
                delayedInvisibleLocker();
            }
        }
    }

    /**
     * 使用这个Handel替代getHandel(),避免多播放器同时工作的相互影响
     */
    private ExHandel mExHandel=new ExHandel(Looper.getMainLooper()){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            ILogger.d(TAG,"handleMessage-->whit:"+msg.what);
            if(null!=msg&&MESSAGE_HIDE_CONTROLLER==msg.what){
                if(null!=mControllerController&&mControllerController.getVisibility()==View.VISIBLE){
                    PlayerUtils.getInstance().startAlphaAnimatioTo(mControllerController, MATION_DRAUTION, false, new PlayerUtils.OnAnimationListener() {
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            if(null!=mControllerController) mControllerController.setVisibility(GONE);
                            if(null!=mProgressBar) mProgressBar.setVisibility(VISIBLE);
                        }
                    });
                }
                if(null!=mControllerTitle&&mControllerTitle.getVisibility()==View.VISIBLE){
                    PlayerUtils.getInstance().startAlphaAnimatioTo(mControllerTitle, MATION_DRAUTION, false, new PlayerUtils.OnAnimationListener() {
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            if(null!=mControllerTitle) mControllerTitle.setVisibility(GONE);
                        }
                    });
                }
            }if(null!=msg&&MESSAGE_HIDE_LOCKER==msg.what){
                if(null!=mControllerLocker){
                    PlayerUtils.getInstance().startAlphaAnimatioTo(mControllerLocker, MATION_DRAUTION, false, new PlayerUtils.OnAnimationListener() {
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            if(null!=mControllerLocker) mControllerLocker.setVisibility(GONE);
                        }
                    });
                }
            }
        }
    };

    /**
     * 取消控制器隐藏延时任务
     * @param message
     */
    private void removeDelayedRunnable(int message){
//        ILogger.d(TAG,"removeDelayedRunnable-->message:"+message);
        if(null!=mExHandel){
            if(0==message){
                mExHandel.removeCallbacksAndMessages(null);
            }else{
                mExHandel.removeMessages(message);
            }
        }
    }

    /**
     * 启动延时隐藏控制器任务
     */
    private void delayedInvisibleController() {
//        ILogger.d(TAG,"delayedInvisibleController-->");
        try {
            if(null!=mControllerController&&null!=mExHandel){
                Message obtain = Message.obtain();
                obtain.what=MESSAGE_HIDE_CONTROLLER;
                mExHandel.sendMessageDelayed(obtain,5000);
            }
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    /**
     * 启动延时隐藏控制锁
     */
    private void delayedInvisibleLocker() {
//        ILogger.d(TAG,"delayedInvisibleLocker-->");
        try {
            if(null!=mControllerLocker&&null!=mExHandel){
                Message obtain = Message.obtain();
                obtain.what=MESSAGE_HIDE_LOCKER;
                mExHandel.sendMessageDelayed(obtain,5000);
            }
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    /**
     * 重置内部状态
     */
    private void reset(){
//        ILogger.d(TAG,"reset-->"+getOrientationStr());
        removeDelayedRunnable(0);
        if(null!=mSeekBar) {
            mSeekBar.setProgress(0);
            mSeekBar.setSecondaryProgress(0);
            mSeekBar.setMax(0);
        }
        if(null!=mProgressBar) {
            mProgressBar.setProgress(0);
            mProgressBar.setSecondaryProgress(0);
            mProgressBar.setMax(0);
        }
        if(null!=mExHandel) mExHandel.removeCallbacksAndMessages(null);
        if(null!=mTotalDuration) mTotalDuration.setText(PlayerUtils.getInstance().stringForAudioTime(0));
        if(null!=mPlayIcon) mPlayIcon.setImageResource(R.mipmap.ic_player_play);
    }

    @Override
    public void setVideoTitle(String videoTitle) {
        ((TextView) findViewById(R.id.controller_title)).setText(videoTitle);
    }

    /**
     * 设置给用户看的虚拟的视频总时长
     * @param totalDuration 单位：秒
     */
    @Override
    public void setPreViewTotalDuration(String totalDuration) {
        int duration = PlayerUtils.getInstance().parseInt(totalDuration);
        if(duration>0) setPreViewTotalDuration(duration*1000);
    }

    /**
     * @param itemPlayerMode 是否处于列表播放模式(需要在开始播放之前设置),列表播放模式下首次渲染不会显示控制器,否则首次渲染会显示控制器 true:处于列表播放模式 false:不处于列表播放模式
     */
    @Override
    public void setListItemPlayerMode(boolean itemPlayerMode) {
        super.setListItemPlayerMode(itemPlayerMode);
    }

    /**
     * 设置给用户看的虚拟的视频总时长
     * @param totalDuration 单位：毫秒
     */
    public void setPreViewTotalDuration(long totalDuration){
        if(totalDuration>0){
            try {
                this.mPreViewTotalTime = totalDuration;
            }catch (Throwable e){
                e.printStackTrace();
            }finally {
                if(null!=mTotalDuration) mTotalDuration.setText(PlayerUtils.getInstance().stringForAudioTime(mPreViewTotalTime));
            }
        }
    }

    @Override
    public void setTitleTopOffset(int topOffset) {
        if(isOrientationPortrait()){
            this.mTitleOffset=topOffset;//只记录用户设置的偏移量,竖屏的不记录,竖屏交给宿主设置
        }
        findViewById(R.id.controller_title_margin).getLayoutParams().height=mTitleOffset;
    }

    @Override
    protected void enterPipWindow() {
        findViewById(R.id.controller_root_view).setVisibility(View.GONE);
    }

    @Override
    protected void quitPipWindow() {
        findViewById(R.id.controller_root_view).setVisibility(View.VISIBLE);
    }

    /**
     * 使得否显示返回按钮(竖屏),横屏模式下都一定会显示
     * @param showBackBtn true:显示返回按钮 false:隐藏返回按钮
     */
    public void showBackBtn(boolean showBackBtn){
        this.showBackBtn=showBackBtn;
        if(isOrientationPortrait()){
            findViewById(R.id.controller_title_back).setVisibility(showBackBtn?View.VISIBLE:View.GONE);
        }
    }

    /**
     * 是否显示菜单栏
     * @param tv 投屏按钮是否显示
     * @param window 悬浮窗按钮是否显示
     * @param menu 菜单按钮是否显示
     */
    public void showMenus(boolean tv,boolean window,boolean menu) {
        this.showTv=tv;
        this.showWindow=window;
        this.showMenu=menu;
        if(isOrientationPortrait()){
            findViewById(R.id.controller_title_tv).setVisibility(tv?View.VISIBLE:View.GONE);
            findViewById(R.id.controller_title_window).setVisibility(window?View.VISIBLE:View.GONE);
            findViewById(R.id.controller_title_menu).setVisibility(menu?View.VISIBLE:View.GONE);
        }
    }

    /**
     * 是否启用全屏按钮播放功能
     * @param enable true:启用 false:禁止 默认是开启的
     */
    public void enableFullScreen(boolean enable) {
        findViewById(R.id.controller_btn_fullscreen).setVisibility(enable?VISIBLE:GONE);
    }

    public ImageView getTitleBack() {
        return ((ImageView) findViewById(R.id.controller_title_back));
    }

    public ImageView getPlayerIcon() {
        return ((ImageView) findViewById(R.id.controller_play_icon));
    }

    public TextView getTitleText() {
        return ((TextView) findViewById(R.id.controller_title));
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onReset() {
//        ILogger.d(TAG,"onReset-->"+getOrientationStr());
        reset();
        changedUIState(View.GONE,View.VISIBLE,View.GONE,View.GONE,View.GONE,View.GONE,0);
    }

    @Override
    public void onDestroy() {
//        ILogger.d(TAG,"onDestroy-->"+getOrientationStr());
        removeAllController();
        reset();
        itemPlayerMode =false;showBackBtn=false;showTv=false;showWindow=false;showMenu=false;
        changedUIState(View.GONE,View.VISIBLE,View.GONE,View.GONE,View.GONE,View.GONE,0);
    }

    //============================================手势交互============================================

    @Override
    public void onSingleTap() {
        if(PlayerUtils.getInstance().isFastClick(500)){
            if(isLocked()){
                toggleLocker(false);
            }else{
                toggleController(false);
            }
        }
    }

    @Override
    public void onDoubleTap() {
        if(!isLocked()){
            if (null != mVideoPlayerControl) mVideoPlayerControl.togglePlay();//回调给播放器
        }
    }

    @Override
    public void onStartSlide() {
        if(null!=mGesturePositionView){
            hideController();
            mGesturePositionView.onStartSlide();
        }
    }

    @Override
    public void onStopSlide() {
        if(null!=mGesturePositionView) mGesturePositionView.onStopSlide();
    }

    @Override
    public void onPositionChange(int slidePosition, int currentPosition, int duration) {
        if(null!=mGesturePositionView) mGesturePositionView.onPositionChange(slidePosition,currentPosition,duration);
    }

    @Override
    public void onBrightnessChange(int percent) {
        if(null!=mGesturePositionView) mGesturePositionView.onBrightnessChange(percent);
    }

    @Override
    public void onVolumeChange(int percent) {
        if(null!=mGesturePositionView) mGesturePositionView.onVolumeChange(percent);
    }
}