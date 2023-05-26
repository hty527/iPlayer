package com.android.iplayer.media.core;

import android.content.Context;
import com.android.iplayer.media.MediaFactory;

/**
 * created by hty
 * 2022/9/15
 * Desc:IjkPlayer播放解码器的工厂类，{@link IJkMediaPlayer}
 */
public class IjkPlayerFactory extends MediaFactory<IJkMediaPlayer> {

    private static boolean isLive=false;

    public static IjkPlayerFactory create() {
        return create(false);
    }

    /**
     * 直播模式下{@link IJkMediaPlayer}内部会针对直播拉流做一些基础设置，当然也可以你自己获取到player来自定义设置
     * @param isLive true：直播模式 false：非直播模式
     * @return
     */
    public static IjkPlayerFactory create(boolean isLive) {
        IjkPlayerFactory.isLive=isLive;
        return new IjkPlayerFactory();
    }

    @Override
    public IJkMediaPlayer createPlayer(Context context) {
        return new IJkMediaPlayer(context,isLive);
    }
}