package com.android.iplayer.base;

import android.content.Context;
import com.android.iplayer.interfaces.IMediaPlayer;
import com.android.iplayer.listener.OnMediaEventListener;

/**
 * created by hty
 * 2022/6/28
 * Desc:多媒体解码器基类，自定义多媒体解码器必须继承此类重写&&赋值所关心的监听器
 * 1、自定义解码器实现请参考com.android.iplayer.media.core.MediaPlayer类
 * 2、所有子类必须将OnMediaEventListener事件回调给mListener
 */
public abstract class AbstractMediaPlayer implements IMediaPlayer {

    protected final String TAG = AbstractMediaPlayer.class.getSimpleName();
    protected Context mContext ;
    protected OnMediaEventListener mListener;//播放器监听器

    public AbstractMediaPlayer(Context context){
        this.mContext=context;
    }

    protected Context getContext() {
        return mContext;
    }

    @Override
    public void setMediaEventListener(OnMediaEventListener listener) {
        this.mListener=listener;
    }

    @Override
    public void release() {
        mListener=null;
    }
}