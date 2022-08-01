package com.android.iplayer.widget;

import android.graphics.Outline;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.ViewOutlineProvider;

/**
 * created by hty
 * 2022/6/30
 * Desc:View圆角设置
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class LayoutProvider extends ViewOutlineProvider {

    private float mRadius;

    public LayoutProvider(float radius){
        this.mRadius = radius;
    }

    @Override
    public void getOutline(View view, Outline outline) {
        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), mRadius);
        view.setClipToOutline(true);
    }
}