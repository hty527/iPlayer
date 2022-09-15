package com.android.iplayer.media.core;

import android.content.Context;
import com.android.iplayer.media.MediaFactory;

/**
 * created by hty
 * 2022/9/15
 * Desc:系统MediaPlayer的工厂类，{@link MediaPlayer}
 */
public class MediaPlayerFactory extends MediaFactory<MediaPlayer> {

    public static MediaPlayerFactory create() {
        return new MediaPlayerFactory();
    }

    @Override
    public MediaPlayer createPlayer(Context context) {
        return new MediaPlayer(context);
    }
}