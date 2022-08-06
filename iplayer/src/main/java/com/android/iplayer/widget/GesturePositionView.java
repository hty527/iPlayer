package com.android.iplayer.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.android.iplayer.R;
import com.android.iplayer.interfaces.IGestureControl;
import com.android.iplayer.utils.PlayerUtils;

/**
 * created by hty
 * 2022/8/6
 * Desc:手势交互声音、亮度、快进、快退等UI交互
 */
public class GesturePositionView extends FrameLayout implements IGestureControl {

    private View mGesturePresent;//交互UI
    private ImageView mPresentIcon;//ICON
    private TextView mPresentText;//进度文字
    private ProgressBar mPresentProgress;//亮度、声音进度

    public GesturePositionView(@NonNull Context context) {
        this(context,null);
    }

    public GesturePositionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public GesturePositionView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.player_gesture_position,this);
        mGesturePresent = findViewById(R.id.gesture_present);
        mPresentIcon = (ImageView) findViewById(R.id.gesture_present_icon);
        mPresentText = (TextView) findViewById(R.id.gesture_present_text);
        mPresentProgress = (ProgressBar) findViewById(R.id.gesture_present_progress);
    }

    @Override
    public void onSingleTap() {}

    @Override
    public void onDoubleTap() {}


    @Override
    public void onStartSlide() {
        if(null!=mGesturePresent) {
            mGesturePresent.setVisibility(View.VISIBLE);
            mGesturePresent.setAlpha(1.0f);
        }
    }

    @Override
    public void onStopSlide() {
        if(null!=mGesturePresent){
            mGesturePresent.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mGesturePresent.setVisibility(GONE);
                        }
                    })
                    .start();
        }
    }

    /**
     * 播放进度调节
     * @param slidePosition 滑动进度
     * @param currentPosition 当前播放进度
     * @param duration 视频总长度
     */
    @Override
    public void onPositionChange(int slidePosition, int currentPosition, int duration) {
        if(null!=mPresentProgress) mPresentProgress.setVisibility(View.GONE);
        if(null!=mPresentText) mPresentText.setVisibility(View.VISIBLE);
        if(null!=mPresentIcon) mPresentIcon.setImageResource(slidePosition>currentPosition?R.mipmap.ic_player_gesture_next:R.mipmap.ic_player_gesture_last);
        if(null!=mPresentText) mPresentText.setText(String.format("%s/%s", PlayerUtils.getInstance().stringForAudioTime(slidePosition),PlayerUtils.getInstance().stringForAudioTime(currentPosition)));
    }

    /**
     * 屏幕亮度调节
     * @param percent 亮度百分比
     */
    @Override
    public void onBrightnessChange(int percent) {
        if(null!=mPresentText) mPresentText.setVisibility(View.GONE);
        if(null!=mPresentIcon) mPresentIcon.setImageResource(R.mipmap.ic_player_brightness);
        if(null!=mPresentProgress){
            mPresentProgress.setVisibility(View.VISIBLE);
            mPresentProgress.setProgress(percent);
        }
    }

    /**
     * 声音调节
     * @param percent 音量百分比
     */
    @Override
    public void onVolumeChange(int percent) {
        if(null!=mPresentText) mPresentText.setVisibility(View.GONE);
        if(null!=mPresentIcon) mPresentIcon.setImageResource(0==percent?R.mipmap.ic_player_sound_off:R.mipmap.ic_player_sound);
        if(null!=mPresentProgress){
            mPresentProgress.setVisibility(View.VISIBLE);
            mPresentProgress.setProgress(percent);
        }
    }
}