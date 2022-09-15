package com.android.iplayer.media.core;

import android.content.Context;
import com.android.iplayer.media.MediaFactory;

/**
 * created by hty
 * 2022/9/15
 * Desc:ExoPlayer播放解码器的工厂类，{@link ExoMediaPlayer}
 */
public class ExoPlayerFactory extends MediaFactory<ExoMediaPlayer> {

    public static ExoPlayerFactory create() {
        return new ExoPlayerFactory();
    }

    @Override
    public ExoMediaPlayer createPlayer(Context context) {
        return new ExoMediaPlayer(context);
    }
}