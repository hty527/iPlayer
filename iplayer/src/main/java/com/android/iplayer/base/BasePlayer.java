package com.android.iplayer.base;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.android.iplayer.R;
import com.android.iplayer.interfaces.IBasePlayer;
import com.android.iplayer.interfaces.IPlayerControl;
import com.android.iplayer.interfaces.IRenderView;
import com.android.iplayer.listener.OnPlayerEventListener;
import com.android.iplayer.listener.OnWindowActionListener;
import com.android.iplayer.manager.IVideoManager;
import com.android.iplayer.manager.IWindowManager;
import com.android.iplayer.media.IMediaPlayer;
import com.android.iplayer.media.IVideoPlayer;
import com.android.iplayer.model.PlayerState;
import com.android.iplayer.utils.ILogger;
import com.android.iplayer.utils.PlayerUtils;
import com.android.iplayer.widget.view.ScreenOrientationRotate;
import com.android.iplayer.widget.view.WindowPlayerFloatView;
import java.io.File;

/**
 * Created by hty
 * 2022/6/28
 * Desc:一个默认没有UI交互的播放器基类
 * 1、可调用{@link #setController(BaseController)}来绑定一个UI视图交互控制器
 * 2、如需实现自定义视频解码器，则需注册{@link #setOnPlayerActionListener(OnPlayerEventListener)}监听器，实现createMediaPlayer方法来创建一个解码器。每个视频播放任务都将实例化一个createMediaPlayer解码器
 * 3、播放器支持在任意界面和位置直接开启全屏、Activity级别悬浮窗、全局悬浮窗 播放，请阅读IPlayerControl接口内的方法实现
 * 4、如需支持多播放器同时播放，则需要在开始播放前调用IVideoManager.getInstance().setInterceptTAudioFocus(true);
 * 5、特别注意：mParentContext：为什么会有这个变量？当播放从一个Activity跳转到另一个Activity转场衔接播放、从全局悬浮窗到Activity转场衔接播放时，播放器的宿主Activity发生了变化。此时播放器内部的startFullScreen和手势缩放控制屏幕亮度等或其它和Activity相关功能都将会失效。
 *    所以需要在转场后调用{@link #setParentContext(Context)}(context传入当前Activity的上下文)、恢复转场时来调用setParentContext(null)置空临时上下文。
 */
public abstract class BasePlayer extends FrameLayout implements IPlayerControl, IBasePlayer, ScreenOrientationRotate.OnDisplayOrientationListener {

    protected static final String TAG = BasePlayer.class.getSimpleName();
    private BaseController mController;//视图控制器
    protected OnPlayerEventListener mOnPlayerActionListener;//播放器事件监听器(宿主调用)
    private int mScreenOrientation= IMediaPlayer.ORIENTATION_PORTRAIT;//当前播放器方向
    private String mDataSource;//播放地址
    private AssetFileDescriptor mAssetsSource;//Assetss资产目录下的文件地址
    private ViewGroup mParent;//自己的宿主
    private int[] mPlayerParams;//自己的宽高属性和位于父容器的层级位置
    private IVideoPlayer mIVideoPlayer;
    private boolean mIsActivityWindow,mIsGlobalWindow,mContinuityPlay,mRestoreDirection=true,mLandscapeWindowTranslucent;//是否开启了Activity级别悬浮窗\是否开启了全局悬浮窗\是否开启了连续播放模式\当播放器在横屏状态下收到播放完成事件时是否自动还原到竖屏状态\横屏状态下是否启用沉浸式全屏
    private Context mParentContext;//临时的上下文,播放器内部会优先使用这个上下文来获取当前的Activity.业务方便开启转场、全局悬浮窗后设置此上下文。在Activity销毁时置空此上下文
    private ScreenOrientationRotate mOrientationRotate;//屏幕方向监听
    private int mDisplayLastOrientation;//设备最近一次的屏幕方向

    public BasePlayer(Context context) {
        this(context,null);
    }

    public BasePlayer(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BasePlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context,R.layout.player_base_video,this);
        //将自己与播放器解码模块绑定
        mIVideoPlayer = new IVideoPlayer();
        mIVideoPlayer.attachPlayer(this);
        initViews();
        mOrientationRotate = new ScreenOrientationRotate(getTargetContext());
        mOrientationRotate.setOnDisplayOrientationListener(this);
        mOrientationRotate.disable();//默认关闭屏幕角度变化监听
    }

    protected abstract void initViews();

    /**
     * 改变控制器方向
     * @param orientation
     */
    private void setScreenOrientation(int orientation) {
        this.mScreenOrientation=orientation;
        if(null!= mController) mController.onScreenOrientation(orientation);
    }

    /**
     * 改变播放器窗口属性
     * @param isActivityWindow true:Activity窗口模式 false:非Activity窗口模式
     * @param isGlobalWindow true:全局悬浮窗窗口 false:非全局悬浮窗窗口模式
     */
    private void setWindowPropertyPlayer(boolean isActivityWindow,boolean isGlobalWindow) {
        this.mIsActivityWindow =isActivityWindow;
        this.mIsGlobalWindow=isGlobalWindow;
        if(null!= mController) mController.setWindowPropertyPlayer(isActivityWindow,isGlobalWindow);
    }

    public boolean isActivityWindow() {
        return mIsActivityWindow;
    }

    public boolean isGlobalWindow() {
        return mIsGlobalWindow;
    }

    /**
     * 隐藏控制栏和导航栏
     * @param decorView
     */
    private void hideSystemBar(ViewGroup decorView) {
        Activity activity = PlayerUtils.getInstance().getActivity(getTargetContext());
        if(null!=decorView&&null!=activity&&!activity.isFinishing()){
            //全屏模式下是否启用全屏沉浸样式，也可以给Activity的style添加沉浸属性：<item name="android:windowLayoutInDisplayCutoutMode" tools:ignore="NewApi">shortEdges</item>
            if(mLandscapeWindowTranslucent){
                WindowManager.LayoutParams attributes = activity.getWindow().getAttributes();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    attributes.layoutInDisplayCutoutMode=WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                }
            }
            int uiOptions = decorView.getSystemUiVisibility();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            decorView.setSystemUiVisibility(uiOptions);
            activity.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    /**
     * 还原原有的控制栏和导航栏设置
     * @param decorView
     */
    private void showSysBar(ViewGroup decorView) {
        Activity activity = PlayerUtils.getInstance().getActivity(getTargetContext());
        if(null!=activity&&!activity.isFinishing()){
            int uiOptions = decorView.getSystemUiVisibility();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                uiOptions &= ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                uiOptions &= ~View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            decorView.setSystemUiVisibility(uiOptions);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private ViewGroup getDecorView(){
        Activity activity = PlayerUtils.getInstance().getActivity(getTargetContext());
        if(null!=activity&&!activity.isFinishing()){
            ViewGroup viewGroup = (ViewGroup) activity.getWindow().getDecorView();
            return viewGroup;
        }
        return null;
    }

    private Context getTargetContext(){
        if(null!= getParentContext()){
            return getParentContext();
        }
        return getContext();
    }

    private Object getDataSource(){
        if(!TextUtils.isEmpty(mDataSource)){
            return mDataSource;
        }
        if(null!=mAssetsSource){
            return mAssetsSource;
        }
        return null;
    }

    /**
     * 用户是否开启了“自动旋转”
     * @return true:开启 false:已禁止
     */
    private boolean isRevolveSetting(){
        int autoRevolve=1;
        try {
            autoRevolve = Settings.System.getInt(getContext().getContentResolver(), Settings.System.ACCELEROMETER_ROTATION);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 1==autoRevolve;
    }

    /**
     * 用户竖屏垂直0°拿着手机，竖屏
     * @param activity
     */
    private void onOrientationPortrait(Activity activity) {
        if(isRevolveSetting()&&isPlaying()&&mScreenOrientation!=IMediaPlayer.ORIENTATION_PORTRAIT){
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            quitFullScreen();
        }
    }

    /**
     * 用户朝左270°拿着手机，横屏
     * @param activity
     */
    private void onOrientationLandscape(Activity activity) {
        if(isRevolveSetting()&&isPlaying()&&mScreenOrientation!=IMediaPlayer.ORIENTATION_LANDSCAPE){
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            startFullScreen();
        }
    }

    /**
     * 用户朝右90°拿着手机，反向横屏,仅当系统开启自动旋转+正在播放生效
     * @param activity
     */
    private void onOrientationReverseLandscape(Activity activity) {
        if(isRevolveSetting()&&isPlaying()&&mScreenOrientation!=IMediaPlayer.ORIENTATION_LANDSCAPE){
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            startFullScreen();
        }
    }

    /**
     * 屏幕方向变化
     * @param orientation 0、90、180、270
     */
    @Override
    public void onOrientationChanged(int orientation) {
        Activity activity = PlayerUtils.getInstance().getActivity(getTargetContext());
//        ILogger.d(TAG,"onOrientationChanged-->screenOrientation:"+orientation+",activity:"+activity+",isFinish:"+(null!=activity?activity.isFinishing():true));
        if (activity == null || activity.isFinishing()) return;
        //记录用户手机上一次放置的位置
        int lastOrientation = mDisplayLastOrientation;
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
            //手机平放时，重置为原始位置 -1
            mDisplayLastOrientation = -1;
            return;
        }
        if (orientation > 350 || orientation < 10) {
            int o = activity.getRequestedOrientation();
            if (o == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE && lastOrientation == 0) return;
            if (mDisplayLastOrientation == 0) return;
            //0度，用户竖直拿着手机
            mDisplayLastOrientation = 0;
            onOrientationPortrait(activity);
        } else if (orientation > 80 && orientation < 100) {
            int o = activity.getRequestedOrientation();
            if (o == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT && lastOrientation == 90) return;
            if (mDisplayLastOrientation == 90) return;
            //90度，用户右侧横屏拿着手机
            mDisplayLastOrientation = 90;
            onOrientationReverseLandscape(activity);
        } else if (orientation > 260 && orientation < 280) {
            int o = activity.getRequestedOrientation();
            if (o == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT && lastOrientation == 270) return;
            if (mDisplayLastOrientation == 270) return;
            //270度，用户左侧横屏拿着手机
            mDisplayLastOrientation = 270;
            onOrientationLandscape(activity);
        }
    }

    /**
     * 重新获取焦点时保持全屏状态
     * @param hasWindowFocus
     */
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if(hasWindowFocus&&mScreenOrientation==IMediaPlayer.ORIENTATION_LANDSCAPE){
            hideSystemBar(getDecorView());
        }
    }

    //===========================来自播放器解码器回调事件,外界请不要调用=================================

    @Override
    public BasePlayer getVideoPlayer() {
        return this;
    }

    @Override
    public AbstractMediaPlayer getMediaPlayer() {
        if(null!=mOnPlayerActionListener){
            return mOnPlayerActionListener.createMediaPlayer();
        }
        return null;
    }

    @Override
    public IRenderView getRenderView() {
        if(null!=mOnPlayerActionListener){
            return mOnPlayerActionListener.createRenderView();
        }
        return null;
    }

    /**
     * 播放器播放状态
     * @param state 播放器内部状态
     * @param message
     */
    @Override
    public void onPlayerState(PlayerState state,String message) {
        ILogger.d(TAG,"onPlayerState-->state:"+state+",message:"+message);
        if(null!=mIVideoPlayer) this.setKeepScreenOn(mIVideoPlayer.isPlaying());//播放视频过程中请求屏幕常亮
        if(state==PlayerState.STATE_COMPLETION){//正常的播放器结束
            if(mRestoreDirection&&!mContinuityPlay&&mScreenOrientation==IMediaPlayer.ORIENTATION_LANDSCAPE){//未开启连续播放模式下,先退出可能存在的全屏播放状态
                quitFullScreen();
            }
        }
        PlayerState tempState=state;
        String tempMessage=message;
        if(null!= mController) mController.onPlayerState(tempState,tempMessage);//回调状态至控制器
        if(null!= mOnPlayerActionListener) mOnPlayerActionListener.onPlayerState(tempState,tempMessage);//回调状态至持有播放器的宿主
    }

    @Override
    public void onBuffer(int percent) {
        if(null!= mController){
            mController.onBuffer(percent);
        }
    }

    @Override
    public void onVideoSizeChanged(int width, int height) {
        if(null!=mOnPlayerActionListener) mOnPlayerActionListener.onVideoSizeChanged(width,height);
    }

    @Override
    public void onProgress(long currentDurtion, long totalDurtion) {
//        ILogger.d(TAG,"progress-->currentDurtion:"+currentDurtion+",totalDurtion:"+totalDurtion);
        if(null!= mController) mController.onProgress(currentDurtion,totalDurtion);
        if(null!=mOnPlayerActionListener) mOnPlayerActionListener.onProgress(currentDurtion,totalDurtion);
    }

    //=========================来自控制器的回调事件,也提供给外界调用的公开方法============================

    /**
     * 设置视图控制器
     * @param controller 继承VideoBaseController的控制器
     */
    @Override
    public void setController(BaseController controller) {
        PlayerUtils.getInstance().removeViewFromParent(mController);
        this.mController=controller;
        PlayerUtils.getInstance().removeViewFromParent(controller);
        FrameLayout controllerView = (FrameLayout) findViewById(R.id.player_controller);
        if(null!=controllerView){
            controllerView.removeAllViews();
            if(null!= mController){
                mController.attachedPlayer(this);//绑定播放器代理人
                controllerView.addView(mController,new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));//添加到播放器窗口
                mController.onCreate();//初始化
            }
        }
    }

    /**
     * 返回播放器控制器
     * @return
     */
    public BaseController getController() {
        return mController;
    }

    /**
     * 设置是否循环播放
     * @param loop 设置是否循环播放 true:循环播放 flase:禁止循环播放
     */
    @Override
    public void setLoop(boolean loop) {
        if(null!=mIVideoPlayer) mIVideoPlayer.setLoop(loop);
    }

    /**
     * 设置播放进度回调间隔时间
     * @param callBackSpaceMilliss 设置播放进度回调间隔时间 单位：毫秒,数字越大性能越好,越小回调越频繁
     */
    @Override
    public void setProgressCallBackSpaceMilliss(int callBackSpaceMilliss) {
        if(null!=mIVideoPlayer) mIVideoPlayer.setCallBackSpaceMilliss(callBackSpaceMilliss);
    }

    /**
     * 设置播放状态监听
     * @param listener 设置播放状态监听,如需自定义解码器,必须实现此监听
     */
    @Override
    public void setOnPlayerActionListener(OnPlayerEventListener listener) {
        mOnPlayerActionListener = listener;
    }

    /**
     * 设置String类型播放地址
     * 网络地址:http://或https://
     * raw目录下地址:android.resource://" + getPackageName() + "/" + R.raw.xxx
     * @param dataSource
     */
    @Override
    public void setDataSource(String dataSource){
        this.mDataSource=dataSource;
        this.mAssetsSource=null;
        if(null!=mIVideoPlayer) mIVideoPlayer.setDateSource(dataSource);
    }

    /**
     * 设置Assets类型的播放地址
     * @param dataSource 设置Assets类型的播放地址
     */
    @Override
    public void setDataSource(AssetFileDescriptor dataSource){
        this.mAssetsSource=dataSource;
        this.mDataSource=null;
        if(null!=mIVideoPlayer) mIVideoPlayer.setDateSource(dataSource);
    }

    /**
     * 设置本地File路劲的播放地址
     * @param dataSource 设置本地File路劲的播放地址,请注意先申请"存储"权限
     */
    @Override
    public void setDataSource(File dataSource) {
        if(null!=dataSource){
            String filePath = Uri.parse("file://" + dataSource.getAbsolutePath()).toString();
            setDataSource(filePath);
        }
    }

    /**
     * 设置缩放模式
     * @param scaleModel 设置缩放模式 请参阅IMediaPlayer类中定义的常量值
     */
    @Override
    public void setZoomModel(int scaleModel) {
        if(null!=mIVideoPlayer){
            mIVideoPlayer.setZoomModel(scaleModel);
            if(null!=mController) mController.onZoomModel(scaleModel);
        }
        if(null!=mOnPlayerActionListener) mOnPlayerActionListener.onZoomModel(scaleModel);
    }

    /**
     * 是否开启重力旋转。当系统"自动旋转"开启+正在播放生效
     * @param enable 是否开启重力旋转。当系统"自动旋转"开启+正在播放生效
     */
    @Override
    public void setAutoChangeOrientation(boolean enable) {
        if(null!= mOrientationRotate){
            if(enable){
                mOrientationRotate.enable();
            }else{
                mOrientationRotate.disable();
            }
        }
    }

    /**
     * 设置视频画面旋转角度
     * @param degree 设置视频画面旋转角度
     */
    @Override
    public void setDegree(int degree) {
        if(null!=mIVideoPlayer) mIVideoPlayer.setDegree(degree);
    }

    /**
     * 设置播放速度
     * @param speed 设置播放速度 从0.5f-2.0f
     */
    @Override
    public void setSpeed(float speed) {
        if(null!=mIVideoPlayer) mIVideoPlayer.setSpeed(speed);
    }

    /**
     * 设置左右声道音量，从0.0f-1.0f
     * @param leftVolume 设置左声道音量，1.0f-1.0f
     * @param rightVolume 设置右声道音量，1.0f-1.0f
     */
    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        if(null!=mIVideoPlayer) mIVideoPlayer.setVolume(leftVolume,rightVolume);
    }

    /**
     * 设置是否静音
     * @param mute 设置是否静音,true:无声 false:跟随系统音量
     * @return 是否静音,true:无声 false:跟随系统音量
     */
    @Override
    public boolean setSoundMute(boolean mute) {
        if(null!=mIVideoPlayer){
            boolean soundMute = mIVideoPlayer.setSoundMute(mute);
            if(null!=mController) mController.onMute(soundMute);
            if(null!=mOnPlayerActionListener) mOnPlayerActionListener.onMute(soundMute);
            return soundMute;
        }
        return false;
    }

    /**
     * 是否启用了静音
     * @return true:启用了静音 false:未启用静音
     */
    @Override
    public boolean isSoundMute() {
        if(null!=mIVideoPlayer) return mIVideoPlayer.isSoundMute();
        return false;
    }

    /**
     * 开启、关闭静音
     * @return 是否静音,true:静音 false:跟随系统音量
     */
    @Override
    public boolean toggleMute() {
        if(null!=mIVideoPlayer){
            boolean mute = mIVideoPlayer.toggleMute();
            if(null!=mController) mController.onMute(mute);
            if(null!=mOnPlayerActionListener) mOnPlayerActionListener.onMute(mute);
            return mute;
        }
        return false;
    }

    /**
     * 设置画面镜像旋转
     * @param mirror 设置画面镜像旋转 true:画面翻转 false:正常
     */
    @Override
    public boolean setMirror(boolean mirror) {
        if(null!=mIVideoPlayer){
            boolean isMirror = mIVideoPlayer.setMirror(mirror);
            if(null!=mController) mController.onMirror(isMirror);
            if(null!=mOnPlayerActionListener) mOnPlayerActionListener.onMirror(isMirror);
            return isMirror;
        }
        return false;
    }

    /**
     * 画面奖项
     * @return true:镜像 false:正常
     */
    @Override
    public boolean toggleMirror() {
        if(null!=mIVideoPlayer){
            boolean isMirror = mIVideoPlayer.toggleMirror();
            if(null!=mController) mController.onMirror(isMirror);
            if(null!=mOnPlayerActionListener) mOnPlayerActionListener.onMirror(isMirror);
            return isMirror;
        }
        return false;
    }

    /**
     * @param restoreDirection 设置当播放器在横屏状态下收到播放完成事件时是否自动还原到竖屏状态,true:自动还原到竖屏 false:保留当前屏幕方向状态
     */
    @Override
    public void setPlayCompletionRestoreDirection(boolean restoreDirection) {
        this.mRestoreDirection=restoreDirection;
    }

    /**
     * @param landscapeWindowTranslucent 设置当播放器在开启横屏状态下播放时是否启用全屏沉浸样式，true:启用沉浸式全屏 false:保留状态栏及菜单栏位置(隐藏状态栏及菜单栏图标及按钮)，使用标准的全屏样式
     */
    @Override
    public void setLandscapeWindowTranslucent(boolean landscapeWindowTranslucent) {
        this.mLandscapeWindowTranslucent = landscapeWindowTranslucent;
    }

    /**
     * 设置播放器在移动网络能否继续工作
     * @param mobileNetwork 设置播放器在移动网络能否继续工作 true:允许工作 flase:禁止
     */
    @Override
    public void setMobileNetwork(boolean mobileNetwork) {
        if(null!=mIVideoPlayer) mIVideoPlayer.setMobileNetwork(mobileNetwork);
    }

    /**
     * 设置是否监听并处理音频焦点事件
     * @param interceptTAudioFocus 设置是否监听并处理音频焦点事件 true:拦截，并在收到音频焦点失去后暂停播放 false:什么也不处理
     */
    @Override
    public void setInterceptTAudioFocus(boolean interceptTAudioFocus) {
        if(null!=mIVideoPlayer){
            mIVideoPlayer.setInterceptTAudioFocus(interceptTAudioFocus);
        }else{
            IVideoManager.getInstance().setInterceptTAudioFocus(interceptTAudioFocus);
        }
    }

    /**
     * @param reCatenationCount 设置当播放器遇到链接视频文件失败时自动重试的次数，内部自动重试次数为3次
     */
    @Override
    public void setReCatenationCount(int reCatenationCount) {
        if(null!=mIVideoPlayer){
            mIVideoPlayer.setReCatenationCount(reCatenationCount);
        }
    }

    /**
     * 开异步播放
     */
    @Override
    public void prepareAsync() {
        togglePlay();
    }

    /**
     * 开始播放
     */
    @Override
    public void startPlay() {
        togglePlay();
    }

    /**
     * 播放
     */
    @Override
    public void play() {
        togglePlay();
    }

    /**
     * 暂停
     */
    @Override
    public void pause() {
        togglePlay();
    }

    /**
     * 重新播放
     */
    @Override
    public void rePlay() {
        togglePlay();
    }

    /**
     * 开始播放\暂停
     */
    @Override
    public void togglePlay() {
        playOrPause();
    }

    /**
     * 开始播放\暂停
     */
    @Override
    public void playOrPause() {
        playOrPause(getDataSource());
    }

    /**
     * 开始播放\暂停
     * @param dataSource 传入播放地址 开始播放\暂停
     * 在开始播放之前可调用IWindowManager.getInstance().quitGlobaWindow();结束并退出悬浮窗窗口播放
     */
    @Override
    public void playOrPause(Object dataSource) {
        if(null!=mIVideoPlayer){
            mIVideoPlayer.playOrPause(dataSource);
        }
    }

    /**
     * 全屏播放
     */
    @Override
    public void startFullScreen() {
        startFullScreen(Color.parseColor("#000000"));
    }

    /**
     * 全屏播放
     * @param bgColor 开启全屏模式播放:横屏时播放器的背景颜色,内部默认用黑色#000000
     */
    @Override
    public void startFullScreen(int bgColor) {
//        ILogger.d(TAG,"startFullScreen");
        if(mScreenOrientation==IMediaPlayer.ORIENTATION_LANDSCAPE) return;
        Activity activity = PlayerUtils.getInstance().getActivity(getTargetContext());
        if (null != activity&& !activity.isFinishing()) {
            ViewGroup viewGroup = (ViewGroup) activity.getWindow().getDecorView();
            if(null==viewGroup){
                return;
            }
            //1.保存播放器在父布局中的宽、高、index层级等属性(如果存在的话)
            mPlayerParams = new int[3];
            mPlayerParams[0]=this.getMeasuredWidth();
            mPlayerParams[1]=this.getMeasuredHeight();
            if(null!=getParent()&& getParent() instanceof ViewGroup){
                mParent = (ViewGroup) getParent();
                mPlayerParams[2]=mParent.indexOfChild(this);//保存播放器本身的宽高和位于父容器的索引位置,恢复正常模式时需准确的还原到父容器index
            }
            PlayerUtils.getInstance().removeViewFromParent(this);//从原宿主中移除自己
            //2.改变屏幕方向为横屏状态,播放器所在的Activity需要添加属性：android:configChanges="orientation|screenSize"
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);//改变屏幕方向
            setScreenOrientation(IMediaPlayer.ORIENTATION_LANDSCAPE);//更新控制器方向状态
            //3.隐藏NavigationBar和StatusBar
            hideSystemBar(viewGroup);
            //给播放器添加一个背景颜色
            findViewById(R.id.player_surface).setBackgroundColor(bgColor!=0?bgColor:Color.parseColor("#000000"));//设置一个背景颜色
            //4.添加到此播放器宿主context的window中
            viewGroup.addView(this, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
        }
    }

    /**
     * 退出全屏播放
     */
    @Override
    public void quitFullScreen() {
//        ILogger.d(TAG,"quitLandscapeScreen");
        Activity activity = PlayerUtils.getInstance().getActivity(getTargetContext());
        if(null!=activity&&!activity.isFinishing()){
            ViewGroup viewGroup = (ViewGroup) activity.getWindow().getDecorView();
            if(null==viewGroup){
                return;
            }
            //1:从Window窗口中移除自己
            PlayerUtils.getInstance().removeViewFromParent(this);
            //2.改变屏幕方向为竖屏
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//改变屏幕方向
            setScreenOrientation(IMediaPlayer.ORIENTATION_PORTRAIT);
            findViewById(R.id.player_surface).setBackgroundColor(Color.parseColor("#00000000"));//设置纯透明背景
            //3.还原全屏设置为正常设置
            showSysBar(viewGroup);
            //3.将自己交给此前的宿主ViewGroup,并还原播放器在原宿主的宽、高、index位置
            if(null!=mParent){
                if(null!=mPlayerParams&&mPlayerParams.length>0){
//                    ILogger.d(TAG,"index:"+mPlayerParams[2]);
                    mParent.addView(this, mPlayerParams[2],new LayoutParams(mPlayerParams[0], mPlayerParams[1]));//将自己还原到父容器的index位置,取消了Gravity.CENTER属性
                }else{
                    mParent.addView(this, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                }
//                ILogger.d(TAG,"quitLandscapeScreen-->已退出全屏");
            }else{
                //通知宿主监听器触发返回事件
//                ILogger.d(TAG,"quitLandscapeScreen-->退出全屏无宿主接收,销毁播放器");
                //无宿主接收时直接停止播放并销毁播放器
                onDestroy();
            }
        }
    }

    /**
     * 开启\退出全屏播放
     */
    @Override
    public void toggleFullScreen() {
        if(mScreenOrientation==IMediaPlayer.ORIENTATION_LANDSCAPE){
            quitFullScreen();
            return;
        }
        //非工作中状态不允许全屏
        if(null!=mIVideoPlayer&&!mIVideoPlayer.isWork()){
            return;
        }
        startFullScreen();
    }

    @Override
    public void startWindow() {
        startWindow(true);
    }

    @Override
    public void startWindow(boolean isAutoSorption) {
        startWindow(0,0,0,0,0,0,isAutoSorption);
    }

    @Override
    public void startWindow(float radius, int bgColor) {
        startWindow(radius,bgColor,true);
    }

    @Override
    public void startWindow(float radius, int bgColor, boolean isAutoSorption) {
        startWindow(0,0,0,0,radius,bgColor,isAutoSorption);
    }

    @Override
    public void startWindow(int width, int height, float startX, float startY) {
        startWindow(width,height,startX,startY,true);
    }

    @Override
    public void startWindow(int width, int height, float startX, float startY, boolean isAutoSorption) {
        startWindow(width,height,startX,startY,0,0,true);
    }

    @Override
    public void startWindow(int width, int height, float startX, float startY, float radius) {
        startWindow(width, height, startX, startY, radius, true);
    }

    @Override
    public void startWindow(int width, int height, float startX, float startY, float radius, boolean isAutoSorption) {
        startWindow(width,height,startX,startY,radius,Color.parseColor("#99000000"),isAutoSorption);
    }

    @Override
    public void startWindow(int width, int height, float startX, float startY, float radius, int bgColor) {
        startWindow(width,height,startX,startY,radius,bgColor,true);
    }

    /**
     * 开启Activity级别的小窗口播放
     * @param width 窗口播放器的宽,当小于=0时用默认
     * 开启可拖拽的窗口播放
     * 默认宽为屏幕1/2+30dp,高为1/2+30dp的16:9比例,X起始位置为:播放器原宿主的右下方,距离原宿主View顶部12dp,右边12dp(如果原宿主不存在,则位于屏幕右上角距离顶部60dp位置)
     * 全局悬浮窗口和局部小窗口不能同时开启
     * 横屏下不允许开启
     * @param height 窗口播放器的高,当小于=0时用默认
     * @param startX 窗口位于屏幕中的X轴起始位置,当小于=0时用默认
     * @param startY 窗口位于屏幕中的Y轴起始位置
     * @param radius 窗口的圆角 单位:像素
     * @param bgColor 窗口的背景颜色
     * @param isAutoSorption 触摸松手后是否自动吸附到屏幕边缘(悬停时距离屏幕边缘12dp),true:自动吸附,false:保持在床后的最后位置状态
     */
    @Override
    public void startWindow(int width, int height, float startX, float startY, float radius, int bgColor, boolean isAutoSorption) {
        ILogger.d(TAG,"startWindow-->width:"+width+",height:"+height+",startX:"+startX+",startY:"+startY+",radius:"+radius+",bgColor:"+bgColor+",windowProperty:"+ mIsActivityWindow +",screenOrientation:"+mScreenOrientation+",isAutoSorption:"+isAutoSorption);
        if(mIsActivityWindow ||mScreenOrientation==IMediaPlayer.ORIENTATION_LANDSCAPE) return;//已开启窗口模式或者横屏情况下不允许开启小窗口
        Activity activity = PlayerUtils.getInstance().getActivity(getTargetContext());
        if (null != activity&& !activity.isFinishing()) {
            ViewGroup viewGroup = (ViewGroup) activity.getWindow().getDecorView();
            if(null==viewGroup){
                return;
            }
            int[] screenLocation=new int[2];
            //保存播放器本身的宽高和位于父容器的索引位置,恢复正常模式时需准确的还原到父容器index
            mPlayerParams = new int[3];
            mPlayerParams[0]=this.getMeasuredWidth();
            mPlayerParams[1]=this.getMeasuredHeight();
            //1.从原有竖屏窗口移除自己前保存自己的Parent,直接开启全屏是不存在宿主ViewGroup的,可直接窗口转场
            if(null!=getParent()&& getParent() instanceof ViewGroup){
                mParent = (ViewGroup) getParent();
                mParent.getLocationInWindow(screenLocation);
                mPlayerParams[2]=mParent.indexOfChild(this);
//                ILogger.d(TAG,"startWindow-->parent_id:"+getId()+",parentX:"+screenLocation[0]+",parentY:"+screenLocation[1]+",parentWidth:"+mParent.getWidth()+",parentHeight:"+mParent.getHeight());
            }
            PlayerUtils.getInstance().removeViewFromParent(this);//从原宿主中移除自己
            //2.改变播放器横屏或窗口播放状态
            setWindowPropertyPlayer(true,false);
            //3.获取宿主的View属性和startX、Y轴
            //如果传入的宽高不存在,则使用默认的16：9的比例创建Window View
            if(width<=0){
                width = PlayerUtils.getInstance().getScreenWidth(getContext())/2+PlayerUtils.getInstance().dpToPxInt(30f);
                height = width*9/16;
//                ILogger.d(TAG,"startWindow-->未传入宽高,width:"+width+",height:"+height);
            }
            //如果传入的startX不存在，则startX起点位于屏幕宽度1/2-距离右侧12dp位置，startY起点位于宿主View的下方12dp处
            if(startX<=0&&null!=mParent){
                startX=(PlayerUtils.getInstance().getScreenWidth(getContext())/2-PlayerUtils.getInstance().dpToPxInt(30f))-PlayerUtils.getInstance().dpToPxInt(12f);
                startY=screenLocation[1]+mParent.getHeight()+PlayerUtils.getInstance().dpToPxInt(12f);
//                ILogger.d(TAG,"startWindow-->未传入X,Y轴,取父容器位置,startX:"+startX+",startY:"+startY);
            }
            //如果宿主也不存在，则startX起点位于屏幕宽度1/2-距离右侧12dp位置，startY起点位于屏幕高度-Window View 高度+12dp位置处
            if(startX<=0){
                startX=(PlayerUtils.getInstance().getScreenWidth(getContext())/2-PlayerUtils.getInstance().dpToPxInt(30f))-PlayerUtils.getInstance().dpToPxInt(12f);
                startY=PlayerUtils.getInstance().dpToPxInt(60f);
//                ILogger.d(TAG,"startWindow-->未传入X,Y轴或取父容器位置失败,startX:"+startX+",startY:"+startY);
            }
            ILogger.d(TAG,"startWindow-->final:width:"+width+",height:"+height+",startX:"+startX+",startY:"+startY);
            //4.转场到window中,并指定宽高和x,y轴
            WindowPlayerFloatView container=new WindowPlayerFloatView(viewGroup.getContext());
            container.setOnWindowActionListener(new OnWindowActionListener() {
                @Override
                public void onMovie(float x, float y) {

                }

                @Override
                public void onClick(BasePlayer basePlayer, Object coustomParams) {

                }

                @Override
                public void onClose() {
//                    ILogger.d(TAG,"startWindow-->onClose");
                    quitWindow();//退出小窗口
                }
            });
            container.setId(R.id.player_window);
            viewGroup.addView(container, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
            container.addPlayerView(this,width,height,startX,startY,radius,bgColor,isAutoSorption);//先将播放器包装到可托拽的容器中
        }
    }

    /**
     * 退出窗口播放
     */
    @Override
    public void quitWindow() {
//        ILogger.d(TAG,"quitWindow");
        Activity activity = PlayerUtils.getInstance().getActivity(getTargetContext());
        if(null!=activity&&!activity.isFinishing()){
            ViewGroup viewGroup = (ViewGroup) activity.getWindow().getDecorView();
            if(null==viewGroup){
//                ILogger.d(TAG,"quitWindow-->窗口不存在");
                return;
            }
            //1:从Window窗口中移除自己
            View playerView = viewGroup.findViewById(R.id.player_window);
//            ILogger.d(TAG,"quitWindow-->getId():"+getId());
            PlayerUtils.getInstance().removeViewFromParent(playerView);//移除拖拽窗口View
            PlayerUtils.getInstance().removeViewFromParent(this);//从拖拽窗口View中移除播放器
            //2.改变窗口状态
            setWindowPropertyPlayer(false,false);
            //3.将自己交给此前的宿主ViewGroup
            if(null!=mParent){
                if(null!=mPlayerParams&&mPlayerParams.length>0){
                    mParent.addView(this, mPlayerParams[2],new LayoutParams(mPlayerParams[0], mPlayerParams[1]));//将自己还原到父容器的index位置，取消了Gravity.CENTER属性
                }else{
                    mParent.addView(this, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                }
//                ILogger.d(TAG,"quitWindow-->已退出窗口");
            }else{
                //通知宿主监听器触发返回事件
//                ILogger.d(TAG,"quitWindow-->退出窗口无宿主接收,通知控制器返回");
                //无宿主接收时直接停止播放并销毁播放器
                onDestroy();
            }
        }
    }

    /**
     * 开启\退出窗口播放
     */
    @Override
    public void toggleWindow() {
        if(mIsActivityWindow){
            quitWindow();
            return;
        }
        //非工作中状态不允许开启窗口模式
        if(null!=mIVideoPlayer&&!mIVideoPlayer.isWork()){
            return;
        }
        startWindow();
    }

    @Override
    public boolean startGlobalWindow() {
        return startGlobalWindow(true);
    }

    @Override
    public boolean startGlobalWindow(boolean isAutoSorption) {
        return startGlobalWindow(0,0,0,0,0,0,isAutoSorption);
    }

    @Override
    public boolean startGlobalWindow(float radius, int bgColor) {
        return startGlobalWindow(radius,bgColor,true);
    }

    @Override
    public boolean startGlobalWindow(float radius, int bgColor, boolean isAutoSorption) {
        return startGlobalWindow(0,0,0,0,radius,bgColor,isAutoSorption);
    }

    @Override
    public boolean startGlobalWindow(int width, int height, float startX, float startY) {
        return startGlobalWindow(width,height,startX,startY,true);
    }

    @Override
    public boolean startGlobalWindow(int width, int height, float startX, float startY, boolean isAutoSorption) {
        return startGlobalWindow(width,height,startX,startY,0,0,isAutoSorption);
    }

    @Override
    public boolean startGlobalWindow(int width, int height, float startX, float startY, float radius) {
        return startGlobalWindow(width,height,startX,startY,radius,true);
    }

    @Override
    public boolean startGlobalWindow(int width, int height, float startX, float startY, float radius, boolean isAutoSorption) {
        return startGlobalWindow(width,height,startX,startY,radius,0,isAutoSorption);
    }

    @Override
    public boolean startGlobalWindow(int width, int height, float startX, float startY, float radius, int bgColor) {
        return startGlobalWindow(width,height,startX,startY,radius,bgColor,true);
    }

    /**
     *
     * @param width 窗口播放器的宽,当小于=0时用默认
     * 开启可拖拽的全局悬浮窗口播放
     * 默认宽为屏幕1/2+30dp,高为1/2+30dp的16:9比例,X起始位置为:播放器原宿主的右下方,距离原宿主View顶部12dp,右边12dp(如果原宿主不存在,则位于屏幕右上角距离顶部60dp位置),
     * 全局悬浮窗口和局部小窗口不能同时开启
     * 横屏下不允许开启,需要在取得悬浮窗权限之后再调用
     * 需要声明权限：
     *     <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
     *     <uses-permission android:name="android.permission.SYSTEM_OVERLAY_WINDOW" />
     * @param height 窗口播放器的高,当小于=0时用默认
     * @param startX 窗口位于屏幕中的X轴起始位置,当小于=0时用默认
     * @param startY 窗口位于屏幕中的Y轴起始位置,当小于=0时用默认
     * @param radius 窗口的圆角 单位:像素
     * @param bgColor 窗口的背景颜色
     * @param isAutoSorption 触摸松手后是否自动吸附到屏幕边缘(悬停时距离屏幕边缘12dp),true:自动吸附,false:保持在床后的最后位置状态
     * @return
     */
    @Override
    public boolean startGlobalWindow(int width, int height, float startX, float startY, float radius, int bgColor, boolean isAutoSorption) {
        ILogger.d(TAG,"startGlobalWindow-->width:"+width+",height:"+height+",startX:"+startX+",startY:"+startY+",radius:"+radius+",bgColor:"+bgColor +",screenOrientation:"+mScreenOrientation);
        if(mScreenOrientation==IMediaPlayer.ORIENTATION_LANDSCAPE){
            return false;
        }
        boolean existPermission = PlayerUtils.getInstance().existPermission(getContext(), Manifest.permission.SYSTEM_ALERT_WINDOW);//检查开发者是否申明悬浮窗权限
        boolean hasPermission = PlayerUtils.getInstance().checkWindowsPermission(getContext());//检查是否获取了悬浮窗权限
//        ILogger.d(TAG,"startGlobalWindow-->hasPermission:"+hasPermission+",existPermission:"+existPermission);
        if(!existPermission){
            Toast.makeText(getContext(),getContext().getResources().getString(R.string.player_window_permission),Toast.LENGTH_SHORT).show();
//            ILogger.d(TAG,"startGlobalWindow-->清单文件中未申明悬浮窗权限,请检查："+Manifest.permission.SYSTEM_ALERT_WINDOW);
            return false;
        }
        //已申明,判断是否获取悬浮窗权限
        if(hasPermission){
            Activity activity = PlayerUtils.getInstance().getActivity(getTargetContext());
//            ILogger.d(TAG,"startGlobalWindow-->1,activity:"+activity+",isFinishing:"+(null!=activity?activity.isFinishing():true));
            if (null != activity&&!activity.isFinishing()) {//将this添加到悬浮窗,然后从悬浮窗移除之后又将this添加到Activity中。此时Activity的isFinishing()状态为:true。所有这里干脆不判断isFinishing()了
                try {
                    int[] screenLocation=new int[2];
                    ViewGroup parent=null;
                    //1.从原有竖屏窗口移除自己前保存自己的Parent,直接开启全屏是不存在宿主ViewGroup的,可直接窗口转场
                    if(null!=getParent()&& getParent() instanceof ViewGroup){
                        parent = (ViewGroup) getParent();
                        parent.getLocationInWindow(screenLocation);
//                        ILogger.d(TAG,"startGlobalWindow-->parent_id:"+getId()+",parentX:"+screenLocation[0]+",parentY:"+screenLocation[1]+",parentWidth:"+parent.getWidth()+",parentHeight:"+parent.getHeight());
                    }
                    PlayerUtils.getInstance().removeViewFromParent(this);//从原宿主中移除自己
                    //2.获取宿主的View属性和startX、Y轴
                    //如果传入的宽高不存在,则使用默认的16：9的比例创建Window View
                    if(width<=0){
                        width = PlayerUtils.getInstance().getScreenWidth(getContext())/2+PlayerUtils.getInstance().dpToPxInt(30f);
                        height = width*9/16;
//                        ILogger.d(TAG,"startGlobalWindow-->未传入宽高,width:"+width+",height:"+height);
                    }
                    //如果传入的startX不存在，则startX起点位于屏幕宽度1/2-距离右侧15dp位置，startY起点位于宿主View的下方12dp处
                    if(startX<=0&&null!=parent){
                        startX=(PlayerUtils.getInstance().getScreenWidth(getContext())/2-PlayerUtils.getInstance().dpToPxInt(30f))-PlayerUtils.getInstance().dpToPxInt(12f);
                        startY=screenLocation[1]+parent.getHeight()+PlayerUtils.getInstance().dpToPxInt(12f);
//                        ILogger.d(TAG,"startGlobalWindow-->未传入X,Y轴,取父容器位置,startX:"+startX+",startY:"+startY);
                    }
                    //如果宿主也不存在，则startX起点位于屏幕宽度1/2-距离右侧12dp位置，startY起点位于屏幕高度-Window View 高度+12dp位置处
                    if(startX<=0){
                        startX=(PlayerUtils.getInstance().getScreenWidth(getContext())/2-PlayerUtils.getInstance().dpToPxInt(30f))-PlayerUtils.getInstance().dpToPxInt(12f);
                        startY=PlayerUtils.getInstance().dpToPxInt(60f);
//                        ILogger.d(TAG,"startGlobalWindow-->未传入X,Y轴或取父容器位置失败,startX:"+startX+",startY:"+startY);
                    }
                    ILogger.d(TAG,"startGlobalWindow-->final:width:"+width+",height:"+height+",startX:"+startX+",startY:"+startY);
                    //3.转场到window中,并指定宽高和x,y轴
                    boolean success= IWindowManager.getInstance().addGolbalWindow(getContext(), this, width, height, startX, startY,radius,bgColor,isAutoSorption);
//                    ILogger.d(TAG,"startGlobalWindow--悬浮窗创建结果："+success);
                    //4.改变播放器横屏或窗口播放状态
                    if(success){
                        setWindowPropertyPlayer(false,true);
                    }
                    return success;
                }catch (Throwable e){
                    e.printStackTrace();
                }
            }else{
//                ILogger.d(TAG,"startGlobalWindow-->上下文为空,开启失败");
            }
        }else{
            //向用户申请悬浮窗权限
            try {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    intent.setAction(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    intent.setData(Uri.parse( "package:"+ PlayerUtils.getInstance().getPackageName(
                            getContext().getApplicationContext())));
                } else {
                    intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                    intent.setData(Uri.fromParts("package", PlayerUtils.getInstance().getPackageName(
                            getContext().getApplicationContext()), null));
                }
                getContext().getApplicationContext().startActivity(intent);
            }catch (Throwable e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 退出全局悬浮窗口播放
     */
    @Override
    public void quitGlobaWindow() {
        IWindowManager.getInstance().quitGlobaWindow();
        setWindowPropertyPlayer(false,false);
    }

    /**
     * 开启\退出全局悬浮窗口播放
     */
    @Override
    public void toggleGlobaWindow() {
        if(mIsGlobalWindow){
            quitGlobaWindow();
            return;
        }
        //非工作中状态不允许开启窗口模式
        if(null!=mIVideoPlayer&&!mIVideoPlayer.isWork()){
            return;
        }
        startGlobalWindow();
    }

    /**
     * 告诉播放器进入了画中画模式
     */
    @Override
    public void enterPipWindow() {
        if(null!= mController) mController.enterPipWindow();
    }

    /**
     * 告诉播放器退出了画中画模式
     */
    @Override
    public void quitPipWindow() {
        if(null!= mController) mController.quitPipWindow();
    }

    @Override
    public void onCompletion() {
        if(null!=mIVideoPlayer) mIVideoPlayer.onCompletion();
    }

    /**
     * 设置连续播放模式
     * @param continuityPlay 设置是否连续播放模式(需要在视频播放完成结束前调用),true:连续播放模式开启 false:关闭连续播放模式,播放器内部在收到continuityPlay为true的时候,不会自动退出全屏\小窗口\悬浮窗口等模式
     */
    @Override
    public void setContinuityPlay(boolean continuityPlay) {
        this.mContinuityPlay=continuityPlay;
    }

    /**
     * 返回视频宽
     * @return 单位:像素
     */
    @Override
    public int getVideoWidth() {
        if(null!=mIVideoPlayer){
            return mIVideoPlayer.getVideoWidth();
        }
        return 0;
    }

    /**
     * 返回视频高
     * @return 单位:像素
     */
    @Override
    public int getVideoHeight() {
        if(null!=mIVideoPlayer){
            return mIVideoPlayer.getVideoHeight();
        }
        return 0;
    }

    /**
     * 返回视频总长度
     * @return 单位:毫秒
     */
    @Override
    public long getDuration() {
        if(null!=mIVideoPlayer) {
            return mIVideoPlayer.getDurtion();
        }
        return 0;
    }

    /**
     * 返回当前正在播放的位置
     * @return 单位:毫秒
     */
    @Override
    public long getCurrentPosition() {
        if(null!=mIVideoPlayer) {
            return mIVideoPlayer.getCurrentPosition();
        }
        return 0;
    }

    /**
     * 返回缓冲进度
     * @return 单位：百分比
     */
    @Override
    public int getBuffer() {
        if(null!=mIVideoPlayer) {
            return mIVideoPlayer.getBuffer();
        }
        return 0;
    }

    /**
     * 设置缓冲和读取视频流超时时长
     * @param prepareTimeout 设置准备和读数据超时阈值,需在{@link #prepareAsync()}之前调用方可生效 准备超时阈值,即播放器在建立链接、解析流媒体信息的超时阈值
     * @param readTimeout    读数据超时阈值
     */
    @Override
    public void setTimeout(int prepareTimeout, int readTimeout) {
        if(null!=mIVideoPlayer) mIVideoPlayer.setTimeout(prepareTimeout,readTimeout);
    }

    /**
     * 跳转
     * @param msec 毫秒进度条
     */
    @Override
    public void seekTo(long msec) {
        seekTo(msec,true);
    }

    /**
     * 精准跳转
     * @param msec 快进\快退 毫秒进度条
     * @param accurate 是否精准快进快退
     */
    @Override
    public void seekTo(long msec, boolean accurate) {
        if (null != mIVideoPlayer) mIVideoPlayer.seekTo(msec, accurate);
    }

    /**
     * 返回播放器内部是否正在工作中
     * @return 非失败、初始、完成状态的其它状态均为播放中
     */
    @Override
    public boolean isWorking() {
        if(null!=mIVideoPlayer) {
            return mIVideoPlayer.isWork();
        }
        return false;
    }

    /**
     * 返回播放器内部是否正在播放
     * @return true:播放中 false:未正在播放
     */
    @Override
    public boolean isPlaying() {
        if(null!=mIVideoPlayer) {
            return mIVideoPlayer.isPlaying();
        }
        return false;
    }

    /**
     * @param context 当播放器开启转场、全局悬浮窗功能时,在业务层面设置一个当前的上下文,方便内部处理Window逻辑
     */
    @Override
    public void setParentContext(Context context) {
        this.mParentContext =context;
    }

    public Context getParentContext() {
        return mParentContext;
    }

    /**
     * 是否允许返回(横屏时先退出横屏)
     * @return
     */
    @Override
    public boolean isBackPressed() {
        //退出全屏模式
        if(mScreenOrientation==IMediaPlayer.ORIENTATION_LANDSCAPE){
            quitFullScreen();
            return false;
        }
        //退出局部窗口模式
        if(mIsActivityWindow){
            quitWindow();
            return false;
        }
        return true;
    }

    /**
     * 尝试恢复播放
     */
    @Override
    public void onResume(){
        if(null!= mController) mController.onResume();
        if(null!=mIVideoPlayer) mIVideoPlayer.onResume();
    }

    /**
     * 尝试暂停播放
     */
    @Override
    public void onPause(){
        if(null!=mIVideoPlayer) mIVideoPlayer.onPause();
        if(null!= mController) mController.onResume();
    }

    /**
     * 结束播放,鉴于停止播放比较耗时，在子线程中操作
     */
    @Override
    public void onStop(){
        if(null!=mIVideoPlayer) mIVideoPlayer.onStop();
    }

    /**
     * 转场和全局悬浮窗恢复播放器内部状态
     */
    @Override
    public void onRecover(){
//        ILogger.d(TAG,"onRecover-->");
        setScreenOrientation(IMediaPlayer.ORIENTATION_PORTRAIT);
        setWindowPropertyPlayer(false,false);
    }

    /**
     * 还原播放器及controller内部所有状态
     */
    @Override
    public void onReset(){
        mDataSource=null;mAssetsSource=null;
        if(null!=mIVideoPlayer) mIVideoPlayer.onReset();
    }

    @Override
    public void onRelease() {
        onDestroy();
    }

    /**
     * 销毁播放器
     */
    @Override
    public void onDestroy(){
        if(null!= mOrientationRotate) mOrientationRotate.onReset();
        if(null!=mController) mController.onDestroy();
        if(null!=mIVideoPlayer){
            mIVideoPlayer.onDestroy();
            mIVideoPlayer=null;
        }
        if(null!=mParent){
            mParent.removeAllViews();
            mParent=null;
        }
        mIsActivityWindow =false;mContinuityPlay=false;mDataSource=null;mAssetsSource=null;
        mOnPlayerActionListener=null;mScreenOrientation=IMediaPlayer.ORIENTATION_PORTRAIT;
    }
}