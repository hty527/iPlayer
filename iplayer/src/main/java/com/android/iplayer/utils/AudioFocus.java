package com.android.iplayer.utils;

import android.content.Context;
import android.media.AudioManager;

/**
 * created by hty
 * 2022/6/28
 * Desc:焦点监控捕获处理
 */
public class AudioFocus {

//    public static final String TAG="AudioFocus";
    private int mVolumeWhenFocusLossTransientCanDuck;
    private AudioManager mAudioManager;

    public AudioFocus(){
        this(PlayerUtils.getInstance().getContext());
    }

    public AudioFocus(Context context) {
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * 请求音频焦点
     */
    public int requestAudioFocus(OnAudioFocusListener focusListener) {
        this.mFocusListener=focusListener;
        if(null!=mAudioManager){
            int requestAudioFocus = mAudioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
//            ILogger.d(TAG,"requestAudioFocus-->requestAudioFocus"+requestAudioFocus);
            return requestAudioFocus;
        }
        return 1;
    }

    /**
     * 停止播放释放音频焦点
     */
    public void releaseAudioFocus() {
        if(null!=mAudioManager&&null!=onAudioFocusChangeListener){
            mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
        }

    }

    private AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener=new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
//            CLogger.d(TAG,"onAudioFocusChange:focusChange:"+focusChange);
            int volume;
            switch (focusChange) {
                //重新获取到了焦点
                case AudioManager.AUDIOFOCUS_GAIN:
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
//                    ILogger.d(TAG,"重新获取到了焦点");
                    volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    if (mVolumeWhenFocusLossTransientCanDuck > 0 && volume == mVolumeWhenFocusLossTransientCanDuck / 2) {
                        // 恢复音量
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mVolumeWhenFocusLossTransientCanDuck,
                                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    }
                    //恢复播放
                    if(null!=mFocusListener){
                        mFocusListener.onFocusStart();
                    }
                    break;
                //被其他播放器抢占
                case AudioManager.AUDIOFOCUS_LOSS:
//                    ILogger.d(TAG,"被其他播放器抢占");
                    if(null!=mFocusListener){
                        mFocusListener.onFocusStop();
                    }
                    break;
                //暂时失去焦点，例如来电占用音频输出
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
//                    ILogger.d(TAG,"暂时失去焦点");
                    if(null!=mFocusListener){
                        mFocusListener.onFocusStop();
                    }
                    break;
                //瞬间失去焦点，例如通知占用了音频输出
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
//                    ILogger.d(TAG,"瞬间失去焦点");
                    if(null!=mFocusListener){
                        volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                        if (volume > 0) {
                            mVolumeWhenFocusLossTransientCanDuck = volume;
                            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mVolumeWhenFocusLossTransientCanDuck / 2,
                                    AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                        }
                        mFocusListener.onFocusStart();
                    }
                    break;
            }
        }
    };

    public interface OnAudioFocusListener{
        void onFocusStart();
        void onFocusStop();
    }

    public OnAudioFocusListener mFocusListener;

    public void onDestroy(){
        releaseAudioFocus();
        mAudioManager=null;mFocusListener=null;
    }
}