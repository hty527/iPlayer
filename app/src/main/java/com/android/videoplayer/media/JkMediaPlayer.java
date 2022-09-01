package com.android.videoplayer.media;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.android.iplayer.base.AbstractMediaPlayer;
import com.android.videoplayer.media.help.RawDataSourceProvider;
import java.io.IOException;
import java.util.Map;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.IjkTimedText;

/**
 * created by hty
 * 2022/7/1
 * Desc:IJK解码器示例
 */
public class JkMediaPlayer extends AbstractMediaPlayer {

    private IjkMediaPlayer mMediaPlayer;
    private int mBuffer;//缓冲进度

    public JkMediaPlayer(Context context) {
        super(context);
        if(null!=context){
            mMediaPlayer=new IjkMediaPlayer();
        }
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
        setDataSource(dataSource,null);
    }

    @Override
    public void setDataSource(String path, Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        if(null!=mMediaPlayer){
            try {
                Uri uri = Uri.parse(path);
                if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(uri.getScheme())) {
                    RawDataSourceProvider rawDataSourceProvider = RawDataSourceProvider.create(getContext(), uri);
                    mMediaPlayer.setDataSource(rawDataSourceProvider);
                } else {
                    //处理UA问题
                    if (headers != null&&headers.size()>0) {
                        String userAgent = headers.get("User-Agent");
                        if (!TextUtils.isEmpty(userAgent)) {
                            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "user_agent", userAgent);
                            // 移除header中的User-Agent，防止重复
                            headers.remove("User-Agent");
                        }
                        mMediaPlayer.setDataSource(getContext(), uri, headers);
                    }else{
                        mMediaPlayer.setDataSource(path);
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setDataSource(AssetFileDescriptor dataSource) throws IOException, IllegalArgumentException, IllegalStateException {
        if(null!=dataSource&&null!=mMediaPlayer){
            try {
                mMediaPlayer.setDataSource(new RawDataSourceProvider(dataSource));
            }catch (Throwable e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setTimeout(int prepareTimeout, int readTimeout) {
//        if(null!=mMediaPlayer) mMediaPlayer.setTimeout(prepareTimeout,readTimeout);
    }

    @Override
    public void setSpeed(float speed) {
        if(null!=mMediaPlayer) mMediaPlayer.setSpeed(speed);
    }

    @Override
    public void seekTo(long msec) throws IllegalStateException {
        if(null!=mMediaPlayer) mMediaPlayer.seekTo(msec);
    }

    @Override
    public void seekTo(long msec, boolean accurate) throws IllegalStateException {
        if(null!=mMediaPlayer) mMediaPlayer.seekTo(msec);
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
    public void prepare() throws IOException, IllegalStateException {}

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
            final IjkMediaPlayer mediaPlayer = mMediaPlayer;//用于在列表播放时避免卡顿
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
            final IjkMediaPlayer mediaPlayer = mMediaPlayer;//用于在列表播放时避免卡顿
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
        super.onRelease();
    }

    @Override
    public void setOnPreparedListener(OnPreparedListener listener) {
        this.mOnPreparedListener=listener;
        if(null!=mMediaPlayer) mMediaPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                if(null!=mOnPreparedListener) mOnPreparedListener.onPrepared(JkMediaPlayer.this);
            }
        });
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {
        this.mOnCompletionListener=listener;
        if(null!=mMediaPlayer) mMediaPlayer.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer mp) {
                if(null!=mOnCompletionListener) mOnCompletionListener.onCompletion(JkMediaPlayer.this);
            }
        });
    }

    @Override
    public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
        this.mOnBufferingUpdateListener=listener;
        if(null!=mMediaPlayer) mMediaPlayer.setOnBufferingUpdateListener(new IMediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(IMediaPlayer mp, int percent) {
                mBuffer=percent;
                if(null!=mOnBufferingUpdateListener) mOnBufferingUpdateListener.onBufferingUpdate(JkMediaPlayer.this,percent);
            }
        });
    }

    @Override
    public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
        this.mOnSeekCompleteListener=listener;
        if(null!=mMediaPlayer) mMediaPlayer.setOnSeekCompleteListener(new IMediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(IMediaPlayer mp) {
                if(null!=mOnSeekCompleteListener) mOnSeekCompleteListener.onSeekComplete(JkMediaPlayer.this);
            }
        });
    }

    @Override
    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener) {
        this.mOnVideoSizeChangedListener=listener;
        if(null!=mMediaPlayer) mMediaPlayer.setOnVideoSizeChangedListener(new IMediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
                if(null!=mOnVideoSizeChangedListener) mOnVideoSizeChangedListener.onVideoSizeChanged(JkMediaPlayer.this,width,height,sar_num,sar_den);
            }
        });
    }

    @Override
    public void setOnErrorListener(OnErrorListener listener) {
        this.mOnErrorListener=listener;
        if(null!=mMediaPlayer) mMediaPlayer.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                if(null!=mOnErrorListener) {
                    return mOnErrorListener.onError(JkMediaPlayer.this,what,extra);
                }
                return true;
            }
        });
    }

    @Override
    public void setOnInfoListener(OnInfoListener listener) {
        this.mOnInfoListener=listener;
        if(null!=mMediaPlayer) mMediaPlayer.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer mp, int what, int extra) {
                if(null!=mOnInfoListener){
                    return  mOnInfoListener.onInfo(JkMediaPlayer.this,what,extra);
                }
                return true;
            }
        });
    }

    @Override
    public void setOnTimedTextListener(OnTimedTextListener listener) {
        this.mOnTimedTextListener=listener;
        if(null!=mMediaPlayer) mMediaPlayer.setOnTimedTextListener(new IMediaPlayer.OnTimedTextListener() {
            @Override
            public void onTimedText(IMediaPlayer mp, IjkTimedText text) {
                if(null!=mOnTimedTextListener) mOnTimedTextListener.onTimedText(JkMediaPlayer.this,null!=text?text.getText():"");
            }
        });
    }

    @Override
    public void setOnMessageListener(OnMessageListener listener) {

    }
}