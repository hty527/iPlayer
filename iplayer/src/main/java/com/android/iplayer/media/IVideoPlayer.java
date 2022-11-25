package com.android.iplayer.media;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.android.iplayer.R;
import com.android.iplayer.base.AbstractMediaPlayer;
import com.android.iplayer.base.BasePlayer;
import com.android.iplayer.interfaces.IBasePlayer;
import com.android.iplayer.interfaces.IRenderView;
import com.android.iplayer.listener.OnMediaEventListener;
import com.android.iplayer.manager.IVideoManager;
import com.android.iplayer.media.core.MediaPlayerFactory;
import com.android.iplayer.model.PlayerState;
import com.android.iplayer.utils.AudioFocus;
import com.android.iplayer.utils.ILogger;
import com.android.iplayer.utils.PlayerUtils;
import com.android.iplayer.utils.ThreadPool;
import com.android.iplayer.widget.view.MediaTextureView;
import java.util.Timer;
import java.util.TimerTask;

/**
 * created by hty
 * 2022/6/28
 * Desc:视频解码\播放\进度更新\特性功能等处理
 * 1、可支持用户自定义视频解码器，内部默认使用系统的MediaPlayer解码器。详细使用请参考BasePlayer文档描述
 */
public final class IVideoPlayer implements OnMediaEventListener , AudioFocus.OnAudioFocusListener {

    private static final String TAG = IVideoPlayer.class.getSimpleName();
    //播放器容器与播放器管理者绑定关系的监听器，必须实现监听
    private IBasePlayer mBasePlayer;
    //播放器画面渲染核心
    private AbstractMediaPlayer mMediaPlayer;//视频格式文件解码器
    private IRenderView mRenderView;//画面渲染
    private AudioFocus mAudioFocusManager;//多媒体焦点监听,失去焦点暂停播放
    //内部播放器状态,初始为默认/重置状态
    private PlayerState sPlayerState = PlayerState.STATE_RESET;
    //是否循环播放/是否静音/是否镜像
    private boolean mLoop=false,mSoundMute=false,mMirrors=false;
    //左右声道设置
    private float mLeftVolume=1.0f,mRightVolume=1.0f;
    //裁剪缩放模式，默认为原始大小，定宽等高，可能高度会留有黑边
    private int mZoomMode=IMediaPlayer.MODE_ZOOM_TO_FIT;
    //远程资源地址
    private String mDataSource;
    private AssetFileDescriptor mAssetsSource;//Assetss资产目录下的文件地址
    //进度计时器
    private PlayerTimerTask mPlayerTimerTask;
    private Timer mTimer;
    //播放进度回调间隔时间,默认的播放器进度间隔1秒回调
    private long DEFAULT_CALLBACK_TIME=1000;
    private long mCallBackSpaceMilliss =DEFAULT_CALLBACK_TIME;
    //需要跳转的进度位置
    private long mSeekDuration;
    //视频宽、高
    private int mVideoWidth,mVideoHeight;
    //链接视频源超时时长\读取视频流超时时长
    private int mPrepareTimeout=10*1000,mReadTimeout=15*1000;
    //链接视频文件发生错误的重试次数
    private int mReCatenationCount=3;
    private int mReCount;//重试的次数

    /**
     * 播放状态,回调给播放控制器宿主
     * @param playerState 播放状态
     * @param message 描述信息
     */
    private void onPlayerState(PlayerState playerState, String message) {
//        ILogger.d(TAG,"onPlayerState-->playerState:"+playerState+",message:"+message);
        if(null!= mBasePlayer) mBasePlayer.onPlayerState(playerState,message);
    }

    /**
     * 实时播放进度条,回调给播放控制器宿主
     * @param currentPosition 当前播放时长进度 毫秒
     * @param duration 总时长 毫秒
     */
    private void onProgress(long currentPosition, long duration) {
        if(null!= mBasePlayer) mBasePlayer.onProgress(currentPosition,duration);
    }

    //===========================================视频播放逻辑=========================================

    /**
     * 实例化一个播放器解码器,如果宿主自定义解码器则使用宿主自定义解码器,否则使用内部默认解码器
     * @return 返回一个自定义的MediaPlayer
     */
    private AbstractMediaPlayer newInstanceMediaPlayer() {
        AbstractMediaPlayer mediaPlayer;
        mediaPlayer = mBasePlayer.getMediaPlayer();
        if(null==mediaPlayer){
            Context context = mBasePlayer.getVideoPlayer().getContext();
            mediaPlayer= MediaPlayerFactory.create().createPlayer(context);
        }
        return mediaPlayer;
    }

    /**
     * 实例化一个播放器画面渲染器,如果宿主自定义渲染器则使用宿主自定义渲染器,否则使用内部默认渲染器
     * @param context 上下文
     * @return 返回一个自定义的VideoRenderView
     */
    private IRenderView newInstanceRenderView(Context context) {
        IRenderView renderView;
        renderView = mBasePlayer.getRenderView();
        if(null==renderView){
            renderView=new MediaTextureView(context);
        }
        renderView.attachMediaPlayer(mMediaPlayer);
        return renderView;
    }

    /**
     * 创建播放器
     */
    private boolean initMediaPlayer(){
        if(null!= mBasePlayer){
            mMediaPlayer = newInstanceMediaPlayer();
            BasePlayer videoPlayer = mBasePlayer.getVideoPlayer();
            ILogger.d(TAG,getString(R.string.player_core_name,"解码器内核：")+mMediaPlayer.getClass().getSimpleName());
            mMediaPlayer.setMediaEventListener(this);
            mMediaPlayer.setLooping(mLoop);
            if(mSoundMute){
                mMediaPlayer.setVolume(0,0);
            }else{
                mMediaPlayer.setVolume(mLeftVolume,mRightVolume);
            }
            //设置播放参数
            mMediaPlayer.setBufferTimeMax(2.0f);
            mMediaPlayer.setTimeout(mPrepareTimeout, mReadTimeout);
            initTextureView(videoPlayer.getContext());
            attachedVideoView(videoPlayer);
            return true;
        }
        return false;
    }

    private void initTextureView(Context context){
        if(null==context) return;
        mRenderView = newInstanceRenderView(context);
        ILogger.d(TAG,getString(R.string.player_render_name,"渲染器内核：")+mRenderView.getClass().getSimpleName());
    }

    //释放解码器\移除画面组件
    private void releaseTextureView(){
//        ILogger.d(TAG,"releaseTextureView");
        if(null!=mMediaPlayer){
            try {
                if(mMediaPlayer.isPlaying()){
                    mMediaPlayer.stop();
                }
//                mMediaPlayer.reset();//别重置了,直接销毁
                mMediaPlayer.release();//这个方法有点卡顿,请解码器内部做好处理
            }catch (Throwable e){
                e.printStackTrace();
            }finally {
                if(null!=mRenderView){
                    PlayerUtils.getInstance().removeViewFromParent(mRenderView.getView());
                }
                releaseSurfaceTexture();
                mRenderView =null;mMediaPlayer=null;
            }
        }
    }

    //释放渲染组件
    private void releaseSurfaceTexture(){
//        ILogger.d(TAG,"releaseSurfaceTexture");
        if(null!=mRenderView) mRenderView.release();
    }

    private void firstPlay() {
        sPlayerState = PlayerState.STATE_START;
        onPlayerState(sPlayerState,getString(R.string.player_media_start,"首帧渲染"));
        startTimer();
        if(mSeekDuration>0){
            long seekDuration =mSeekDuration;
            mSeekDuration=0;
            seekTo(seekDuration);
        }
        listenerAudioFocus();
    }

    @Override
    public void onPrepared(IMediaPlayer mp) {
        ILogger.d(TAG,"onPrepared-->seek:"+mSeekDuration);
        mReCount =0;//重置重试次数
        if(null!=mMediaPlayer){
            mp.start();
            firstPlay();
        }else{
            mSeekDuration=0;
            onError(null,0,0);
        }
    }

    @Override
    public void onBufferUpdate(IMediaPlayer mp, int percent) {
//        ILogger.d(TAG,"onBufferingUpdate-->percent:"+percent);
        if(null!= mBasePlayer) mBasePlayer.onBuffer(percent);
    }

    @Override
    public void onSeekComplete(IMediaPlayer mp) {
        ILogger.d(TAG,"onSeekComplete,buffer:");
        mSeekDuration=0;
        startTimer();
        sPlayerState = PlayerState.STATE_PLAY;
        onPlayerState(sPlayerState,getString(R.string.player_media_seek,"快进快退恢复播放"));
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
        ILogger.d(TAG,"onVideoSizeChanged,width:"+width+",height:"+height);
        this.mVideoWidth=width;
        this.mVideoHeight=height;
        if(null!= mRenderView){
            mRenderView.setVideoSize(width,height);
            mRenderView.setZoomMode(mZoomMode);
            mRenderView.setMirror(mMirrors);
            mRenderView.setSarSize(sar_num,sar_den);
        }
        if(null!= mBasePlayer) mBasePlayer.onVideoSizeChanged(width,height);
    }

    /**
     * @param mp
     * @param what
     * @param extra
     * @return
     */
    @Override
    public boolean onInfo(IMediaPlayer mp, int what, int extra) {
//        ILogger.d(TAG,"onInfo-->what:"+what+",extra:"+extra);
        switch (what) {
            case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START://开始首帧渲染

                break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_START://缓冲开始
                sPlayerState = PlayerState.STATE_BUFFER;
                onPlayerState(sPlayerState,getString(R.string.player_media_buffer_start,"缓冲开始"));
                break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_END://缓冲结束
            case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH://缓冲结束
                sPlayerState = PlayerState.STATE_PLAY;
                onPlayerState(sPlayerState,getString(R.string.player_media_buffer_end,"缓冲结束"));
                break;
            case IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED://视频旋转变化了
                if(null!= mRenderView) mRenderView.setDegree(extra);
                break;
        }
        return true;
    }

    @Override
    public void onCompletion(IMediaPlayer mp) {
        ILogger.d(TAG,"onCompletion："+mLoop+",mp:"+mp);
        mSeekDuration=0;
        stopTimer();
        sPlayerState = PlayerState.STATE_COMPLETION;
        onPlayerState(sPlayerState,getString(R.string.player_media_completion,"播放完成"));
    }

    @Override
    public boolean onError(IMediaPlayer mp, int what, int extra) {
        ILogger.e(TAG,"onError,what:"+what+",extra:"+extra+",reCount:"+ mReCount);//直播拉流会有-38的错误
        if(-38==what) return true;
        if(-10000==what&& mReCount <mReCatenationCount&&null!=mMediaPlayer){
            reCatenation();
            return true;
        }
        stopTimer();
        sPlayerState = PlayerState.STATE_ERROR;
        onPlayerState(sPlayerState,getErrorMessage(what));
        return true;
    }

    /**
     * 内部重试
     */
    private void reCatenation() {
        mReCount +=1;
        startPlayer(getDataSource());
    }

    /**
     * 返回错误描述
     * @param what
     * @return
     */
    private String getErrorMessage(int what) {
        switch (what) {
            case IMediaPlayer.MEDIA_ERROR_IO:
            case IMediaPlayer.MEDIA_ERROR_MALFORMED:
            case IMediaPlayer.MEDIA_ERROR_UNSUPPORTED:
            case IMediaPlayer.MEDIA_ERROR_TIMED_OUT:
                return getString(R.string.player_media_error_timeout,"播放失败,播放链接超时");
            case IMediaPlayer.MEDIA_ERROR_UNSUPPORT_PROTOCOL:
            case IMediaPlayer.MEDIA_ERROR_UNSUPPORT_VIDEO_CODEC:
                return getString(R.string.player_media_error_file_invalid,"播放失败,不支持的视频文件格式");
            case IMediaPlayer.MEDIA_ERROR_DNS_PARSE_FAILED:
            case IMediaPlayer.MEDIA_ERROR_CREATE_SOCKET_FAILED:
            case IMediaPlayer.MEDIA_ERROR_CONNECT_SERVER_FAILED:
            case IMediaPlayer.MEDIA_ERROR_UNSUPPORT_AUDIO_CODEC:
                return getString(R.string.player_media_error_dns,"播放失败,链接DNS失败");
            case IMediaPlayer.MEDIA_ERROR_BAD_REQUEST:
            case IMediaPlayer.MEDIA_ERROR_UNAUTHORIZED_CLIENT:
            case IMediaPlayer.MEDIA_ERROR_ACCESSS_FORBIDDEN:
            case IMediaPlayer.MEDIA_ERROR_TARGET_NOT_FOUND:
            case IMediaPlayer.MEDIA_ERROR_FILE_NOT_FOUND:
            case IMediaPlayer.MEDIA_ERROR_OTHER_ERROR_CODE:
            case IMediaPlayer.MEDIA_ERROR_SERVER_EXCEPTION:
            case IMediaPlayer.MEDIA_ERROR_INVALID_DATA:
            case IMediaPlayer.MEDIA_ERROR_INVALID_URL:
                return getString(R.string.player_media_error_path_invalid,"播放失败,请检查视频文件地址有效性");
            case IMediaPlayer.MEDIA_ERROR_VIDEO_DECODE_FAILED:
            case IMediaPlayer.MEDIA_ERROR_AUDIO_DECODE_FAILED:
                return getString(R.string.player_media_error_core,"视频解码失败");
            default:
                return what+"";
        }
    }

    /**
     * 提供给宿主调用
     * @param dataSource
     */
    private void startPlayer(Object dataSource) {
        //检查播放地址
        if(!checkedDataSource()){
            ILogger.d(TAG,"startPlayer-->地址为空");
            sPlayerState = PlayerState.STATE_ERROR;
            onPlayerState(sPlayerState,getString(R.string.player_media_error_path_empty,"播放地址为空,请检查!"));
            return;
        }
        //检查是网络地址还是本地的资源视频地址
        boolean hasNet = PlayerUtils.getInstance().hasNet(mDataSource,mAssetsSource);
        //检查网络链接状态
        if(hasNet&&!PlayerUtils.getInstance().isCheckNetwork()){
            ILogger.d(TAG,"startPlayer-->网络未连接");
            sPlayerState = PlayerState.STATE_ERROR;
            onPlayerState(sPlayerState,getString(R.string.player_media_error_net,"网络未连接"));
            return;
        }
        //检查移动流量网络下是否允许播放
        boolean mobileNetwork = PlayerUtils.getInstance().mobileNetwork(IVideoManager.getInstance().isMobileNetwork());
        if(hasNet&&!mobileNetwork){
            ILogger.d(TAG,"startPlayer-->移动网络下");
            sPlayerState = PlayerState.STATE_MOBILE;
            onPlayerState(sPlayerState,getString(R.string.player_media_mobile,"移动网络播放"));
            return;
        }
        boolean result = createPlayer();
        if(result){
            sPlayerState = PlayerState.STATE_PREPARE;
            onPlayerState(sPlayerState,getString(R.string.player_media_reday,"播放准备中"));
            try {
                if(dataSource instanceof String){
                    this.mDataSource = (String) dataSource;
                    mMediaPlayer.setDataSource(mDataSource);
                }else if(dataSource instanceof AssetFileDescriptor){
                    this.mAssetsSource = (AssetFileDescriptor) dataSource;
                    mMediaPlayer.setDataSource(mAssetsSource);
                }
                ILogger.d(TAG,"startPlayer-->source:"+(null!=mAssetsSource?mAssetsSource:mDataSource));
                mMediaPlayer.prepareAsync();
            } catch (Throwable e) {
                e.printStackTrace();
                sPlayerState = PlayerState.STATE_ERROR;
                onPlayerState(sPlayerState,getString(R.string.player_media_play_error,"播放失败,error:")+e.getMessage());
            }
        }else{
            sPlayerState = PlayerState.STATE_ERROR;
            onPlayerState(sPlayerState,"ViewGroup is avail");
        }
    }

    /**
     * 音频焦点监听
     */
    private void listenerAudioFocus() {
        if(IVideoManager.getInstance().isInterceptTAudioFocus()){
            if(null==mAudioFocusManager) mAudioFocusManager= new AudioFocus();
            mAudioFocusManager.requestAudioFocus(this);
        }
    }

    /**
     * 检查播放地址的有效性
     * @return
     */
    private boolean checkedDataSource() {
        if(!TextUtils.isEmpty(mDataSource)){
            return true;
        }
        if(null!=mAssetsSource){
            return true;
        }
        return false;
    }

    /**
     * 创建一个播放器
     */
    private boolean createPlayer() {
        releaseTextureView();
        boolean result = initMediaPlayer();
        return result;
    }

    /**
     * 从资源string中获取文字返回
     * @param id 源字符串ID
     * @param defaultStr 源字符串
     * @return
     */
    private String getString(int id,String defaultStr){
        Context context = PlayerUtils.getInstance().getContext();
        if(null!=context){
            return context.getResources().getString(id);
        }
        return defaultStr;
    }

    /**
     * 播放进度、闹钟倒计时进度 计时器
     */
    private class PlayerTimerTask extends TimerTask {
        @Override
        public void run() {
            try {
                if(null!=mMediaPlayer&&isPlaying()){
                    ThreadPool.getInstance().runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                onProgress(mMediaPlayer.getCurrentPosition(),mMediaPlayer.getDuration());
                            }catch (Throwable e){
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }catch (RuntimeException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 开始计时任务
     */
    private void startTimer() {
        if(null== mPlayerTimerTask){
            mTimer = new Timer();
            mPlayerTimerTask = new PlayerTimerTask();
            mTimer.schedule(mPlayerTimerTask, 0, mCallBackSpaceMilliss);
        }
    }

    /**
     * 结束计时任务
     */
    private void stopTimer() {
        if (null != mPlayerTimerTask) {
            mPlayerTimerTask.cancel();
            mPlayerTimerTask = null;
        }
        if (null != mTimer) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    /**
     * 视频解码及渲染View转场
     * @param basePlayer
     */
    private void attachedVideoView(BasePlayer basePlayer){
//        ILogger.d(TAG,"attachedVideoView:id:"+basePlayer);
        if(null!= mRenderView &&null!=basePlayer){
            ViewGroup viewGroup = basePlayer.findViewById(R.id.player_surface);
            if(null!=viewGroup){
                PlayerUtils.getInstance().removeViewFromParent(mRenderView.getView());
                viewGroup.removeAllViews();
                viewGroup.addView(mRenderView.getView(),new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT));
                mRenderView.requestDrawLayout();
            }
        }
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
     * 获得音频焦点
     */
    @Override
    public void onFocusStart() {}

    /**
     * 失去音频焦点
     */
    @Override
    public void onFocusStop() {
        if(isPlaying()){
            onPause();
        }
    }

    /**
     * 注册播放器监听器 必须实现
     * @param iBasePlayer
     */
    public void attachPlayer(IBasePlayer iBasePlayer) {
        this.mBasePlayer =iBasePlayer;
    }

    /**
     * 在开始播放前设置播放地址
     * @param dataSource raw或net地址
     */
    public void setDateSource(String dataSource) {
        this.mAssetsSource=null;
        this.mDataSource=dataSource;
    }

    /**
     * 在开始播放前设置播放地址
     * @param dataSource assets目录下的文件地址
     */
    public void setDateSource(AssetFileDescriptor dataSource) {
        this.mAssetsSource=dataSource;
        this.mDataSource=null;
    }

    /**
     * 设置是否循环播放
     * @param loop
     */
    public void setLoop(boolean loop) {
        this.mLoop=loop;
        if(null!=mMediaPlayer){
            try {
                mMediaPlayer.setLooping(loop);
            }catch (RuntimeException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置是否静音播放
     * @param soundMute true:无声 false:跟随系统音量
     * @return 是否静音,true:无声 false:跟随系统音量
     */
    public boolean setSoundMute(boolean soundMute) {
        this.mSoundMute=soundMute;
        if(null!=mMediaPlayer){
            try {
                mMediaPlayer.setVolume(mSoundMute?0f:1.0f,mSoundMute?0f:1.0f);
            }catch (RuntimeException e){
                e.printStackTrace();
            }
        }
        return mSoundMute;
    }

    /**
     * 返回播放器是否启用了静音模式
     * @return true:静音 false:系统原生
     */
    public boolean isSoundMute() {
        return mSoundMute;
    }

    /**
     * 静音、系统原生
     * @return true:静音 false:系统原生
     */
    public boolean toggleMute() {
        boolean isMute = !mSoundMute;
        setSoundMute(isMute);
        return isMute;
    }

    /**
     * 设置缩放模式
     * @param zoomModel 请适用IMediaPlayer类中定义的常量值
     */
    public void setZoomModel(int zoomModel) {
        this.mZoomMode=zoomModel;
        if(null!= mRenderView) mRenderView.setZoomMode(zoomModel);
    }

    /**
     * 设置画面旋转角度
     * @param degree 画面的旋转角度
     */
    public void setDegree(int degree) {
        if(null!= mRenderView) mRenderView.setDegree(degree);
    }

    /**
     * 是否监听并处理音频焦点事件
     * @param interceptTAudioFocus true:拦截，并在收到音频焦点失去后暂停播放 false:什么也不处理
     */
    public void setInterceptTAudioFocus(boolean interceptTAudioFocus){
        IVideoManager.getInstance().setInterceptTAudioFocus(interceptTAudioFocus);
    }

    /**
     * @param reCatenationCount 设置当播放器遇到链接视频文件失败时自动重试的次数，内部自动重试次数为3次
     */
    public void setReCatenationCount(int reCatenationCount) {
        this.mReCatenationCount = reCatenationCount;
    }

    /**
     * 设置画面渲染是否镜像
     * @param mirror true:镜像 false:正常
     * @return true:镜像 false:正常
     */
    public boolean setMirror(boolean mirror) {
        this.mMirrors=mirror;
        if(null!= mRenderView){
            return mRenderView.setMirror(mirror);
        }
        return false;
    }

    /**
     * 画面渲染是否镜像
     * @return true:镜像 false:正常
     */
    public boolean toggleMirror() {
        boolean isMirrors = ! mMirrors;
        return setMirror(isMirrors);
    }

    /**
     * 设置倍速播放
     * @param speed
     */
    public void setSpeed(float speed) {
        if(null!=mMediaPlayer) mMediaPlayer.setSpeed(speed);
    }

    /**
     * 设置左右声道音量，从0.0f-1.0f
     * @param leftVolume 设置左声道音量，1.0f-1.0f
     * @param rightVolume 设置右声道音量，1.0f-1.0f
     */
    public void setVolume(float leftVolume, float rightVolume) {
        this.mLeftVolume=leftVolume;this.mRightVolume=rightVolume;
        if(null!=mMediaPlayer) mMediaPlayer.setVolume(leftVolume,rightVolume);
    }

    /**
     * 设置View旋转角度
     * @param rotation
     */
    public void setRotation(int rotation) {
        if(null!= mRenderView) mRenderView.setViewRotation(rotation);
    }

    /**
     * 设置进度回调间隔时间长 单位：毫秒
     * @param callBackSpaceMilliss
     */
    public void setCallBackSpaceMilliss(long callBackSpaceMilliss) {
        mCallBackSpaceMilliss = callBackSpaceMilliss;
    }

    /**
     * 是否支持4G网络播放
     * @param mobileNetwork
     */
    public void setMobileNetwork(boolean mobileNetwork) {
        IVideoManager.getInstance().setMobileNetwork(mobileNetwork);
    }

    public int getVideoWidth() {
        return mVideoWidth;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }

    /**
     * 播放和暂停,推荐外部调用此方法
     */
    public void playOrPause(){
        playOrPause(getDataSource());
    }

    public void playOrPause(Object dataSource) {
        if(null==dataSource) {
            sPlayerState = PlayerState.STATE_ERROR;
            onPlayerState(sPlayerState,getString(R.string.player_media_error_path_empty,"播放地址为空,请检查!"));
            return;
        }
        switch (sPlayerState) {
            case STATE_RESET:
            case STATE_STOP:
            case STATE_MOBILE:
            case STATE_COMPLETION:
            case STATE_ERROR:
            case STATE_DESTROY:
                startPlayer(dataSource);
                break;
            case STATE_PREPARE:
            case STATE_BUFFER:
            case STATE_START:
            case STATE_PLAY:
            case STATE_ON_PLAY:
                onPause(true);
                break;
            case STATE_ON_PAUSE:
            case STATE_PAUSE://用户手动触发恢复播放时改变内部暂停状态为生命周期的暂停状态
                sPlayerState = PlayerState.STATE_ON_PAUSE;
                onResume();
                break;
        }
    }

    /**
     * 返回内部播放状态
     * @return
     */
    public boolean isPlaying(){
        try {
            return null!=mMediaPlayer&&(sPlayerState.equals(PlayerState.STATE_PREPARE)
                    || sPlayerState.equals(PlayerState.STATE_START)
                    || sPlayerState.equals(PlayerState.STATE_PLAY)
                    || sPlayerState.equals(PlayerState.STATE_ON_PLAY)
                    || sPlayerState.equals(PlayerState.STATE_BUFFER));
        }catch (RuntimeException e){

        }
        return false;
    }

    /**
     * 返回内部工作状态
     * @return
     */
    public boolean isWork(){
        try {
            return null!=mMediaPlayer&&(sPlayerState.equals(PlayerState.STATE_PREPARE)
                    || sPlayerState.equals(PlayerState.STATE_START)
                    || sPlayerState.equals(PlayerState.STATE_PLAY)
                    || sPlayerState.equals(PlayerState.STATE_ON_PLAY)
                    || sPlayerState.equals(PlayerState.STATE_PAUSE)
                    || sPlayerState.equals(PlayerState.STATE_ON_PAUSE)
                    || sPlayerState.equals(PlayerState.STATE_BUFFER));
        }catch (RuntimeException e){

        }
        return false;
    }

    public PlayerState getPlayerState() {
        return sPlayerState;
    }

    /**
     * 播放状态下允许快进、快退调节
     * @param msec 0:重新播放 其它:快进快退
     */
    public void seekTo(long msec) {
        if(msec<0||!checkedDataSource()) return;
        if(0==msec){
            playOrPause();
            return;
        }
        if(isWork()){
            try {
                if(null!=mMediaPlayer){
                    mMediaPlayer.seekTo(msec);
                }
            }catch (RuntimeException e){
                e.printStackTrace();
            }
        }else{
            //未开始播放情况下重新开始播放并立即跳转
            mSeekDuration=msec;
            playOrPause();
        }
    }

    /**
     * 播放状态下允许快进、快退调节
     * @param msec 0:重新播放 其它:快进快退
     * @param accurate 是否精准seek
     */
    public void seekTo(long msec, boolean accurate) {
        if(msec<0||!checkedDataSource()) return;
        if(isPlaying()){
            try {
                if(null!=mMediaPlayer){
                    mMediaPlayer.seekTo(msec,accurate);
                }
            }catch (RuntimeException e){
                e.printStackTrace();
            }
        }else{
            //未开始播放情况下重新开始播放并立即跳转到指定未知播放
            mSeekDuration=msec;
            playOrPause();
        }
    }

    /**
     * 设置超时时间
     * @param prepareTimeout 设置准备和读数据超时阈值,之前调用方可生效 准备超时阈值,即播放器在建立链接、解析流媒体信息的超时阈值
     * @param readTimeout    读数据超时阈值
     */
    public void setTimeout(int prepareTimeout, int readTimeout) {
        this.mPrepareTimeout=prepareTimeout;
        this.mReadTimeout=readTimeout;
        if(null!=mMediaPlayer) mMediaPlayer.setTimeout(prepareTimeout,readTimeout);
    }

    /**
     * 返回视频的总时长
     * @return 毫秒
     */
    public long getDurtion() {
        if(null!=mMediaPlayer){
            try {
                return mMediaPlayer.getDuration();
            }catch (RuntimeException e){
                e.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * 返回当前正在播放的位置
     * @return 毫秒
     */
    public long getCurrentPosition() {
        if(null!=mMediaPlayer){
            try {
                return mMediaPlayer.getCurrentPosition();
            }catch (RuntimeException e){
                e.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * 返回当前缓冲进度
     * @return 单位：百分比
     */
    public int getBuffer() {
        if(null!=mMediaPlayer){
            try {
                return mMediaPlayer.getBuffer();
            }catch (RuntimeException e){
                e.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * 生命周期调用恢复播放,在用户主动暂停的情况下不主动恢复播放
     * 手动点击暂停\恢复请调用playOrPause
     */
    public void onResume() {
        if(checkedDataSource()&&sPlayerState==PlayerState.STATE_ON_PAUSE){
            startTimer();
            try {
                if(null!=mMediaPlayer){
                    mMediaPlayer.start();
                }
                listenerAudioFocus();
                sPlayerState = PlayerState.STATE_ON_PLAY;
                onPlayerState(sPlayerState,getString(R.string.player_media_resume,"恢复播放"));
            }catch (RuntimeException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 生命周期调用暂停播放
     * 手动点击暂停\恢复请调用playOrPause
     */
    public void onPause() {
        onPause(false);
    }

    private void onPause(boolean isClick) {
        if(checkedDataSource()&&isPlaying()){
            stopTimer();
            try {
                if(null!=mMediaPlayer){
                    mMediaPlayer.pause();
                }
            }catch (RuntimeException e){
                e.printStackTrace();
            }
            sPlayerState = isClick?PlayerState.STATE_PAUSE:PlayerState.STATE_ON_PAUSE;
            onPlayerState(sPlayerState,getString(R.string.player_media_pause,"暂停播放"));
        }
    }

    /**
     * 主动完成播放,会回调COMPLETION状态给控制器和宿主
     */
    public void onCompletion() {
        stopTimer();
        releaseTextureView();
        sPlayerState = PlayerState.STATE_COMPLETION;
        onPlayerState(sPlayerState,getString(R.string.player_media_completion,"播放结束"));
    }

    /**
     * 结束播放,不销毁内部播放地址,可能用户还会重新播放
     */
    public void onStop() {
        stopTimer();
        releaseTextureView();
        sPlayerState = PlayerState.STATE_STOP;
        onPlayerState(sPlayerState,getString(R.string.player_media_stop,"停止播放"));
    }

    /**
     * 清除/还原播放器及播放状态
     */
    public void onReset() {
        stopTimer();
        releaseTextureView();
        mDataSource=null;mAssetsSource=null;mVideoWidth=0;mVideoHeight=0;
        mReCount =0;
        sPlayerState = PlayerState.STATE_RESET;
        onPlayerState(sPlayerState,getString(R.string.player_media_reset,"结束播放并重置"));
    }

    /**
     * 销毁播放器,一旦销毁内部所有持有对象将被回收
     */
    public void onDestroy() {
        stopTimer();
        ThreadPool.getInstance().reset();
        releaseTextureView();
        sPlayerState = PlayerState.STATE_DESTROY;
        onPlayerState(sPlayerState,getString(R.string.player_media_destroy,"播放器销毁"));
        if(null!=mAudioFocusManager){
            mAudioFocusManager.onDestroy();
            mAudioFocusManager=null;
        }
        mLoop=false;mSoundMute=false;mMirrors=false;mVideoWidth=0;mVideoHeight=0;mPrepareTimeout=0;mReadTimeout=0;mZoomMode=0;
        mReCount =0;
        mBasePlayer =null;mDataSource=null;mAssetsSource=null;mLeftVolume=1.0f;mRightVolume=1.0f;
        mCallBackSpaceMilliss =DEFAULT_CALLBACK_TIME;
    }
}