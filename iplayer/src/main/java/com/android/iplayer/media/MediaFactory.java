package com.android.iplayer.media;

import android.content.Context;
import com.android.iplayer.base.AbstractMediaPlayer;

/**
 * created by hty
 * 2022/9/15
 * Desc:所有解码器的工厂构造可继承此类来实现创建自己的解码器
 */
public abstract class MediaFactory<M extends AbstractMediaPlayer> {

    /**
     * 构造播放器解码器
     * @param context 上下文
     * @return 继承自AbstractMediaPlayer的解码器
     */
    public abstract M createPlayer(Context context);
}