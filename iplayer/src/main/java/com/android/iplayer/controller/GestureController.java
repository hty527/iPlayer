package com.android.iplayer.controller;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.android.iplayer.base.BaseController;
import com.android.iplayer.interfaces.IGestureControl;
import com.android.iplayer.utils.PlayerUtils;

/**
 * created by hty
 * 2022/8/5
 * Desc:带有手势交互的基础控制器,需要实现手势交互的控制器可继承此类
 */
public abstract class GestureController extends BaseController implements View.OnTouchListener ,IGestureControl{//GestureDetector.OnGestureListener

    private GestureDetector mGestureDetector;
    private AudioManager mAudioManager;
    //设置相关
    private boolean mCanTouchPosition = true;//是否可以滑动调节进度，默认可以
    private boolean mCanTouchInPortrait;//是否在竖屏模式下开始手势控制，默认关闭
    private boolean mIsGestureEnabled = true;//是否开启手势控制，默认开启，关闭之后，手势调节进度，音量，亮度功能将关闭
    private boolean mIsDoubleTapTogglePlayEnabled;//是否开启双击播放/暂停，默认关闭
    //逻辑相关
    private boolean mChangePosition;//是否允许滑动seek播放
    private boolean mChangeBrightness;//是否允许滑动更改屏幕亮度
    private boolean mChangeVolume;//是否允许滑动更改系统音量
    private int mStreamVolume;
    private float mBrightness;
    private int mSeekPosition=-1;
    private boolean mFirstTouch;
    private boolean mCanSlide;
    private boolean isLocker;//屏幕锁是否启用

    public GestureController(Context context) {
        super(context);
    }

    @Override
    public int getLayoutId() {
        return 0;
    }

    @Override
    public void initViews() {
        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        mGestureDetector = new GestureDetector(getContext(), new SimpleOnGesture());
        this.setOnTouchListener(this);
    }

    /**
     * 设置是否可以滑动调节进度，默认可以
     * @param canTouchPosition true:允许滑动快进快退 false:不允许滑动快进快退
     */
    public void setCanTouchPosition(boolean canTouchPosition) {
        mCanTouchPosition = canTouchPosition;
    }

    /**
     * 是否在竖屏模式下开始手势控制，默认开启
     * @param canTouchInPortrait true:开始竖屏状态下的手势交互 false:关闭竖屏状态下的手势交互
     */
    public void setCanTouchInPortrait(boolean canTouchInPortrait) {
        mCanTouchInPortrait = canTouchInPortrait;
        mCanSlide=mCanTouchInPortrait;
    }

    /**
     * 是否开启手势控制，默认关闭，关闭之后，手势调节进度，音量，亮度功能将关闭
     * @param gestureEnabled true:允许手势交互 false:不允许手势交互
     */
    public void setGestureEnabled(boolean gestureEnabled) {
        mIsGestureEnabled = gestureEnabled;
    }

    /**
     * 是否开启双击播放/暂停，默认关闭
     * @param enabled true:允许双击播放\暂停 false:不允许双击播放\暂停
     */
    public void setDoubleTapTogglePlayEnabled(boolean enabled) {
        mIsDoubleTapTogglePlayEnabled = enabled;
    }

    /**
     * 接管处理手势识别
     */
    private class SimpleOnGesture extends GestureDetector.SimpleOnGestureListener{

        private boolean isDoubleTap=false;

        @Override
        public boolean onDown(MotionEvent e) {
            boolean edge = PlayerUtils.getInstance().isEdge(getParentContext(), e,isOrientationPortrait());
//            ILogger.d(TAG,"onDown-->isPlayering:"+isPlayering()+",edge:"+edge+",mIsGestureEnabled:"+mIsGestureEnabled+",e:"+e.getAction());
            if (!isPlayering() //不处于播放状态
                    || !mIsGestureEnabled //关闭了手势
                    || edge) //处于屏幕边沿
                return true;
            mStreamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            Activity activity = getActivity();
            if (activity == null) {
                mBrightness = 0;
            } else {
                mBrightness = activity.getWindow().getAttributes().screenBrightness;
            }
            mFirstTouch = true;
            mChangePosition = false;
            mChangeBrightness = false;
            mChangeVolume = false;
            return true;
        }

        /**
         * 单机
         * @param e
         * @return
         */
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if(isDoubleTap){//如果刚刚响应了双击事件，则放弃此次单击事件
                isDoubleTap=false;
                return true;
            }
//            ILogger.d(TAG,"onSingleTapConfirmed");
            onSingleTap();
            return true;
        }

        /**
         * 双击，双击事件消费后单击事件也消费了
         * @param e
         * @return
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
//            ILogger.d(TAG,"onDoubleTap");
            isDoubleTap=true;
            if(mIsDoubleTapTogglePlayEnabled){
                GestureController.this.onDoubleTap();
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//            boolean edge1 = PlayerUtils.getInstance().isEdge(getParentContext(), e1);
//            boolean edge2 = PlayerUtils.getInstance().isEdge(getParentContext(), e2);
//        ILogger.d(TAG,"onScroll-->e1:"+e1.getAction()+",e2:"+e2+",distanceX:"+distanceX+",distanceY:"+distanceY+",mIsGestureEnabled:"+mIsGestureEnabled+",mCanSlide:"+mCanSlide+",edge1:"+edge1+",edge2:"+edge2+",mFirstTouch:"+mFirstTouch);
            if (!isPlayering() //不处于播放状态
                    || !mIsGestureEnabled //关闭了手势
                    || !mCanSlide //关闭了滑动手势
                    || isLocked() //锁住了屏幕
                    || PlayerUtils.getInstance().isEdge(getParentContext(), e1,isOrientationPortrait())) {// //处于屏幕边沿
                return true;
            }
            float deltaX = e1.getX() - e2.getX();
            float deltaY = e1.getY() - e2.getY();
            if (mFirstTouch) {
                mChangePosition = Math.abs(distanceX) >= Math.abs(distanceY);
                if (!mChangePosition) {
                    //半屏宽度
                    int halfScreen = PlayerUtils.getInstance().getScreenWidth(getContext()) / 2;
                    if (e2.getX() > halfScreen) {
                        mChangeVolume = true;
                    } else {
                        mChangeBrightness = true;
                    }
                }
                if (mChangePosition) {
                    //根据用户设置是否可以滑动调节进度来决定最终是否可以滑动调节进度
                    mChangePosition = mCanTouchPosition;
                }
                if (mChangePosition || mChangeBrightness || mChangeVolume) {
                    onStartSlide();
                }
                mFirstTouch = false;
            }
//            ILogger.d(TAG,"onScroll-->mChangePosition:"+mChangePosition+",mChangeBrightness:"+mChangeBrightness+",mChangeVolume:"+mChangeVolume);
            if (mChangePosition) {//seek播放进度
                slideToChangePosition(deltaX);
            } else if (mChangeBrightness) {//更改屏幕亮度
                slideToChangeBrightness(deltaY);
            } else if (mChangeVolume) {//更改系统音量
                slideToChangeVolume(deltaY);
            }
            return true;
        }
    }

    @Override
    public void setScreenOrientation(int orientation) {
        if(isOrientationPortrait()){
            mCanSlide= mCanTouchInPortrait;
        }else{
            mCanSlide=true;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if(null!=mGestureDetector){
            return mGestureDetector.onTouchEvent(event);
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(null!=mGestureDetector){
            //滑动结束时事件处理
            if (!mGestureDetector.onTouchEvent(event)) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_UP://离开屏幕时跳转播放进度
                        stopSlide();
                        if (mSeekPosition > -1) {
                            if(null!=mVideoPlayerControl) mVideoPlayerControl.seekTo(mSeekPosition);
                            mSeekPosition = -1;
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        stopSlide();
                        mSeekPosition = -1;
                        break;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 改变播放进度
     * @param deltaX
     */
    private void slideToChangePosition(float deltaX) {
        if(null!=mVideoPlayerControl){
            deltaX = -deltaX;
            int width = getMeasuredWidth();
            int duration = (int) mVideoPlayerControl.getDuration();
            int currentPosition = (int) mVideoPlayerControl.getCurrentPosition();
            int position = (int) (deltaX / width * 120000 + currentPosition);
            if (position > duration) position = duration;
            if (position < 0) position = 0;
            onPositionChange(position, currentPosition, duration);
            mSeekPosition = position;
        }
    }

    /**
     * 改变屏幕亮度
     * @param deltaY
     */
    private void slideToChangeBrightness(float deltaY) {
        Activity activity = getActivity();
        if (activity == null) return;
        Window window = activity.getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        int height = getMeasuredHeight();
        if (mBrightness == -1.0f) mBrightness = 0.5f;
        float brightness = deltaY * 2 / height * 1.0f + mBrightness;
        if (brightness < 0) {
            brightness = 0f;
        }
        if (brightness > 1.0f) brightness = 1.0f;
        int percent = (int) (brightness * 100);
        attributes.screenBrightness = brightness;
        window.setAttributes(attributes);
        onBrightnessChange(percent);
    }


    /**
     * 改变音量
     * @param deltaY
     */
    private void slideToChangeVolume(float deltaY) {
        int streamMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int height = getMeasuredHeight();
        float deltaV = deltaY * 2 / height * streamMaxVolume;
        float index = mStreamVolume + deltaV;
        if (index > streamMaxVolume) index = streamMaxVolume;
        if (index < 0) index = 0;
        int percent = (int) (index / streamMaxVolume * 100);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) index, 0);
        onVolumeChange(percent);
    }

    /**
     * 手势操作取消
     */
    private void stopSlide() {
        onStopSlide();
    }

    /**
     * 是否锁住了屏幕
     * @return
     */
    protected boolean isLocked() {
        return isLocker;
    }

    protected void setLocker(boolean locker) {
        isLocker = locker;
    }
}