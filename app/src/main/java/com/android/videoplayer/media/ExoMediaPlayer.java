package com.android.videoplayer.media;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.view.Surface;
import com.android.iplayer.base.AbstractMediaPlayer;
import java.io.IOException;
import java.util.Map;

/**
 * created by hty
 * 2022/8/1
 * Desc:EXO解码器
 */
public class ExoMediaPlayer extends AbstractMediaPlayer {

//    private SimpleExoPlayer mMediaPlayer;

    public ExoMediaPlayer(Context context) {
        super(context);
    }

    @Override
    public void setLooping(boolean loop) {

    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {

    }

    @Override
    public void setBufferTimeMax(float timeSecond) {

    }

    @Override
    public void setSurface(Surface surface) {

    }

    @Override
    public void setDataSource(String dataSource) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {

    }

    @Override
    public void setDataSource(String dataSource, Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {

    }

    @Override
    public void setDataSource(AssetFileDescriptor dataSource) throws IOException, IllegalArgumentException, IllegalStateException {

    }

    @Override
    public void setTimeout(int prepareTimeout, int readTimeout) {

    }

    @Override
    public void setSpeed(float speed) {

    }

    @Override
    public void seekTo(long msec) throws IllegalStateException {

    }

    @Override
    public void seekTo(long msec, boolean accurate) throws IllegalStateException {

    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public long getCurrentPosition() {
        return 0;
    }

    @Override
    public long getDuration() {
        return 0;
    }

    @Override
    public void prepare() throws IOException, IllegalStateException {

    }

    @Override
    public void prepareAsync() throws IllegalStateException {

    }

    @Override
    public void start() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void reset() {

    }

    @Override
    public void release() {

    }

    @Override
    public void setOnPreparedListener(OnPreparedListener listener) {

    }

    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {

    }

    @Override
    public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {

    }

    @Override
    public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {

    }

    @Override
    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener) {

    }

    @Override
    public void setOnErrorListener(OnErrorListener listener) {

    }

    @Override
    public void setOnInfoListener(OnInfoListener listener) {

    }

    @Override
    public void setOnTimedTextListener(OnTimedTextListener listener) {

    }

    @Override
    public void setOnMessageListener(OnMessageListener listener) {

    }
}
