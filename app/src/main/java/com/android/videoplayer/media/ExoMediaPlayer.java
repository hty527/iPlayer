package com.android.videoplayer.media;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.android.iplayer.base.AbstractMediaPlayer;
import com.android.iplayer.interfaces.IMediaPlayer;
import com.android.videoplayer.media.help.ExoMediaSourceHelper;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.video.VideoSize;
import java.io.IOException;
import java.util.Map;

/**
 * created by hty
 * 2022/8/1
 * Desc:EXO解码器示例
 */
public class ExoMediaPlayer extends AbstractMediaPlayer implements Player.Listener {

    private ExoPlayer mMediaPlayer;
    private boolean isPlaying;//用这个boolean标记是否首帧播放

    public ExoMediaPlayer(Context context) {
        super(context);
        isPlaying=false;
        mMediaPlayer = new ExoPlayer.Builder(context,
                new DefaultRenderersFactory(context),
                new DefaultMediaSourceFactory(context))
                .build();
        mMediaPlayer.addListener(this);
    }

    @Override
    public void setLooping(boolean loop) {
        if(null!=mMediaPlayer) mMediaPlayer.setRepeatMode(loop?Player.REPEAT_MODE_ALL:Player.REPEAT_MODE_OFF);
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        if(null!=mMediaPlayer) mMediaPlayer.setVolume((leftVolume+rightVolume)/2);
    }

    @Override
    public void setBufferTimeMax(float timeSecond) {
        //不支持
    }

    @Override
    public void setSurface(Surface surface) {
        if(null!=mMediaPlayer) mMediaPlayer.setVideoSurface(surface);
    }

    @Override
    public void setDisplay(SurfaceHolder surfaceHolder) {
//        if(null!=mMediaPlayer) mMediaPlayer.setDisplay(surfaceHolder);
        if(null!=surfaceHolder){
            setSurface(surfaceHolder.getSurface());
        }else{
            setSurface(null);
        }
    }

    @Override
    public void setDataSource(String dataSource) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        setDataSource(dataSource,null);
    }

    @Override
    public void setDataSource(String dataSource, Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        if(null!=mMediaPlayer) mMediaPlayer.setMediaSource(ExoMediaSourceHelper.getInstance().getMediaSource(dataSource,headers));
    }

    @Override
    public void setDataSource(AssetFileDescriptor dataSource) throws IOException, IllegalArgumentException, IllegalStateException {
        //不支持
    }

    @Override
    public void setTimeout(int prepareTimeout, int readTimeout) {
        //不支持
    }

    @Override
    public void setSpeed(float speed) {
        if(null!=mMediaPlayer){
            PlaybackParameters parameters=new PlaybackParameters(speed);
            mMediaPlayer.setPlaybackParameters(parameters);
        }
    }

    @Override
    public void seekTo(long msec) throws IllegalStateException {
        seekTo(msec,true);
    }

    @Override
    public void seekTo(long msec, boolean accurate) throws IllegalStateException {
        if(null!=mMediaPlayer) mMediaPlayer.seekTo(msec);
    }

    /**
     * 是否正在播放 ExoMediaPlayer 解码器这个方法只能在main线程中被调用,否则会报错误：java.lang.IllegalStateException: Player is accessed on the wrong thread.
     * @return 返回是否正在播放
     */
    @Override
    public boolean isPlaying() {
        if (mMediaPlayer == null)
            return false;
        int state = mMediaPlayer.getPlaybackState();
        switch (state) {
            case Player.STATE_BUFFERING:
            case Player.STATE_READY:
                return mMediaPlayer.getPlayWhenReady();
            case Player.STATE_IDLE:
            case Player.STATE_ENDED:
            default:
                return false;
        }
    }

    /**
     * ExoMediaPlayer 解码器这个方法只能在main线程中被调用,否则会报错误：java.lang.IllegalStateException: Player is accessed on the wrong thread.
     * @return 返回当前正在播放的位置
     */
    @Override
    public long getCurrentPosition() {
        if(null!=mMediaPlayer){
            if(null!=mOnBufferingUpdateListener) mOnBufferingUpdateListener.onBufferingUpdate(this,mMediaPlayer.getBufferedPercentage());
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    /**
     * ExoMediaPlayer 解码器这个方法只能在main线程中被调用,否则会报错误：java.lang.IllegalStateException: Player is accessed on the wrong thread.
     * @return
     */
    @Override
    public long getDuration() {
        if(null!=mMediaPlayer){
            return mMediaPlayer.getDuration();
        }
        return 0;
    }

    @Override
    public int getBuffer() {
        try {
            if(null!=mMediaPlayer){
                return mMediaPlayer.getBufferedPercentage();
            }
        }catch (Throwable e){
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void prepare() throws IOException, IllegalStateException {
        isPlaying=false;
        if(null!=mMediaPlayer) mMediaPlayer.prepare();
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        isPlaying=false;
        if(null!=mMediaPlayer) mMediaPlayer.prepare();
    }

    @Override
    public void start() {
        if(null!=mMediaPlayer) mMediaPlayer.setPlayWhenReady(true);
    }

    @Override
    public void pause() {
        if(null!=mMediaPlayer) mMediaPlayer.setPlayWhenReady(false);
    }

    @Override
    public void stop() {
        if(null!=mMediaPlayer) mMediaPlayer.stop();
    }

    @Override
    public void reset() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.clearMediaItems();
            mMediaPlayer.setVideoSurface(null);
        }
    }

    @Override
    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.removeListener(this);
            mMediaPlayer.release();
        }
    }

    @Override
    public void setOnPreparedListener(OnPreparedListener listener) {
        this.mOnPreparedListener=listener;
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {
        this.mOnCompletionListener=listener;
    }

    @Override
    public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
        this.mOnBufferingUpdateListener=listener;
    }

    @Override
    public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
        this.mOnSeekCompleteListener=listener;
    }

    @Override
    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener) {
        this.mOnVideoSizeChangedListener=listener;
    }

    @Override
    public void setOnErrorListener(OnErrorListener listener) {
        this.mOnErrorListener=listener;
    }

    @Override
    public void setOnInfoListener(OnInfoListener listener) {
        this.mOnInfoListener=listener;
    }

    @Override
    public void setOnTimedTextListener(OnTimedTextListener listener) {
        this.mOnTimedTextListener=listener;
    }

    @Override
    public void setOnMessageListener(OnMessageListener listener) {

    }

    //==========================================EXO解码器回调=========================================

    @Override
    public void onPlaybackStateChanged(int playbackState) {
//        Logger.d(TAG,"onPlaybackStateChanged-->playbackState:"+playbackState+",isPlaying:"+isPlaying);
        switch (playbackState) {
            case Player.STATE_BUFFERING:
                if(null!=mOnInfoListener) mOnInfoListener.onInfo(this, IMediaPlayer.MEDIA_INFO_BUFFERING_START,0);
                break;
            case Player.STATE_READY:
                if(null!=mMediaPlayer) mMediaPlayer.setPlayWhenReady(true);
                if(null!=mOnInfoListener) mOnInfoListener.onInfo(this, isPlaying ? IMediaPlayer.MEDIA_INFO_BUFFERING_END : IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START,0);//如果还未进行过播放,则被认为是首帧播放
                isPlaying=true;
                break;
            case Player.STATE_ENDED:
                if(null!=mOnCompletionListener) mOnCompletionListener.onCompletion(null);
                break;
            default:
                if(null!=mOnInfoListener) mOnInfoListener.onInfo(this, playbackState,0);
        }
    }

    @Override
    public void onPlayerError(PlaybackException error) {
        if(null!=mOnErrorListener) mOnErrorListener.onError(this,error.errorCode,0);
    }

    @Override
    public void onVideoSizeChanged(VideoSize videoSize) {
        if(null!=mOnVideoSizeChangedListener) mOnVideoSizeChangedListener.onVideoSizeChanged(this, videoSize.width, videoSize.height,0,0);
    }
}