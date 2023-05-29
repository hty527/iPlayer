package com.android.player.utils;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.android.player.App;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * created by hty
 * 2022/7/1
 * Desc:
 */
public class ScreenUtils {

    private volatile static ScreenUtils mInstance;

    public static synchronized ScreenUtils getInstance() {
        synchronized (ScreenUtils.class) {
            if (null == mInstance) {
                mInstance = new ScreenUtils();
            }
        }
        return mInstance;
    }
    /**
     * 获取屏幕宽度
     * @return
     */
    public int getScreenWidth() {
        return App.getInstance().getContext().getResources().getDisplayMetrics().widthPixels;
    }

    public float getScreenWidthDP() {
        int dpInt = pxToDpInt(getScreenWidth());
        return dpInt;
    }

    public float getScreenHeightDP() {
        int dpInt = pxToDpInt(getScreenHeight());
        return dpInt;
    }

    /**
     * 获取屏幕高度
     * @return
     */
    public int getScreenHeight() {
        return App.getInstance().getContext().getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 将px转换成dp
     * @param pxValue
     * @return
     */
    public int pxToDpInt(float pxValue) {
        final float scale = App.getInstance().getContext().getResources().getDisplayMetrics().density;
        return (int) (pxValue / (scale <= 0 ? 1 : scale) + 0.5f);
    }

    /**
     * 将dp转换成px
     * @param dipValue
     * @return
     */
    public int dpToPxInt(float dipValue) {
        final float scale = App.getInstance().getContext().getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * 将自己从父Parent中移除
     * @param view
     */
    public void removeParent(View view) {
        try {
            if(null!=view&&null!=view.getParent()){
                if(view.getParent() instanceof ViewGroup){
                    ((ViewGroup) view.getParent()).removeView(view);
                }else if(view.getParent() instanceof ViewParent){

                }
            }
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    public Activity getActivity(Context context) {
        if (context == null) return null;
        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextThemeWrapper) {
            return getActivity(((ContextThemeWrapper) context).getBaseContext());
        }
        return null;
    }

    /**
     * 通过反射的方式获取状态栏高度
     * @return
     */
    public int getStatusBarHeight(Context context) {
        int StatusBarHeight = 0;
        try {
            int resourceId = context.getApplicationContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                StatusBarHeight = context.getApplicationContext().getResources().getDimensionPixelSize(resourceId);
            }
        }catch (Exception e){
            e.printStackTrace();
            StatusBarHeight=dpToPxInt(25f);
        }
        return StatusBarHeight;
    }

    /**
     * 将数据转换为万为单位
     * @param no
     * @return
     */

    public String formatWan(long no) {
        double n = (double) no / 10000;
        return changeDouble(n) + "万";
    }

    public String formatWan(String no, boolean round) {
        int parseInt = parseInt(no);
        return formatWan(parseInt,round);
    }

    public String formatWan(long no, boolean round) {
        if (round && no <= 10000) return String.valueOf(no);
        double n = (double) no / 10000;
        return changeDouble(n) + "万";
    }

    public double changeDouble(Double dou) {
        try {
            NumberFormat nf = new DecimalFormat("0.0 ");
            dou = Double.parseDouble(nf.format(dou));
            return dou;
        }catch (RuntimeException e){

        }
        return dou;
    }

    public double changeDouble(float num) {
        double parseDouble = 0.0;
        try {
            NumberFormat nf = new DecimalFormat("0.00");
            parseDouble = Double.parseDouble(nf.format(num));
            return parseDouble;
        } catch (Exception e) {
            return parseDouble;
        }
    }

    public int parseInt(String content){
        return parseInt(content,0);
    }

    public int parseInt(String content, int defaultValue){
        if(TextUtils.isEmpty(content)) return defaultValue;
        try {
            return Integer.parseInt(content);
        }catch (NumberFormatException e){
            e.printStackTrace();
            return 0;
        }
    }

    public long parseLong(String content){
        return parseLong(content,0);
    }

    public long parseLong(String content, int defaultValue){
        if(TextUtils.isEmpty(content)) return defaultValue;
        try {
            return Long.valueOf(content);
        }catch (NumberFormatException e){
            e.printStackTrace();
            return 0;
        }
    }
}