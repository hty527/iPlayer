package com.android.player.ui.widget;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.android.iplayer.utils.PlayerUtils;
import com.android.player.R;
import com.android.player.utils.ScreenUtils;
import com.android.player.utils.StatusUtils;

/**
 * created by hty
 * 2022/7/5
 * Desc:标题栏View 内部维护白色和头面给两套样式两套
 */
public class TitleView extends FrameLayout {

    public static final int STYLE_LIGHT=0;//白底
    public static final int STYLE_COLOR=1;//透明底
    private int mTitleStyle=STYLE_LIGHT;

    public TitleView(Context context) {
        this(context,null);
    }

    public TitleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public TitleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.view_title_view,this);
        if(null!=attrs){
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TitleView);
            mTitleStyle = typedArray.getInt(R.styleable.TitleView_titleStyle, STYLE_LIGHT);
            typedArray.recycle();
        }
        fullScreen();
        ImageView btnBack = (ImageView) findViewById(R.id.view_back);
        TextView titleText = (TextView) findViewById(R.id.view_title);
        ImageView titleBg = (ImageView) findViewById(R.id.view_title_bg);
        titleBg.getLayoutParams().height=PlayerUtils.getInstance().getNavigationHeight(getContext())+ ScreenUtils.getInstance().dpToPxInt(49f);
        if(0==mTitleStyle){
            btnBack.setColorFilter(Color.parseColor("#333333"));
            titleText.setTextColor(Color.parseColor("#333333"));
            titleBg.setImageResource(R.mipmap.ic_title_bg);
        }else{
            btnBack.setColorFilter(Color.parseColor("#FFFFFF"));
            titleText.setTextColor(Color.parseColor("#FFFFFF"));
            titleBg.setImageResource(0);
        }
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null!=mOnTitleActionListener){
                    mOnTitleActionListener.onBack();
                }
            }
        });
    }

    /**
     * 全屏
     */
    private void fullScreen(){
        Activity activity = PlayerUtils.getInstance().getActivity(getContext());
        if(null!=activity){
            findViewById(R.id.view_status_bar).getLayoutParams().height= PlayerUtils.getInstance().getNavigationHeight(activity);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //系统版本大于19
                setTranslucentStatus(true,activity);
            }
            //Android5.0以上
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = activity.getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
            }
            StatusUtils.setStatusTextColor1(0==mTitleStyle, activity);
        }
    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on,Activity activity) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    public interface OnTitleActionListener{
        void onBack();
    }

    private OnTitleActionListener mOnTitleActionListener;

    public void setTitle(String title){
        ((TextView) findViewById(R.id.view_title)).setText(title);
    }

    /**
     * 是否开启返回按钮
     * @param enable true:开启 false:禁用 默认是开启的
     */
    public void enableTitleBack(boolean enable){
        findViewById(R.id.view_back).setVisibility(enable?VISIBLE:GONE);
    }

    public void setOnTitleActionListener(OnTitleActionListener onTitleActionListener) {
        mOnTitleActionListener = onTitleActionListener;
    }
}