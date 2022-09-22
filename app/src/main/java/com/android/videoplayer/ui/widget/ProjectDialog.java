package com.android.videoplayer.ui.widget;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.android.videoplayer.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

/**
 * created by hty
 * 2022/9/22
 * Desc:代码平台仓库选择
 */
public class ProjectDialog extends BottomSheetDialog {

    public ProjectDialog(Context context) {
        super(context, R.style.MenuButtomAnimationStyle);
        setContentView(R.layout.dialog_project_menu);
        initLayoutPrams();
        findViewById(R.id.btn_github).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if(null!=mOnMenuActionListener) mOnMenuActionListener.onSelected("https://github.com/hty527/iPlayer");
            }
        });
        findViewById(R.id.btn_gitee).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if(null!=mOnMenuActionListener) mOnMenuActionListener.onSelected("https://gitee.com/hty527/iPlayer");
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

    public interface OnMenuActionListener{
        void onSelected(String url);
    }
}