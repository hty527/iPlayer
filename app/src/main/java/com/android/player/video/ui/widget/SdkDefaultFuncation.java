package com.android.player.video.ui.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.android.player.R;
import com.android.player.video.listener.OnMenuActionListener;

/**
 * created by hty
 * 2022/8/25
 * Desc:SDK提供的默认播放器、控制器、UI交互组件等示例
 */
public class SdkDefaultFuncation extends LinearLayout implements View.OnClickListener {

    private PlayerMenuView mMenuView;
    private int MEDIA_CORE=0;//多媒体解码器 0:系统默认 1:ijk 2:exo
    private int RENDER_CORE=0;//画面渲染器 0:TextureView 1:SurfaceView
    private View[] buttons=new View[3];//解码器按钮

    public SdkDefaultFuncation(Context context) {
        this(context,null);
    }

    public SdkDefaultFuncation(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SdkDefaultFuncation(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.view_sdk_funcation,this);
    }

    private void initViews() {
        mMenuView = (PlayerMenuView) findViewById(R.id.menu_view);
        mMenuView.setOnMenuActionListener(new OnMenuActionListener() {
            @Override
            public void onSpeed(float speed) {
                if(null!=mOnActionListener) mOnActionListener.setSpeed(speed);
            }

            @Override
            public void onZoom(int zoomModel) {
                if(null!=mOnActionListener) mOnActionListener.setZoomModel(zoomModel);
            }

            @Override
            public void onScale(int scale) {

            }

            @Override
            public void onMute(boolean mute) {
                if(null!=mOnActionListener) mOnActionListener.setSoundMute(mute);
            }

            @Override
            public void onMirror(boolean mirror) {
                if(null!=mOnActionListener) mOnActionListener.setMirror(mirror);
            }
        });

        //解码器
        buttons[0]=findViewById(R.id.btn_core_1);
        buttons[1]=findViewById(R.id.btn_core_2);
        buttons[2]=findViewById(R.id.btn_core_3);
        buttons[MEDIA_CORE].setSelected(true);
        buttons[0].setOnClickListener(this);
        buttons[1].setOnClickListener(this);
        buttons[2].setOnClickListener(this);
        buttons[0].setTag(0);
        buttons[1].setTag(1);
        buttons[2].setTag(2);
        if(null!=mOnActionListener){
            mOnActionListener.onMediaCore(MEDIA_CORE);
            mOnActionListener.setCanTouchInPortrait(true);
        }

        //渲染器
        View render1 = findViewById(R.id.btn_render_1);
        render1.setSelected(true);
        render1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.btn_render_1).setSelected(true);
                findViewById(R.id.btn_render_2).setSelected(false);
                RENDER_CORE =0;
                if(null!=mOnActionListener) {
                    mOnActionListener.onRenderCore(RENDER_CORE);
                    mOnActionListener.rePlay(null);
                }
            }
        });
        //渲染器
        findViewById(R.id.btn_render_2).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.btn_render_1).setSelected(false);
                findViewById(R.id.btn_render_2).setSelected(true);
                RENDER_CORE =1;
                if(null!=mOnActionListener) {
                    mOnActionListener.onRenderCore(RENDER_CORE);
                    mOnActionListener.rePlay(null);
                }
            }
        });
        //手势交互
        View touch_1 = findViewById(R.id.touch_1);
        touch_1.setSelected(true);
        touch_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.touch_1).setSelected(true);
                findViewById(R.id.touch_2).setSelected(false);
                if(null!=mOnActionListener) mOnActionListener.setCanTouchInPortrait(true);
            }
        });
        findViewById(R.id.touch_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.touch_1).setSelected(false);
                findViewById(R.id.touch_2).setSelected(true);
                if(null!=mOnActionListener) mOnActionListener.setCanTouchInPortrait(false);
            }
        });
        //重力旋转
        View orientation_1 = findViewById(R.id.orientation_1);
        orientation_1.setSelected(true);
        orientation_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.orientation_1).setSelected(true);
                findViewById(R.id.orientation_2).setSelected(false);
                if(null!=mOnActionListener) mOnActionListener.onChangeOrientation(true);
            }
        });
        findViewById(R.id.orientation_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.orientation_1).setSelected(false);
                findViewById(R.id.orientation_2).setSelected(true);
                if(null!=mOnActionListener) mOnActionListener.onChangeOrientation(false);
            }
        });
        //测试地址播放
        findViewById(R.id.play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editText = (EditText) findViewById(R.id.input);
                String url = editText.getText().toString().trim();
                if(TextUtils.isEmpty(url)){
                    Toast.makeText(getContext(),"请粘贴或输入播放地址后再播放!",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(null!=mOnActionListener) mOnActionListener.rePlay(url);
            }
        });
    }

    @Override
    public void onClick(View view) {
        if((int)view.getTag()==MEDIA_CORE) return;
        switch (view.getId()) {
            case R.id.btn_core_1:
                MEDIA_CORE =0;
                break;
            case R.id.btn_core_2:
                MEDIA_CORE =1;
                break;
            case R.id.btn_core_3:
                MEDIA_CORE =2;
                break;
        }
        for (View button : buttons) {
            button.setSelected(false);
        }
        buttons[MEDIA_CORE].setSelected(true);
        if(null!=mOnActionListener) {
            mOnActionListener.onMediaCore(MEDIA_CORE);
            mOnActionListener.rePlay(null);
        }
    }

    public void onReset() {
        if(null!=mMenuView) mMenuView.onReset();
    }

    public void updateMute(boolean isMute, boolean isSet) {
        if(null!=mMenuView) mMenuView.updateMute(isMute,isSet);
    }

    public interface OnActionListener{
        void setSpeed(float speed);
        void setZoomModel(int zoomModel);
        void setSoundMute(boolean mute);
        void setMirror(boolean mirror);
        void setCanTouchInPortrait(boolean canTouchInPortrait);
        void onChangeOrientation(boolean changeOrientation);
        void rePlay(String url);
        void onMediaCore(int mediaCore);
        void onRenderCore(int renderCore);
    }

    private OnActionListener mOnActionListener;

    public void setOnActionListener(OnActionListener onActionListener) {
        mOnActionListener = onActionListener;
        initViews();
    }
}