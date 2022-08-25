package com.android.videoplayer.pager.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.android.videoplayer.R;
import com.android.videoplayer.utils.GlideModel;
import com.android.videoplayer.utils.Logger;

/**
 * created by hty
 * 2022/7/5
 * Desc:唱片机
 */
public class MusicDiscView extends RelativeLayout {

    private static final String TAG = "MusicDiscView";
    //旋转一圈需要多久
    private int mRotationDurtion=10;
    private ObjectAnimator mDiscObjectAnimator;
    private ImageView mImageView;

    public MusicDiscView(Context context) {
        this(context,null);
    }

    public MusicDiscView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MusicDiscView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context,R.layout.view_music_disc_layout,this);
        mImageView=findViewById(R.id.view_disc_cover);
    }

    /**
     * 开始旋转动画
     */
    private synchronized void startAnimator(){
        if(null!=mDiscObjectAnimator){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if(mDiscObjectAnimator.isPaused()){
                    mDiscObjectAnimator.resume();
                }else{
                    if(mDiscObjectAnimator.isRunning()){
                        return;
                    }
                    mDiscObjectAnimator.start();
                }
            }
        }else{
            ObjectAnimator discObjectAnimator = getDiscObjectAnimator();
            if(null!=discObjectAnimator){
                discObjectAnimator.start();
            }
        }
    }

    /**
     * 暂停旋转动画
     */
    private synchronized void pausAnimator() {
        if(null!=mDiscObjectAnimator){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mDiscObjectAnimator.pause();
            }else{
                mDiscObjectAnimator.cancel();
                mDiscObjectAnimator=null;
                this.clearAnimation();
                this.setRotation(0);
            }
        }
    }

    /**
     * 停止旋转动画
     */
    private void stopAnimator(boolean resetRotation){
        if(null!=mDiscObjectAnimator){
            mDiscObjectAnimator.cancel();
            mDiscObjectAnimator=null;
        }
        this.clearAnimation();
        if(resetRotation){
            this.setRotation(0);
        }
    }

    /**
     * 创建一个旋转动画实体
     * @return
     */
    private ObjectAnimator getDiscObjectAnimator() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, View.ROTATION, 0, 360);
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
        objectAnimator.setDuration(mRotationDurtion * 1000);
        objectAnimator.setInterpolator(new LinearInterpolator());
        this.mDiscObjectAnimator=objectAnimator;
        return objectAnimator;
    }

    /**
     * 显示悬浮窗
     * @param view
     */
    public void showWindowAnimation(View view){
        if(null==view) return;
        if(view.getVisibility()==VISIBLE) return;
        view.clearAnimation();
        view.setVisibility(VISIBLE);
        AnimatorSet animatorSet=new AnimatorSet();
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(view, "scaleX", 0.0f, 1.0f).setDuration(350);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(view, "scaleY", 0.0f, 1.0f).setDuration(350);
        animatorSet.playTogether(animator1,animator2);
        animatorSet.start();
    }

    /**
     * 隐藏悬浮窗
     * @param view
     */
    public void hideWindowAnimation(final View view){
        if(null==view) return;
        if(view.getVisibility()==GONE) return;
        view.clearAnimation();
        AnimatorSet animatorSet=new AnimatorSet();
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 0.0f).setDuration(260);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 0.0f).setDuration(260);
        animatorSet.playTogether(animator1,animator2);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(GONE);
            }
        });
        animatorSet.start();
    }

    /**
     * 更新音频文件封面
     * @param filePath
     */
    public void setMusicFront(String filePath) {
        stopAnimator(true);
        if(null==mImageView) return;
        if(!TextUtils.isEmpty(filePath)){
            GlideModel.getInstance().loadCirImage(getContext(),mImageView,filePath,R.mipmap.ic_music_disc);
        }else{
            mImageView.setImageResource(R.mipmap.ic_music_disc);
        }
//        GlideModel.getInstance().setMusicComposeFront(getContext(),mViewCover,filePath,SCALE_DISC_MINI_SIZE
//                ,SCALE_MUSIC_PIC_MINE_SIZE,R.mipmap.ic_music_disc,R.mipmap.ic_music_default_cover);
    }

    /**
     * 设置唱片机旋转一圈耗时
     * @param rotationDurtion
     */
    public void setRotationDurtion(int rotationDurtion) {
        mRotationDurtion = rotationDurtion;
    }

    public void onResume(){
        Logger.d(TAG,"onResume");
        startAnimator();
    }

    public void onPause(){
        Logger.d(TAG,"onPause");
        pausAnimator();
    }

    public void onStop(){
        Logger.d(TAG,"onStop");
        stopAnimator(true);
    }

    public void onRelease() {
        Logger.d(TAG,"onRelease");
        stopAnimator(true);
    }

    public void onDestroy(){
        Logger.d(TAG,"onDestroy");
        stopAnimator(true);
    }
}