package com.android.iplayer.media.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Build;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.android.iplayer.base.AbstractMediaPlayer;
import com.android.iplayer.utils.PlayerUtils;
import java.io.IOException;
import java.util.Map;

/**
 * created by hty
 * 2022/6/28
 * Desc:默认的多媒体解码器
 */
public class MediaPlayer extends AbstractMediaPlayer implements android.media.MediaPlayer.OnBufferingUpdateListener,
        android.media.MediaPlayer.OnCompletionListener,android.media.MediaPlayer.OnPreparedListener,
        android.media.MediaPlayer.OnSeekCompleteListener, android.media.MediaPlayer.OnVideoSizeChangedListener,
        android.media.MediaPlayer.OnInfoListener, android.media.MediaPlayer.OnErrorListener {

    private android.media.MediaPlayer mMediaPlayer;
    private int mBuffer;//缓冲进度

    public MediaPlayer(Context context) {
        super(context);
        mMediaPlayer=new android.media.MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnSeekCompleteListener(this);
        mMediaPlayer.setOnVideoSizeChangedListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    @Override
    public void setLooping(boolean loop) {
        if(null!=mMediaPlayer) mMediaPlayer.setLooping(loop);
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        if(null!=mMediaPlayer) mMediaPlayer.setVolume(leftVolume,rightVolume);
    }

    @Override
    public void setBufferTimeMax(float timeSecond) {
//        if(null!=mMediaPlayer) mMediaPlayer.setBufferTimeMax(timeSecond);
    }

    @Override
    public void setSurface(Surface surface) {
        if(null!=mMediaPlayer) mMediaPlayer.setSurface(surface);
    }

    @Override
    public void setDisplay(SurfaceHolder surfaceHolder) {
        if(null!=mMediaPlayer) mMediaPlayer.setDisplay(surfaceHolder);
    }

    @Override
    public void setDataSource(String dataSource) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        if(null!=mMediaPlayer){
            try {
                Uri uri = Uri.parse(dataSource);
                mMediaPlayer.setDataSource(null==getContext()? PlayerUtils.getInstance().getContext():getContext(),uri);
            }catch (Throwable e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setDataSource(String path, Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        if(null!=mMediaPlayer){
            try {
                mMediaPlayer.setDataSource(getContext(), Uri.parse(path),headers);
            }catch (Throwable e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setDataSource(AssetFileDescriptor dataSource) throws IOException, IllegalArgumentException, IllegalStateException {
        if(null!=mMediaPlayer) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    mMediaPlayer.setDataSource(dataSource);
                }catch (Throwable e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void setTimeout(long prepareTimeout, long readTimeout) {
//        if(null!=mMediaPlayer) mMediaPlayer.setTimeout(prepareTimeout,readTimeout);
    }

    @Override
    public void setSpeed(float speed) {}

    @Override
    public void seekTo(long msec) throws IllegalStateException {
        if(null!=mMediaPlayer) mMediaPlayer.seekTo((int) msec);
    }

    @SuppressLint({"WrongConstant", "NewApi"})
    @Override
    public void seekTo(long msec, boolean accurate) throws IllegalStateException {
        if(null!=mMediaPlayer){
            if((Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)){
                mMediaPlayer.seekTo(msec,accurate?1:0);
            }else{
                mMediaPlayer.seekTo((int) msec);
            }
        }
    }

    @Override
    public boolean isPlaying() {
        if(null!=mMediaPlayer){
            return mMediaPlayer.isPlaying();
        }
        return false;
    }

    @Override
    public long getCurrentPosition() {
        if(null!=mMediaPlayer){
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public long getDuration() {
        if(null!=mMediaPlayer){
            return mMediaPlayer.getDuration();
        }
        return 0;
    }

    @Override
    public int getBuffer() {
        return mBuffer;
    }

    @Override
    public void prepare() throws IOException, IllegalStateException {
        if(null!=mMediaPlayer) mMediaPlayer.prepare();
    }

    @Override
    public void prepareAsync() throws IllegalStateException{
        if(null!=mMediaPlayer) mMediaPlayer.prepareAsync();
    }

    @Override
    public void start() {
        if(null!=mMediaPlayer) mMediaPlayer.start();
    }

    @Override
    public void pause() {
        if(null!=mMediaPlayer) mMediaPlayer.pause();
    }

    @Override
    public void stop() {
        if(null!=mMediaPlayer) mMediaPlayer.stop();
    }

    @Override
    public void reset() {
        mBuffer=0;
        if(null!=mMediaPlayer){
            final android.media.MediaPlayer mediaPlayer = mMediaPlayer;//用于在列表播放时避免卡顿
            new Thread() {
                @Override
                public void run() {
                    try {
                        mediaPlayer.reset();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    @Override
    public void release() {
        mBuffer=0;
        if(null!=mMediaPlayer){
            mMediaPlayer.setOnErrorListener(null);
            mMediaPlayer.setOnCompletionListener(null);
            mMediaPlayer.setOnInfoListener(null);
            mMediaPlayer.setOnBufferingUpdateListener(null);
            mMediaPlayer.setOnPreparedListener(null);
            mMediaPlayer.setOnVideoSizeChangedListener(null);
            mMediaPlayer.setSurface(null);
            mMediaPlayer.setDisplay(null);
            final android.media.MediaPlayer mediaPlayer = mMediaPlayer;//用于在列表播放时避免卡顿
            mMediaPlayer=null;
            new Thread() {
                @Override
                public void run() {
                    try {
                        mediaPlayer.release();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
        super.release();
    }

    @Override
    public void onPrepared(android.media.MediaPlayer mediaPlayer) {
        if(null!=mListener) mListener.onPrepared(MediaPlayer.this);
    }

    @Override
    public void onBufferingUpdate(android.media.MediaPlayer mediaPlayer, int percent) {
        mBuffer=percent;
        if(null!=mListener) mListener.onBufferUpdate(MediaPlayer.this,percent);
    }

    @Override
    public void onCompletion(android.media.MediaPlayer mediaPlayer) {
        if(null!=mListener) mListener.onCompletion(MediaPlayer.this);
    }

    @Override
    public void onSeekComplete(android.media.MediaPlayer mediaPlayer) {
        if(null!=mListener) mListener.onSeekComplete(MediaPlayer.this);
    }

    @Override
    public void onVideoSizeChanged(android.media.MediaPlayer mediaPlayer, int width, int height) {
        if(null!=mListener) mListener.onVideoSizeChanged(MediaPlayer.this,width,height,0,0);
    }

    @Override
    public boolean onInfo(android.media.MediaPlayer mediaPlayer, int what, int extra) {
        if(null!=mListener) {
            return mListener.onInfo(MediaPlayer.this,what,extra);
        }
        return true;
    }

    @Override
    public boolean onError(android.media.MediaPlayer mediaPlayer, int what, int extra) {
        if(null!=mListener){
            return mListener.onError(MediaPlayer.this,what,extra);
        }
        return true;
    }
}