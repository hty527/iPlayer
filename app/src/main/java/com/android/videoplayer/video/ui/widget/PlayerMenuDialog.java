package com.android.videoplayer.video.ui.widget;

import android.content.Context;
import android.support.design.widget.BottomSheetDialog;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.android.iplayer.listener.OnMenuActionListener;
import com.android.videoplayer.R;

/**
 * created by hty
 * 2022/7/11
 * Desc:播放器功能交互菜单
 */
public class PlayerMenuDialog extends BottomSheetDialog {

    private final PlayerMenuView mMenuView;

    public PlayerMenuDialog(Context context) {
        super(context, R.style.MenuButtomAnimationStyle);
        setContentView(R.layout.dialog_menu);
        initLayoutPrams();
        findViewById(R.id.dialog_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlayerMenuDialog.this.dismiss();
            }
        });
        mMenuView = (PlayerMenuView) findViewById(R.id.dialog_menus);
        mMenuView.setOnMenuActionListener(new OnMenuActionListener() {
            @Override
            public void onSpeed(float speed) {
                if(null!=mOnMenuActionListener) mOnMenuActionListener.onSpeed(speed);
            }

            @Override
            public void onZoom(int zoomModel) {
                if(null!=mOnMenuActionListener) mOnMenuActionListener.onZoom(zoomModel);
            }

            @Override
            public void onScale(int scale) {
                if(null!=mOnMenuActionListener) mOnMenuActionListener.onScale(scale);
            }

            @Override
            public void onMute(boolean mute) {
                if(null!=mOnMenuActionListener) mOnMenuActionListener.onMute(mute);
            }

            @Override
            public void onMirror(boolean mirror) {
                if(null!=mOnMenuActionListener) mOnMenuActionListener.onMirror(mirror);
            }
        });
    }

    protected void initLayoutPrams(){
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        WindowManager.LayoutParams attributes = window.getAttributes();
        WindowManager systemService = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics=new DisplayMetrics();
        systemService.getDefaultDisplay().getMetrics(displayMetrics);
        attributes.height= FrameLayout.LayoutParams.WRAP_CONTENT;
        attributes.width= systemService.getDefaultDisplay().getWidth();
        attributes.gravity= Gravity.BOTTOM;
    }

    private OnMenuActionListener mOnMenuActionListener;

    public void setOnMenuActionListener(OnMenuActionListener onMenuActionListener) {
        mOnMenuActionListener = onMenuActionListener;
    }

    public void onReset() {
        if(null!=mMenuView) mMenuView.onReset();
    }
}