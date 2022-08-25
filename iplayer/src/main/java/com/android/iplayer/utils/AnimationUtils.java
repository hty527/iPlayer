package com.android.iplayer.utils;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

/**
 * created by hty
 * 2022/8/25
 * Desc:动画处理
 */
public class AnimationUtils {

    private static final String TAG = "AnimationUtils";
    private static AnimationUtils mInstance;

    public static synchronized AnimationUtils getInstance() {
        synchronized (AnimationUtils.class) {
            if (null == mInstance) {
                mInstance = new AnimationUtils();
            }
        }
        return mInstance;
    }

    public interface OnAnimationListener{
        void onAnimationEnd(Animation animation);
    }

    /**
     * 从所在位置往左边平移
     * @param targetView 锚点View
     * @param durationMillis 动画持续时长
     * @param listener 监听器
     */
    public void startTranslateLocatToLeft(View targetView,long durationMillis,OnAnimationListener listener){
        startTranslateAnimation(targetView,0,false,durationMillis,listener);
    }

    /**
     * 从左边往所在位置平移
     * @param targetView 锚点View
     * @param durationMillis 动画持续时长
     * @param listener 监听器
     */
    public void startTranslateLeftToLocat(View targetView,long durationMillis,OnAnimationListener listener){
        startTranslateAnimation(targetView,0,true,durationMillis,listener);
    }

    /**
     * 从所在位置往上方平移
     * @param targetView 锚点View
     * @param durationMillis 动画持续时长
     * @param listener 监听器
     */
    public void startTranslateLocatToTop(View targetView,long durationMillis,OnAnimationListener listener){
        startTranslateAnimation(targetView,1,false,durationMillis,listener);
    }

    /**
     * 从上方往所在位置平移
     * @param targetView 锚点View
     * @param durationMillis 动画持续时长
     * @param listener 监听器
     */
    public void startTranslateTopToLocat(View targetView,long durationMillis,OnAnimationListener listener){
        startTranslateAnimation(targetView,1,true,durationMillis,listener);
    }


    /**
     * 从所在位置往右边平移
     * @param targetView 锚点View
     * @param durationMillis 动画持续时长
     * @param listener 监听器
     */
    public void startTranslateLocatToRight(View targetView,long durationMillis,OnAnimationListener listener){
        startTranslateAnimation(targetView,2,false,durationMillis,listener);
    }

    /**
     * 从右边往所在位置平移
     * @param targetView 锚点View
     * @param durationMillis 动画持续时长
     * @param listener 监听器
     */
    public void startTranslateRightToLocat(View targetView,long durationMillis,OnAnimationListener listener){
        startTranslateAnimation(targetView,2,true,durationMillis,listener);
    }

    /**
     * 从所在位置往下方平移
     * @param targetView 锚点View
     * @param durationMillis 动画持续时长
     * @param listener 监听器
     */
    public void startTranslateLocatToBottom(View targetView,long durationMillis,OnAnimationListener listener){
        startTranslateAnimation(targetView,3,false,durationMillis,listener);
    }

    /**
     * 从下方往所在位置平移
     * @param targetView 锚点View
     * @param durationMillis 动画持续时长
     * @param listener 监听器
     */
    public void startTranslateBottomToLocat(View targetView,long durationMillis,OnAnimationListener listener){
        startTranslateAnimation(targetView,3,true,durationMillis,listener);
    }

    /**
     * 开始播放 上\下\左\右 平移 进\出 动画
     * @param targetView 锚点View
     * @param direction 以锚点targetView真实所在位置为中心的方向(motion为true时表示从哪个方向来,为false时表示将要往哪个方向去) 0：左，1：上， 2：右，3：下
     * @param motion 动画相对自身targetView所在位置的意图，true:进 flase:出
     * @param durationMillis 动画时长
     * @param listener 监听器
     */
    private void startTranslateAnimation(View targetView,int direction,boolean motion,long durationMillis,OnAnimationListener listener){
        if(null==targetView){
            if(null!=listener) listener.onAnimationEnd(null);
            return;
        }
        new TranslateYAnimation().startTranslateAnimation(targetView,direction,motion,durationMillis,listener);
    }

    private class TranslateYAnimation{

        private View mTargetView;
        private OnAnimationListener mOnAnimationListener;

        public void startTranslateAnimation(View targetView, int direction,boolean motion, long durationMillis, OnAnimationListener listener) {
            this.mTargetView=targetView;
            this.mOnAnimationListener=listener;
            TranslateAnimation animation=null;
            switch (direction) {
                case 0://左
                    if(motion){//进
                        animation=moveFormLeftToLocat();
                    }else{//出
                        animation=moveFormLocatToLeft();
                    }
                    break;
                case 1://上
                    if(motion){
                        animation=moveFormTopToLocat();
                    }else{
                        animation=moveFromLocatToTop();
                    }
                    break;
                case 2://右
                    if(motion){
                        animation=moveFormRightToLocat();
                    }else{
                        animation=moveFormLocatToRight();
                    }
                    break;
                case 3://下
                    if(motion){
                        animation=moveFormBottomToLocat();
                    }else{
                        animation=moveFormLocatToBottom();
                    }
                    break;
            }
            if(null!=animation){
                animation.setDuration(durationMillis);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if(null!=mOnAnimationListener) mOnAnimationListener.onAnimationEnd(animation);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                mTargetView.setVisibility(View.VISIBLE);
                mTargetView.startAnimation(animation);
            }else{
                if(null!=mOnAnimationListener) mOnAnimationListener.onAnimationEnd(null);
            }
        }
    }

    /**
     * 从控件所在位置平移到控件所在位置左边
     * 从所在位置往左出场
     * @return
     */
    private TranslateAnimation moveFormLocatToLeft() {
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        return animation;
    }

    /**
     * 从控件所在位置左边平移到控件所在位置
     * 从所在位置左边往所在位置进场
     * @return
     */
    private TranslateAnimation moveFormLeftToLocat() {
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, -1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        return animation;
    }

    /**
     * 从控件所在位置平移到控件所在位置的顶部
     * 从所在位置往上出场
     * @return
     */
    private TranslateAnimation moveFromLocatToTop() {
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f);
        return animation;
    }

    /**
     * 从控件所在位置顶部平移到控件所在位置
     * 从上方往所在位置进场
     * @return
     */
    private TranslateAnimation moveFormTopToLocat() {
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        return animation;
    }

    /**
     * 从控件所在位置平移到控件所在位置右边
     * 从所在位置往右出场
     * @return
     */
    private TranslateAnimation moveFormLocatToRight() {
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        return animation;
    }

    /**
     * 从控件所在位置右边平移到控件所在位置
     * 从所在位置右边往所在位置进场
     * @return
     */
    private TranslateAnimation moveFormRightToLocat() {
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        return animation;
    }

    /**
     * 从控件所在位置平移到控件所在位置的底部
     * 从所在位置往下出场
     * @return
     */
    private TranslateAnimation moveFormLocatToBottom() {
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 1.0f);
        return animation;
    }

    /**
     * 从控件所在位置底部平移到控件所在位置
     * 从下方往所在位置进场
     * @return
     */
    private TranslateAnimation moveFormBottomToLocat() {
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        return animation;
    }


    /**
     * 播放透明动画
     * @param view 目标View
     * @param duration 动画时长
     * @param isFillAfter 是否停留在最后一帧
     * @param listener 状态监听器
     */
    public void startAlphaAnimatioTo(View view, int duration, boolean isFillAfter, OnAnimationListener listener) {
        if(null==view) return;
        new AnimationTask().start(view,duration,isFillAfter,listener);
    }

    /**
     * 动画执行
     */
    private class AnimationTask{

        private View mView;
        private OnAnimationListener mOnAnimationListener;

        public void start(View view, int duration, boolean isFillAfter, OnAnimationListener listener) {
            this.mView=view;
            this.mOnAnimationListener=listener;
            AlphaAnimation alphaAnim = new AlphaAnimation(1f, 0);
            alphaAnim.setDuration(duration);
            alphaAnim.setFillAfter(isFillAfter);
            alphaAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if(null!=mOnAnimationListener) mOnAnimationListener.onAnimationEnd(animation);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mView.startAnimation(alphaAnim);
        }
    }

    /**
     * 播放透明动画
     * @param view 目标View
     * @param duration 动画时长
     * @param isFillAfter 是否停留在最后一帧
     * @param listener 状态监听器
     */
    public void startAlphaAnimatioFrom(View view, int duration, boolean isFillAfter, OnAnimationListener listener) {
        if(null==view) return;
        new AnimationTaskFrom().start(view,duration,isFillAfter,listener);
    }

    /**
     * 动画执行
     */
    private class AnimationTaskFrom{

        private View mView;
        private OnAnimationListener mOnAnimationListener;

        public void start(View view, int duration, boolean isFillAfter, OnAnimationListener listener) {
            this.mView=view;
            this.mOnAnimationListener=listener;
            mView.setVisibility(View.VISIBLE);
            AlphaAnimation alphaAnim = new AlphaAnimation(0f, 1f);
            alphaAnim.setDuration(duration);
            alphaAnim.setFillAfter(isFillAfter);
            alphaAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if(null!=mOnAnimationListener) mOnAnimationListener.onAnimationEnd(animation);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mView.startAnimation(alphaAnim);
        }
    }
}