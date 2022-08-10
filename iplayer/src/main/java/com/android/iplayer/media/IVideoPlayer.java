package com.android.iplayer.media;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.text.TextUtils;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.android.iplayer.R;
import com.android.iplayer.base.AbstractMediaPlayer;
import com.android.iplayer.base.BasePlayer;
import com.android.iplayer.interfaces.IMediaPlayer;
import com.android.iplayer.interfaces.IMediaPlayerControl;
import com.android.iplayer.manager.IVideoManager;
import com.android.iplayer.model.PlayerState;
import com.android.iplayer.utils.AudioFocus;
import com.android.iplayer.utils.PlayerUtils;
import com.android.iplayer.utils.ILogger;
import com.android.iplayer.utils.ThreadPool;
import com.android.iplayer.widget.MediaTextureView;
import java.util.Timer;
import java.util.TimerTask;

/**
 * created by hty
 * 2022/6/28
 * Desc:视频解码\播放\功能处理
 * 如需使用自定义解码器,请继承AbstractMediaPlayer
 */
public final class IVideoPlayer implements IMediaPlayer.OnBufferingUpdateListener,
        IMediaPlayer.OnCompletionListener, IMediaPlayer.OnPreparedListener, IMediaPlayer.OnInfoListener, IMediaPlayer.OnVideoSizeChangedListener,
        IMediaPlayer.OnErrorListener, IMediaPlayer.OnSeekCompleteListener, TextureView.SurfaceTextureListener, AudioFocus.OnAudioFocusListener {

    private static final String TAG = IVideoPlayer.class.getSimpleName();
    //播放器容器与播放器管理者绑定关系的监听器，必须实现监听
    private IMediaPlayerControl mIMediaPlayerControl;
    //播放器画面渲染核心
    private AbstractMediaPlayer mMediaPlayer;//视频格式文件解码器
    private MediaTextureView mTextureView;//画面渲染
    private Surface mSurface;
    private SurfaceTexture mSurfaceTexture;
    private AudioFocus mAudioFocusManager;//多媒体焦点监听,失去焦点暂停播放
    //内部播放器状态,初始为默认/重置状态
    private PlayerState sPlayerState = PlayerState.STATE_RESET;
    //是否循环播放/是否静音
    private boolean mLoop=false,mSoundMute=false;
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
    //播放超时\读取视频流超时时长
    private int mPrepareTimeout=10,mReadTimeout=15;

    /**
     * 播放状态,回调给播放控制器宿主
     * @param playerState 播放状态
     * @param message 描述信息
     */
    private void onPlayerState(PlayerState playerState, String message) {
        ILogger.d(TAG,"onPlayerState-->playerState:"+playerState+",message:"+message);
        if(null!= mIMediaPlayerControl) mIMediaPlayerControl.onPlayerState(playerState,message);
    }

    /**
     * 实时播放进度条,回调给播放控制器宿主
     * @param currentPosition 当前播放时长进度 毫秒
     * @param duration 总时长 毫秒
     * @param buffer 当前缓冲进度 百分比
     */
    private void onProgress(long currentPosition, long duration, int buffer) {
        if(null!= mIMediaPlayerControl) mIMediaPlayerControl.onProgress(currentPosition,duration,buffer);
    }

    //===========================================视频播放逻辑=========================================

    /**
     * 实例化一个播放器解码器,如果宿主自定义解码器则使用宿主自定义解码器,否则使用内部默认解码器
     * @return 返回一个自定义的MediaPlayer
     */
    private AbstractMediaPlayer newInstanceMediaPlayer() {
        AbstractMediaPlayer mediaPlayer;
        mediaPlayer = mIMediaPlayerControl.getMediaPlayer();
        if(null==mediaPlayer){
            Context context = mIMediaPlayerControl.getVideoPlayer().getContext();
            mediaPlayer=new MediaPlayer(context);
        }
        return mediaPlayer;
    }

    /**
     * 创建播放器
     */
    private boolean initMediaPlayer(){
        if(null!= mIMediaPlayerControl){
            mMediaPlayer = newInstanceMediaPlayer();
            BasePlayer videoPlayer = mIMediaPlayerControl.getVideoPlayer();
            ILogger.d(TAG,"initMediaPlayer-->解码器内核："+mMediaPlayer.getClass().getSimpleName());
            mMediaPlayer.setOnBufferingUpdateListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnInfoListener(this);
            mMediaPlayer.setOnVideoSizeChangedListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnSeekCompleteListener(this);
            mMediaPlayer.setLooping(mLoop);
            if(mSoundMute){
                mMediaPlayer.setVolume(0,0);
            }else{
                mMediaPlayer.setVolume(1.0f,1.0f);
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
        mTextureView=new MediaTextureView(context);
        mTextureView.setZoomMode(IVideoManager.getInstance().getZoomModel());
        mTextureView.setSaveFromParentEnabled(true);
        mTextureView.setDrawingCacheEnabled(false);
        mTextureView.setSurfaceTextureListener(this);
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
                PlayerUtils.getInstance().removeViewFromParent(mTextureView);
                releaseSurfaceTexture();
                mTextureView=null;mMediaPlayer=null;
            }
        }
    }

    //释放渲染组件
    private void releaseSurfaceTexture(){
//        ILogger.d(TAG,"releaseSurfaceTexture");
        try {
            if(null!=mSurfaceTexture){
                mSurfaceTexture.release();
            }
            if(null!=mSurface){
                mSurface.release();
            }
        }catch (Throwable e){
            e.printStackTrace();
        }finally {
            mSurfaceTexture=null;mSurface=null;
        }
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer mp, int percent) {
//        ILogger.d(TAG,"onBufferingUpdate-->percent:"+percent);
        if(null!= mIMediaPlayerControl) mIMediaPlayerControl.onBuffer(percent);
    }

    @Override
    public void onCompletion(IMediaPlayer mp) {
        ILogger.d(TAG,"onCompletion："+mLoop+",mp:"+mp);
        mSeekDuration=0;
        stopTimer();
        sPlayerState = PlayerState.STATE_COMPLETION;
        onPlayerState(sPlayerState,"播放结束");
    }

    @Override
    public boolean onError(IMediaPlayer mp, int what, int extra) {
        ILogger.d(TAG,"onError,what:"+what+",extra:"+extra);//直播拉流会有-38的错误
        stopTimer();
        sPlayerState = PlayerState.STATE_ERROR;
        onPlayerState(sPlayerState,getErrorMessage(what));
        return true;
    }

    /**
     * @param mp
     * @param what
     * @param extra
     * @return
     */
    @Override
    public boolean onInfo(IMediaPlayer mp, int what, int extra) {
        ILogger.d(TAG,"onInfo-->what:"+what+",extra:"+extra);
        if(what== IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START){//开始渲染
            sPlayerState = PlayerState.STATE_START;
            onPlayerState(sPlayerState,"首帧渲染");
            startTimer();
            if(mSeekDuration>0){
                long seekDuration =mSeekDuration;
                mSeekDuration=0;
                seekTo(seekDuration);
            }
            listenerAudioFocus();
        }else if(what== IMediaPlayer.MEDIA_INFO_BUFFERING_START){//缓冲开始
            sPlayerState = PlayerState.STATE_BUFFER;
            onPlayerState(sPlayerState,"缓冲开始");
        }else if(what== IMediaPlayer.MEDIA_INFO_BUFFERING_END||what==IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH){//缓冲结束
            sPlayerState = PlayerState.STATE_PLAY;
            onPlayerState(sPlayerState,"缓冲结束");
        }else{
            sPlayerState = PlayerState.STATE_PLAY;
            onPlayerState(sPlayerState,"缓冲结束");
        }
        return true;
    }

    @Override
    public void onPrepared(IMediaPlayer mp) {
        ILogger.d(TAG,"onPrepared-->seek:"+mSeekDuration);
        if(null!=mMediaPlayer){
            mp.start();
        }else{
            mSeekDuration=0;
            onError(null,0,0);
        }
    }

    @Override
    public void onSeekComplete(IMediaPlayer mp) {
        ILogger.d(TAG,"onSeekComplete,buffer:");
        mSeekDuration=0;
        startTimer();
        sPlayerState = PlayerState.STATE_PLAY;
        onPlayerState(sPlayerState,"快进快退恢复播放");
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
        ILogger.d(TAG,"onVideoSizeChanged,width:"+width+",height:"+height);
        this.mVideoWidth=width;
        this.mVideoHeight=height;
        if(null!=mTextureView){
            mTextureView.setMeasureSize(width,height);
            mTextureView.setZoomMode(IVideoManager.getInstance().getZoomModel());
        }
        if(null!=mIMediaPlayerControl) mIMediaPlayerControl.onVideoSizeChanged(width,height);
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
                return "播放失败,播放链接超时";
            case IMediaPlayer.MEDIA_ERROR_UNSUPPORT_PROTOCOL:
            case IMediaPlayer.MEDIA_ERROR_UNSUPPORT_VIDEO_CODEC:
                return "播放失败,不支持的视频文件格式";
            case IMediaPlayer.MEDIA_ERROR_DNS_PARSE_FAILED:
            case IMediaPlayer.MEDIA_ERROR_CREATE_SOCKET_FAILED:
            case IMediaPlayer.MEDIA_ERROR_CONNECT_SERVER_FAILED:
            case IMediaPlayer.MEDIA_ERROR_UNSUPPORT_AUDIO_CODEC:
                return "播放失败,链接DNS失败";
            case IMediaPlayer.MEDIA_ERROR_BAD_REQUEST:
            case IMediaPlayer.MEDIA_ERROR_UNAUTHORIZED_CLIENT:
            case IMediaPlayer.MEDIA_ERROR_ACCESSS_FORBIDDEN:
            case IMediaPlayer.MEDIA_ERROR_TARGET_NOT_FOUND:
            case IMediaPlayer.MEDIA_ERROR_FILE_NOT_FOUND:
            case IMediaPlayer.MEDIA_ERROR_OTHER_ERROR_CODE:
            case IMediaPlayer.MEDIA_ERROR_SERVER_EXCEPTION:
            case IMediaPlayer.MEDIA_ERROR_INVALID_DATA:
            case IMediaPlayer.MEDIA_ERROR_INVALID_URL:
                return "播放失败,请检查视频文件地址有效性";
            case IMediaPlayer.MEDIA_ERROR_VIDEO_DECODE_FAILED:
            case IMediaPlayer.MEDIA_ERROR_AUDIO_DECODE_FAILED:
                return "视频解码失败";
        }
        return null;
    }

    /**
     * 提供给宿主调用
     * @param dataSource
     */
    private void startPlayer(Object dataSource) {
        //检查播放地址
        if(!checkedDataSource()){
//            ILogger.d(TAG,"startPlayer-->地址为空");
            sPlayerState = PlayerState.STATE_ERROR;
            onPlayerState(sPlayerState,"播放地址为空,请检查!");
            return;
        }
        //检查是网络地址还是本地的资源视频地址
        boolean hasNet = PlayerUtils.getInstance().hasNet(mDataSource,mAssetsSource);
        //检查网络链接状态
        if(hasNet&&!PlayerUtils.getInstance().isCheckNetwork()){
//            ILogger.d(TAG,"startPlayer-->网络未连接");
            sPlayerState = PlayerState.STATE_ERROR;
            onPlayerState(sPlayerState,"网络未连接");
            return;
        }
        //检查移动流量网络下是否允许播放
        boolean mobileNetwork = PlayerUtils.getInstance().mobileNetwork(IVideoManager.getInstance().isMobileNetwork());
        if(hasNet&&!mobileNetwork){
//            ILogger.d(TAG,"startPlayer-->移动网络下");
            sPlayerState = PlayerState.STATE_MOBILE;
            onPlayerState(sPlayerState,"移动网络播放");
            return;
        }
        ILogger.d(TAG,"startPlayer-->");
        boolean result = createPlayer();
        if(result){
            sPlayerState = PlayerState.STATE_PREPARE;
            onPlayerState(sPlayerState,"播放准备中");
            try {
                if(dataSource instanceof String){
                    this.mDataSource = (String) dataSource;
                    ILogger.d(TAG,"startPlayer-->string");
                    mMediaPlayer.setDataSource(mDataSource);
                }else if(dataSource instanceof AssetFileDescriptor){
                    this.mAssetsSource = (AssetFileDescriptor) dataSource;
                    ILogger.d(TAG,"startPlayer-->assets");
                    mMediaPlayer.setDataSource(mAssetsSource);
                }
                mMediaPlayer.prepareAsync();
            } catch (Throwable e) {
                e.printStackTrace();
                sPlayerState = PlayerState.STATE_ERROR;
                onPlayerState(sPlayerState,"播放失败,error:"+e.getMessage());
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

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
//        ILogger.d(TAG,"onSurfaceTextureAvailable-->width:"+width+",height:"+height);
        if(null==mTextureView||null==mMediaPlayer) return;
        if(null!=mSurfaceTexture){
            mTextureView.setSurfaceTexture(mSurfaceTexture);
        }else{
            mSurfaceTexture = surfaceTexture;
            mSurface =new Surface(surfaceTexture);
            mMediaPlayer.setSurface(mSurface);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {}

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
//        ILogger.d(TAG,"onSurfaceTextureUpdated");
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
                                onProgress(mMediaPlayer.getCurrentPosition(),mMediaPlayer.getDuration(),0);
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
        if(null!=mTextureView&&null!=basePlayer){
            ViewGroup viewGroup = basePlayer.findViewById(R.id.player_surface);
            if(null!=viewGroup){
                PlayerUtils.getInstance().removeViewFromParent(mTextureView);
                viewGroup.removeAllViews();
                viewGroup.addView(mTextureView,new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT));
                mTextureView.requestLayout();
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
    public void onFocusStart() {
        ILogger.d(TAG,"onFocusStart-->");
    }

    /**
     * 失去音频焦点
     */
    @Override
    public void onFocusStop() {
        ILogger.d(TAG,"onFocusStop-->");
        if(isPlaying()){
            onPause();
        }
    }

    /**
     * 注册播放器监听器 必须实现
     * @param listener
     */
    public void setIMediaPlayerControl(IMediaPlayerControl listener) {
        this.mIMediaPlayerControl =listener;
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
     * 是否静音播放
     * @param soundMute
     */
    public void setSoundMute(boolean soundMute) {
        this.mSoundMute=soundMute;
        if(null!=mMediaPlayer){
            try {
                mMediaPlayer.setVolume(mSoundMute?0f:1.0f,mSoundMute?0f:1.0f);
            }catch (RuntimeException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置缩放模式
     * @param zoomModel 请适用IMediaPlayer类中定义的常量值
     */
    public void setZoomModel(int zoomModel) {
        IVideoManager.getInstance().setZoomModel(zoomModel);
        if(null!=mTextureView) mTextureView.setZoomMode(zoomModel);
    }

    /**
     * 是否监听并处理音频焦点事件
     * @param interceptTAudioFocus true:拦截，并在收到音频焦点失去后暂停播放 false:什么也不处理
     */
    public void setInterceptTAudioFocus(boolean interceptTAudioFocus){
        IVideoManager.getInstance().setInterceptTAudioFocus(interceptTAudioFocus);
    }

    /**
     * 设置播放器镜像角度
     * @param mirror
     */
    public void setMirror(boolean mirror) {
        if(null!=mTextureView){
            mTextureView.setMirror(mirror);
        }
    }

    /**
     * 设置倍速播放
     * @param speed
     */
    public void setSpeed(float speed) {
        if(null!=mMediaPlayer) mMediaPlayer.setSpeed(speed);
    }

    /**
     * 设置画面镜像角度
     * @param degree
     */
    public void setRotation(int degree) {
        if(null!=mTextureView) mTextureView.setRotation(degree);
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
            onPlayerState(sPlayerState,"播放地址为空,请检查!");
            return;
        }
        ILogger.d(TAG,"playOrPause-->source:"+dataSource+"\nstate:"+sPlayerState);
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
                onPlayerState(sPlayerState,"恢复播放");
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
            onPlayerState(sPlayerState,"暂停播放");
        }
    }

    /**
     * 主动完成播放,会回调COMPLETION状态给控制器和宿主
     */
    public void onCompletion() {
        stopTimer();
        releaseTextureView();
        sPlayerState = PlayerState.STATE_COMPLETION;
        onPlayerState(sPlayerState,"播放结束");
    }

    /**
     * 结束播放,不销毁内部播放地址,可能用户还会重新播放
     */
    public void onStop() {
        stopTimer();
        releaseTextureView();
        sPlayerState = PlayerState.STATE_STOP;
        onPlayerState(sPlayerState,"停止播放");
    }

    /**
     * 清除/还原播放器及播放状态
     */
    public void onReset() {
        stopTimer();
        releaseTextureView();
        mDataSource=null;mAssetsSource=null;mVideoWidth=0;mVideoHeight=0;
        sPlayerState = PlayerState.STATE_RESET;
        onPlayerState(sPlayerState,"结束播放并重置");
    }

    /**
     * 销毁播放器,一旦销毁内部所有持有对象将被回收
     */
    public void onDestroy() {
        stopTimer();
        ThreadPool.getInstance().reset();
        releaseTextureView();
        sPlayerState = PlayerState.STATE_DESTROY;
        onPlayerState(sPlayerState,"播放器销毁");
        if(null!=mAudioFocusManager){
            mAudioFocusManager.onDestroy();
            mAudioFocusManager=null;
        }
        mLoop=false;mSoundMute=false;mVideoWidth=0;mVideoHeight=0;mPrepareTimeout=0;mReadTimeout=0;
        IVideoManager.getInstance().setZoomModel(IMediaPlayer.MODE_ZOOM_CROPPING);
        mIMediaPlayerControl =null;mDataSource=null;mAssetsSource=null;
        mCallBackSpaceMilliss =DEFAULT_CALLBACK_TIME;
    }
}