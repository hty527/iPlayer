package com.android.iplayer.base;

import android.content.Context;
import com.android.iplayer.interfaces.IMediaPlayer;

/**
 * created by hty
 * 2022/6/28
 * Desc:多媒体解码器基类，自定义多媒体解码器必须继承此类重写&&赋值所关心的监听器
 * 1、自定义解码器实现请参考com.android.iplayer.media.core.MediaPlayer类
 */
public abstract class AbstractMediaPlayer implements IMediaPlayer {

    protected final String TAG = AbstractMediaPlayer.class.getSimpleName();
    protected Context mContext ;
    protected OnPreparedListener mOnPreparedListener;//准备监听
    protected OnCompletionListener mOnCompletionListener;//播放完成
    protected OnBufferingUpdateListener mOnBufferingUpdateListener;//缓冲状态
    protected OnSeekCompleteListener mOnSeekCompleteListener;//快进\快退
    protected OnVideoSizeChangedListener mOnVideoSizeChangedListener;//视频分辨率大小
    protected OnErrorListener mOnErrorListener;//失败状态
    protected OnInfoListener mOnInfoListener;//状态信息
    protected OnTimedTextListener mOnTimedTextListener;//字母监听
    protected OnLogEventListener mOnLogEventListener;//日志监听
    protected OnMessageListener mOnMessageListener;//live直播模式下的消息透传监听

    public AbstractMediaPlayer(Context context){
        this.mContext=context;
    }

    protected Context getContext() {
        return mContext;
    }

    protected void onRelease(){
        mOnPreparedListener=null;mOnVideoSizeChangedListener=null;
        mOnCompletionListener=null;mOnErrorListener=null;
        mOnBufferingUpdateListener=null;mOnInfoListener=null;
        mOnSeekCompleteListener=null;mOnTimedTextListener=null;
        mOnLogEventListener=null;mOnMessageListener=null;
    }
}