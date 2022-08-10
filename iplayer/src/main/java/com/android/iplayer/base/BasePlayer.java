package com.android.iplayer.base;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.android.iplayer.R;
import com.android.iplayer.controller.VideoController;
import com.android.iplayer.interfaces.IMediaPlayer;
import com.android.iplayer.interfaces.IVideoPlayerControl;
import com.android.iplayer.listener.OnPlayerEventListener;
import com.android.iplayer.interfaces.IMediaPlayerControl;
import com.android.iplayer.listener.OnWindowActionListener;
import com.android.iplayer.manager.IVideoManager;
import com.android.iplayer.manager.IWindowManager;
import com.android.iplayer.media.IVideoPlayer;
import com.android.iplayer.model.PlayerState;
import com.android.iplayer.utils.PlayerUtils;
import com.android.iplayer.utils.ILogger;
import com.android.iplayer.widget.WindowPlayerFloatView;

/**
 * Created by hty
 * 2022/6/28
 * 1、如需播放器支持全屏、小窗口、全局悬浮窗口播放功能，则VideoPlayerView需要一个ViewGroup容器来包裹。
 * 2、如播放视频场景是打开一个Activity直接全屏\直接小窗口模式播放(按返回键直接关闭Activity),则只能new 一个播放器对象来使用。
 * Desc:通用视频播放容器基类,交互控制内部只实现了：点击 暂停、开始播放
 * 自定义属性：initController:true:内部初始化一个默认的控制器交互组件 flase:不初始化默认的控制器组件
 * 自定义多媒体解码器的适用：
 * 注册setOnPlayerActionListener监听器，实现createMediaPlayer方法来创建一个解码器。每个视频播放任务都将实例化一个createMediaPlayer解码器
 */
public abstract class BasePlayer extends FrameLayout implements IVideoPlayerControl, IMediaPlayerControl {

    protected static final String TAG = BasePlayer.class.getSimpleName();
    private BaseController mController;//视图控制器
    protected OnPlayerEventListener mOnPlayerActionListener;//播放器事件监听器(宿主调用)
    private int mScreenOrientation= IMediaPlayer.ORIENTATION_PORTRAIT;//当前播放器方向
    private String mDataSource;//播放地址
    private AssetFileDescriptor mAssetsSource;//Assetss资产目录下的文件地址
    private ViewGroup mParent;//自己的宿主
    private int[] mPlayerParams;//自己的宽高属性和位于父容器的层级位置
    private IVideoPlayer mIVideoPlayer;
    private boolean mIsWindowProperty,mContinuityPlay,mIsGlobalWindow;//是否开启了窗口播放模式/是否开启了连续播放模式/是否处于全局悬浮窗|画中画模式
    private Context mTempContext;//临时的上下文,播放器内部会优先使用这个上下文来获取当前的Activity.业务方便开启转场、全局悬浮窗后设置此上下文。在Activity销毁时置空此上下文

    public BasePlayer(Context context) {
        this(context,null);
    }

    public BasePlayer(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BasePlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context,R.layout.player_base_video,this);
        if(null!=attrs){
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BasePlayer);
            //是否创建默认的控制器
            boolean createController = typedArray.getBoolean(R.styleable.BasePlayer_initController, false);
            if(createController){
                setController(new VideoController(context));
            }
            typedArray.recycle();
        }
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                togglePlay();
            }
        });
        //将自己与播放器解码绑定
        mIVideoPlayer = new IVideoPlayer();
        mIVideoPlayer.setIMediaPlayerControl(this);
        initViews();
    }

    protected abstract void initViews();

    /**
     * 改变控制器方向
     * @param orientation
     */
    private void setScreenOrientation(int orientation) {
        this.mScreenOrientation=orientation;
        if(null!= mController) mController.setScreenOrientationPlayer(orientation);
    }

    /**
     * 改变播放器窗口属性
     * @param isWindowProperty true:窗口模式 false:非窗口模式
     * @param isGlobalWindow true:全局悬浮窗窗口|画中画模式 false:Activity局部悬浮窗窗口模式
     */
    private void setWindowPropertyPlayer(boolean isWindowProperty,boolean isGlobalWindow) {
        this.mIsWindowProperty=isWindowProperty;
        this.mIsGlobalWindow=isGlobalWindow;
        if(null!= mController) mController.setWindowPropertyPlayer(isWindowProperty,isGlobalWindow);
    }

    /**
     * 隐藏控制栏和导航栏
     * @param decorView
     */
    private void hideSystemBar(ViewGroup decorView) {
        Activity activity = PlayerUtils.getInstance().getActivity(getViewContext());
        if(null!=activity&&!activity.isFinishing()){
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
        Activity activity = PlayerUtils.getInstance().getActivity(getViewContext());
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
        Activity activity = PlayerUtils.getInstance().getActivity(getViewContext());
        if(null!=activity&&!activity.isFinishing()){
            ViewGroup viewGroup = (ViewGroup) activity.getWindow().getDecorView();
            return viewGroup;
        }
        return null;
    }

    private Context getViewContext(){
        if(null!=mTempContext){
            return mTempContext;
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
            if(!mContinuityPlay&&mScreenOrientation==IMediaPlayer.ORIENTATION_LANDSCAPE){//未开启连续播放模式下,先退出可能存在的全屏播放状态
                quitFullScreen();
            }
        }
        PlayerState tempState=state;
        String tempMessage=message;
        if(null!= mController) mController.onStatePlayer(tempState,tempMessage);//回调状态至控制器
        if(null!= mOnPlayerActionListener) mOnPlayerActionListener.onPlayerState(tempState,tempMessage);//回调状态至持有播放器的宿主
    }

    @Override
    public void onBuffer(int percent) {
        if(null!= mController){
            mController.onBufferPlayer(percent);
        }
    }

    @Override
    public void onVideoSizeChanged(int width, int height) {
        if(null!=mOnPlayerActionListener) mOnPlayerActionListener.onVideoSizeChanged(width,height);
    }

    @Override
    public void onProgress(long currentDurtion, long totalDurtion, int bufferPercent) {
//        ILogger.d(TAG,"progress-->currentDurtion:"+currentDurtion+",totalDurtion:"+totalDurtion+",bufferPercent:"+bufferPercent);
        if(null!= mController) mController.onProgressPlayer(currentDurtion,totalDurtion,bufferPercent);
        if(null!=mOnPlayerActionListener) mOnPlayerActionListener.onProgress(currentDurtion,totalDurtion,bufferPercent);
    }

    //=========================来自控制器的回调事件,也提供给外界调用的公开方法============================

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
     * 设置缩放模式
     * @param scaleModel 设置缩放模式 请适用IMediaPlayer类中定义的常量值
     */
    @Override
    public void setZoomModel(int scaleModel) {
        if(null!=mIVideoPlayer){
            mIVideoPlayer.setZoomModel(scaleModel);
        }else{
            IVideoManager.getInstance().setZoomModel(scaleModel);
        }
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
     * 设置是否静音
     * @param mute 设置是否静音,true:无声 false:跟随系统音量
     */
    @Override
    public void setSoundMute(boolean mute) {
        if(null!=mIVideoPlayer) mIVideoPlayer.setSoundMute(mute);
    }

    /**
     * 设置画面镜像旋转
     * @param mirror 设置画面镜像旋转 true:画面翻转 false:正常
     */
    @Override
    public void setMirror(boolean mirror) {
        if(null!=mIVideoPlayer) mIVideoPlayer.setMirror(mirror);
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
     * 设置标题
     * @param title 设置标题,也可以调用控制器的setTitle方法
     */
    @Override
    public void setTitle(String title) {
        if(null!= mController) mController.setVideoTitle(title);
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
        Activity activity = PlayerUtils.getInstance().getActivity(getViewContext());
//        ILogger.d(TAG,"startFullScreen-->activity:"+activity);
        if (null != activity&& !activity.isFinishing()) {
//            ILogger.d(TAG,"startFullScreen-->1");
            ViewGroup viewGroup = (ViewGroup) activity.getWindow().getDecorView();
            if(null==viewGroup){
                return;
            }
            //1.保存父布局,如果存在的话
            //保存播放器本身的宽高和位于父容器的索引位置,恢复正常模式时需准确的还原到父容器index
            mPlayerParams = new int[3];
            mPlayerParams[0]=this.getMeasuredWidth();
            mPlayerParams[1]=this.getMeasuredHeight();
            if(null!=getParent()&& getParent() instanceof ViewGroup){
//                ILogger.d(TAG,"startFullScreen-->移除自己的parent");
                mParent = (ViewGroup) getParent();
                mPlayerParams[2]=mParent.indexOfChild(this);
            }
            PlayerUtils.getInstance().removeViewFromParent(this);//从原宿主中移除自己
            //2.改变屏幕方向
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);//改变屏幕方向
            setScreenOrientation(IMediaPlayer.ORIENTATION_LANDSCAPE);//更新控制器方向状态
            findViewById(R.id.player_surface).setBackgroundColor(bgColor!=0?bgColor:Color.parseColor("#000000"));//设置一个背景颜色
            //3.隐藏NavigationBar和StatusBar
            hideSystemBar(viewGroup);
            //4.转场到横屏的window中
//            ILogger.d(TAG,"startFullScreen-->getId():"+getId());
//            if(getId()<=0){
//                this.setId(R.id.player_window);//宿主没有设置ID情况下设置一个ID
//            }
//            ILogger.d(TAG,"startFullScreen-->addView");
            viewGroup.addView(this, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
        }
    }

    /**
     * 退出全屏播放
     */
    @Override
    public void quitFullScreen() {
//        ILogger.d(TAG,"quitLandscapeScreen");
        Activity activity = PlayerUtils.getInstance().getActivity(getViewContext());
        if(null!=activity&&!activity.isFinishing()){
            ViewGroup viewGroup = (ViewGroup) activity.getWindow().getDecorView();
            if(null==viewGroup){
                return;
            }
            //1:从Window窗口中移除自己
//            @SuppressLint("ResourceType")
//            View playerView = viewGroup.findViewById(getId() <= 0 ? R.id.player_window : getId());
//            ILogger.d(TAG,"quitLandscapeScreen-->getId():"+getId());
//            PlayerUtils.getInstance().removeViewFromParent(playerView);
            PlayerUtils.getInstance().removeViewFromParent(this);
            //2.改变屏幕方向
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//改变屏幕方向
            setScreenOrientation(IMediaPlayer.ORIENTATION_PORTRAIT);
            findViewById(R.id.player_surface).setBackgroundColor(Color.parseColor("#00000000"));//设置纯透明背景
            //3.将自己交给此前的宿主ViewGroup,注意还需要还原播放器原有的宽高属性
            showSysBar(viewGroup);//重置全屏设置
            if(null!=mParent){
                if(null!=mPlayerParams&&mPlayerParams.length>0){
                    ILogger.d(TAG,"index:"+mPlayerParams[2]);
                    mParent.addView(this, mPlayerParams[2],new LayoutParams(mPlayerParams[0], mPlayerParams[1], Gravity.CENTER));//将自己还原到父容器的index位置
                }else{
                    mParent.addView(this, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
                }
//                ILogger.d(TAG,"quitLandscapeScreen-->已退出全屏");
            }else{
                //通知宿主监听器触发返回事件
//                ILogger.d(TAG,"quitLandscapeScreen-->退出全屏无宿主接收,通知控制器返回并销毁播放器");
                if(null!= mController) mController.onBack();
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

    /**
     * 开启Activity级别的小窗口播放
     */
    @Override
    public void startWindow() {
        startWindow(0,0,0,0);
    }

    /**
     * 开启Activity级别的小窗口播放
     * @param radius 窗口的圆角 单位:像素
     * 开启可拖拽的窗口播放
     * 默认宽为屏幕1/2+30dp,高为1/2+30dp的16:9比例,X起始位置为:播放器原宿主的右下方,距离原宿主View顶部15dp,右边15dp(如果原宿主不存在,则位于屏幕右上角距离顶部60dp位置)
     * 全局悬浮窗口和局部小窗口不能同时开启
     * 横屏下不允许开启
     * @param bgColor 窗口的背景颜色
     */
    @Override
    public void startWindow(float radius, int bgColor) {
        startWindow(0,0,0,0,radius,bgColor);
    }

    /**
     * 开启Activity级别的小窗口播放
     * @param width 窗口播放器的宽,当小于=0时用默认
     * 开启可拖拽的窗口播放
     * 默认宽为屏幕1/2+30dp,高为1/2+30dp的16:9比例,X起始位置为:播放器原宿主的右下方,距离原宿主View顶部15dp,右边15dp(如果原宿主不存在,则位于屏幕右上角距离顶部60dp位置)
     * 全局悬浮窗口和局部小窗口不能同时开启
     * 横屏下不允许开启
     * @param height 窗口播放器的高,当小于=0时用默认
     * @param startX 窗口位于屏幕中的X轴起始位置,当小于=0时用默认
     * @param startY 窗口位于屏幕中的Y轴起始位置
     */
    @Override
    public void startWindow(int width, int height, float startX, float startY) {
        startWindow(width,height,startX,startY,0);
    }

    /**
     * 开启Activity级别的小窗口播放
     * @param width 窗口播放器的宽,当小于=0时用默认
     * 开启可拖拽的窗口播放
     * 默认宽为屏幕1/2+30dp,高为1/2+30dp的16:9比例,X起始位置为:播放器原宿主的右下方,距离原宿主View顶部15dp,右边15dp(如果原宿主不存在,则位于屏幕右上角距离顶部60dp位置)
     * 全局悬浮窗口和局部小窗口不能同时开启
     * 横屏下不允许开启
     * @param height 窗口播放器的高,当小于=0时用默认
     * @param startX 窗口位于屏幕中的X轴起始位置,当小于=0时用默认
     * @param startY 窗口位于屏幕中的Y轴起始位置
     * @param radius 窗口的圆角 单位:像素
     */
    @Override
    public void startWindow(int width, int height, float startX, float startY, float radius) {
        startWindow(width,height,startX,startY,radius,Color.parseColor("#99000000"));
    }

    /**
     * 开启Activity级别的小窗口播放
     * @param width 窗口播放器的宽,当小于=0时用默认
     * 开启可拖拽的窗口播放
     * 默认宽为屏幕1/2+30dp,高为1/2+30dp的16:9比例,X起始位置为:播放器原宿主的右下方,距离原宿主View顶部15dp,右边15dp(如果原宿主不存在,则位于屏幕右上角距离顶部60dp位置)
     * 全局悬浮窗口和局部小窗口不能同时开启
     * 横屏下不允许开启
     * @param height 窗口播放器的高,当小于=0时用默认
     * @param startX 窗口位于屏幕中的X轴起始位置,当小于=0时用默认
     * @param startY 窗口位于屏幕中的Y轴起始位置
     * @param radius 窗口的圆角 单位:像素
     * @param bgColor 窗口的背景颜色
     */
    @Override
    public void startWindow(int width, int height, float startX, float startY, float radius, int bgColor) {
        ILogger.d(TAG,"startWindow-->width:"+width+",height:"+height+",startX:"+startX+",startY:"+startY+",radius:"+radius+",bgColor:"+bgColor+",windowProperty:"+mIsWindowProperty+",screenOrientation:"+mScreenOrientation);
        if(mIsWindowProperty||mScreenOrientation==IMediaPlayer.ORIENTATION_LANDSCAPE) return;//已开启窗口模式或者横屏情况下不允许开启小窗口
        Activity activity = PlayerUtils.getInstance().getActivity(getViewContext());
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
            //如果传入的startX不存在，则startX起点位于屏幕宽度1/2-距离右侧15dp位置，startY起点位于宿主View的下方15dp处
            if(startX<=0&&null!=mParent){
                startX=(PlayerUtils.getInstance().getScreenWidth(getContext())/2-PlayerUtils.getInstance().dpToPxInt(30f))-PlayerUtils.getInstance().dpToPxInt(15f);
                startY=screenLocation[1]+mParent.getHeight()+PlayerUtils.getInstance().dpToPxInt(15f);
//                ILogger.d(TAG,"startWindow-->未传入X,Y轴,取父容器位置,startX:"+startX+",startY:"+startY);
            }
            //如果宿主也不存在，则startX起点位于屏幕宽度1/2-距离右侧15dp位置，startY起点位于屏幕高度-Window View 高度+15dp位置处
            if(startX<=0){
                startX=(PlayerUtils.getInstance().getScreenWidth(getContext())/2-PlayerUtils.getInstance().dpToPxInt(30f))-PlayerUtils.getInstance().dpToPxInt(15f);
                startY=PlayerUtils.getInstance().dpToPxInt(60f);
//                ILogger.d(TAG,"startWindow-->未传入X,Y轴或取父容器位置失败,startX:"+startX+",startY:"+startY);
            }
            ILogger.d(TAG,"startWindow-->width:"+width+",height:"+height+",startX:"+startX+",startY:"+startY);
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
            container.addPlayerView(this,width,height,startX,startY,radius,bgColor);//先将播放器包装到可托拽的容器中
            viewGroup.addView(container, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
        }
    }

    /**
     * 退出窗口播放
     */
    @Override
    public void quitWindow() {
//        ILogger.d(TAG,"quitWindow");
        Activity activity = PlayerUtils.getInstance().getActivity(getViewContext());
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
                    mParent.addView(this, mPlayerParams[2],new LayoutParams(mPlayerParams[0], mPlayerParams[1], Gravity.CENTER));//将自己还原到父容器的index位置
                }else{
                    mParent.addView(this, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
                }
//                ILogger.d(TAG,"quitWindow-->已退出窗口");
            }else{
                //通知宿主监听器触发返回事件
//                ILogger.d(TAG,"quitWindow-->退出窗口无宿主接收,通知控制器返回");
                if(null!= mController) mController.onBack();
            }
        }
    }

    /**
     * 开启\退出窗口播放
     */
    @Override
    public void toggleWindow() {
        if(mIsWindowProperty){
            quitWindow();
            return;
        }
        //非工作中状态不允许开启窗口模式
        if(null!=mIVideoPlayer&&!mIVideoPlayer.isWork()){
            return;
        }
        startWindow();
    }

    /**
     * 开启全局悬浮窗窗口播放
     * @return true:开启悬浮窗成功 false:开启悬浮窗失败
     */
    @Override
    public boolean startGlobalWindow() {
        return startGlobalWindow(0,0,0,0);
    }

    /**
     * 开启全局悬浮窗窗口播放
     * @param radius 窗口的圆角 单位:像素
     * 开启可拖拽的全局悬浮窗口播放
     * 默认宽为屏幕1/2+30dp,高为1/2+30dp的16:9比例,X起始位置为:播放器原宿主的右下方,距离原宿主View顶部15dp,右边15dp(如果原宿主不存在,则位于屏幕右上角距离顶部60dp位置),
     * 全局悬浮窗口和局部小窗口不能同时开启
     * 横屏下不允许开启,需要在取得悬浮窗权限之后再调用
     * 需要声明权限：
     *     <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
     *     <uses-permission android:name="android.permission.SYSTEM_OVERLAY_WINDOW" />
     * @param bgColor 窗口的背景颜色
     * @return true:开启悬浮窗成功 false:开启悬浮窗失败
     */
    @Override
    public boolean startGlobalWindow(float radius, int bgColor) {
        return startGlobalWindow(0,0,0,0,radius,bgColor);
    }

    /**
     * 开启全局悬浮窗窗口播放
     * @param width 窗口播放器的宽,当小于=0时用默认
     * 开启可拖拽的全局悬浮窗口播放
     * 默认宽为屏幕1/2+30dp,高为1/2+30dp的16:9比例,X起始位置为:播放器原宿主的右下方,距离原宿主View顶部15dp,右边15dp(如果原宿主不存在,则位于屏幕右上角距离顶部60dp位置),
     * 全局悬浮窗口和局部小窗口不能同时开启
     * 横屏下不允许开启,需要在取得悬浮窗权限之后再调用
     * 需要声明权限：
     *     <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
     *     <uses-permission android:name="android.permission.SYSTEM_OVERLAY_WINDOW" />
     * @param height 窗口播放器的高,当小于=0时用默认
     * @param startX 窗口位于屏幕中的X轴起始位置,当小于=0时用默认
     * @param startY 窗口位于屏幕中的Y轴起始位置
     * @return true:开启悬浮窗成功 false:开启悬浮窗失败
     */
    @Override
    public boolean startGlobalWindow(int width, int height, float startX, float startY) {
        return startGlobalWindow(width,height,startX,startY,0);
    }

    /**
     * 开启全局悬浮窗窗口播放
     * @param width 窗口播放器的宽,当小于=0时用默认
     * 开启可拖拽的全局悬浮窗口播放
     * 默认宽为屏幕1/2+30dp,高为1/2+30dp的16:9比例,X起始位置为:播放器原宿主的右下方,距离原宿主View顶部15dp,右边15dp(如果原宿主不存在,则位于屏幕右上角距离顶部60dp位置),
     * 全局悬浮窗口和局部小窗口不能同时开启
     * 横屏下不允许开启,需要在取得悬浮窗权限之后再调用
     * 需要声明权限：
     *     <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
     *     <uses-permission android:name="android.permission.SYSTEM_OVERLAY_WINDOW" />
     * @param height 窗口播放器的高,当小于=0时用默认
     * @param startX 窗口位于屏幕中的X轴起始位置,当小于=0时用默认
     * @param startY 窗口位于屏幕中的Y轴起始位置,当小于=0时用默认
     * @param radius 窗口的圆角 单位:像素
     * @return true:开启悬浮窗成功 false:开启悬浮窗失败
     */
    @Override
    public boolean startGlobalWindow(int width, int height, float startX, float startY, float radius) {
        return startGlobalWindow(width,height,startX,startY,radius,Color.parseColor("#99000000"));
    }

    /**
     * @param width 窗口播放器的宽,当小于=0时用默认
     * 开启可拖拽的全局悬浮窗口播放
     * 默认宽为屏幕1/2+30dp,高为1/2+30dp的16:9比例,X起始位置为:播放器原宿主的右下方,距离原宿主View顶部15dp,右边15dp(如果原宿主不存在,则位于屏幕右上角距离顶部60dp位置),
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
     * @return true:开启悬浮窗成功 false:开启悬浮窗失败
     */
    @Override
    public boolean startGlobalWindow(int width, int height, float startX, float startY, float radius, int bgColor) {
        ILogger.d(TAG,"startGlobalWindow-->width:"+width+",height:"+height+",startX:"+startX+",startY:"+startY+",radius:"+radius+",bgColor:"+bgColor+",windowProperty:"+mIsWindowProperty+",screenOrientation:"+mScreenOrientation);
        if(mIsWindowProperty||mScreenOrientation==IMediaPlayer.ORIENTATION_LANDSCAPE){
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
            Activity activity = PlayerUtils.getInstance().getActivity(getViewContext());
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
                    //如果传入的startX不存在，则startX起点位于屏幕宽度1/2-距离右侧15dp位置，startY起点位于宿主View的下方15dp处
                    if(startX<=0&&null!=parent){
                        startX=(PlayerUtils.getInstance().getScreenWidth(getContext())/2-PlayerUtils.getInstance().dpToPxInt(30f))-PlayerUtils.getInstance().dpToPxInt(15f);
                        startY=screenLocation[1]+parent.getHeight()+PlayerUtils.getInstance().dpToPxInt(15f);
//                        ILogger.d(TAG,"startGlobalWindow-->未传入X,Y轴,取父容器位置,startX:"+startX+",startY:"+startY);
                    }
                    //如果宿主也不存在，则startX起点位于屏幕宽度1/2-距离右侧15dp位置，startY起点位于屏幕高度-Window View 高度+15dp位置处
                    if(startX<=0){
                        startX=(PlayerUtils.getInstance().getScreenWidth(getContext())/2-PlayerUtils.getInstance().dpToPxInt(30f))-PlayerUtils.getInstance().dpToPxInt(15f);
                        startY=PlayerUtils.getInstance().dpToPxInt(60f);
//                        ILogger.d(TAG,"startGlobalWindow-->未传入X,Y轴或取父容器位置失败,startX:"+startX+",startY:"+startY);
                    }
                    ILogger.d(TAG,"startGlobalWindow-->width:"+width+",height:"+height+",startX:"+startX+",startY:"+startY);
                    //3.转场到window中,并指定宽高和x,y轴
                    boolean success= IWindowManager.getInstance().addGolbalWindow(getContext(), this, width, height, startX, startY,radius,bgColor);
//                    ILogger.d(TAG,"startGlobalWindow--悬浮窗创建结果："+success);
                    //4.改变播放器横屏或窗口播放状态
                    if(success){
                        setWindowPropertyPlayer(true,true);
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
        if(mIsWindowProperty){
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
        if(null!= mController) mController.enterPipWindowPlayer();
    }

    /**
     * 告诉播放器退出了画中画模式
     */
    @Override
    public void quitPipWindow() {
        if(null!= mController) mController.quitPipWindowPlayer();
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
    public boolean isWork() {
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
    public void setTempContext(Context context) {
        this.mTempContext=context;
    }

    public Context getTempContext() {
        return mTempContext;
    }

    //=================================提供给控制器或外界调用的公开方法=================================

    /**
     * 设置视图控制器
     * @param controller 继承VideoBaseController的控制器
     */
    @Override
    public void setController(BaseController controller) {
        this.mController=controller;
        PlayerUtils.getInstance().removeViewFromParent(mController);
        FrameLayout controllerView = (FrameLayout) findViewById(R.id.player_controller);
        if(null!=controllerView){
            controllerView.removeAllViews();
            if(null!= mController){
                mController.attachedVideoPlayerControl(this);//绑定播放器代理人
                controllerView.addView(mController,new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));//添加到播放器窗口
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
     * 是否允许返回(横屏时先退出横屏)
     * @return
     */
    @Override
    public boolean isBackPressed() {
        ILogger.d(TAG,"isBackPressed-->screenOrientation:"+mScreenOrientation+",isWindowProperty:"+mIsWindowProperty);
        //退出全屏模式
        if(mScreenOrientation==IMediaPlayer.ORIENTATION_LANDSCAPE){
            quitFullScreen();
            return false;
        }
        //退出局部窗口模式
        if(mIsWindowProperty){
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
        if(null!= mController) mController.onResumePlayer();
        if(null!=mIVideoPlayer) mIVideoPlayer.onResume();
    }

    /**
     * 尝试暂停播放
     */
    @Override
    public void onPause(){
        if(null!=mIVideoPlayer) mIVideoPlayer.onPause();
        if(null!= mController) mController.onPausePlayer();
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
        if(null!=mIVideoPlayer){
            mIVideoPlayer.onDestroy();
            mIVideoPlayer=null;
        }
        if(null!=mParent){
            mParent.removeAllViews();
            mParent=null;
        }
        mIsWindowProperty=false;mContinuityPlay=false;mDataSource=null;mAssetsSource=null;
        mOnPlayerActionListener=null;mScreenOrientation=IMediaPlayer.ORIENTATION_PORTRAIT;
    }
}