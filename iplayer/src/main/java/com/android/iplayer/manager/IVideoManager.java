package com.android.iplayer.manager;

/**
 * created by hty
 * 2022/7/3
 * Desc:播放器内部的公共设置等
 */
public final class IVideoManager {

    private volatile static IVideoManager mInstance;
    //是否支持在4G环境下播放
    private boolean mIsMobileNetwork;
    //是否监听并处理音频焦点事件？
    private boolean mInterceptTAudioFocus;//true:拦截，并在收到音频焦点失去后暂停播放 false:什么也不处理

    //调用入口
    public static synchronized IVideoManager getInstance() {
        synchronized (IVideoManager.class) {
            if (null == mInstance) {
                mInstance = new IVideoManager();
            }
        }
        return mInstance;
    }

    /**
     * 是否支持4G网络播放
     * @param mobileNetwork
     */
    public IVideoManager setMobileNetwork(boolean mobileNetwork) {
        mIsMobileNetwork = mobileNetwork;
        return mInstance;
    }

    public boolean isMobileNetwork() {
        return mIsMobileNetwork;
    }

    /**
     * 是否监听并处理音频焦点事件
     * @return true:拦截，并在收到音频焦点失去后暂停播放 false:什么也不处理
     */
    public boolean isInterceptTAudioFocus() {
        return mInterceptTAudioFocus;
    }

    /**
     * 是否监听并处理音频焦点事件
     * @param interceptTAudioFocus true:拦截，并在收到音频焦点失去后暂停播放 false:什么也不处理
     */
    public IVideoManager setInterceptTAudioFocus(boolean interceptTAudioFocus) {
        mInterceptTAudioFocus = interceptTAudioFocus;
        return mInstance;
    }
}