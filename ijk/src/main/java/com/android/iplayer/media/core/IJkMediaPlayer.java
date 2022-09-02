package com.android.iplayer.media.core;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.android.iplayer.base.AbstractMediaPlayer;
import com.android.iplayer.media.RawDataSourceProvider;
import java.io.IOException;
import java.util.Map;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * created by hty
 * 2022/7/1
 * Desc:IJK解码器示例
 */
public class IJkMediaPlayer extends AbstractMediaPlayer implements IMediaPlayer.OnPreparedListener,
        IMediaPlayer.OnBufferingUpdateListener, IMediaPlayer.OnSeekCompleteListener, IMediaPlayer.OnVideoSizeChangedListener,
        IMediaPlayer.OnInfoListener, IMediaPlayer.OnCompletionListener, IMediaPlayer.OnErrorListener {

    private IjkMediaPlayer mMediaPlayer;
    private int mBuffer;//缓冲进度

    public IJkMediaPlayer(Context context) {
        this(context,false);
    }

    public IJkMediaPlayer(Context context, boolean isLive) {
        super(context);
        if(null!=context){
            mMediaPlayer=new IjkMediaPlayer();
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnBufferingUpdateListener(this);
            mMediaPlayer.setOnSeekCompleteListener(this);
            mMediaPlayer.setOnVideoSizeChangedListener(this);
            mMediaPlayer.setOnInfoListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnErrorListener(this);
            setOption(isLive);
        }
    }

    public IjkMediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    /**
     * 初始化Option设置
     * @param isLive 是否是应用于直播拉流模式
     */
    private void setOption(boolean isLive) {
        //解决seek跳转时，可能出现跳转的位置和自己选择的进度不一致，是因为seek只支持关键帧，视频的关键帧比较少导致的。
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
        if(isLive){
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "tcp");
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_flags", "prefer_tcp");
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);
            //增加rtmp打开速度. 没有缓存会黑屏1s.
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "buffer_size", 1024);//1316
            //丢帧阈值
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 30);
            //视频帧率
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "fps", 30);
            //环路滤波
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
            //设置无packet缓存 是否开启预缓冲，通常直播项目会开启，达到秒开的效果，不过带来了播放丢帧卡顿的体验
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);
            //不限制拉流缓存大小
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "infbuf", 1);
            //设置最大缓存数量
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "max-buffer-size", 1024);
            //设置最小解码帧数
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", 3);
            //启动预加载 须要准备好后自动播放
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1);
            //设置探测包数量
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probsize", "4096");
            //设置分析流时长
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", "2000000");
            //开启硬解码 硬解码失败 再自动切换到软解码
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0);
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 0);
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 0);
            /**
             * 播放延时的解决方案
             */
            // 每处理一个packet以后刷新io上下文
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1L);
            // 不额外优化（使能非规范兼容优化，默认值0 ）
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "fast", 1);
            // 自动旋屏
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 0);
            // 处理分辨率变化
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 0);
            // 最大缓冲大小,单位kb
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "max-buffer-size", 0);
            // 默认最小帧数2
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", 2);
            // 最大缓存时长
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max_cached_duration", 300);
            // 是否限制输入缓存数
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "infbuf", 1);
            // 缩短播放的rtmp视频延迟在1s内
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "nobuffer");
            // 播放前的探测Size，默认是1M, 改小一点会出画面更快
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 200); //1024L)
            // 设置是否开启环路过滤: 0开启，画面质量高，解码开销大，48关闭，画面质量差点，解码开销小
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48L);
            // 跳过帧 ？？
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_frame", 0);
            // 视频帧处理不过来的时候丢弃一些帧达到同步的效果
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 5);
        }
    }

    /**
     * 用户可自定义设置option
     * @param category
     * @param name
     * @param value
     */
    public void setOption(int category, String name, long value){
        if(null!=mMediaPlayer) mMediaPlayer.setOption(category,name,value);
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
        super.release();
    }

    @Override
    public void onPrepared(IMediaPlayer mp) {
        if(null!=mListener) mListener.onPrepared(IJkMediaPlayer.this);
    }

    @Override
    public void onCompletion(IMediaPlayer mp) {
        if(null!=mListener) mListener.onCompletion(IJkMediaPlayer.this);
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer mp, int percent) {
        mBuffer=percent;
        if(null!=mListener) mListener.onBufferUpdate(IJkMediaPlayer.this,percent);
    }

    @Override
    public void onSeekComplete(IMediaPlayer mp) {
        if(null!=mListener) mListener.onSeekComplete(IJkMediaPlayer.this);
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
        if(null!=mListener) mListener.onVideoSizeChanged(IJkMediaPlayer.this,width,height,sar_num,sar_den);
    }

    @Override
    public boolean onError(IMediaPlayer mp, int what, int extra) {
        if(null!=mListener) {
            return mListener.onError(IJkMediaPlayer.this,what,extra);
        }
        return true;
    }

    @Override
    public boolean onInfo(IMediaPlayer mp, int what, int extra) {
        if(null!=mListener){
            return mListener.onInfo(IJkMediaPlayer.this,what,extra);
        }
        return true;
    }
}