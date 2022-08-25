package com.android.videoplayer.video.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.android.iplayer.utils.AnimationUtils;
import com.android.iplayer.widget.view.LayoutProvider;
import com.android.videoplayer.R;
import com.android.videoplayer.utils.Logger;
import com.android.videoplayer.utils.ScreenUtils;

/**
 * created by hty
 * 2022/8/19
 * Desc:播放器新人引导
 */
public class PlayerNewbieView extends FrameLayout {

    private static final String TAG = "PlayerNewbieView";
    private ObjectAnimator mAnimator;

    public PlayerNewbieView(@NonNull Context context) {
        this(context,null);
    }

    public PlayerNewbieView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PlayerNewbieView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.view_player_newbie,this);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    /**
     * 将自己依附在锚点View之上
     * @param targetView 锚点View
     */
    public void updateWindow(View targetView) {
        if(null!=targetView){
            int[] windowLocation = new int [2];
            targetView.getLocationInWindow(windowLocation);
            Logger.d(TAG,"updateWindow-->view:2x:"+windowLocation[0]+",2y:"+windowLocation[1]);
            //移动手指的位置和圆角
            LinearLayout handle = findViewById(R.id.ll_handel);
            FrameLayout.LayoutParams layoutParams = (LayoutParams) handle.getLayoutParams();
            layoutParams.width=targetView.getMeasuredWidth();
            layoutParams.height=targetView.getMeasuredHeight();
            layoutParams.setMargins(windowLocation[0],windowLocation[1],0,0);
            handle.setLayoutParams(layoutParams);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                handle.setOutlineProvider(new LayoutProvider(ScreenUtils.getInstance().dpToPxInt(3f)));
            }
            //滑动提示动画
            mAnimator = ObjectAnimator.ofFloat(findViewById(R.id.ic_handel), "translationX", 0.0f , 50f, 0f , 0f);
            mAnimator.setDuration(1600);//动画时间
            mAnimator.setInterpolator(new BounceInterpolator());//实现反复移动的效果
            mAnimator.setRepeatCount(4);//设置动画重复次数n+1
            mAnimator.setStartDelay(600);//设置动画延时执行
            mAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    Logger.d(TAG,"onAnimationEnd");
                    dismiss();
                }
            });
            mAnimator.start();//启动动画
        }
    }

    public interface OnDismissListener{
        void onDismiss();
    }

    private OnDismissListener mOnDismissListener;

    public void setOnDismissListener(OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
    }

    private void reset() {
        if(null!=mAnimator){
            mAnimator.cancel();
            mAnimator=null;
        }
    }


    private void dismiss() {
        reset();
        AnimationUtils.getInstance().startAlphaAnimatioTo(findViewById(R.id.ll_handel), 300, false, new AnimationUtils.OnAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                if(null!=mOnDismissListener){
                    mOnDismissListener.onDismiss();
                }
            }
        });
    }
}