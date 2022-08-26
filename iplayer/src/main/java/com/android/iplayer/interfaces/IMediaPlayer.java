package com.android.iplayer.interfaces;

import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.view.Surface;
import java.io.IOException;
import java.util.Map;

/**
 * created by hty
 * 2022/6/28
 * Desc:解码器扩展接口
 */
public interface IMediaPlayer {

    //播放器方向
    /** 竖屏 */
    int ORIENTATION_PORTRAIT    =0;
    /** 横屏 */
    int ORIENTATION_LANDSCAPE   =1;

    //画面渲染填充方式
    /** 原始大小填充模式,在视频宽高比例与手机宽高比例不一致时,播放可能留有黑边 */
    int MODE_ZOOM_TO_FIT = 0;
    /** 视频裁剪缩放模式,裁剪铺满全屏模式 */
    int MODE_ZOOM_CROPPING = 1;
    /** 拉伸铺满屏幕模式,视频完全填满显示窗口,视频与窗口比例不匹配画面会有变形 */
    int MODE_NOZOOM_TO_FIT = 2;

    /** 音视频开始渲染 */
    int MEDIA_INFO_VIDEO_RENDERING_START    = 3;
    /** 开始缓存数据 */
    int MEDIA_INFO_BUFFERING_START          = 701;
    /** 结束缓存数据 */
    int MEDIA_INFO_BUFFERING_END            = 702;
    /** 结束缓存数据 */
    int MEDIA_INFO_NETWORK_BANDWIDTH        = 703;
    /** 视频旋转变化了 */
    int MEDIA_INFO_VIDEO_ROTATION_CHANGED   = 10001;

    /** Input/Output相关错误,一般是网络超时 */
    int MEDIA_ERROR_IO = -1004;
    int MEDIA_ERROR_MALFORMED = -1007;
    int MEDIA_ERROR_UNSUPPORTED = -1010;
    int MEDIA_ERROR_TIMED_OUT = -110;
    //
    /** 不支持的流媒体协议 */
    int MEDIA_ERROR_UNSUPPORT_PROTOCOL  	= -10001;
    /** DNS解析失败 */
    int MEDIA_ERROR_DNS_PARSE_FAILED  		= -10002;
    /** 创建socket失败 */
    int MEDIA_ERROR_CREATE_SOCKET_FAILED 	= -10003;
    /** 连接服务器失败 */
    int MEDIA_ERROR_CONNECT_SERVER_FAILED 	= -10004;
    /** http请求返回400 */
    int MEDIA_ERROR_BAD_REQUEST 			= -10005;
    /** http请求返回401 */
    int MEDIA_ERROR_UNAUTHORIZED_CLIENT 	= -10006;
    /** http请求返回403 */
    int MEDIA_ERROR_ACCESSS_FORBIDDEN 		= -10007;
    /** http请求返回404 */
    int MEDIA_ERROR_TARGET_NOT_FOUND 		= -10008;
    int MEDIA_ERROR_FILE_NOT_FOUND 		    = -10000;
    /** http请求返回4xx */
    int MEDIA_ERROR_OTHER_ERROR_CODE 		= -10009;
    /** http请求返回5xx */
    int MEDIA_ERROR_SERVER_EXCEPTION 		= -10010;
    /** 无效的媒体数据 */
    int MEDIA_ERROR_INVALID_DATA 			= -10011;
    /** 不支持的视频编码类型 */
    int MEDIA_ERROR_UNSUPPORT_VIDEO_CODEC 	= -10012;
    /** 不支持的音频编码类型 */
    int MEDIA_ERROR_UNSUPPORT_AUDIO_CODEC 	= -10013;
    /** 视频解码失败 */
    int MEDIA_ERROR_VIDEO_DECODE_FAILED     = -10016;
    /** 音频解码失败 */
    int MEDIA_ERROR_AUDIO_DECODE_FAILED     = -10017;
    /** 8次以上3xx跳转 */
    int MEDIA_ERROR_3XX_OVERFLOW            = -10018;
    /** 播放地址无效,只在多URL播放时出现 */
    int MEDIA_ERROR_INVALID_URL             = -10019;

    /**
     * 设置是否循环播放
     * @param loop true:循环播放 false:禁止循环播放
     */
    void setLooping(boolean loop);

    /**
     * 设置当前播放音频,范围为 0.0f -- 1.0f,左右声道的音量建议一致
     * @param leftVolume  左声道音量
     * @param rightVolume 右声道音量
     */
    void setVolume(float leftVolume,float rightVolume);

    /**
     * 设置播放器缓存数据时长的最大阈值,只对直播有效,须在{@link #prepareAsync()}之前调用。该值较大,则主播和观众之间延迟较大,该值较小,则对网络波动更敏感,容易引发卡顿
     * @param timeSecond 播放器缓存的最大时长,单位:秒
     */
    void setBufferTimeMax(float timeSecond);

    /**
     * 绑定画面渲染视图
     * @param surface 视图渲染器
     */
    void setSurface(Surface surface);

    /**
     * 设置播放地址
     * @param dataSource 远程视频文件地址
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws IllegalStateException
     */
    void setDataSource(String dataSource) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    /**
     * 设置播放地址
     * @param dataSource 远程视频文件地址
     * @param headers header头部参数
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws IllegalStateException
     */
    void setDataSource(String dataSource, Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    /**
     * 设置播放地址
     * @param dataSource 远程视频文件地址
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws IllegalStateException
     */
    void setDataSource(AssetFileDescriptor dataSource) throws IOException, IllegalArgumentException, IllegalStateException;

    /**
     * 设置准备和读数据超时阈值,需在{@link #prepareAsync()}之前调用方可生效
     * @param prepareTimeout 准备超时阈值,即播放器在建立链接、解析流媒体信息的超时阈值
     * @param readTimeout    读数据超时阈值
     */
    void setTimeout(int prepareTimeout, int readTimeout);

    /**
     * 设置播放速度 从0.5f-2.0f
     * @param speed 从0.5f-2.0f
     */
    void setSpeed(float speed);

    /**
     * 快进快退
     * @param msec 目标时间点,单位:毫秒
     * @throws IllegalStateException
     */
    void seekTo(long msec) throws IllegalStateException;

    /**
     * 快进快退
     * @param msec     目标时间点,单位:毫秒
     * @param accurate 是否进行精准seek
     * @throws IllegalStateException
     */
    void seekTo(long msec, boolean accurate) throws IllegalStateException;

    /**
     * 返回播放器内部是否正在播放
     * @return true:正在播放 false:未正在播放
     */
    boolean isPlaying();

    /**
     * 返回当前播放进度
     * @return 播放进度,视频文件的时间戳,单位:毫秒
     */
    long getCurrentPosition();

    /**
     * 返回当前媒体文件的总时长长度
     * @return 单位:毫秒
     */
    long getDuration();

    /**
     * 返回当前缓冲进度
     * @return 单位:百分比
     */
    int getBuffer();

    /**
     * 同步播放器准备
     * @throws IOException
     * @throws IllegalStateException
     */
    void prepare() throws IOException, IllegalStateException;

    /**
     * 异步准备
     * @throws IllegalStateException
     */
    void prepareAsync() throws IllegalStateException;

    /**
     * 开始播放
     */
    void start();

    /**
     * 暂停播放
     */
    void pause();

    /**
     * 停止播放
     */
    void stop();

    /**
     * 清除播放器内部缓存
     */
    void reset();

    /**
     * 释放播放器内部缓存及状态,释放后所有已设置的监听器失效
     */
    void release();

    /**
     * 设置播放器准备监听器,当调用{@link #prepareAsync()}时必须实现异步监听接口,并在异步回调onPrepared()方法中开始播放视频
     * @param listener
     */
    void setOnPreparedListener(OnPreparedListener listener);

    /**
     * 设置播放完成监听器,当设置{@link #setLooping(boolean loop)}为true时,此回调设置无效
     * @param listener
     */
    void setOnCompletionListener(OnCompletionListener listener);

    /**
     * 设置缓冲监听器
     * @param listener
     */
    void setOnBufferingUpdateListener(OnBufferingUpdateListener listener);

    /**
     * 设置快进\快退监听器
     * @param listener
     */
    void setOnSeekCompleteListener(OnSeekCompleteListener listener);

    /**
     * 设置视频分辨率变化监听器
     * @param listener
     */
    void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener);

    /**
     * 设置错误监听器
     * @param listener
     */
    void setOnErrorListener(OnErrorListener listener);

    /**
     * 设置状态监听器
     * @param listener
     */
    void setOnInfoListener(OnInfoListener listener);

    /**
     * 设置字幕监听器
     * @param listener
     */
    void setOnTimedTextListener(OnTimedTextListener listener);

    /**
     * 设置自定义消息监听器,应用场景:播放器与自由服务器交互时消息透传
     * @param listener
     */
    void setOnMessageListener(OnMessageListener listener);

    /**
     * 与{@link #prepareAsync()}相对应,在调用{@link #prepareAsync()}之后且准备成功会发出onPrepared回调
     */
    interface OnPreparedListener {
        void onPrepared(IMediaPlayer mp);
    }

    /**
     * 播放完成时会发出此回调
     */
    interface OnCompletionListener {
        void onCompletion(IMediaPlayer mp);
    }

    interface OnBufferingUpdateListener {
        void onBufferingUpdate(IMediaPlayer mp, int percent);
    }

    /**
     * 在seek成功后会发出此回调
     */
    interface OnSeekCompleteListener {
        void onSeekComplete(IMediaPlayer mp);
    }

    /**
     * 视频的宽高发生变化时会有相应的回调
     */
    interface OnVideoSizeChangedListener {
        void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den);
    }

    /**
     * 错误监听器,播放器遇到错误时会将相应的错误码通过此回调接口告知开发者
     */
    interface OnErrorListener {
        boolean onError(IMediaPlayer mp, int what, int extra);
    }

    /**
     * 消息监听器,会将关于播放器的消息告知开发者,例如:视频渲染、音频渲染等
     */
    interface OnInfoListener {
        boolean onInfo(IMediaPlayer mp, int what, int extra);
    }

    /**
     * 字幕监听器
     */
    interface OnTimedTextListener {
        void onTimedText(IMediaPlayer mp, String text);
    }

    /**
     * 日志回调监听器,播放器会将收集的日志交予开发者
     */
    interface OnLogEventListener {
        void onLogEvent(IMediaPlayer mp, String log);
    }

    /**
     * 扩展：live直播流模式下消息通道监听器,可设置此监听器获取主播端通过消息通道发送的信息
     */
    interface OnMessageListener {
        void onMessage(IMediaPlayer mp, Bundle bundle);
    }
}